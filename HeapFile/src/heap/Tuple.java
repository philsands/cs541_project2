package heap;

public class Tuple
{
	private byte[] tuple;
	
	public Tuple()
	{
		tuple = new byte[HFPage.PAGE_SIZE];
	}
	
	public Tuple(byte[] data, int start, int length)
	{
		tuple = new byte[length];
		for (int i = 0; i < length; i++)
		{
			tuple[i] = data[i + start];
		}
	}
	
	public int getLength()
	{
		return tuple.length;
	}
	
	public byte[] getTupleByteArray()
	{
		return tuple;
	}

}
