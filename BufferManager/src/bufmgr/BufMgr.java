/***
 * Phil Sands
 * Qiaomu Yao
 * CS 541 - Project 2
 * March 9, 2015
 */

package bufmgr;

import diskmgr.*;
import global.*;


import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Random;

import chainexception.ChainException;
import java.util.Queue;

public class BufMgr implements GlobalConst{

	public Page bufPool[];			// array to manage buffer pool
	private Descriptor[] bufDescr;	// array of information regarding what is in the buffer pool
	private HashTable<Integer,Integer> pageFrameDirectory;
	private String replacementPolicy;
	private ArrayList<PageId> recency;
	private int numbufs;
	private Queue<Integer> queue;


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
		this.numbufs=numbufs;
		bufPool = new Page[numbufs];
		recency = new ArrayList<PageId>();
		bufDescr = new Descriptor[numbufs];
		for(int i=0;i<numbufs;i++){
			
			PageId pageid=new PageId(INVALID_PAGEID);
			pageid.pid=INVALID_PAGEID;
			bufPool[i]=new Page();
			bufDescr[i]=new Descriptor(pageid,0,false,false);
		}
		
		pageFrameDirectory = new HashTable<Integer,Integer>();	// 20 is arbitrarily chosen prime number for now
		this.numbufs = numbufs;
		// ignore lookAheadSize
		this.replacementPolicy = replacementPolicy;
		queue = new LinkedList<Integer>();
        initializeQueue();

	}
	public void initializeQueue() {
        for (int i = 0; i < numbufs; i++)
                queue.add(i);
	}
	/*
     * Return the first empty frame if the buffer is full throw an exception
     */
    public int getFirstEmptyFrame() throws BufferPoolExceededException {
            if (queue.size() == 0)
                    throw new BufferPoolExceededException(null, "BUFFER_POOL_EXCEED");
            else
                    return queue.poll();
    }

    /*
     * Check whether the buffer is full or not !
     */
    public boolean isFull() {
            return (queue.size() == 0);
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
	 * @throws BufferPoolExceededException 
	 * @throws InvalidPageNumberException 
	 * @throws HashEntryNotFoundException 
	 * @throws IOException 
	 * @throws FileIOException 
	 * @throws PagePinnedException 
	 * @throws DiskMgrException 
	 */
	public void pinPage(PageId pageno, Page page, boolean emptyPage) throws BufferPoolExceededException, InvalidPageNumberException, HashEntryNotFoundException, PagePinnedException, FileIOException, IOException, DiskMgrException {
		// search buffer pool for existence of page using hash
		System.out.println("unpinPage:  "+getNumUnpinned());
		//if(emptyPage==true) return;
		PageId writablePageID=new PageId(INVALID_PAGEID);
		boolean writable=false;
		System.out.println("PID:  "+pageno.pid);
		int index=-1;
		if(pageFrameDirectory.contain(pageno.pid))
		{
			
			//System.out.println("HashTab Has this page");
			int FrameNum= pageFrameDirectory.get(pageno.pid);
			// if found, increment pin count for page and return pointer to page
				System.out.println("Frame in Pinpage:"+ FrameNum+"PID:"+pageno.pid);
				//System.out.println("HashTab Has this page, page pair is not null");
				if(bufDescr[FrameNum].getPinCount()==0){
					System.out.println("QUEUE REMOVE");
					queue.remove(FrameNum);
				}
				bufDescr[FrameNum].incrementPinCount();
				page.setpage(bufPool[FrameNum].getpage());
				return;
			
		}
		
		// find the new frame for new page
		
		
			// if not, choose frame from set of replacement candidates, read page using diskmgr, and pin it
			// call LIRS function to determine which frame to swap out
			// if dirty, write out before flushing from buffer
		else if(queue.size()>0){
			// choose a frame (from the
            // set of replacement candidates) to hold this page
			Page pg=new Page();
			index = getFirstEmptyFrame();
			System.out.println("INDEX IS:" + index);
            // Also, must write out the old page in chosen frame if it is
            // dirty before reading new page.
			if (bufDescr[index].pageNumber.pid!=INVALID_PAGEID&&bufDescr[index].getDirtyBit()) {
                flushPage(bufDescr[index].getPageNumber());
                System.out.println("DIRTY !!!");
                //Minibase.DiskManager.write_page(pageno,bufPool[index]);
                pageFrameDirectory.remove(bufDescr[index].getPageNumber().pid);
                
			}
			
            try { Minibase.DiskManager.read_page(pageno,pg); } // read the page	
            catch (InvalidPageNumberException | FileIOException | IOException e)
            { e.printStackTrace();}
			
            page.setpage(pg.getpage());
            page.setPage(bufPool[index]);
            //bufPool[index] = page; // add to pool
            //has to deal with dirtybit
			int pinc=bufDescr[index].getPinCount();
            bufDescr[index] = new Descriptor(new PageId(pageno.pid),pinc+1,false,false); // add to descirptor
            pageFrameDirectory.put(pageno.pid,index); // add to hashtable
			
		}
		else{
			
			int frameNumber=getLIRSCandidate();
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!"+frameNumber);
			// get a candidate frame and remove it from the policy
			
			if(frameNumber != -1) // there are replacement candidates
			{
			// flush page
			if(bufDescr[frameNumber].getDirtyBit()) flushPage(bufDescr[frameNumber].getPageNumber());
			// remove old page from the hash map
			pageFrameDirectory.remove(bufDescr[frameNumber].getPageNumber().pid);
			Page pg = new Page();
			recency.add(pageno);
			try { Minibase.DiskManager.read_page(pageno,pg); } // read the page
			catch (InvalidPageNumberException | FileIOException | IOException e)
			{ e.printStackTrace();}
				page.setpage(pg.getpage());
				page.setPage(bufPool[index]);
				//bufPool[frameNumber] = page; // add to pool
				bufDescr[frameNumber]=new Descriptor(new PageId(pageno.pid),1,false,false); // update to descirptor
				pageFrameDirectory.put(pageno.pid,frameNumber); // add to hashtable
			}
			else // 
			{
				System.out.println("Memory FUll...");
				throw new BufferPoolExceededException(new Exception(),"bufmgr.BufferPoolExceededException");
			}
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
	 * @exception PageUnpinnedException 
	 * @exception HashEntryNotFoundException 
	 * @exception PageUnpinnedException
	 */
	public void unpinPage(PageId pageno, boolean dirty) throws PageUnpinnedException, HashEntryNotFoundException,InvalidPageNumberException {
		
		
		
		if(pageFrameDirectory.contain(pageno.pid))
		{
			
			int FrameNum= pageFrameDirectory.get(pageno.pid);
			
			//System.out.println("unpinPage-haspage");
			
			
			int pintemp=bufDescr[FrameNum].getPinCount();
			if(pintemp==0)
			{
				//throw exception;
				throw new PageUnpinnedException(null,"BUFMGR: Page Unpinned");
			}
			else if(pintemp>0)
			{
				bufDescr[FrameNum].setDirtyBit(dirty);
				bufDescr[FrameNum].decrementPinCount();
				
				if (bufDescr[FrameNum].getPinCount() == 0){
					System.out.println("QUEUE ADD!");
                    queue.add(FrameNum);
				}
			}
			
			
		}
		else
		{
			//throw exception
			throw new HashEntryNotFoundException(null,"HashEntryNotFoundException");
			
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
	 * @throws ChainException 
	 * @throws IOException 
	 */
	public PageId newPage(Page firstpage, int howmany) throws IOException, ChainException 
	{
		// allocate new pages
		
		System.out.println("newPage");
		if(getNumUnpinned()!=0){
			final PageId pgid=new PageId();
			//if(isFull()) return null;
			try
			{
				Minibase.DiskManager.allocate_page(pgid,howmany);
			} 
			catch(ChainException | IOException e)
			{
				System.out.println(e);
				e.printStackTrace();
			}
		
			try
			{
				this.pinPage(pgid, firstpage, false);
			}
			catch (Exception e)
			{
				Minibase.DiskManager.deallocate_page(pgid, howmany);
				System.out.println("Pinpage In NewPage");
				return null;
			}
			return pgid;
			
		}else{
			System.out.println("Buffer FULL");
			return null;
		}
	}

	/**
	 * This method should be called to delete a page that is on disk.
	 * This routine must call the method in diskmgr package to
	 * deallocate the page.
	 *
	 * @param globalPageId the page number in the data base.
	 * @throws ChainException 
	 */
	public void freePage(PageId globalPageId) throws ChainException {
		//System.out.println("freePage");
		if(globalPageId.pid<0) return;
		if(pageFrameDirectory.contain(globalPageId.pid)){
			try{
			int framenum= pageFrameDirectory.get(globalPageId.pid);
			
			
			//System.out.println("FREEPAGE FRAME:");
			//System.out.println(globalPageId.pid);
			
			if(framenum>=numbufs){
				throw new InvalidRunSizeException(null,"Too large page");
			}
			if(framenum<0){
				Minibase.DiskManager.deallocate_page(globalPageId);
				return;
			}
			
				if(bufDescr[framenum].getPinCount()<=1){
					
					if (bufDescr[framenum].getPinCount() != 0)
			            unpinPage(globalPageId,false);
					bufDescr[framenum].setDirtyBit(false);
					
					pageFrameDirectory.remove(globalPageId.pid);
						
					removeAllPageReferences(globalPageId);
					
					//flushPage(globalPageId);
					
					Minibase.DiskManager.deallocate_page(globalPageId);
			
				}
				else
				{
				System.out.println("Page is used by another user.");
				throw new PagePinnedException(new Exception(),"bufmgr.PagePinnedException");
				}
			}catch (Exception e) {
                throw new PagePinnedException(null, "BUFMGR:FAIL_PAGE_FREE");
			}

		
		}else{
		//bufPool[pagepair.getFrameNum()] = null;
			try{
			Minibase.DiskManager.deallocate_page(globalPageId);
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		
	}

	/**
	 * Used to flush a particular page of the buffer pool to disk.
	 * This method calls the write_page method of the diskmgr package.
	 *
	 * @param pageid the page number in the database.
	 * @throws IOException 
	 * @throws FileIOException 
	 * @throws HashEntryNotFoundException 
	 * @throws PagePinnedException 
	 * @throws DiskMgrException 
	 */

	public void flushPage(PageId pageid) throws PagePinnedException,InvalidPageNumberException, FileIOException, IOException, HashEntryNotFoundException, PagePinnedException, DiskMgrException{
		//System.out.println("flushPage");
		int FrameNum= pageFrameDirectory.get(pageid.pid);
		
		if(FrameNum==INVALID_PAGEID){
			throw new InvalidPageNumberException(null,"Invalid pageNumber");
		}
		Page apage=null;
		if(bufPool[FrameNum]!=null)
			apage=new Page(bufPool[FrameNum].getpage().clone());
			if(bufDescr[FrameNum].getPageNumber().pid==INVALID_PAGEID){
				throw new InvalidPageNumberException(null,"Invalid pageNumber");
			}
			try{
			if(apage!=null){
			Minibase.DiskManager.write_page(pageid,apage);
			bufDescr[FrameNum].setDirtyBit(false);
			}
			else{
				throw new HashEntryNotFoundException(null,
                        "BUF_MNGR: PAGE NOT FLUSHED ID EXCEPTION!");
			}
			}catch(Exception e){
				throw new DiskMgrException(e, "DB.java: flushPage() failed");
			}
			
			
			
		
		
	}

	/**
	 * Used to flush all dirty pages in the buffer pool to disk
	 * @throws PagePinnedException 
	 * @throws HashEntryNotFoundException 
	 * @throws DiskMgrException 
	 *
	 */
	public void flushAllPages() throws HashEntryNotFoundException, PagePinnedException, DiskMgrException {
		for(int i=0;i<numbufs;i++){
			if(bufDescr[i].getDirtyBit()==true)
				try{
				flushPage(bufDescr[i].getPageNumber());
				}catch(IOException|FileIOException|InvalidPageNumberException e){
					System.out.print("Flush_Page error");
				}
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
		return queue.size();
	}

};

class Descriptor {
	public PageId pageNumber;
	private int pinCount;
	public boolean dirtyBit;
	public boolean isInsert;
	public Descriptor(PageId pn, int pc, boolean db,boolean ii) {
		pageNumber = pn;
		pinCount = pc;
		dirtyBit = db;
		isInsert=ii;
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
	public void setPageNumber(PageId id){
	
		this.pageNumber=id;
	}
	public void setPageNumberId(int id){
		this.pageNumber.pid=id;
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

<<<<<<< HEAD
=======
class HashTable implements GlobalConst{
	private LinkedList<PageFramePair> directory[];
	private int tableSize;
	public static final int A = 42;		// these numbers have no real significance and were chosen for a consistent hash
	public static final int B = 13298;

	public HashTable(int ts)
	{
		System.out.println("HashTable");
		directory = new LinkedList[ts];
		for(int i=0;i<ts;i++){
			directory[i]=new LinkedList<PageFramePair>();
		}
		tableSize = ts;
	}

	public int hashFunction(PageId key) {
		return (A*key.pid + B) % tableSize;
	}

	public boolean insert(PageId pn, int fn)
	{
		System.out.println("Hash insert");
		if(pn.pid==INVALID_PAGEID) return false;
		int bucketNumber = hashFunction(pn);
		System.out.println();
		System.out.println(bucketNumber);
		if (!hasPage(bucketNumber, pn))
		{
			directory[bucketNumber].addLast(new PageFramePair(pn,fn));
			
			return true;
		}

		return false;
	}

	public PageFramePair search(PageId pn)
	{
		System.out.println("Hash search");
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

	public boolean remove(PageId pn)
	{
		System.out.println("Hash remove");
		int bucketNumber = hashFunction(pn);
		if(pn.pid==INVALID_PAGEID) return true;
		for (int i = 0; i < directory[bucketNumber].size(); i++)
		{
			if ((directory[bucketNumber].get(i)).getPageNum() == pn) 
			{
				directory[bucketNumber].remove(i);
				return true;
			}
		}
		return false;
	}

	public boolean hasPage(PageId pn){//hasPage with hashFunction
		System.out.println("Hash haspage1");
		if(pn.pid==INVALID_PAGEID) return false;
		int bn = hashFunction(pn);
		if (directory[bn] == null) return false;
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
		if(pn.pid==INVALID_PAGEID) return false;
		System.out.println("Hash haspage2");
		if (directory[bn] == null) return false;
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
>>>>>>> a22d52636b3d412aceca55ab1505ddd6661d82ca
