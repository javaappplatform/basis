/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.localfile;

import github.javaappplatform.platform.resource.IResourceAPI;
import github.javaappplatform.platform.resource.internal.IInternalDirectory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

/**
 * TODO javadoc
 * @author funsheep
 */
class InternalLocalFileDirectory implements IInternalDirectory
{


	private final URI uri;
	private final Path path;


	protected InternalLocalFileDirectory(URI uri)
	{
		this.uri = uri;
		this.path = Paths.get(uri);
	}

	protected InternalLocalFileDirectory(URI uri, Path path)
	{
		this.uri = uri;
		this.path = path;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public URI uri()
	{
		return this.uri;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String name()
	{
		return this.path.getFileName().toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int resourceType()
	{
		return IResourceAPI.TYPE_FILE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean exists()
	{
		return Files.exists(this.path);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isWritable()
	{
		return Files.isWritable(this.path);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URI[] getChildren() throws IOException
	{
		if (!this.exists())
			return null;
		ArrayList<URI> children = new ArrayList<>();
		for (Path child : Files.newDirectoryStream(this.path))
		{
			children.add(child.toUri());
		}
		return children.toArray(new URI[children.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void discard()
	{
		//do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close()
	{
		//do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean create() throws IOException
	{
		if (this.exists())
			return false;
		Files.createDirectory(this.path);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean delete()
	{
		boolean deleted = false;

		if (this.exists())
		{
			try
			{
				Files.walkFileTree(this.path, new SimpleFileVisitor<Path>()
				{

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
					{
						Files.delete(file);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
					{

						if (exc == null)
						{
							Files.delete(dir);
							return FileVisitResult.CONTINUE;
						}
						throw exc;
					}
				});
				deleted = !this.exists();
			}
			catch (IOException ex)
			{
				//die silently
			}
		}
		return deleted;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean ensureExistence() throws IOException
	{
		if (this.exists())
			return false;
		Files.createDirectories(this.path);
		return true;
	}

}
