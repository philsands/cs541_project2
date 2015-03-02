/***
 * Phil Sands
 * Qiaomu Yao
 * CS 541 - Project 2
 * March 9, 2015
 */

package bufmgr;

import diskmgr.*;
import global.PageId;
import global.Page;

import java.util.LinkedList;
import java.util.ArrayList;

public class BufMgr {

	private Page bufPool[];			// array to manage buffer pool
	private Descriptor bufDescr[];	// array of information regarding what is in the buffer pool
	private HashTable pageFrameDirectory;
	private String replacementPolicy;
	private ArrayList<PageId> recency;
	private int numbufs;
	private DiskMgr disk;

	/**
	 * Create the BufMgr object.
	 * Allocate pages (frames) for the buffer pool in main memory and
	 * make the buffer manager aware that the replacement policy is specified by replacerArg (e.g., LH, Clock, LRU, MRU, LIRS, etc.).
	 *
	 * @param numbufs number of buffers in the buffer pool
	 * @param lookAheadSize number of pages to be looked ahead
	 * @param replacementPolicy Name of the replacement policy
	 */
	public BufMgr(int numbufs, int lookAheadSize, String replacementPolicy) {
		// initialize all buffer pool components
		bufPool = new Page[numbufs];
		disk=new DiskMgr();
		bufDescr = new Descriptor[numbufs];
		pageFrameDirectory = new HashTable(13);	// 13 is arbitrarily chosen prime number for now
		this.numbufs = numbufs;
		// ignore lookAheadSize
		this.replacementPolicy = replacementPolicy;
	}

	/**
	 * Pin a page.
	 * First check if this page is already in the buffer pool.
	 * If it is, increment the pin_count and return a pointer to this
	 * page.
	 * If the pin_count was 0 before the call, the page was a
	 * replacement candidate, but is no longer a candidate.
	 * If the page is not in the pool, choose a frame (from the
	 * set of replacement candidates) to hold this page, read the
	 * page (using the appropriate method from {\em diskmgr} package) and pin it.
	 * Also, must write out the old page in chosen frame if it is dirty
	 * before reading new page.__ (You can assume that emptyPage==false for
	 * this assignment.)
	 *
	 * @param pageno page number in the Minibase.
	 * @param page the pointer point to the page.
	 * @param emptyPage true (empty page); false (non-empty page)
	 */
	public void pinPage(PageId pageno, Page page, boolean emptyPage) {
		// search buffer pool for existence of page using hash
		if(emptyPage==true) return;
		if(pageFrameDirectory.hasPage(pageno))
		{
			PageFramePair pagepair= pageFrameDirectory.search(pageno);
			// if found, increment pin count for page and return pointer to page
			if(pagepair!=null){
				bufDescr[pagepair.getFrameNum()].incrementPinCount();
				page=bufPool[pagepair.getFrameNum()];
				recency.add(pagepair.getPageNum());
				return;
			}
		}
		
		// find the new frame for new page
		int newframe=-1;
		for (int i = 0; i < numbufs; i++)
		{
			if (bufPool[i] != null)
			{
				newframe = i;
				break;
			}
		}
				
		// no free frame
		if (newframe==-1)
		{	
			// if not, choose frame from set of replacement candidates, read page using diskmgr, and pin it
			// call LIRS function to determine which frame to swap out
			int newframeID = getLIRSCandidate();
			
			// if dirty, write out before flushing from buffer
			if(bufDescr[newframeID].getDirtyBit()) 
				flushPage(pageno);
			try
			{
				disk.read_page(pageno,page);
			}
			catch(Exception e)
			{
				System.out.print("Read Exception");
			}
			bufPool[newframeID] = page;
			bufDescr[newframeID] = new Descriptor(pageno,0,false);
			removeAllPageReferences(pageno);
			pageFrameDirectory.remove(pageno);
			pageFrameDirectory.insert(pageno,newframeID);
			recency.add(pageno);
			this.pinPage(pageno, bufPool[newframeID], false);
		}
	}

