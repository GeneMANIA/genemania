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

import java.awt.Desktop;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

public class SystemUtils {
	public static boolean openBrowser(URL url) {
        if (!Desktop.isDesktopSupported())
            return false;
        try {
            Desktop.getDesktop().browse(url.toURI());
            return true;
        } catch (IOException e) {
        } catch (URISyntaxException e) {
            return false;
		}
        
        for (String browser : new String[] { "xdg-open", "htmlview", "firefox", "mozilla", "konqueror", "chrome", "chromium" }) {
	        final ProcessBuilder builder = new ProcessBuilder(browser, url.toString());
	        try {
	            builder.start();
	            return true;
	        } catch (IOException e) {
	        }
        }
        return false;
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
