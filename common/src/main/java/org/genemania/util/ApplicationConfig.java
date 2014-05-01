/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2010 University of Toronto.
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

/**
 * ApplicationConfig: TODO add description
 * Created Aug 21, 2008
 * @author Ovi Comes
 */
package org.genemania.util;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.genemania.Constants;

public class ApplicationConfig {

	// __[static]______________________________________________________________
	private static Log log = LogFactory.getLog(ApplicationConfig.class);
	private static ApplicationConfig instance = new ApplicationConfig();

	// __[fields]______________________________________________________________
	Configuration config;

	// __[constructors]________________________________________________________
	protected ApplicationConfig() {
		try {
			ConfigurationFactory factory = new ConfigurationFactory(Constants.APP_CONFIG_FILENAME);
			config = factory.getConfiguration();
		} catch (ConfigurationException x) {
			log.error("Application configuration error.", x);
		}
	}

	// __[accessors]___________________________________________________________
	public static ApplicationConfig getInstance() {
		return instance;
	}

	// __[public interface]____________________________________________________
	public String getProperty(String key) {
		return config.getString(key);
	}
}
