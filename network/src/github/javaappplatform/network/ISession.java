/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network;

import github.javaappplatform.commons.collection.SemiDynamicByteArray;
import github.javaappplatform.commons.events.IListener;
import github.javaappplatform.commons.events.ITalker;
import github.javaappplatform.network.msg.IMessage;

import java.io.Closeable;
import java.io.IOException;


/**
 * TODO javadoc
 * @author funsheep
 */
public interface ISession extends ITalker, Closeable
{

	public int sessionID();


	public void asyncSend(int type, byte[] data) throws IOException;

	public void asyncSend(int type, SemiDynamicByteArray data) throws IOException;

	public void asyncSend(int type, byte[] data, int off, int len, IListener callback, int protocol) throws IOException;

	public void asyncSend(int type, SemiDynamicByteArray data, int len, IListener callback, int protocol) throws IOException;
	

	public boolean hasReceivedMSGs();

	public IMessage receiveMSG();

	public int state();

	@Override
	public void close();

	public IClientUnit client();

	public Object attach(Object attachment);

	public Object attachment();

}
