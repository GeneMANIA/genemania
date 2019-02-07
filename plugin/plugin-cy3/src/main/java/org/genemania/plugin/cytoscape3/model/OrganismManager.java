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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.cytoscape.application.CyApplicationConfiguration;
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
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.cytoscape3.task.LoadRemoteNetworksTask;
import org.genemania.plugin.cytoscape3.task.LoadRemoteOrganismsTask;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetManager;

import okhttp3.OkHttpClient;

public class OrganismManager {

	private static final String ORGANISMS_SER = "organisms.ser";
	
	/** @deprecated **/
	private static final String OLD_CACHE_EXPIRES = "organisms.cache.expires";
	private static final String CACHE_EXPIRES = "organisms.cache.expires.hours"; // 0 or less means no cache
	
	private boolean initialized;
	private boolean offline;
	private Set<Organism> localOrganisms = new LinkedHashSet<>();
	private Set<Organism> remoteOrganisms = new LinkedHashSet<>();
	private String loadRemoteOrganismsErrorMessage;
	
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	
	private final GeneMania plugin;
	private final CytoscapeUtils cytoscapeUtils;
	private final CyServiceRegistrar serviceRegistrar;
	
	private final OkHttpClient httpClient = new OkHttpClient(); // Avoid creating several instances
	
	private final Object lock = new Object();
	public Object get;

	public OrganismManager(GeneMania plugin, CytoscapeUtils cytoscapeUtils, CyServiceRegistrar serviceRegistrar) {
		this.plugin = plugin;
		this.cytoscapeUtils = cytoscapeUtils;
		this.serviceRegistrar = serviceRegistrar;
		
		DataSetManager dataSetManager = plugin.getDataSetManager();
		dataSetManager.addDataSetChangeListener((dataSet, progress) -> setLocalData(dataSet));
		
		// ==================
		// Note: We don't want to initialize local data here (when loading the app) anymore,
		//       which would popup a dialog automatically, because no local data is necessary
		//       if all the user wants is to use the online search!
		// initLocalData();
		// ==================
		
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
		if (localOrganisms.isEmpty()) {
			DataSetManager dataSetManager = plugin.getDataSetManager();
			
			if (dataSetManager.getDataSet() == null) // Lazy load local organisms
				initLocalData();
		}
		
		return new LinkedHashSet<>(localOrganisms);
	}
	
	public Set<Organism> getRemoteOrganisms() {
		return new LinkedHashSet<>(remoteOrganisms);
	}
	
