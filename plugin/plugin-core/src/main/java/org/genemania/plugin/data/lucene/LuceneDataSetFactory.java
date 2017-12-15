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
package org.genemania.plugin.data.lucene;

import java.io.File;

import org.genemania.plugin.FileUtils;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.data.IDataSetFactory;
import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.plugin.view.util.UiUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class LuceneDataSetFactory implements IDataSetFactory {

	private final DataSetManager dataSetManager;
	private final UiUtils uiUtils;
	private final FileUtils fileUtils;
	private final CytoscapeUtils cytoscapeUtils;
	private final TaskDispatcher taskDispatcher;

	public LuceneDataSetFactory(DataSetManager dataSetManager, UiUtils uiUtils, FileUtils fileUtils,
			CytoscapeUtils cytoscapeUtils, TaskDispatcher taskDispatcher) {
		this.dataSetManager = dataSetManager;
		this.uiUtils = uiUtils;
		this.fileUtils = fileUtils;
		this.cytoscapeUtils = cytoscapeUtils;
		this.taskDispatcher = taskDispatcher;
	}
	
	@Override
	public DataSet create(File path, Node documentRoot) throws SAXException {
		return new LuceneDataSet(path, documentRoot, dataSetManager, uiUtils, fileUtils, cytoscapeUtils, taskDispatcher);
	}

	@Override
	public String getId() {
		return "org.genemania.data.lucene.LuceneDataSet"; //$NON-NLS-1$
	}
}
