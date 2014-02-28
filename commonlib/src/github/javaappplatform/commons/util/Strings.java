package github.javaappplatform.commons.util;

public class Strings
{

	// table to convert a nibble to a hex char.
	private static final char[] HEX_CHAR = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };


	public static String toHexString(byte[] b)
	{
		return toHexString(b, 0, b.length);
	}

	public static String toHexString(byte[] b, int off, int len)
	{
		final StringBuffer sb = new StringBuffer(len * 2);
		for (int i = off; i < off+len; i++)
		{
			// look up high nibble char
			sb.append(HEX_CHAR[(b[i] & 0xf0) >>> 4]);
			// look up low nibble char
			sb.append(HEX_CHAR[b[i] & 0x0f]);
		}
		return sb.toString();
	}


	private Strings()
	{
		//no instance
	}

}