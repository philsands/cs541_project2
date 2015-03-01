package bufmgr;

import diskmgr.*;
import global.PageId;
import global.Page;
import java.util.LinkedList;
import java.util.List;

public class BufMgr {
	
	private Page bufPool[];			// array to manage buffer pool
	private Descriptor bufDescr[];	// array of information regarding what is in the buffer pool
	private HashTable pageFrameDirectory;
	private String replacementPolicy;
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
		if(pageFrameDirectory.hasPage(pageno.pid)){
			
			PageFramePair pagepair= pageFrameDirectory.search(pageno.pid);
			if(pagepair!=null){
				int pintemp=bufDescr[pagepair.getFrameNum()].pinCount++;
				page=bufPool[pagepair.getFrameNum()];
				return;
			}
		}
		else{
			// the newframeID here is not right, should use LIRS
			int newframeID=pageFrameDirectory.hashFunction(pageno.pid);
			if(bufDescr[newframeID].dirtyBit) flushPage(pageno);
			try{
			disk.read_page(pageno,page);
			}catch(Exception e){
				System.out.print("Read Exception");
			}
			bufPool[newframeID]=page;
			Descriptor des=new Descriptor(pageno,0,false);
			bufDescr[newframeID]=des;
			pageFrameDirectory.remove(pageno.pid);
			pageFrameDirectory.insert(pageno.pid,newframeID);
			
		}

		// if found, increment pin count for page and return pointer to page
		// if not, choose frame from set of replacement candidates, read page using diskmgr, and pin it
		// if dirty, write out before flushing from buffer
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
		if(pageFrameDirectory.hasPage(pageno.pid)){
			PageFramePair pagepair= pageFrameDirectory.search(pageno.pid);
			int pintemp=bufDescr[pagepair.getFrameNum()].pinCount;
			if(pintemp==0){//throw exception;
				
			}else if(pintemp>0){
				bufDescr[pagepair.getFrameNum()].pinCount--;
				bufDescr[pagepair.getFrameNum()].dirtyBit=false;
				
			}
		}
		else{
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
	public PageId newPage(Page firstpage, int howmany) {
		//find the new frame for new page
		int newframe=-1;
		for(int i=0;i<numbufs;i++){
			if(bufPool[i]!=null){
				newframe=i;
				break;
			}
		}
		if(newframe==-1){//buffer is full, flush and search again;
			flushAllPages();
			for(int i=0;i<numbufs;i++){
				if(bufPool[i]!=null){
					newframe=i;
					break;
				}
			}
		}
		PageId pgid=null;
		if(newframe==-1){
			System.out.println("no more buffer");
			return null;
		}else{
			try{
			pgid=disk.allocate_page(howmany);
			}catch(Exception e){
				System.out.print("Allocate error");
			}
			try{
				disk.read_page(pgid,firstpage);
				}catch(Exception e){
					System.out.print("Read_Page error");
				}
			return pgid;
		}
		
	}
	
	/**
	* This method should be called to delete a page that is on disk.
	* This routine must call the method in diskmgr package to
	* deallocate the page.
	*
	* @param globalPageId the page number in the data base.
	*/
	public void freePage(PageId globalPageId) {}
	
	/**
	* Used to flush a particular page of the buffer pool to disk.
	* This method calls the write_page method of the diskmgr package.
	*
	* @param pageid the page number in the database.
	*/
	public void flushPage(PageId pageid) {}
	
	/**
	* Used to flush all dirty pages in the buffer pool to disk
	*
	*/
	public void flushAllPages() {}
	
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
			if (d.pinCount == 0)
				unpinned++;
		}
		
		return unpinned;
	}

};

class Descriptor {
	public PageId pageNumber;
	public int pinCount;
	public boolean dirtyBit;
	
	public Descriptor(PageId pn, int pc, boolean db) {
		pageNumber = pn;
		pinCount = pc;
		dirtyBit = db;
	};
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
	
	public int hashFunction(int key) {
		return (A*key + B) % tableSize;
	}
	
	public boolean insert(int pn, int fn)
	{
		int bucketNumber = hashFunction(pn);
		
		if (!hasPage(bucketNumber, pn))
		{
			directory[bucketNumber].addLast(new PageFramePair(pn,fn));
			return true;
		}
		
		return false;
	}
	
	public PageFramePair search(int pn)
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
	
	public void remove(int pn)
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
	
	public boolean hasPage(int pn){//hasPage with hashFunction
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
	
	public boolean hasPage(int bn, int pn)
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
	private int pageNum;
	private int frameNum;
	
	public PageFramePair(int pn, int fn)
	{
		pageNum = pn;
		frameNum = fn;
	}
	
	public int getPageNum() {return pageNum;}
	public int getFrameNum() {return frameNum;}
}

