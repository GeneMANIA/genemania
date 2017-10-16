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

/**
 * 
 */
package org.genemania.plugin.data;

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.filechooser.FileFilter;

import org.genemania.plugin.Strings;


/**
 * A FileFilter that accepts a file called DataSet.DATA_FILE_NAME or a
 * directory that contains such a file.
 */
public class DataSetFileFilter extends FileFilter {
	@Override
	public boolean accept(File file) {
		if (DataSetManager.DATA_FILE_NAME.equals(file.getName())) {
			return true;
		}
		if (!file.isDirectory()) {
			return false;
		}
		return file.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return DataSetManager.DATA_FILE_NAME.equals(name);
			}
		}).length > 0;
	}

	@Override
	public String getDescription() {
		return Strings.dataSetFileFilter_description;
	}
}