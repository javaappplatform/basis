/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.internal;

import github.javaappplatform.commons.util.Close;
import github.javaappplatform.platform.resource.IResourceAPI.OpenOption;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TODO javadoc
 * @author funsheep
 */
public abstract class AOutputStreamJoin implements Closeable
{

	private class SyncedOutputStream extends OutputStream
	{

		private boolean closed = false;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void write(int b) throws IOException
		{
			AOutputStreamJoin.this.lock.lock();
			try
			{
				if (this.closed || AOutputStreamJoin.this.open == null)
					throw new IOException("Stream is closed.");
				if (AOutputStreamJoin.this.error != null)
					throw AOutputStreamJoin.this.error;
				try
				{
					AOutputStreamJoin.this.open.write(b);
				}
				catch (IOException ex)
				{
					AOutputStreamJoin.this.error = ex;
					throw ex;
				}
			}
			finally
			{
				AOutputStreamJoin.this.lock.unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void write(byte[] b) throws IOException
		{
			this.write(b, 0, b.length);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void write(byte[] b, int off, int len) throws IOException
		{
			AOutputStreamJoin.this.lock.lock();
			try
			{
				if (this.closed || AOutputStreamJoin.this.open == null)
					throw new IOException("Stream is closed.");
				if (AOutputStreamJoin.this.error != null)
					throw AOutputStreamJoin.this.error;
				try
				{
					AOutputStreamJoin.this.open.write(b, off, len);
				}
				catch (IOException ex)
				{
					AOutputStreamJoin.this.error = ex;
					throw ex;
				}
			}
			finally
			{
				AOutputStreamJoin.this.lock.unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void flush() throws IOException
		{
			AOutputStreamJoin.this.lock.lock();
			try
			{
				if (this.closed || AOutputStreamJoin.this.open == null)
					throw new IOException("Stream is closed.");
				if (AOutputStreamJoin.this.error != null)
					throw AOutputStreamJoin.this.error;
				try
				{
					AOutputStreamJoin.this.open.flush();
				}
				catch (IOException ex)
				{
					AOutputStreamJoin.this.error = ex;
					throw ex;
				}
			}
			finally
			{
				AOutputStreamJoin.this.lock.unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void close() throws IOException
		{
			if (!this.closed)
			{
				AOutputStreamJoin.this._close();
				this.closed = true;
			}
		}

	}


	private final ReentrantLock lock = new ReentrantLock();
	private int opened = 0;
	private OutputStream open;
	private IOException error;


	public OutputStream open(int options) throws IOException
	{
		AOutputStreamJoin.this.lock.lock();
		try
		{
			if (OpenOption.CREATE_IN_ANY_CASE.isSetIn(options))
			{
				Close.close(this.open);
				this.open = null;
				this.error = null;
			}
			if (this.open == null)
			{
				this.open  = this.openJoin(options);
				this.error = null;
			}
			this.opened++;
			return new SyncedOutputStream();
		}
		finally
		{
			AOutputStreamJoin.this.lock.unlock();
		}
	}

	protected abstract OutputStream openJoin(int options) throws IOException;

	private void _close()
	{
		AOutputStreamJoin.this.lock.lock();
		try
		{
			if (this.opened == 0)
				return;
			this.opened--;
			if (this.opened == 0)
			{
				Close.close(this.open);
				this.open = null;
				this.error = null;
			}
		}
		finally
		{
			AOutputStreamJoin.this.lock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close()
	{
		AOutputStreamJoin.this.lock.lock();
		try
		{
			Close.close(this.open);
			this.open = null;
			this.error = null;
			this.opened = 0;
		}
		finally
		{
			AOutputStreamJoin.this.lock.unlock();
		}
	}

}
