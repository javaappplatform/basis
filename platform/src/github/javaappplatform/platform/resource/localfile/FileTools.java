/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.localfile;

import github.javaappplatform.commons.collection.SmallSet;
import github.javaappplatform.commons.log.Logger;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Set;

/**
 * TODO javadoc
 * @author funsheep
 */
public class FileTools
{

	private static final Logger LOGGER = Logger.getLogger();


	public static final Set<OpenOption> parseWrite(int options)
	{
		SmallSet<OpenOption> oo = new SmallSet<>();
		if (github.javaappplatform.platform.resource.IResourceAPI.OpenOption.APPEND_TO_RESOURCE.isSetIn(options))
			oo.add(StandardOpenOption.APPEND);
		if (github.javaappplatform.platform.resource.IResourceAPI.OpenOption.CREATE_IN_ANY_CASE.isSetIn(options))
		{
			oo.add(StandardOpenOption.CREATE);
			oo.add(StandardOpenOption.WRITE);
			oo.add(StandardOpenOption.TRUNCATE_EXISTING);
			oo.remove(StandardOpenOption.APPEND);
		}
		if (github.javaappplatform.platform.resource.IResourceAPI.OpenOption.CREATE_ONLY_WHEN_NEW.isSetIn(options))
		{
			oo.add(StandardOpenOption.CREATE);
			oo.add(StandardOpenOption.WRITE);
		}
		if (github.javaappplatform.platform.resource.IResourceAPI.OpenOption.DELETE_ON_CLOSE.isSetIn(options))
		{
			oo.add(StandardOpenOption.DELETE_ON_CLOSE);
		}
		return oo;
	}

//	public static final WatchService startService(final Path path, final TalkerStub source) throws IOException
//	{
//		final WatchService watcher = path.getFileSystem().newWatchService();
//		path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.OVERFLOW);
//		Thread t = new Thread()
//		{
//
//			@SuppressWarnings("unused")
//			@Override
//			public void run()
//			{
//				try
//				{
//					for (;;)
//					{
//						// retrieve key
//						WatchKey key = watcher.take();
//
//						// process events
//						for (WatchEvent<?> event : key.pollEvents())
//						{
//							int kind;
//							Object data;
//
//							if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE)
//							{
//								kind = IDirectory.EVENT_CHILD_CREATED;
//								data = event.context().toString();
//							}
//							else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE)
//							{
//								kind = IDirectory.EVENT_CHILD_DELETED;
//								data = event.context().toString();
//							}
//							else
//							{
//								kind = IDirectory.EVENT_OVERFLOW;
//								data = null;
//							}
//
//							new ResourceEventJob(kind, data, source);
//						}
//
//						// reset the key
//						boolean valid = key.reset();
//						if (!valid)
//						{
//							//i do not understand the concept of this reset, so i hope this is correct.
//							//stupid documentation by oracle.
//							break;
//						}
//					}
//				}
//				catch (ClosedWatchServiceException ex)
//				{
//					//die silently
//				}
//				catch (InterruptedException ex)
//				{
//					//die silently
//				}
//			}
//		};
//		t.setDaemon(true);
//		t.start();
//		return watcher;
//	}


	public static final Set<CopyOption> parseCopy(int options)
	{
		SmallSet<CopyOption> oo = new SmallSet<>();
		if (github.javaappplatform.platform.resource.IResourceAPI.OpenOption.CREATE_IN_ANY_CASE.isSetIn(options))
		{
			oo.add(StandardCopyOption.REPLACE_EXISTING);
		}
		return oo;
	}

	public static final void copy(Path from, Path to, int options) throws IOException
	{
		Set<CopyOption> oo = FileTools.parseCopy(options);
		try
		{
			Files.copy(from, to, oo.toArray(new CopyOption[oo.size()]));
		}
		catch (IOException e)
		{
			FileTools.deleteOnFail(options, to);
			throw e;
		}
	}

	public static final void deleteOnFail(int options, Path path)
	{
		if (github.javaappplatform.platform.resource.IResourceAPI.OpenOption.DELETE_ON_FAIL.isSetIn(options))
		{
			try
			{
				Files.deleteIfExists(path);
			} catch (Exception e)
			{
				//at least we tried.
				LOGGER.info("Could not delete file as requested on fail. " + path);
			}
		}
	}

	private FileTools()
	{
		//no instance
	}

}
