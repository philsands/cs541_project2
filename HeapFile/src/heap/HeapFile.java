package heap;

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
	private TreeMap<DirectoryEntry, HFPage> HFPageDirectory;

	public HeapFile(String name) {
		HFName = name;
		HFPages = new LinkedList<HFPage>();
		HFPageDirectory = new TreeMap<DirectoryEntry, HFPage>(new FreeSpaceComparator());
	}

	// must be done in O(log(n))
	public RID insertRecord(byte[] record)
	{
		HFPage insertPage = null;
		RID rid = null;
		// if there are no pages, or if the page in the directory with the most free space hasn't enough free space, make a new page; otherwise, add data to page with most free space and re-sort
		if (HFPages.isEmpty() || HFPageDirectory.firstEntry().getKey().freespace <= record.length)
		{
			insertPage = new HFPage();
			rid = insertPage.insertRecord(record);
			HFPages.add(insertPage);
		}
		else
		{
			// find the first page in the directory that has enough space for the given record
			insertPage = HFPageDirectory.firstEntry().getValue();
			rid = insertPage.insertRecord(record);	
			// remove and re-insert record in directory in order to update free space key comparison
			HFPageDirectory.remove(HFPageDirectory.firstEntry().getKey());
		}

		HFPageDirectory.put(new DirectoryEntry(rid.pageno,insertPage.getFreeSpace()),insertPage);
		return rid;
	}
	
	public Tuple getRecord(RID rid)
	{
		return new Tuple();
	}

	// must be done in O(log(n))
	public void updateRecord(RID rid, Tuple newRecord)
	{
		// don't assume that update will fit on current page
	}

	// must be done in O(log(n))
	public void deleteRecord(RID rid)
	{
		
	}
	
	private HFPage findPage(RID rid)
	{
		
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
		return new HeapScan(new HeapFile("dummy"));
	}
}

class DirectoryEntry
{
	public PageId pid;
	public int freespace;
	
	DirectoryEntry(PageId pid, int fs)
	{
		this.pid = pid;
		freespace = fs;
	}
}

class FreeSpaceComparator implements Comparator<DirectoryEntry>
{
	@Override
	public int compare(DirectoryEntry first, DirectoryEntry second) {
		return first.freespace - second.freespace;
	}
}