/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2008-2011 University of Toronto.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.genemania.plugin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

public class SystemUtils {
	public static boolean openBrowser(URL url) {
		String osName = System.getProperty("os.name"); //$NON-NLS-1$
		
		try {
			if (osName.startsWith("Windows")) { //$NON-NLS-1$
				Runtime runtime = Runtime.getRuntime();
				Process p = runtime.exec(new String[] { "rundll32", "url.dll,FileProtocolHandler", url.toString() }); //$NON-NLS-1$ //$NON-NLS-2$
				return p.waitFor() == 0;
			} else if (osName.startsWith("Mac OS")) { //$NON-NLS-1$
				Class<?> fileManagerClass = Class.forName("com.apple.eio.FileManager"); //$NON-NLS-1$
				Method method = fileManagerClass.getDeclaredMethod("openURL", new Class[] { String.class }); //$NON-NLS-1$
				method.invoke(null, new Object[] { url.toString() });
				return true;
			} else {
				// Assume POSIX/UNIX
				Runtime runtime = Runtime.getRuntime();
				Process p = runtime.exec(new String[] { "xdg-open", url.toString() }); //$NON-NLS-1$
				return p.waitFor() == 0;
			}
		} catch (ClassNotFoundException e) {
			return false;
		} catch (SecurityException e) {
			return false;
		} catch (NoSuchMethodException e) {
			return false;
		} catch (IllegalArgumentException e) {
			return false;
		} catch (IllegalAccessException e) {
			return false;
		} catch (InvocationTargetException e) {
			return false;
		} catch (IOException e) {
			return false;
		} catch (InterruptedException e) {
			return false;
		}
	}
	
	public static String escape(String data) {
		try {
			return URLEncoder.encode(data, "UTF-8"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			Logger logger = Logger.getLogger(SystemUtils.class);
			logger.error(e.getMessage(), e);
			return null;
		}
	}
	
	public static String unescape(String data) {
		try {
			return URLDecoder.decode(data, "UTF-8"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			Logger logger = Logger.getLogger(SystemUtils.class);
			logger.error(e.getMessage(), e);
			return null;
		}
	}
}
