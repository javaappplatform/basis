/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.network.interfaces;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.network.interfaces.impl.AManagedPoolInterface;
import github.javaappplatform.platform.extension.ExtensionRegistry;
import github.javaappplatform.platform.extension.ServiceInstantiationException;

public class PlatformPoolInterface extends AManagedPoolInterface
{
	public static final String POOL_EXT = "github.javaappplatform.platform.network.pool";

	private static final Logger LOGGER = Logger.getLogger();


	/**
	 * {@inheritDoc}
	 */
	@Override
	public IPool<?> getPool(String id)
	{
		try
		{
			return (IPool<?>) ExtensionRegistry.getExtensionByName(id).getService();
		} catch (ServiceInstantiationException e)
		{
			LOGGER.severe("Could not find pool for ID: " + id, e);
			return null;
		}
	}
}
