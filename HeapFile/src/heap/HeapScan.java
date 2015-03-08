package heap;

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeMap;

import global.Minibase;
import global.PageId;
import global.RID;

public class HeapScan {
	
	private HeapFile heapy;
	private TreeMap<RID,HFPage> directory;
	private NavigableSet<RID> ridSet;
	private Iterator<RID> ridIterator;
	RID currid;
	PageId curPageId;
	HFPage curPage;

	protected HeapScan(HeapFile hf) {
		heapy = hf;
		directory = heapy.getMap();
		ridSet = directory.navigableKeySet();
		ridIterator = ridSet.iterator();
	}
	
	protected void finalize() throws Throwable
	{
		close();
	}

	public void close()
	{
		Minibase.BufferManager.unpinPage(curPageId, false);
	}
	
	public boolean hasNext()
	{
		return ridIterator.hasNext();
	}
	
	public Tuple getNext(RID rid)
	{
		rid = ridIterator.next();
		curPageId = rid.pageno;
		Minibase.BufferManager.pinPage(curPageId, curPage, true);
		return heapy.getRecord(rid);
	}
}