	public String getLoadRemoteOrganismsErrorMessage() {
		return loadRemoteOrganismsErrorMessage;
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
		// First try to load them from local cache
		loadRemoteOrganismsFromCache();
		
		if (remoteOrganisms.isEmpty()) // No valid cache -- Load from the server
			loadRemoteOrganismsFromServer();
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
	
	private void saveRemoteOrganismToCache() {
		if (remoteOrganisms.isEmpty())
			return;
		
		File dir = getAppDir();
		
		if (dir != null) {
			File file = new File(dir, ORGANISMS_SER);
			
			try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
				ObjectOutputStream oos = new ObjectOutputStream(out);
				oos.writeObject(remoteOrganisms);
			} catch (final Exception e) {
				LogUtils.log(this.getClass(), e);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void loadRemoteOrganismsFromCache() {
		remoteOrganisms.clear();
		
		final int expires = getCacheExpiresValue();
		
		if (expires <= 0)
			return; // Ignore cache!
		
		File dir = getAppDir();
		
		if (dir != null) {
			File file = new File(dir, ORGANISMS_SER);
			
			try {
				if (!file.exists() || !file.canRead())
					return;
				
				Path path = Paths.get(file.getAbsolutePath());
				BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
				
				if (System.currentTimeMillis() - attr.lastModifiedTime().toMillis() > TimeUnit.HOURS.toMillis(expires))
					return; // Cache is too old
			} catch (final Exception e) {
				LogUtils.log(this.getClass(), e);
			}
			
			Set<Organism> newValue = null;
			
			try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
				ObjectInputStream ois = new ObjectInputStream(in);
				newValue = (Set<Organism>) ois.readObject();
			} catch (final Exception e) {
				LogUtils.log(this.getClass(), e);
			}
			
			if (newValue != null && !newValue.isEmpty()) {
				remoteOrganisms.addAll(newValue);
				initialized = true;
				propertyChangeSupport.firePropertyChange("organisms", Collections.emptySet(), newValue);
			}
		}
	}
	
	private void loadRemoteOrganismsFromServer() {
		LoadRemoteOrganismsTask task1 = new LoadRemoteOrganismsTask(httpClient, cytoscapeUtils);
		LoadRemoteNetworksTask task2 = new LoadRemoteNetworksTask(httpClient, cytoscapeUtils);
		
		DialogTaskManager taskManager = serviceRegistrar.getService(DialogTaskManager.class);
		taskManager.execute(new TaskIterator(task1, task2), new TaskObserver() {
			@Override
			public void taskFinished(ObservableTask ot) {
				// Nothing to do here...
			}
			@Override
			public void allFinished(FinishStatus finishStatus) {
				// First, retrieve the organisms
				loadRemoteOrganismsErrorMessage = task1.getErrorMessage();
				
				if (finishStatus != FinishStatus.getSucceeded() && finishStatus.getException() != null) {
					loadRemoteOrganismsErrorMessage = finishStatus.getException().getMessage();
					return;
				}
				
				final Set<Organism> oldValue = new LinkedHashSet<>(remoteOrganisms);
				final Set<Organism> newValue = task1.getOrganisms();
				
				synchronized (lock) {
					remoteOrganisms.clear();
					remoteOrganisms.addAll(newValue);
				}
				
				// Then retrieve the networks and assign them to their organisms
				if (task2.getErrorMessage() != null) {
					if (loadRemoteOrganismsErrorMessage == null)
						loadRemoteOrganismsErrorMessage = task2.getErrorMessage();
					else
						loadRemoteOrganismsErrorMessage = "1) " + loadRemoteOrganismsErrorMessage + "; 2) "
								+ task2.getErrorMessage();
				}
				
				task2.getNetworkGroups().forEach((id, groups) -> {
					for (Organism org : remoteOrganisms) {
						if (id == org.getId()) {
							org.setInteractionNetworkGroups(groups);
							break;
						}
					}
				});
				
				// Finally, fire the property change event
				initialized = true;
				
				if (finishStatus == FinishStatus.getSucceeded() && loadRemoteOrganismsErrorMessage == null)
					propertyChangeSupport.firePropertyChange("organisms", oldValue, newValue);
				else
					propertyChangeSupport.firePropertyChange("loadRemoteOrganismsException", null, loadRemoteOrganismsErrorMessage);
				
				final long expires = getCacheExpiresValue();
				
				if (expires > -1) // -1 means "don't even create cache"
					new Thread(() -> saveRemoteOrganismToCache()).start();
			}
		});
	}
	
	private int getCacheExpiresValue() {
		try {
			// Convert the old property name to the new one, if it exists:
			String value = cytoscapeUtils.getPreference(OLD_CACHE_EXPIRES);
			
			if (value != null) {
				cytoscapeUtils.setPreference(CACHE_EXPIRES, value);
				cytoscapeUtils.removePreference(OLD_CACHE_EXPIRES);
			} else {
				value = cytoscapeUtils.getPreference(CACHE_EXPIRES);
			}
			
			if (value != null)
				return (int) Float.parseFloat(value);
		} catch (Exception e) {
			LogUtils.log(getClass(), e);
		}
		
		return 0;
	}
	
	private File getAppDir() {
		CyApplicationConfiguration appConfig = serviceRegistrar.getService(CyApplicationConfiguration.class);
		
		try {
			final File appDir = appConfig.getAppConfigurationDirectoryLocation(this.getClass());
			
			if (appDir != null) {
				if (appDir.exists() || appDir.mkdir())
					return appDir;
			}
		} catch (final Exception e) {
			LogUtils.log(this.getClass(), e);
		}
		
		return null;
	}
}
