/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2017 University of Toronto.
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
package org.genemania.plugin.cytoscape3.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedHashSet;
import java.util.Set;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;
import org.genemania.domain.Organism;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.OrganismMediator;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.LogUtils;
import org.genemania.plugin.cytoscape3.task.LoadRemoteOrganismsTask;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetManager;

public class OrganismManager {

	private boolean initialized;
	private boolean offline;
	private Set<Organism> localOrganisms = new LinkedHashSet<>();
	private Set<Organism> remoteOrganisms = new LinkedHashSet<>();
	private Exception loadRemoteOrganismsException;
	
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	
	private final GeneMania plugin;
	private final CyServiceRegistrar serviceRegistrar;
	
	private final Object lock = new Object();
	public Object get;

	public OrganismManager(GeneMania plugin, CyServiceRegistrar serviceRegistrar) {
		this.plugin = plugin;
		this.serviceRegistrar = serviceRegistrar;
		
		DataSetManager dataSetManager = plugin.getDataSetManager();
		dataSetManager.addDataSetChangeListener((dataSet, progress) -> setLocalData(dataSet));
		initLocalData();
		
		loadRemoteOrganisms();
	}
	
	public boolean isInitialized() {
		return initialized;
	}
	
	public boolean isOffline() {
		return offline;
	}
	
	public void setOffline(boolean offline) {
		if (offline != this.offline) {
			this.offline = offline;
			propertyChangeSupport.firePropertyChange("offline", !offline, offline);
		}
	}
	
	public Set<Organism> getOrganisms() {
		return offline ? getLocalOrganisms() : getRemoteOrganisms();
	}
	
	public Set<Organism> getLocalOrganisms() {
		return new LinkedHashSet<>(localOrganisms);
	}
	
	public Set<Organism> getRemoteOrganisms() {
		return new LinkedHashSet<>(remoteOrganisms);
	}
	
	public Exception getLoadRemoteOrganismsException() {
		return loadRemoteOrganismsException;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
	}
	
	public void loadRemoteOrganisms() {
		LoadRemoteOrganismsTask task = new LoadRemoteOrganismsTask();
		
		DialogTaskManager taskManager = serviceRegistrar.getService(DialogTaskManager.class);
		taskManager.execute(new TaskIterator(task), new TaskObserver() {
			@Override
			public void taskFinished(ObservableTask ot) {
				// Nothing to do here...
			}
			@Override
			public void allFinished(FinishStatus finishStatus) {
				loadRemoteOrganismsException = finishStatus.getException();
				
				Set<Organism> oldValue = new LinkedHashSet<>(remoteOrganisms);
				Set<Organism> newValue = task.getOrganisms();
				
				synchronized (lock) {
					remoteOrganisms.clear();
					remoteOrganisms.addAll(newValue);
				}
				
				initialized = true;
				
				if (finishStatus == FinishStatus.getSucceeded())
					propertyChangeSupport.firePropertyChange("organisms", oldValue, newValue);
				else
					propertyChangeSupport.firePropertyChange("loadRemoteOrganismsException", null, loadRemoteOrganismsException);
			}
		});
	}
	
	private void initLocalData() {
		if (plugin.getDataSetManager().getDataSet() == null)
			plugin.initializeData(serviceRegistrar.getService(CySwingApplication.class).getJFrame(), true);
	}
	
	private void setLocalData(final DataSet data) {
		try {
			Set<Organism> oldValue = new LinkedHashSet<>(localOrganisms);

			OrganismMediator mediator = data.getMediatorProvider().getOrganismMediator();
			Set<Organism> newValue = new LinkedHashSet<>(mediator.getAllOrganisms());

			synchronized (lock) {
				localOrganisms.clear();
				localOrganisms.addAll(newValue);
			}

			if (isOffline())
				propertyChangeSupport.firePropertyChange("organisms", oldValue, newValue);
		} catch (DataStoreException e) {
			LogUtils.log(getClass(), e);
		}
    }
}
