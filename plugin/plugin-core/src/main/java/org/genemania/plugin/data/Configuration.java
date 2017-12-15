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

package org.genemania.plugin.data;

import java.awt.Window;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

public class Configuration implements IConfiguration {
	protected final DataSet data;
	
	public Configuration(DataSet data) {
		this.data = data;
	}
	
	public void write() throws IOException {
		String configPath = data.getFullPath(DataSetManager.DATA_FILE_NAME);
		FileWriter writer = new FileWriter(configPath);
		try {
			write(writer);
		} finally {
			writer.close();
		}
	}

	public void showUi(Window parent) {
	}

	public boolean hasUi() {
		return false;
	}

	public void write(Writer out) {
		PrintWriter writer = new PrintWriter(out);
		try {
			writer.printf("<?xml version=\"1.0\" encoding=\"utf-8\"?>"); //$NON-NLS-1$
			writer.printf("<genemania>\n"); //$NON-NLS-1$
			writer.printf("    <type>%s</type>\n", getClass().getName()); //$NON-NLS-1$
			writer.printf("    <data-version>%s</data-version>\n", escape(data.getVersion().toString())); //$NON-NLS-1$
			writer.printf("    <access-mode>%s</access-mode>\n", escape(data.getAccessMode())); //$NON-NLS-1$
			writer.printf("</genemania>\n"); //$NON-NLS-1$
		} finally {
			writer.close();
		}
	}
	
	protected String escape(String text) {
		if (text == null) {
			return ""; //$NON-NLS-1$
		}
		text = text.replaceAll("&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
		return text.replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
