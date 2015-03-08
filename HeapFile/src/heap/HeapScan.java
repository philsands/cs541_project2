package heap;

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeMap;

import global.PageId;
import global.RID;

public class HeapScan {
	
	private HeapFile heapy;
	private TreeMap<RID,HFPage> directory;
	private NavigableSet<RID> ridSet;
	private Iterator<RID> ridIterator;
	RID currid;
	PageId curPageId;

	protected HeapScan(HeapFile hf) {
		heapy = hf;
		directory = heapy.getMap();
		ridSet = directory.navigableKeySet();
		ridIterator = ridSet.iterator();
	}
	
	protected void finalize() throws Throwable
	{
		
	}

	public void close()
	{
		
	}
	
	public boolean hasNext()
	{
		return ridIterator.hasNext();
	}
	
	public Tuple getNext(RID rid)
	{
		rid = ridIterator.next();
		//HFPage curPage = directory.get(rid);
		return heapy.getRecord(rid);
	}
}
