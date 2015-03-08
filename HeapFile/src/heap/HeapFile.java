package heap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

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
	private ArrayList<DirectoryEntry> HFPageDirectory;

	public HeapFile(String name) {
		HFName = name;
		HFPages = new LinkedList<HFPage>();
		HFPageDirectory = new ArrayList<DirectoryEntry>();
	}

	// must be done in O(log(n))
	public RID insertRecord(byte[] record)
	{
		HFPage insertPage = null;
		RID rid = null;
		// if there are no pages, or if the page in the directory with the most free space hasn't enough free space, make a new page; otherwise, add data to page with most free space and re-sort
		if (HFPages.isEmpty() || HFPageDirectory.get(0).pagePointer.getFreeSpace() >= record.length)
		{
			insertPage = new HFPage();
			rid = insertPage.insertRecord(record);
			HFPages.add(insertPage);
			HFPageDirectory.add(new DirectoryEntry(insertPage));
		}
		else
		{
			// find the first page in the directory that has enough space for the given record
			insertPage = HFPageDirectory.get(0).pagePointer;
			rid = insertPage.insertRecord(record);	
		}
		
		Collections.sort(HFPageDirectory);
		return rid;
	}
	
	public Tuple getRecord(RID rid)
	{
		return new Tuple();
	}

	// must be done in O(log(n))
	public void updateRecord(RID rid, Tuple newRecord)
	{
		
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

class DirectoryEntry implements Comparable
{
	public HFPage pagePointer;
	
	public DirectoryEntry(HFPage pp)
	{
		pagePointer = pp;
	}

	public int compareTo(Object that) {
		return this.pagePointer.getFreeSpace() - ((DirectoryEntry)(that)).pagePointer.getFreeSpace();
	}
}