	private int getLIRSCandidate()
	{
		int frameToRemove = -1;
		int RD = 0;	// Reuse Distance
		int R = 0;	// Recency
		int maxRDR = 0;
		
		// for each page in bufPool eligible to be swapped out, calculate Reuse Distance and Recency
		for (int i = 0; i < this.getNumBuffers(); i++)
		{
			if (bufDescr[i].getPinCount() == 0)
			{
				RD = calculateReuseDistance(bufDescr[i].getPageNumber());
				R = calculateRecency(bufDescr[i].getPageNumber());
				
				// determine greater of RD and R and store in RD
				if (R > RD)
					RD = R;
				
				if (RD > maxRDR)
				{
					maxRDR = RD;
					frameToRemove = i;
				}
			}
		}
		
		return frameToRemove;
	}
	
	private int calculateReuseDistance(PageId pn)
	{
		int pageAccess = 0;
		int mostRecent = 0;
		int distance = 0;
		for (int i = recency.size() - 1; i >= 0; i--)
		{
			if (recency.get(i) == pn && pageAccess == 0)
			{
				mostRecent = i;
				pageAccess++;
			}
			else if (recency.get(i) == pn && pageAccess == 1)
			{
				distance = mostRecent - i;
				return distance;
			}
		}
		
		return Integer.MAX_VALUE;	// i.e. Infinity
	}
	
	private int calculateRecency(PageId pn)
	{
		if (recency.lastIndexOf(pn) != -1)
			return (recency.size() - recency.lastIndexOf(pn));
		
		return -1;
	}
	
	/**
	 * Unpin a page specified by a pageId.
	 * This method should be called with dirty==true if the client has
	 * modified the page.
	 * If so, this call should set the dirty bit
	 * for this frame.
	 * Further, if pin_count>0, this method should
	 * decrement it.
	 *If pin_count=0 before this call, throw an exception
	 * to report error.
	 *(For testing purposes, we ask you to throw
	 * an exception named PageUnpinnedException in case of error.)
	 *
	 * @param pageno page number in the Minibase.
	 * @param dirty the dirty bit of the frame
	 */
	public void unpinPage(PageId pageno, boolean dirty) {
		if(dirty!=true) return;
		if(pageFrameDirectory.hasPage(pageno))
		{
			PageFramePair pagepair= pageFrameDirectory.search(pageno);
			int pintemp=bufDescr[pagepair.getFrameNum()].getPinCount();
			if(pintemp==0)
			{//throw exception;

			}
			else if(pintemp>0)
			{
				bufDescr[pagepair.getFrameNum()].decrementPinCount();
				bufDescr[pagepair.getFrameNum()].setDirtyBit(false);
			}
		}
		else
		{
			//throw exception
		}
	}

	/**
	 * Allocate new pages.* Call DB object to allocate a run of new pages and
	 * find a frame in the buffer pool for the first page
	 * and pin it. (This call allows a client of the Buffer Manager
	 * to allocate pages on disk.) If buffer is full, i.e., you
	 * can't find a frame for the first page, ask DB to deallocate
	 * all these pages, and return null.
	 *
	 * @param firstpage the address of the first page.
	 * @param howmany total number of allocated new pages.
	 *
	 * @return the first page id of the new pages.__ null, if error.
	 */
	public PageId newPage(Page firstpage, int howmany) 
	{
		// allocate new pages
		PageId pgid = null;
		try
		{
			pgid=disk.allocate_page(howmany);
		} 
		catch(Exception e)
		{
			System.out.print("Allocate error");
		}
		try
		{
			disk.read_page(pgid,firstpage);
		} 
		catch(Exception e)
		{
			System.out.print("Read_Page error");
		}
		
		// find the new frame for new page
		int newframe=-1;
		for (int i = 0; i < numbufs; i++)
		{
			if (bufPool[i] != null)
			{
				newframe = i;
				break;
			}
		}
		
		// no free frame
		if (newframe==-1)
		{
			//buffer is full, deallocate new pages
			try
			{
				disk.deallocate_page(pgid, howmany);
			} 
			catch(Exception e)
			{
				System.out.print("Deallocate error");
			}
			return null;
		}
		
		bufPool[newframe] = firstpage;
		bufDescr[newframe] = new Descriptor(pgid,0,false);
		pageFrameDirectory.insert(pgid, newframe);
		recency.add(pgid);
		this.pinPage(pgid, bufPool[newframe], false);
		
		return pgid;
	}

