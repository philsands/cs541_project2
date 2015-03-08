package heap;

public class Tuple
{
	private Byte[] tuple;
	
	public Tuple()
	{
		tuple = new Byte[HFPage.PAGE_SIZE];
	}
	
	public Tuple(Byte[] data, boolean IDK, int length)
	{
		tuple = new Byte[length];
		int i = 0;
		for (Byte b : data)
		{
			tuple[i++] = b;
		}
	}
	
	public int getLength()
	{
		return tuple.length;
	}
	
	public Byte[] getTupleByteArray()
	{
		return tuple;
	}

}
