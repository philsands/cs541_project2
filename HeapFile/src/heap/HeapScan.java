package heap;

import bufmgr.BufMgr;
import chainexception.ChainException;
import global.Minibase;
import global.PageId;
import global.RID;

public class HeapScan {
	
	private BufMgr bm = Minibase.BufferManager;
	private RID currid;
	private PageId curPageId;
	private HFPage curPage, headerPage;
	private int totalRecords, i;

	protected HeapScan(HeapFile hf) 
	{
		headerPage = new HFPage();
		bm.pinPage(hf.headerPageId, headerPage, true);
		totalRecords = hf.getRecCnt();
		i = 0;
		curPage = null;
	    currid = null;
	}
	
	protected void finalize() throws Throwable
	{
		close();
	}

	public void close() throws ChainException
	{
		bm.unpinPage(headerPage.getCurPage(), false);
		bm.unpinPage(curPageId, false);
		headerPage = null;
		curPage = null;
		currid = null;
	}
	
	public boolean hasNext()
	{
		if(i < totalRecords && curPage.nextRecord(currid) != null)
	         return true;
		return false;
	}
	
	public Tuple getNext(RID rid)
	{
		// make sure there are more records to check
		if(i < this.totalRecords)
		{
			// check to see whether or not first page has been accessed
			if (currid == null)
			{
				curPage = new HFPage();
				bm.pinPage(headerPage.getCurPage(), curPage, false);
				currid = curPage.firstRecord();
				rid = currid;
				i++;     
	        }
			else
			{
				currid = curPage.nextRecord(currid);
				if (currid == null)
				{
					// this is the case where we have exhausted all records on one page
					PageId getNextId = headerPage.getNextPage();
					if (getNextId != null)
					{
						bm.unpinPage(headerPage.getCurPage(), false);
						bm.pinPage(getNextId, headerPage, false);
						currid = null;
						return getNext(rid);
					}
				}
				
		    }
		}
		else
		{ 
			throw new IllegalStateException("Read all records");
		}	
		
		byte[] record = curPage.selectRecord(rid);
		return new Tuple(record,0,record.length);
	}
}