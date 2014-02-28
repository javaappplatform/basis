package github.javaappplatform.network.utils;

import github.javaappplatform.network.INetworkAPI;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

/**
 * TODO javadoc
 * @author funsheep
 */
public class Connect
{

	public static final URLConnection lightlyTo(URI uri) throws IOException
	{
		return to(uri.toURL(), false);
	}
	
	public static final URLConnection lightlyTo(URL url) throws IOException
	{
		return to(url, false);
	}
	
	public static final URLConnection hardTo(URI uri) throws IOException
	{
		return to(uri.toURL(), true);
	}
	
	public static final URLConnection hardTo(URL url) throws IOException
	{
		return to(url, true);
	}
	
	private static final URLConnection to(URL url, boolean hard) throws IOException
	{
		URLConnection connection = url.openConnection();
		connection.setConnectTimeout(INetworkAPI.CONNECTION_TIMEOUT);
		connection.setReadTimeout(INetworkAPI.CONNECTION_TIMEOUT);
		connection.setUseCaches(false);
		connection.connect();
		// check html header for existence (200 = OK)
		if (connection.getHeaderField(0).equals("HTTP/1.1 200 OK"))
			return connection;
		if (hard)
			throw new IOException("Resource " + url + " does not exist.");
		return null;
	}
	
	private Connect()
	{
		//no instance
	}

}
