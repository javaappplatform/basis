/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 Hendrik Renken
	
	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the 
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network.msg;

import github.javaappplatform.commons.collection.SemiDynamicByteArray;
import github.javaappplatform.commons.events.Event;
import github.javaappplatform.commons.events.IListener;
import github.javaappplatform.commons.util.Arrays2;
import github.javaappplatform.network.INetworkAPI;
import github.javaappplatform.network.ISession;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SendMessage
{
	
//	private static final Logger LOGGER = Logger.getLogger();
	
	private class BlockingCallback implements IListener
	{
		private final ReentrantLock lock = new ReentrantLock();
		private final Condition resultAvailable = this.lock.newCondition();
		private final int[] replies;
		
		private IMessage reply;
	
		
		public BlockingCallback(int... replies)
		{
			this.replies = replies;
		}
		

		@Override
		public void handleEvent(Event e)
		{
			this.lock.lock();
			try
			{
				while (SendMessage.this.session.hasReceivedMSGs())
				{
					IMessage msg = SendMessage.this.session.receiveMSG();
					if (Arrays2.contains(this.replies, msg.type()))
					{
						this.reply = msg;
						SendMessage.this.session.removeListener(this);
						this.resultAvailable.signal();
					}
					else
						throw new IllegalArgumentException("Got unexpect message: " + msg);
				}
					
			}
			finally
			{
				this.lock.unlock();
			}
		}

		public IMessage get() throws TimeoutException
		{
			this.lock.lock();
			try
			{
				while (this.reply == null)
					if (!this.resultAvailable.await(INetworkAPI.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS))
						throw new TimeoutException();
				return this.reply;
			}
			catch (InterruptedException e)
			{
				throw new TimeoutException();
			}
			finally
			{
				this.lock.unlock();
			}
		}
	}

	private int msgtype;
	private MessageBuilder mb;
	private ISession session;


	public SendMessage(int msgtype)
	{
		this.msgtype = msgtype;
		this.mb = new MessageBuilder();
	}

	public static final SendMessage ofType(int msgtype)
	{
		return new SendMessage(msgtype);
	}

	public SendMessage over(ISession ses)
	{
		this.session = ses;
		return this;
	}

	public boolean usingReliableProtocol() throws IOException
	{
		return this.send(INetworkAPI.RELIABLE_PROTOCOL);
	}

	public IMessage andBlockUntilReply(final int reply, final int... additionalReplies) throws IOException
	{
		int[] tmp = new int[additionalReplies.length + 1];
		tmp[0] = reply;
		System.arraycopy(additionalReplies, 0, tmp, 1, additionalReplies.length);
		BlockingCallback callback = new BlockingCallback(tmp);
		this.session.addListener(INetworkAPI.EVENT_MSG_RECEIVED, callback);
		if (this.send(INetworkAPI.RELIABLE_PROTOCOL))
			try
			{
				return callback.get();
			}
			catch (TimeoutException e)
			{
				throw new IOException("Connection timed out.", e);
			}
		return null;
	}


	public boolean usingFastProtocol() throws IOException
	{
		return this.send(INetworkAPI.FAST_UNRELIABLE_PROTOCOL);
	}
	
	private boolean send(int protocol) throws IOException
	{
		if(this.session == null || this.session.state() != INetworkAPI.STATE_CONNECTED)
			return false;
		SemiDynamicByteArray arr = this.mb.get();
		arr.cursor(0);
		this.session.asyncSend(this.msgtype, arr, arr.size(), null, protocol);
		return true;
	}
	
	public SendMessage with(String value)
	{
		this.mb.putString(value);
		return this;
	}

	public SendMessage with(byte value)
	{
		this.mb.putByte(value);
		return this;
	}

	public SendMessage with(int value)
	{
		this.mb.putInt(value);
		return this;
	}

	public SendMessage with(long value)
	{
		this.mb.putLong(value);
		return this;
	}

	public SendMessage with(float value)
	{
		this.mb.putFloat(value);
		return this;
	}

	public SendMessage with(double value)
	{
		this.mb.putDouble(value);
		return this;
	}

	public SendMessage with(boolean value)
	{
		this.mb.putBoolean(value);
		return this;
	}

	public SendMessage with(byte[] value)
	{
		this.mb.putData(value);
		return this;
	}
}
