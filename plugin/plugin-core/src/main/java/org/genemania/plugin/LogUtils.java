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

import java.io.File;
import java.io.IOException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

public class LogUtils {
	
	private static final String ERROR_LOG = "error.log"; //$NON-NLS-1$

	public static void configureLogging(File baseDirectory) throws IOException {
		Logger logger = Logger.getLogger("org.genemania.plugin"); //$NON-NLS-1$
		String logFile = String.format("%s%s%s", baseDirectory.getPath(), File.separator, ERROR_LOG); //$NON-NLS-1$
		FileAppender appender = new FileAppender(new SimpleLayout(), logFile);
		logger.addAppender(appender);
	}
	
	public static void log(Class<?> source, Throwable lastError) {
		if (lastError == null) {
			return;
		}
		Logger logger;
		if (source == null) {
			logger = Logger.getLogger("org.genemania.plugin"); //$NON-NLS-1$
		} else {
			logger = Logger.getLogger(source);
		}
		logger.error("Unexpected error", lastError); //$NON-NLS-1$
	}
}
