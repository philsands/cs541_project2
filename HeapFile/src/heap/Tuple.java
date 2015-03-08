package heap;

public class Tuple
{
	private Byte[] tuple;
	
	public Tuple()
	{
		tuple = new Byte[HFPage.PAGE_SIZE];
	}
	
	public Tuple(byte[] data, boolean IDK, int length)
	{
		tuple = new Byte[length];
		int i = 0;
		for (byte b : data)
		{
			tuple[i++] = new Byte(b);
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
