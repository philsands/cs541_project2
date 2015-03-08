package heap;

import java.util.LinkedList;

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
	private LinkedList<HFPage> HFPageDirectory;	// change this to an appropriate data structure in order to improve performance

	public HeapFile(String name) {
		HFName = name;
		HFPageDirectory = new LinkedList<HFPage>();
	}

	// must be done in O(log(n))
	public RID insertRecord(byte[] record)
	{
		HFPage insertPage = null;
		RID rid = null;
		if (HFPageDirectory.isEmpty())
		{
			insertPage = new HFPage();
			rid = insertPage.insertRecord(record);
			HFPageDirectory.add(insertPage);
		}
		else
		{
			// find the first page in the directory that has enough space for the given record
			// NOTE: will need to update this in order to reach the logarithmic time expectation
			for (HFPage hfp : HFPageDirectory)
			{
				if (hfp.getFreeSpace() >= record.length)
				{
					insertPage = hfp;
					break;
				}
			}
			rid = insertPage.insertRecord(record);
		}
		
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
	
	public int getRecCnt()
	{
		return 0;
	}
	
	public HeapScan openScan()
	{
		return new HeapScan(new HeapFile("dummy"));
	}
}