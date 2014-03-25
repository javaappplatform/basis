package github.javaappplatform.network.internal.tcp;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.network.INetworkAPI;
import github.javaappplatform.network.internal.AClientUnit;
import github.javaappplatform.network.internal.Message;
import github.javaappplatform.network.internal.SendQueue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TODO javadoc
 * 
 * @author funsheep
 */
public abstract class TCPConnection
{
	
	private static final Logger LOGGER = Logger.getLogger();


	/************ Send Messages ***************/
	private final ReentrantLock lock = new ReentrantLock();
	protected final SendQueue msgQueue = new SendQueue(INetworkAPI.MAX_SEND_MESSAGE_COUNTER);
	private final TCPHandler handler;
	private boolean closing = false;


	private final AClientUnit _unit;
	
	
	public TCPConnection(Socket socket, AClientUnit unit)
	{
		this._unit = unit;
		this.handler = new TCPHandler(socket, this);
	}
	

	public int state()
	{
		this.lock.lock();
		try
		{
			if (!this.handler.isConnected())
				return INetworkAPI.STATE_NOT_CONNECTED;
			if (this.closing)
				return INetworkAPI.STATE_CLOSING;

			return INetworkAPI.STATE_CONNECTED;
		} finally
		{
			this.lock.unlock();
		}
	}


	public Message take()
	{
		return this.msgQueue.take();
	}

	public Message poll()
	{
		return this.msgQueue.poll();
	}


	public void sendSystem(Message msg)
	{
		if (this.state() == INetworkAPI.STATE_NOT_CONNECTED)
			return;

		try
		{
			this.msgQueue.priorityPut(msg);
		}
		catch (InterruptedException e)
		{
			assert LOGGER.trace("Timeout on system message send.");
			//do nothing we just fail since network may be down anyway
		}
	}

	public void send(Message msg) throws IOException
	{
		if (this.state() != INetworkAPI.STATE_CONNECTED)
			throw new IOException("Client is not connected or shutting down. Could not send message.");
		try
		{
			this.msgQueue.put(msg);
		} catch (InterruptedException e)
		{
			throw new IOException("Connection Timeout.");
		}
	}

	protected void received(Message msg)
	{
		this._unit.distributeReceivedMSG(msg);
	}


	public void close()
	{
		this.lock.lock();
		try
		{
			if (this.state() != INetworkAPI.STATE_CONNECTED)	//Set closing
				return;
			this.closing = true;
		}
		finally
		{
			this.lock.unlock();
		}
	}
	
	public void waitUntilClosed()
	{
		this.close();
		long current = System.currentTimeMillis();
		final long deadline = current + INetworkAPI.CONNECTION_TIMEOUT;
		while (this.state() != INetworkAPI.STATE_NOT_CONNECTED && this.msgQueue.size() > 0 && current < deadline)
		{
			try
			{
				Thread.sleep(3);
			}
			catch (InterruptedException e)
			{
				//do nothing - when deadline is reached, we shutdown anyway
			}
			current = System.currentTimeMillis();
		}
		
		this.shutdown();
	}

	public void shutdown()
	{
		if (this.state() == INetworkAPI.STATE_NOT_CONNECTED)
			return;

		this.lock.lock();
		try
		{
			this.handler.shutdown();
		} finally
		{
			this.lock.unlock();
		}

	}

	protected abstract void validateState();

	public InetSocketAddress address()
	{
		return this.handler.getAddress();
	}


}