	/**
	 * This method should be called to delete a page that is on disk.
	 * This routine must call the method in diskmgr package to
	 * deallocate the page.
	 *
	 * @param globalPageId the page number in the data base.
	 */
	public void freePage(PageId globalPageId) {
		PageFramePair pagepair= pageFrameDirectory.search(globalPageId);
		try
		{
			disk.deallocate_page(globalPageId);
		}
		catch(Exception e)
		{
			System.out.println("FreePage Exception");
		}
		pageFrameDirectory.remove(pagepair.getPageNum());
		removeAllPageReferences(pagepair.getPageNum());
		bufDescr[pagepair.getFrameNum()] = null;
		bufPool[pagepair.getFrameNum()] = null;
	}

	/**
	 * Used to flush a particular page of the buffer pool to disk.
	 * This method calls the write_page method of the diskmgr package.
	 *
	 * @param pageid the page number in the database.
	 */

	public void flushPage(PageId pageid) {
		PageFramePair pagepair= pageFrameDirectory.search(pageid);
		if (bufDescr[pagepair.getFrameNum()].getDirtyBit())
		{
			try
			{
				disk.write_page(pageid, bufPool[pagepair.getFrameNum()]);
			}
			catch(Exception e)
			{
				System.out.println("Flush Page Error");
			}
		}
		pageFrameDirectory.remove(pagepair.getPageNum());
		removeAllPageReferences(pagepair.getPageNum());
		bufDescr[pagepair.getFrameNum()] = null;
		bufPool[pagepair.getFrameNum()] = null;
	}

	/**
	 * Used to flush all dirty pages in the buffer pool to disk
	 *
	 */
	public void flushAllPages() {
		for(int i=0;i<numbufs;i++){
			if(bufDescr[i].getDirtyBit()==true)
				flushPage(bufDescr[i].getPageNumber());
		}
	}
	
	private void removeAllPageReferences(PageId toRemove)
	{
		for (int i = 0; i < recency.size(); i++)
		{
			if (recency.get(i) == toRemove)
			{
				recency.remove(i);
			}
		}
	}

	/**
	 * Returns the total number of buffer frames.
	 */
	public int getNumBuffers() {return numbufs;}

	/**
	 * Returns the total number of unpinned buffer frames.
	 */
	public int getNumUnpinned() {
		int unpinned = 0;
		for (Descriptor d : bufDescr)
		{
			if (d.getPinCount() == 0)
				unpinned++;
		}

		return unpinned;
	}

};

class Descriptor {
	private PageId pageNumber;
	private int pinCount;
	private boolean dirtyBit;

	public Descriptor(PageId pn, int pc, boolean db) {
		pageNumber = pn;
		pinCount = pc;
		dirtyBit = db;
	}

	// getters
	public PageId getPageNumber() {return pageNumber;}
	public int getPinCount() {return pinCount;}
	public boolean getDirtyBit() {return dirtyBit;}

	// setters
	public int incrementPinCount()
	{
		pinCount++;
		return pinCount;
	}
	public int decrementPinCount()
	{
		if (pinCount > 0)
			pinCount--;
		return pinCount;
	}
	public boolean setDirtyBit(boolean dbv)
	{
		dirtyBit = dbv;
		return dirtyBit;
	}

}

class HashTable {
	private LinkedList<PageFramePair> directory[];
	private int tableSize;
	public static final int A = 42;		// these numbers have no real significance and were chosen for a consistent hash
	public static final int B = 13298;

