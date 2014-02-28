/*
	This file is part of the d3fact common library.
	Copyright (C) 2005-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.utils;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * Warning: Use this only for testing purpose. This makes ssl connections a lot less secure, 'cause there will be no identity check
 * of the connected peer!
 */
public class SSLTrustAll
{
	private static final SSLSocketFactory defaultFactory = HttpsURLConnection.getDefaultSSLSocketFactory();

	private static boolean ACTIVE = false;


	public static void activate()
	{
		if (ACTIVE)
			return;

		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager()
			{
				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers()
				{
					return null;
				}

				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
				{
					// do nothing
				}

				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
				{
					// do nothing
				}
			} };

		SSLContext sc;

		try
		{
			sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			ACTIVE = true;
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		} catch (KeyManagementException e)
		{
			e.printStackTrace();
		}
	}

	public static void deactivate()
	{
		if (!ACTIVE)
			return;
		HttpsURLConnection.setDefaultSSLSocketFactory(defaultFactory);
		ACTIVE = false;
	}
}
