package heap;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.TreeMap;

import global.PageId;
import global.RID;

/**
 * 
 * Phil Sands, Qiaomu Yao
 * CS 541 - Project 2
 * March 9, 2015
 *
 */

public class HeapFile {
	
	private String HFName;
	private LinkedList<HFPage> HFPages;	
	private TreeMap<RID, HFPage> HFPageDirectory;

	public HeapFile(String name) {
		HFName = name;
		HFPages = new LinkedList<HFPage>();
		HFPageDirectory = new TreeMap<RID, HFPage>();
	}

	// must be done in O(log(n))
	public RID insertRecord(byte[] record)
	{
		HFPage insertPage = null;
		RID rid = null;
		Collections.sort(HFPages,new FreeSpaceComparator());
		// if there are no pages, or if the page in the directory with the most free space hasn't enough free space, make a new page; otherwise, add data to page with most free space and re-sort
		if (HFPages.isEmpty() || HFPages.getFirst().getFreeSpace() <= record.length)
		{
			insertPage = new HFPage();
			rid = insertPage.insertRecord(record);
			HFPages.add(insertPage);
		}
		else
		{
			// find the first page in the directory that has enough space for the given record
			insertPage = HFPages.getFirst();
			rid = insertPage.insertRecord(record);	
		}

		HFPageDirectory.put(rid,insertPage);
		return rid;
	}
	
	public Tuple getRecord(RID rid)
	{
		byte[] record = HFPageDirectory.get(rid).selectRecord(rid);
		return new Tuple(record,0,record.length);
	}

	// must be done in O(log(n))
	public void updateRecord(RID rid, Tuple newRecord)
	{
		// don't assume that update will fit on current page
		HFPage curPage = HFPageDirectory.get(rid);
		Tuple curRecord = this.getRecord(rid);
		if (newRecord.getLength() > curPage.getFreeSpace() + curRecord.getLength())
		{
			deleteRecord(rid);
			insertRecord(newRecord.getTupleByteArray());
		}
		else
		{
			curPage.updateRecord(rid, newRecord);
		}
	}

	// must be done in O(log(n))
	public void deleteRecord(RID rid)
	{
		HFPage curPage = HFPageDirectory.get(rid);
		curPage.deleteRecord(rid);
	}
	
	public int getRecCnt()
	{
		int count = 0;
		RID currid = null;
		for (HFPage hfp : HFPages)
		{
			currid = hfp.firstRecord();
			while (hfp.hasNext(currid))
			{
				count++;
			}
		}
		return count;
	}
	
	public HeapScan openScan()
	{
		return new HeapScan(this);
	}
	
	public TreeMap<RID,HFPage> getMap()
	{
		return HFPageDirectory;
	}
}

class FreeSpaceComparator implements Comparator<HFPage>
{
	@Override
	public int compare(HFPage first, HFPage second) {
		return first.getFreeSpace() - second.getFreeSpace();
	}
}