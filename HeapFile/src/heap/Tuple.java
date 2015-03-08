package heap;

public class Tuple
{
	private Byte[] tuple;
	
	public Tuple()
	{
		tuple = new Byte[HFPage.PAGE_SIZE];
	}
	
	public Tuple(byte[] data, int start, int length)
	{
		tuple = new Byte[length];
		for (int i = 0; i < length; i++)
		{
			tuple[i] = data[i + start];
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
