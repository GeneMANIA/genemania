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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.genemania.data.normalizer.DataFileType;
import org.genemania.data.normalizer.DataImportSettings;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.OrganismMediator;
import org.genemania.plugin.LogUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.completion.DynamicTableModel;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.data.IModelManager;
import org.genemania.plugin.data.Namespace;
import org.genemania.plugin.data.lucene.models.UserNetworkEntry;
import org.genemania.plugin.task.GeneManiaTask;
import org.genemania.plugin.task.TaskDispatcher;

public class ImportedDataController {
	private final DataSetManager dataSetManager;
	private final TaskDispatcher taskDispatcher;

	public ImportedDataController(DataSetManager dataSetManager, TaskDispatcher taskDispatcher) {
		this.dataSetManager = dataSetManager;
		this.taskDispatcher = taskDispatcher;
	}

	public boolean hasData(DataSet data) {
		try {
			List<Organism> organisms = data.getMediatorProvider().getOrganismMediator().getAllOrganisms();
			return organisms.size() > 0;
		} catch (DataStoreException e) {
			LogUtils.log(getClass(), e);
		}
		return false;
	}
	
	public List<Organism> getOrganisms(DataSet data) {
		try {
			OrganismMediator mediator = data.getMediatorProvider().getOrganismMediator();
			List<Organism> organisms = new ArrayList<Organism>(mediator.getAllOrganisms());
			Collections.sort(organisms, new Comparator<Organism>() {
				public int compare(Organism o1, Organism o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			return organisms;
		} catch (DataStoreException e) {
			LogUtils.log(getClass(), e);
		}
		return Collections.emptyList();
	}
	
	public DynamicTableModel<UserNetworkEntry> createModel(final DataSet data) {
		DynamicTableModel<UserNetworkEntry> model = new DynamicTableModel<UserNetworkEntry>() {
			public Class<?> getColumnClass(int columnIndex) {
				return String.class;
			}

			public int getColumnCount() {
				return 3;
			}

			public String getColumnName(int columnIndex) {
				switch (columnIndex) {
				case 0:
					return Strings.customNetworkNetworkColumn_name;
				case 1:
					return Strings.customNetworkOrganismColumn_name;
				case 2:
					return Strings.customNetworkGroupColumn_name;
				}
				return ""; //$NON-NLS-1$
			}

			public Object getValueAt(int rowIndex, int columnIndex) {
				UserNetworkEntry entry = items.get(rowIndex);
				switch (columnIndex) {
				case 0:
					return entry.network.getName();
				case 1:
					return entry.organism.getName();
				case 2:
					return entry.group.getName();
				}
				return null;
			}

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return false;
			}

			public void setValueAt(Object value, int rowIndex, int columnIndex) {
			}
		};
		
		try {
			Collection<InteractionNetwork> userDefinedNetworks = data.getUserNetworks();
			for (InteractionNetwork network : userDefinedNetworks) {
				InteractionNetworkGroup group = data.getNetworkGroup(network.getId());
				Organism organism = data.getOrganism(group.getId());
				model.add(new UserNetworkEntry(organism, group, network));
			}
		} catch (ApplicationException e) {
			LogUtils.log(getClass(), e);
		}
		return model;
	}
	
	public void deleteNetworks(Window parent, DataSet data, DynamicTableModel<UserNetworkEntry> model, int[] indices) {
		for (int index : indices) {
			UserNetworkEntry entry = model.get(index);
			try {
				IModelManager manager = data.createModelManager(Namespace.USER);
				try {
					manager.uninstallNetwork(entry.network);
				} finally {
					manager.close();
				}
			} catch (DataStoreException e) {
				data.log(e);
			} catch (ApplicationException e) {
				data.log(e);
			}
		}
		GeneManiaTask task = new GeneManiaTask(Strings.deleteNetwork_status) {
			@Override
			protected void runTask() throws Throwable {
				dataSetManager.reloadDataSet(progress);
			}
		};
		taskDispatcher.executeTask(task, parent, true, false);
		LogUtils.log(getClass(), task.getLastError());
	}
	
	public void updateNetwork(Window parent, DataSet data, InteractionNetwork network, InteractionNetworkGroup group, String color) {
		try {
			IModelManager manager = data.createModelManager(Namespace.USER);
			try {
				InteractionNetworkGroup oldGroup = data.getNetworkGroup(network.getId());
				Organism organism = data.getOrganism(oldGroup.getId());
				manager.installGroup(organism, group, color);
				manager.updateNetwork(network, group);
			} finally {
				manager.close();
			}
		} catch (ApplicationException e) {
			data.log(e);
		} catch (DataStoreException e) {
			data.log(e);
		}
		GeneManiaTask task = new GeneManiaTask(Strings.editNetwork_status) {
			@Override
			protected void runTask() throws Throwable {
				dataSetManager.reloadDataSet(progress);
			}
		};
		taskDispatcher.executeTask(task, parent, true, true);
		LogUtils.log(getClass(), task.getLastError());
	}
	
	public void importNetwork(Window parent, final DataSet data, final DataImportSettings settings, final String networkFile, final DataFileType type) {
		GeneManiaTask task = new GeneManiaTask(Strings.importNetwork_status) {
			@Override
			protected void runTask() throws Throwable {
				InteractionNetwork network = settings.getNetwork();
				long networkId = data.getNextAvailableId(InteractionNetwork.class, Namespace.USER);
				network.setId(networkId);

				IModelManager manager = data.createModelManager(Namespace.USER);
				try {
					manager.installNetwork(settings, networkFile, type, progress);
				} finally {
					manager.close();
				}
				
				if (progress.isCanceled()) {
					return;
				}
				if (network != null) {
					dataSetManager.reloadDataSet(progress);
				}
			}
		};
		taskDispatcher.executeTask(task, parent, true, true);
		LogUtils.log(getClass(), task.getLastError());
	}
}
