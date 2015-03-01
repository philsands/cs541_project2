package bufmgr;

import global.PageId;
import global.Page;

import java.util.LinkedList;
import java.util.List;

public class BufMgr {
	
	private Page bufPool[];			// array to manage buffer pool
	private Descriptor bufDescr[];	// array of information regarding what is in the buffer pool
	private String replacementPolicy;
	private int numbufs;
	
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
		bufDescr = new Descriptor[numbufs];
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
		// if found, increment pin count for page and return pointer to page
		// if not, choose frame from set of replacement candidates, read page using diskmgr, and pin it
		// if dirty, write out before flushing from buffer
	}
	
	private 
	
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
	public void unpinPage(PageId pageno, boolean dirty) {}
	
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
	public PageId newPage(Page firstpage, int howmany) {return new PageId();}
	
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
	public int getNumBuffers() {return 0;}
	
	/**
	* Returns the total number of unpinned buffer frames.
	*/
	public int getNumUnpinned() {return 0;}

};

class Descriptor {
	private PageId pageNumber;
	private int pinCount;
	private boolean dirtyBit;
	
	public Descriptor(PageId pn, int pc, boolean db) {
		pageNumber = pn;
		pinCount = pc;
		dirtyBit = db;
	};
}

class HashTable {
	private LinkedList<Bucket> directory[];
	private int tableSize;
	public static final int A = 42;
	public static final int B = 13298;
	
	
	public HashTable(int ts)
	{
		directory = new LinkedList[ts];
		this.tableSize = ts;
	}
	
	public int hashFunction(int key) {
		return (A*key + B) % tableSize;
	}
}

class Bucket {
	private int pageNum;
	private int frameNum;
	
	public Bucket(int pn, int fn)
	{
		pageNum = pn;
		frameNum = fn;
	}
	
	public int getPageNum() {return pageNum;}
	public int getFrameNum() {return frameNum;}
}

}
