package heap;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.*;

import chainexception.ChainException;
import bufmgr.BufMgr;
import diskmgr.DiskMgr;
import global.Minibase;
import global.PageId;
import global.RID;
import global.Page;

/**
 * 
 * Phil Sands, Qiaomu Yao
 * CS 541 - Project 2
 * March 9, 2015
 *
 */

public class HeapFile {
	
	private DiskMgr dm = Minibase.DiskManager;
	private BufMgr bm = Minibase.BufferManager;
	private String HFName;
	protected PageId headerPageId;
	private LinkedList<HFPage> HFPages;	
	private HashMap<RID, HFPage> HFPageDirectory;
	int count;
	public HeapFile(String name) 
	{
		count=0;
		HFName = name;
	    HFPage headerPage = new HFPage();
	    headerPageId = bm.newPage(headerPage, 1);
	    headerPage.setCurPage(headerPageId);
	    //bm.unpinPage(headerPageId, true);
		if (dm.get_file_entry(HFName) == null)
		{
			// allocate a header page to start
			dm.add_file_entry(HFName, headerPageId);
			bm.unpinPage(headerPageId, false);
		}
		HFPages = new LinkedList<HFPage>();
		HFPageDirectory = new HashMap<RID, HFPage>();
	}

	// must be done in O(log(n))
	public RID insertRecord(byte[] record) throws ChainException
	{
		count++;
		HFPage insertPage = null;
		RID rid = null;
		PageId pgId = new PageId(headerPageId.pid);
		Page page = new Page();
		Collections.sort(HFPages,new FreeSpaceComparator());
		bm.pinPage(pgId,page,false);
		insertPage = new HFPage(page);
		System.out.println(count);
		// if there are no pages, or if the page in the directory with the most free space hasn't enough free space, make a new page; otherwise, add data to page with most free space and re-sort
		if (HFPages.isEmpty())
		{
			
			System.out.println("HI");
			//PageId newPid = dm.allocate_page();
			//dm.read_page(newPid, insertPage);
			rid = insertPage.insertRecord(record);
			//dm.write_page(newPid, insertPage);
			System.out.println(insertPage + "\n" + rid);
			
			HFPages.add(insertPage);
			
		}
		else if (HFPages.getFirst().getFreeSpace() <= record.length){
			System.out.println("HERE");
			HFPages.poll();
		}
		else
		{
			// find the first page in the directory that has enough space for the given record
			insertPage = HFPages.getFirst();
			//dm.read_page(insertPage.getCurPage(), insertPage);
			rid = insertPage.insertRecord(record);
			System.out.println(insertPage + "\n" + rid);
			//dm.write_page(insertPage.getCurPage(), insertPage);
			
		}

		HFPageDirectory.put(rid,insertPage);
		bm.unpinPage(pgId, true);
		return rid;
	}
	
	public Tuple getRecord(RID rid)
	{
		byte[] record = HFPageDirectory.get(rid).selectRecord(rid);
		return new Tuple(record,0,record.length);
	}

	// must be done in O(log(n))
	public boolean updateRecord(RID rid, Tuple newRecord) throws ChainException
	{
		// don't assume that update will fit on current page
		HFPage curPage = HFPageDirectory.get(rid);
		Tuple curRecord = this.getRecord(rid);
	    bm.pinPage(rid.pageno, curPage, false);
		if (newRecord.getLength() > curPage.getFreeSpace() + curRecord.getLength())
		{
			deleteRecord(rid);
			insertRecord(newRecord.getTupleByteArray());
		}
		else
		{
			try
			{
				curPage.updateRecord(rid, newRecord);
				dm.write_page(rid.pageno,curPage);
				bm.unpinPage(rid.pageno, true);
			}
			catch (Exception e)
			{
				bm.unpinPage(rid.pageno, false);
				throw new ChainException(null, "Couldn't update record");
			}
		}
		
		return true;
	}

	// must be done in O(log(n))
	public boolean deleteRecord(RID rid) throws ChainException
	{
		HFPage curPage = HFPageDirectory.get(rid);
		bm.pinPage(rid.pageno, curPage, false);
	    try
	    {
	    	curPage.deleteRecord(rid);
	    	dm.write_page(rid.pageno,curPage);
	        bm.unpinPage(rid.pageno, true);
	    }
	    catch(Exception e)
	    {
	       bm.unpinPage(rid.pageno, false);
	       throw new ChainException(null, "Couldn't delete record");
	    }
		
		return true;
	}
	
	public int getRecCnt()
	{
		int count = 0;
		RID currId = null;
		for (HFPage hfp : HFPages)
		{
			currId = hfp.firstRecord();
			bm.pinPage(currId.pageno, hfp, false);
			while (hfp.hasNext(currId))
			{
				count++;
			}
			bm.unpinPage(currId.pageno, false);
		}
		return count;
	}
	
	public HeapScan openScan()
	{
		return new HeapScan(this);
	}
}

class FreeSpaceComparator implements Comparator<HFPage>
{
	@Override
	public int compare(HFPage first, HFPage second) {
		return first.getFreeSpace() - second.getFreeSpace();
	}
}
