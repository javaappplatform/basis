/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package de.d3fact.platform;

import github.javaappplatform.platform.resource.Append;
import github.javaappplatform.platform.resource.IResourceAPI.OpenOption;
import github.javaappplatform.platform.resource.internal.ResourceTools;

import java.net.URI;
import java.util.Arrays;


public class ScratchBook
{

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
		URI uri = new URI("ws:/testmodels/test.csv?options=CREATE_IN_ANY_CASE+BLOCK_CALL+TEST");

		System.out.println(Arrays.toString(ResourceTools.extractOptions(uri)));

		System.out.println(ResourceTools.cleanURIFromOptions(uri));

		uri = Append.option(OpenOption.CREATE_IN_ANY_CASE).to("ws:/testmodels/tt");
		System.out.println(uri);

		System.out.println(Arrays.toString(ResourceTools.extractOptions(uri)));
		uri = new URI("ws:/testmodels/test.csv?options=CREATE_IN_ANY_CASE");

		System.out.println(Arrays.toString(ResourceTools.extractOptions(uri)));

		System.out.println(ResourceTools.cleanURIFromOptions(uri));

		uri = Append.option(OpenOption.CREATE_IN_ANY_CASE).to("ws:/testmodels/tt");
		System.out.println(uri);

		System.out.println(ResourceTools.extractOptions(uri)[0] == OpenOption.CREATE_IN_ANY_CASE);

//		WorkspaceFileSystem rs = new WorkspaceFileSystem();
//		final IInternalFile file = (IInternalFile) rs.open(new DummyFile(new URI("ws:/TokenSampling/Test.txt"), rs));
//		System.out.println(file.exists());
//
//		OutputStream out = file.openStreamToWrite(IResourceAPI.OPTION_CREATE_IN_ANY_CASE);
//		out.write("Hello World".getBytes());
//
//		System.out.println(file.exists());
//
//		new Thread()
//		{
//			@Override
//			public void run()
//			{
//				System.out.println("Try to read.");
//				BufferedReader reader = null;
//				try
//				{
//					reader = new BufferedReader(new InputStreamReader(file.openStreamToRead(IResourceAPI.OPTION_BLOCK_CALL)));
//					System.out.println(reader.readLine());
//				} catch (IOException e)
//				{
//					Close.close(reader);
//				}
//			}
//		}.start();
//
//		Thread.sleep(3000);
//		Close.close(out);

//		CommandLineParser parser = new GnuParser();
//
//		String[] arguments = { "-name", "test:test2" };
//
//		CommandLine line = parser.parse(options, arguments);
//
//		for (OpenOption o : line.getOptions())
//		{
//			System.out.println(o.getArgName());
//			System.out.println(Arrays.toString(o.getValues()));
//		}
//
////		String curr = System.getProperty("user.dir");
////
//////		URI uri = new URI("java:java.lang.Object#wait()");
////		URI uri = new URI("file://./testmodels/res/color_cube.mmf");
//
////		File parent = new File(curr, uri.getSchemeSpecificPart());
////
////		System.out.println(parent.getAbsolutePath());
////
//		URI uri = new URI("file://./../testmodels/res/color_cube.mmf");
//		uri.normalize();
//
//		System.out.println(uri.getSchemeSpecificPart());
//		File file = new File(uri);

//		parent = new File (curr, uri.getSchemeSpecificPart());
//
//		System.out.println(parent.getAbsolutePath());

////		System.out.println(uri.getFragment());
//
//		System.out.println();
//
//		URI root = new URI("file:///test");
//		System.out.println(root.getSchemeSpecificPart());
//		System.out.println(root.getPath());
//		System.out.println(root.isOpaque());
//		System.out.println(root.isAbsolute());
//
//		System.out.println();
//
//		URI r = root.resolve(uri);
//		System.out.println(r.getSchemeSpecificPart());
//		System.out.println(r.getPath());
//		System.out.println(r.isOpaque());
//		System.out.println(r.isAbsolute());


//		System.out.println(uri.resolve("#mimetype=test&returntype=model"));
//		Sardine s = SardineFactory.begin();
//		List<DavResource> list =
//		System.out.println(s.exists("http://wearefuntastic.net/imageserver/_urlhatsegeaen3/img/347esposatrollkopie.jpg"));
//		URL u = new URL("https://macabeo.cs.uni-paderborn.de/~d3fact/wiki/d3fact-blue-luminated-wallpaper-2560x1600.png");
//		URLConnection con = u.openConnection();
//		con.connect();
//		System.out.println(con.getContentLength());
//		System.out.println(con.getDate());
//		System.out.println(con.getLastModified());
//
//		for (Map.Entry<String, List<String>> e : con.getHeaderFields().entrySet())
//			System.out.println(e.getKey() + " : " + Arrays.toString(e.getValue().toArray()));
//		List<DavResource> list = s.list("http://wearefuntastic.net/imageserver/_urlhatsegeaen3/img/347esposatrollkopie.jpg");
//		for (DavResource r : list)
//			System.out.println(r.getName());
	}

}
