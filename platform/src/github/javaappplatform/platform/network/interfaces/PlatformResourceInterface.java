/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.network.interfaces;

import github.javaappplatform.commons.events.Event;
import github.javaappplatform.commons.events.IListener;
import github.javaappplatform.commons.io.InPipeOut;
import github.javaappplatform.commons.json.JSONWriter;
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.Close;
import github.javaappplatform.network.IClientUnit;
import github.javaappplatform.network.ISession;
import github.javaappplatform.network.interfaces.IClientInterface;
import github.javaappplatform.network.interfaces.IInterfaceType;
import github.javaappplatform.network.interfaces.impl.IMessageAPI;
import github.javaappplatform.network.interfaces.impl.StreamingInterface;
import github.javaappplatform.network.msg.Converter;
import github.javaappplatform.network.msg.IMessage;
import github.javaappplatform.network.msg.MessageReader;
import github.javaappplatform.network.msg.SendMessage;
import github.javaappplatform.platform.resource.CopyResource;
import github.javaappplatform.platform.resource.Directory;
import github.javaappplatform.platform.resource.IDirectory;
import github.javaappplatform.platform.resource.IFile;
import github.javaappplatform.platform.resource.IResource;
import github.javaappplatform.platform.resource.IResourceAPI;
import github.javaappplatform.platform.resource.Resource;
import github.javaappplatform.platform.resource.Stream;
import github.javaappplatform.platform.resource.IResourceAPI.OpenOption;
import github.javaappplatform.platform.utils.URIs;
import github.javaappplatform.platform.utils.concurrent.Concurrent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


public class PlatformResourceInterface implements IClientInterface
{
	protected static final Logger LOGGER = Logger.getLogger();

	public static final String ID = "github.javaappplatform.platform.network.interfaces.PlatformResourceInterface";

	@Deprecated
	protected static final String NAME = "name";
	@Deprecated
	protected static final String PATH = "path";

	protected static final String TYPE = "type";
	protected static final String EXISTS = "exists";
	protected static final String MIMETYPE = "mimetype";
	protected static final String SIZE = "size";
	protected static final String TIMESTAMP = "timestamp";
	protected static final String IS_READABLE = "isReadable";
	protected static final String IS_WRITABLE = "isWritable";
	protected static final String PARENT = "parent";
	protected static final String RESOURCETYPE = "resourcetype";
	protected static final String CHILDREN = "children";

