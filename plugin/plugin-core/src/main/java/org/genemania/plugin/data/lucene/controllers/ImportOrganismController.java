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
package org.genemania.plugin.data.lucene.controllers;

import java.awt.Window;
import java.io.Reader;
import java.util.Collection;
import java.util.Set;

import org.genemania.domain.Node;
import org.genemania.domain.Organism;
import org.genemania.dto.AddOrganismEngineRequestDto;
import org.genemania.engine.IMania;
import org.genemania.engine.Mania2;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.cache.MemObjectCache;
import org.genemania.engine.cache.SynchronizedObjectCache;
import org.genemania.plugin.LogUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.completion.DynamicTableModel;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.data.IModelWriter;
import org.genemania.plugin.data.Namespace;
import org.genemania.plugin.data.lucene.LuceneDataSet;
import org.genemania.plugin.parsers.IdFileParser;
import org.genemania.plugin.task.GeneManiaTask;
import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.util.ChildProgressReporter;

public class ImportOrganismController {
	
	private final DataSetManager dataSetManager;
	private final TaskDispatcher taskDispatcher;

	public ImportOrganismController(DataSetManager dataSetManager, TaskDispatcher taskDispatcher) {
		this.dataSetManager = dataSetManager;
		this.taskDispatcher = taskDispatcher;
	}
	
	public DynamicTableModel<Organism> createModel(DataSet data) {
		return new DynamicTableModel<Organism>() {
			@Override
			public Class<?> getColumnClass(int column) {
				return String.class;
			}

			@Override
			public int getColumnCount() {
				return 2;
			}

			@Override
			public String getColumnName(int column) {
				switch (column) {
				case 0:
					return Strings.importOrganismNameColumn_name;
				case 1:
					return Strings.importOrganismDescriptionColumn_name;
				}
				return null;
			}

			@Override
			public Object getValueAt(int row, int column) {
				Organism organism = get(row);
				switch (column) {
				case 0:
					return organism.getName();
				case 1:
					return organism.getDescription();
				}
				return null;
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}

			@Override
			public void setValueAt(Object value, int row, int column) {
			}
		};
	}

	public void deleteOrganisms(Window parent, LuceneDataSet data, DynamicTableModel<Organism> installedModel, int[] selection) {
		GeneManiaTask task = new GeneManiaTask(Strings.importOrganismDelete_title) {
			@Override
			protected void runTask() throws Throwable {
				for (int index :selection) {
					Organism organism = installedModel.get(index);
					data.deleteOrganism(organism);
				}
				dataSetManager.reloadDataSet(progress);
			}
		};
		taskDispatcher.executeTask(task, parent, true, true);
		LogUtils.log(getClass(), task.getLastError());
		
	}

	public void updateOrganism(Window parent, DataSet data, Organism organism) {
		GeneManiaTask task = new GeneManiaTask(Strings.importOrganismUpdate_title) {
			@Override
			protected void runTask() throws Throwable {
				IModelWriter writer = data.createModelWriter();
				try {
					writer.deleteOrganism(organism);
					writer.addOrganism(organism);
				} finally {
					writer.close();
				}
				dataSetManager.reloadDataSet(progress);
			}
		};
		taskDispatcher.executeTask(task, parent, true, true);
		LogUtils.log(getClass(), task.getLastError());
	}
	
	public void importOrganism(Window parent, final DataSet data, final Reader reader, final Organism organism) {
		GeneManiaTask task = new GeneManiaTask(Strings.importOrganismImport_title) {
			@Override
			protected void runTask() throws Throwable {
				progress.setMaximumProgress(2);
				progress.setStatus(Strings.importOrganismImport_status);
				progress.setProgress(0);
				ChildProgressReporter childProgress = new ChildProgressReporter(progress);
				Set<Node> nodes;
				IdFileParser parser;
				try {
					parser = new IdFileParser(data, Namespace.USER);
					nodes = parser.parseNodes(reader, organism, childProgress);
					
					IModelWriter writer = data.createModelWriter();
					try {
						writer.addOrganism(organism);
						for (Node node : nodes) {
							writer.addNode(node, organism);
						}
					} finally {
						writer.close();
					}
				} finally {
					childProgress.close();
				}
				
				childProgress = new ChildProgressReporter(progress);
				try {
					AddOrganismEngineRequestDto request = new AddOrganismEngineRequestDto();
					request.setProgressReporter(childProgress);
					request.setOrganismId(organism.getId());
					Collection<Long> nodeIds = parser.extractNodeIds(nodes);
					request.setNodeIds(nodeIds);
					
					DataCache cache = new DataCache(new SynchronizedObjectCache(new MemObjectCache(data.getObjectCache(progress, false))));
					IMania mania = new Mania2(cache);
					mania.addOrganism(request);
				} finally {
					childProgress.close();
				}
				
				dataSetManager.reloadDataSet(progress);
			}
		};
		taskDispatcher.executeTask(task, parent, true, true);
		LogUtils.log(getClass(), task.getLastError());
	}
}
