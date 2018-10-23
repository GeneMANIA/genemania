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
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Metadata {
	
	private final Properties configProperties;

	public Metadata() {
		configProperties = new Properties();
		try {
			InputStream stream = getClass().getResourceAsStream("config.properties"); //$NON-NLS-1$
			if (stream == null) {
				return;
			}
			configProperties.load(stream);
		} catch (IOException e) {
			log(e);
		}
	}

	public String getCytoscapeVersion() {
		return getConfigProperty("cytoscapeVersion"); //$NON-NLS-1$
	}
	
	public String getBuildId() {
		return getConfigProperty("buildNumber"); //$NON-NLS-1$
	}
	
	public String getConfigProperty(String key) {
		String value = configProperties.getProperty(key);
		if (value == null || value.length() == 0 || value.startsWith("${")) { //$NON-NLS-1$
			return null;
		}
		return value;
	}

	private void log(IOException e) {
		Logger logger = Logger.getLogger(Metadata.class);
		logger.error("Unexpected exception", e); //$NON-NLS-1$
	}
}
