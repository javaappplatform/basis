/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.webdav;

import github.javaappplatform.network.utils.Connect;
import github.javaappplatform.platform.Platform;
import github.javaappplatform.platform.utils.SSLTrustAll;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

class WebDavTools
{


	public static final URLConnection connect(URI uri) throws IOException
	{
		if(Platform.hasOption("SSLOverride"))
			SSLTrustAll.activate();

		//replace scheme dav:/ with http:/ and davs:/ with https:/
		String replacement = "http";
		int substring = "dav".length();
		if (uri.getScheme().equals("davs"))
		{
			replacement += 's';
			substring++;
		}

		URL url = new URL(replacement + uri.toString().substring(substring));

		return Connect.lightlyTo(url);
	}

}