	public HashTable(int ts)
	{
		directory = new LinkedList[ts];
		tableSize = ts;
	}

	public int hashFunction(PageId key) {
		return (A*key.pid + B) % tableSize;
	}

	public boolean insert(PageId pn, int fn)
	{
		int bucketNumber = hashFunction(pn);

		if (!hasPage(bucketNumber, pn))
		{
			directory[bucketNumber].addLast(new PageFramePair(pn,fn));
			return true;
		}

		return false;
	}

	public PageFramePair search(PageId pn)
	{
		int bn = hashFunction(pn);
		for (int i = 0; i < directory[bn].size(); i++)
		{
			if ((directory[bn].get(i)).getPageNum() == pn) 
			{
				return directory[bn].get(i);
			}
		}
		return null;
	}

	public void remove(PageId pn)
	{
		int bucketNumber = hashFunction(pn);

		for (int i = 0; i < directory[bucketNumber].size(); i++)
		{
			if ((directory[bucketNumber].get(i)).getPageNum() == pn) 
			{
				directory[bucketNumber].remove(i);
				break;
			}
		}
	}

	public boolean hasPage(PageId pn){//hasPage with hashFunction
		int bn = hashFunction(pn);
		for (int i = 0; i < directory[bn].size(); i++)
		{
			if ((directory[bn].get(i)).getPageNum() == pn) 
			{
				return true;
			}
		}

		return false;
	}

	public boolean hasPage(int bn, PageId pn)
	{
		for (int i = 0; i < directory[bn].size(); i++)
		{
			if ((directory[bn].get(i)).getPageNum() == pn) 
			{
				return true;
			}
		}

		return false;
	}
}

class PageFramePair {
	private PageId pageNum;
	private int frameNum;

	public PageFramePair(PageId pn, int fn)
	{
		pageNum = pn;
		frameNum = fn;
	}

	public PageId getPageNum() {return pageNum;}
	public int getFrameNum() {return frameNum;}
}

/*
class LRUCache{

    int size;
    int capacity;

    DoubleLinkedList head;
    DoubleLinkedList tail;
    HashMap<Integer, DoubleLinkedList> map;

    public LRUCache(int capacity) {
        this.size = 0;
        this.capacity = capacity;
        head = null;
        tail = null;
        map = new HashMap<Integer, DoubleLinkedList>();
    }

    public void remove(DoubleLinkedList node) {
        if (node == head && node == tail) {
            head = null;
            tail = null;
        } else if (node == head) {
            head.next.prev = null;
            head = head.next;
        } else if (node == tail) {
            tail.prev.next = null;
            tail = tail.prev;
        } else {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }
        node.prev = null;
        node.next = null;
    }

    public void setHead(DoubleLinkedList node) {
        node.next = head;
        node.prev = null;
        if (head != null) {
            head.prev = node;
        }

        head = node;
        if (tail == null) {
            tail = node;
        }
    }

    public int get(int key) {
        if (!map.containsKey(key)) {
            // if key is not found
            return -1;
        } else {
            // if key is found
            DoubleLinkedList target = map.get(key);
            remove(target);
            setHead(target);
            return head.val;
        }
    }

    public void set(int key, int value) {
        if (this.get(key) != -1) {
            // key exist before, just replace the old value
            DoubleLinkedList old = map.get(key);
            old.val = value;
        } else {
            // this is a new key-value pair, insert it
            DoubleLinkedList newHead = new DoubleLinkedList(key, value);
            map.put(key, newHead);
            setHead(newHead);
            if (size == capacity) {
                // delete tail
                map.remove(tail.key);
                remove(tail);
            } else {
                size++;
            }
        }
    }

    class DoubleLinkedList {
        int key;
        int val;
        DoubleLinkedList prev;
        DoubleLinkedList next;
        public DoubleLinkedList(int k, int v) {
            this.key = k;
            this.val = v;
        }
    }
}*/
