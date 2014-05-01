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
package org.genemania.plugin.apps;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.genemania.domain.Organism;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.plugin.FileUtils;
import org.genemania.plugin.cytoscape.NullCytoscapeUtils;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.data.lucene.LuceneDataSetFactory;
import org.genemania.plugin.parsers.TabDelimitedQueryParser;

public abstract class AbstractPluginApp {
	protected void checkPath(String path) throws ApplicationException {
		File file = new File(path);
		if (!file.exists()) {
			throw new ApplicationException(String.format("'%s' doesn't exist.", path)); //$NON-NLS-1$
		}
	}
	
	protected void checkFile(String path) throws ApplicationException {
		checkPath(path);
		File file = new File(path);
		if (!file.isFile()) {
			throw new ApplicationException(String.format("'%s' is not a file.", path)); //$NON-NLS-1$
		}
	}

	protected void checkWritable(String path) throws ApplicationException {
		File file = new File(path);
		if (file.isFile() && file.canWrite()) {
			return;
		}
		try {
			if (!file.exists() && file.createNewFile()) {
				file.delete();
				return;
			}
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
		throw new ApplicationException(String.format("Cannot create file: %s", path)); //$NON-NLS-1$
	}
	
	protected Organism parseOrganism(DataSet data, String name) throws DataStoreException {
		TabDelimitedQueryParser parser = new TabDelimitedQueryParser();
		return parser.parseOrganism(data, name);
	}

	static DataSetManager createDataSetManager() {
		DataSetManager dataSetManager = new DataSetManager();
		dataSetManager.addDataSetFactory(new LuceneDataSetFactory<Object, Object, Object>(dataSetManager, null, new FileUtils(), new NullCytoscapeUtils<Object, Object, Object>(), null), Collections.emptyMap());
		return dataSetManager;
	}
}
