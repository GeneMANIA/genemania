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
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;



public class SystemUtils {
	
	private static String[] BROWSERS =
        { "xdg-open", "htmlview", "firefox", "mozilla", "konqueror", "chrome", "chromium" };

	/**
	 * Opens the specified URL in the system default web browser.
	 *
	 * @return true if the URL opens successfully.
	 */
	public static boolean openBrowser(final String url) {
        URI uri = null;
        
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URL has an incorrect format: " + url);
        }

        return openBrowser(uri);
	}
	
	/**
	 * Opens the specified URL in the system default web browser.
	 *
	 * @return true if the URL opens successfully.
	 */
	public static boolean openBrowser(final URI uri) {
		if (openURLWithDesktop(uri)) {
			return true;
		} else {
			for (final String browser : BROWSERS) {
				if (openURLWithBrowser(uri.toString(), browser))
					return true;
			}
		}
		
		JOptionPane.showInputDialog(null, "GeneMANIA was unable to open your web browser.. "
				+ "\nPlease copy the following URL and paste it into your browser:", uri);
		
		return false;
	}

    private static boolean openURLWithDesktop(final URI uri) {
        if (!Desktop.isDesktopSupported())
            return false;
        
        try {
            Desktop.getDesktop().browse(uri);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean openURLWithBrowser(final String url, final String browser) {
        final ProcessBuilder builder = new ProcessBuilder(browser, url);
        
        try {
            builder.start();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
