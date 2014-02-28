/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource;

import github.javaappplatform.commons.collection.SmallSet;

import java.util.Collection;

/**
 * TODO javadoc
 * @author funsheep
 */
public interface IResourceAPI
{

	public enum OpenOption
	{
		/**
		 * Indicates that the resource should be created if not existing. However, if the resource exists,
		 * its content will be overwritten by any write operation.
		 * Can be used in combination with {@link #BLOCK_CALL} and {@link #DELETE_ON_CLOSE} and {@link #DELETE_ON_FAIL}.
		 */
		CREATE_IN_ANY_CASE(1 << 0),
		/**
		 * Indicates that the file should be only created if not existing. If the resource already exists,
		 * any write operation fails.
		 * Can be used in combination with {@link #APPEND_TO_RESOURCE} and {@link #BLOCK_CALL} and {@link #DELETE_ON_CLOSE} and {@link #DELETE_ON_FAIL}.
		 */
		CREATE_ONLY_WHEN_NEW(1 << 1),
		/**
		 * Indicates that, if a resource already exists, write operations will append to the available content. If the resource does not exists,
		 * any write operation fails.
		 * Can be used in combination with {@link #CREATE_ONLY_WHEN_NEW} and {@link #BLOCK_CALL} and {@link #DELETE_ON_CLOSE} and {@link #DELETE_ON_FAIL}.
		 */
		APPEND_TO_RESOURCE(1 << 2),
		/**
		 * Indicates that the file should be deleted when the operator (writer or reader) is closed.
		 * Can be used in combination with {@link CREATE_IN_ANY_CASE} and {@link #CREATE_ONLY_WHEN_NEW} and {@link #APPEND_TO_RESOURCE} and {@link #BLOCK_CALL} and
		 * {@link #DELETE_ON_FAIL}.
		 */
		DELETE_ON_CLOSE(1 << 4),
		/**
		 * Indicates that the file should be deleted when the operator fails, i.e. reading from or writing to the resource fails with an exception.
		 * This operation is not guaranteed, e.g. when the JVM unexpectedly quits.
		 * However, it is guaranteed when the JVM exists regulary.
		 * Can be used in combination with {@link #CREATE_IN_ANY_CASE} and {@link #CREATE_ONLY_WHEN_NEW} and {@link #APPEND_TO_RESOURCE} and {@link #BLOCK_CALL} and
		 * {@link #DELETE_ON_CLOSE}.
		 */
		DELETE_ON_FAIL(1 << 5),
		TRY_TO_OPEN(1 << 6);

		public final int flag;

		private OpenOption(int flag)
		{
			this.flag = flag;
		}

		public boolean isSetIn(int options)
		{
			return (options & this.flag) == this.flag;
		}

		public static final OpenOption resolve(String name)
		{
			try
			{
				return valueOf(name);
			}
			catch (IllegalArgumentException ex)
			{
				return null;
			}
		}

		public static final OpenOption[] resolve(int options)
		{
			SmallSet<OpenOption> oo = new SmallSet<>(3);
			for (OpenOption opt : OpenOption.values())
				if (opt.isSetIn(options))
					oo.add(opt);
			return oo.toArray(new OpenOption[oo.size()]);
		}

		public static final int resolve(Collection<OpenOption> options)
		{
			int ret = 0;
			for (OpenOption opt : options)
				ret |= opt.flag;
			return ret;
		}

	}



	public static final int TYPE_UNKNOWN_RESOURCE = 0;

	public static final int TYPE_FILE = 1;

	public static final int TYPE_JAVA_CALL = 2;
}
