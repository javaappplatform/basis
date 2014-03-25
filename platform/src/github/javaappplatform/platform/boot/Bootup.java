/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.boot;

import github.javaappplatform.commons.collection.SmallSet;
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.platform.extension.Extension;
import github.javaappplatform.platform.extension.ExtensionRegistry;
import github.javaappplatform.platform.job.AComputeDoJob;

import java.util.Set;

/**
 * TODO javadoc
 * @author funsheep
 */
public class Bootup extends AComputeDoJob
{

	private static final Logger LOGGER = Logger.getLogger();


	private final Set<Extension> bootEntries;
	private final Set<String> loaded;


	/**
	 * @param name
	 */
	public Bootup()
	{
		super("Bootup Process");
		this.bootEntries = ExtensionRegistry.getExtensions("github.javaappplatform.platform.boot");
		this.loaded = new SmallSet<String>(this.bootEntries.size());
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doJob()
	{
		boolean dead = true;
		try
		{
			for (final Extension entry : this.bootEntries)
			{
				if (!this.loaded.contains(entry.name))
				{
					boolean solved = true;
					String[] requis = entry.getProperty("requirements");
					if (requis != null)
						for (String req : requis)
							if (!this.loaded.contains(req))
							{
								solved = false;
								break;
							}
					if (solved)
					{
						LOGGER.info("Trying to start up {}", entry.name);
						entry.<IBootEntry>getService().startup(entry);
						this.loaded.add(entry.name);
						dead = false;
					}
				}
			}
		}
		catch (Exception e)
		{
			this.finishedWithError(e);
		}

		if (dead || this.bootEntries.size() == this.loaded.size())
		{
			this.finished(null);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public long length()
	{
		return this.bootEntries.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long absoluteProgress()
	{
		return this.loaded.size();
	}

}
