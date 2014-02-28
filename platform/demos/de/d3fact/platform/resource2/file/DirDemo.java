/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package de.d3fact.platform.resource2.file;

import github.javaappplatform.platform.job.JobPlatform;
import github.javaappplatform.platform.resource.IDirectory;
import github.javaappplatform.platform.resource.IFile;
import github.javaappplatform.platform.resource.IResourceAPI.OpenOption;
import github.javaappplatform.platform.resource.localfile.LocalFileSystem;
import github.javaappplatform.platform.utils.URIs;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.Arrays;

/**
 * TODO javadoc
 * @author funsheep
 */
public class DirDemo
{

	public static final void main(String[] args) throws Exception
	{
		LocalFileSystem sys = new LocalFileSystem();
		IDirectory dir = sys.resolveAsDirectory(new URI("file:/Users/work/test/"));
		System.out.println("Name: " + dir.name());
		System.out.println("Exists: " + dir.exists());
		System.out.println("Children: " + Arrays.toString(dir.getChildren()));
		System.out.println("IsWritable: " + dir.isWritable());

		System.out.println("Resolve Dir: " + URIs.resolveChild(dir.uri(), "RelativeDir/"));
		System.out.println("Resolve Dir2: " + URIs.resolveChild(dir.uri(), "/FromDir/"));

		System.out.println("Resolve Dir: " + sys.resolveAsDirectory(URIs.resolveChild(dir.uri(), "RelativeDir/")).uri());
		System.out.println("Resolve Dir2: " + sys.resolveAsDirectory(URIs.resolveChild(dir.uri(), "/FromDir/")).uri());

//		IListener listener = new IListener()
//		{
//
//			@Override
//			public void handleEvent(Event e)
//			{
//				System.out.println(e);
//			}
//		};

//		dir.addListener(IDirectory.EVENT_CREATED, listener);
//		dir.addListener(IDirectory.EVENT_DELETED, listener);
//		dir.addListener(IDirectory.EVENT_CHILD_CREATED, listener);
//		dir.addListener(IDirectory.EVENT_CHILD_DELETED, listener);
//		dir.addListener(IDirectory.EVENT_OVERFLOW, listener);

		sys.open(dir).create();

		System.out.println("Name: " + dir.name());
		System.out.println("Exists: " + dir.exists());
		System.out.println("Children: " + Arrays.toString(dir.getChildren()));
		System.out.println("IsWritable: " + dir.isWritable());

		IFile file = sys.resolveAsResource(URIs.resolveChild(dir.uri(), "Test.tst"));

		System.out.println("Name: " + file.name());
		System.out.println("Exists: " + file.exists());
		System.out.println("IsReadable: " + file.isReadable());
		System.out.println("IsWritable: " + file.isWritable());

		OutputStream out = sys.open(file).openStreamToWrite(OpenOption.CREATE_ONLY_WHEN_NEW.flag);

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
		writer.write("Hello World\n");

		writer.close();

		System.out.println("Name: " + file.name());
		System.out.println("Exists: " + file.exists());
		System.out.println("IsReadable: " + file.isReadable());
		System.out.println("IsWritable: " + file.isWritable());

//		System.out.println(sys.open(dir).delete());

		JobPlatform.waitForShutdown();
	}

}
