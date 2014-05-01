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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

import javax.swing.filechooser.FileFilter;

import org.genemania.plugin.data.DataSetFileFilter;
import org.genemania.plugin.data.DataSetManager;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class DataSetFileFilterTest {
	private File file;
	private FileFilter filter;

	@Before
	public void initialize() throws IOException {
		URL resource = getClass().getResource("data");
		String path = URLDecoder.decode(resource.getPath(), "UTF-8");
		file = new File(path);
		assertTrue(file.isDirectory());
		filter = new DataSetFileFilter();
	}
	
	@Test
	public void testFile() {
		File configFile = new File(String.format("%s%s%s", file.getPath(), File.separator, DataSetManager.DATA_FILE_NAME));
		assertTrue(configFile.isFile());
		assertTrue(filter.accept(configFile));
	}
	
	@Test
	public void testDirectory() {
		assertTrue(filter.accept(file));
	}
	
	@Test
	public void testInvalid() {
		for (File entry : file.listFiles()) {
			if (!DataSetManager.DATA_FILE_NAME.equals(entry.getName())) {
				assertTrue(!filter.accept(entry));
			}
		}
	}
}