	public static final int OPTION_AUTO_FLUSH_STREAM	= 1 << 31;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(ISession ses, IMessage msg)
	{
		try
		{
			switch(msg.type())
			{
				case IMessageAPI.MSGTYPE_RESOURCE_RESOLVE_URI:
					this.resolveURI(msg, ses);
					break;
				case IMessageAPI.MSGTYPE_RESOURCE_OPEN_STREAM:
					this.openStream(msg, ses);
					break;
				case IMessageAPI.MSGTYPE_RESOURCE_DELETE_URI:
					this.deleteURI(msg, ses);
					break;
				case IMessageAPI.MSGTYPE_RESOURCE_CREATE_DIRECTORY:
					this.createDir(msg, ses);
					break;
				case IMessageAPI.MSGTYPE_RESOURCE_COPY_RESOURCES:
					this.copyResources(msg, ses);
					break;
			}
		} catch (IOException e)
		{
			LOGGER.severe("Error while handling message of type " + msg.type(), e);
		}

		// old stuff

		switch(msg.type())
		{
			case IMessageAPI.MSGTYPE_RESOURCES_GET:
			{
				try
				{
					this.getResource(ses, msg);
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			break;
			case IMessageAPI.MSGTYPE_RESOURCES_GET_INFO:
			{
				try
				{
					this.getInfo(ses, msg);
				} catch (URISyntaxException e)
				{
					e.printStackTrace();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			break;
		}
	}

	private void resolveURI(IMessage msg, ISession ses) throws IOException
	{
		MessageReader mr = new MessageReader(msg);
		String uri = mr.readString();
		int resolveAs = mr.readByte();
		try
		{

			switch (resolveAs)
			{
				case IMessageAPI.RESOURCE_RESOLVE_TYPE_DIRECTORY:
					this.resolveAsDirectory(uri, ses);
					break;
				case IMessageAPI.RESOURCE_RESOLVE_TYPE_RESOURCE:
					this.resolveAsResource(uri, ses);
					break;
			}
		} catch (Exception e)
		{
			SendMessage.
				ofType(IMessageAPI.MSGTYPE_RESOURCE_RESOLVE_URI_ERROR).
				with(uri).
				with(e.getMessage()).
				over(ses).
				usingReliableProtocol();
		}
	}

	private void resolveAsDirectory(String uriString, ISession ses) throws Exception
	{
		IDirectory dir = Directory.at(uriString);

		StringWriter sw = new StringWriter();
		JSONWriter jw = new JSONWriter(sw);

		jw.startObject();

		jw.writeField(RESOURCETYPE, dir.resourceType());
		jw.writeField(EXISTS, dir.exists());
		jw.writeField(IS_WRITABLE, dir.isWritable());


		jw.startArrayField(CHILDREN);
		final URI[] children = dir.getChildren();
		if (children != null)
		{
			for (URI child : children)
				jw.write(URIs.extractName(child));
		}

		jw.endArray();

		jw.endObject();

		jw.flush();
		jw.close();

		SendMessage.
			ofType(IMessageAPI.MSGTYPE_RESOURCE_URI_RESOLVED).
			with(uriString).
			with(IMessageAPI.RESOURCE_RESOLVE_TYPE_DIRECTORY).
			with(sw.toString()).
			over(ses).
			usingReliableProtocol();
	}

	private void resolveAsResource(String uriString, ISession ses) throws Exception
	{
		IResource res = Resource.at(uriString);

		StringWriter sw = new StringWriter();
		JSONWriter jw = new JSONWriter(sw);

		jw.startObject();

		jw.writeField(TYPE, res.type());
		jw.writeField(EXISTS, res.exists());
		jw.writeField(MIMETYPE, res.mimetype());
		jw.writeField(SIZE, res.size());
		jw.writeField(TIMESTAMP, res.lastTimeModified());
		jw.writeField(IS_READABLE, res.isReadable());
		jw.writeField(IS_WRITABLE, res.isWritable());

		if(res.type() == IResourceAPI.TYPE_FILE)
		{
			URI parent = ((IFile)res).getParent().uri();
			if (parent != null)
				jw.writeField(PARENT, parent.toString());
			else
				jw.writeNullField(PARENT);
		}
		jw.endObject();

		jw.flush();
		jw.close();

		SendMessage.ofType(IMessageAPI.MSGTYPE_RESOURCE_URI_RESOLVED).with(uriString).with((byte)res.type()).with(sw.toString()).over(ses).usingReliableProtocol();
	}

	private void openStream(IMessage msg, ISession ses) throws IOException
	{
		MessageReader mr = new MessageReader(msg);
		String uriString = mr.readString();
		int streamID = mr.readInt();
		boolean toRead = mr.readByte() == 0;
		int options = mr.readInt();

		IResource res = null;
		try
		{
			res = Resource.at(uriString);
			StreamingInterface streamer = GetInterface.with(StreamingInterface.ID).ffor(ses);

			if (toRead)
			{
				final InputStream stream = Stream.open(res).with(options).toRead();
				if (stream != null)
				{
					SendMessage.ofType(IMessageAPI.MSGTYPE_RESOURCE_STREAM_OPENED).with(streamID).over(ses).usingReliableProtocol();
					this.pipe(stream, streamer.send(streamID, (int) res.size()), options);
				}
			}
			else
			{
				final InputStream stream = streamer.request(streamID);
				//do not block the call, we REALLY have to send back the opened message to get everything going
				this.pipe(stream, Stream.open(res).with(options).toWrite(), options);
				SendMessage.ofType(IMessageAPI.MSGTYPE_RESOURCE_STREAM_OPENED).with(streamID).over(ses).usingReliableProtocol();
			}

		}
		catch (Exception e)
		{
			if (OpenOption.DELETE_ON_FAIL.isSetIn(options) && res != null)
			{
				Resource.deleteIfExists(res);
			}
			SendMessage.ofType(IMessageAPI.MSGTYPE_RESOURCE_OPEN_STREAM_ERROR).with(streamID).with(e.getMessage()).over(ses).usingReliableProtocol();
			LOGGER.warn("Request "+uriString+" threw an error", e);
		}
	}

	private void deleteURI(IMessage msg, ISession ses) throws IOException
	{
		String uriString = Converter.getString(msg.body(), 0);

		try
		{
			Resource.delete(Resource.at(uriString));
			SendMessage.ofType(IMessageAPI.MSGTYPE_RESOURCE_URI_DELETED).with(uriString).over(ses).usingReliableProtocol();
		} catch (Exception e)
		{
			SendMessage.ofType(IMessageAPI.MSGTYPE_RESOURCE_DELETE_URI_ERROR).with(uriString).with(e.getMessage()).over(ses).usingReliableProtocol();
		}
	}

	private void createDir(IMessage msg, ISession ses) throws IOException
	{
		MessageReader mr = new MessageReader(msg);
		final String uriString = mr.readString();
//		boolean ensureExistence = mr.readBoolean(); //not needed. we want to fail, if directory already exists (ensure exsistence is handled in client)

		try
		{
			Directory.create(Directory.at(uriString));
			SendMessage.ofType(IMessageAPI.MSGTYPE_RESOURCE_DIRECTORY_CREATED).with(uriString).over(ses).usingReliableProtocol();
		} catch (Exception e)
		{
			SendMessage.ofType(IMessageAPI.MSGTYPE_RESOURCE_CREATE_DIRECTORY_ERROR).with(uriString).with(e.getMessage()).over(ses).usingReliableProtocol();
		}
	}

	private void copyResources(IMessage msg, final ISession ses) throws IOException
	{
		MessageReader mr = new MessageReader(msg);
		final String fromString = mr.readString();
		final String toString = mr.readString();
		final int options = mr.readInt();

		try
		{
			CopyResource.from(Resource.at(fromString)).withOptions(options).to(Resource.at(toString));
			SendMessage.ofType(IMessageAPI.MSGTYPE_RESOURCE_RESOURCES_COPIED).with(fromString).with(toString).over(ses).usingReliableProtocol();
		} catch (Exception e)
		{
			SendMessage.ofType(IMessageAPI.MSGTYPE_RESOURCE_COPY_RESOURCE_ERROR).with(fromString).with(toString).with(e.getMessage()).over(ses).usingReliableProtocol();
		}
	}

	@Deprecated
	private void getResource(ISession ses, IMessage msg) throws IOException
	{
		Exception error = null;
		StreamingInterface streamer = GetInterface.with(StreamingInterface.ID).ffor(ses);
		MessageReader mr = new MessageReader(msg);

		int streamID = mr.readInt();
		String uristring = mr.readString();

		try
		{
			URI uri = new URI(uristring);

			IResource res = Resource.at(uri);
			InputStream is = Stream.open(res).toRead();

			streamer.sendAsync(is, streamID, (int) res.size(), null);
		}
		catch (URISyntaxException | IOException e)
		{
			error = e;
		}

		if (error != null)
		{
			SendMessage.ofType(IMessageAPI.MSGTYPE_RESOURCES_GET_ERROR).with(uristring).with(error.toString()).over(ses).usingReliableProtocol();
		}
	}

	@Deprecated
	private void getInfo(ISession ses, IMessage msg) throws URISyntaxException, IOException
	{

		URI uri = new URI(Converter.getString(msg.body(), 0));

		StringWriter sw = new StringWriter();
		JSONWriter jw = new JSONWriter(sw);

		jw.startObject();

		// Path
		jw.writeField(PATH, uri.toString());

		// Name
		jw.writeField(NAME, URIs.extractName(uri));

		if (URIs.isDirectory(uri))
		{
			IDirectory dir = Directory.at(uri);

			// Exists
			jw.writeField(EXISTS, dir.exists());


			jw.writeField(TYPE, "folder");

			jw.writeField(SIZE, IResource.UNKNOWN_SIZE);
			jw.writeField(TIMESTAMP, IResource.UNKNOWN_MODIFIED_TIME);

			// Children
			jw.startArrayField(CHILDREN);
			if (dir.exists())
				for (URI child : dir.getChildren())
				{
					jw.write(URIs.extractName(child));
				}
			jw.endArray();
		}
		else
		{
			IResource res = Resource.at(uri);

			// Exists
			jw.writeField(EXISTS, res.exists());

			jw.writeField(TYPE, "file");

			// Size
			jw.writeField(SIZE, res.size());

			// Timestamp
			jw.writeField(TIMESTAMP, res.lastTimeModified());

			// Children
			jw.startArrayField(CHILDREN);
			jw.endArray();
		}


		jw.endObject();

		jw.flush();
		jw.close();

		SendMessage.ofType(IMessageAPI.MSGTYPE_RESOURCES_GET_INFO_RESPONSE).with(jw.toString()).over(ses).usingReliableProtocol();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int type()
	{
		return IInterfaceType.CLIENT_INTERFACE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IClientUnit cunit)
	{
		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose(ISession ses)
	{
		// nothing to do?
	}

	private void pipe(final InputStream in, final OutputStream out, final int options) throws IOException
	{
		final Concurrent error = new Concurrent();

		boolean autoFlush = (options & OPTION_AUTO_FLUSH_STREAM) == OPTION_AUTO_FLUSH_STREAM;

		InPipeOut.run(in, out, new IListener()
		{
			@Override
			public void handleEvent(Event e)
			{
				Close.close(out);
				Close.close(in);
				if(e.type() == InPipeOut.EVENT_PIPE_OK)
					error.pushResult(new Object());
				else if(e.type() == InPipeOut.EVENT_PIPE_ERROR)
				{
					LOGGER.warn(String.valueOf(e.getData()));
					error.pushResult(e.getData());
				}
			}
		}, autoFlush);

		try
		{
			error.retrieveResult(3 * 60 * 1000);	//FIXME three minutes. needs keep alive system
		}
		catch (TimeoutException e1)
		{
			throw new IOException(e1);
		}
		catch (ExecutionException e1)
		{
			throw new IOException(e1.getCause());
		}
	}

}
