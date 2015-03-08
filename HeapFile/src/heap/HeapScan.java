package heap;

import global.RID;

public class HeapScan {

	protected HeapScan(HeapFile hf) {
		// TODO Auto-generated constructor stub
	}
	
	protected void finalize() throws Throwable
	{
		
	}

	public void close()
	{
		
	}
	
	public boolean hasNext()
	{
		return false;
	}
	
	public Tuple getNext(RID rid)
	{
		return new Tuple();
	}
}
