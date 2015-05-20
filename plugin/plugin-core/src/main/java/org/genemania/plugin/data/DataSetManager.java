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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.genemania.engine.cache.SoftRefObjectCache;
import org.genemania.plugin.AbstractGeneMania;
import org.genemania.plugin.LogUtils;
import org.genemania.util.ProgressReporter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class DataSetManager {
	public static final String DATA_FILE_NAME = "genemania.xml"; //$NON-NLS-1$
	protected static final String LAST_DATA_SET = "last_data_set"; //$NON-NLS-1$

	private final Map<String, IDataSetFactory> factories;

	protected final List<DataSetChangeListener> dataSetListeners;

	protected DataSet dataSet;
	protected File dataSourcePath;

	public DataSetManager() {
		factories = new HashMap<String, IDataSetFactory>();
		dataSetListeners = new ArrayList<DataSetChangeListener>();
	}
	
	public void addDataSetFactory(IDataSetFactory factory, Map<?, ?> serviceProperties) {
		factories.put(factory.getId(), factory);
	}
	
	public void removeDataSetFactory(IDataSetFactory factory, Map<?, ?> serviceProperties) {
		factories.remove(factory.getId());
	}
	
	public IDataSetFactory getFactory(String id) {
		return factories.get(id);
	}
	
	public DataSet open(File path) throws SAXException {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			String configPath = getFullPath(path.getPath(), DATA_FILE_NAME);
			File file = new File(configPath);
			Document document = builder.parse(file.toURI().toString());
			Node root = document.getDocumentElement();
			
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			String type = getString(xpath, "type", root); //$NON-NLS-1$
			
			IDataSetFactory dataSetFactory = getFactory(type);
			return dataSetFactory.create(path, root);
		} catch (IOException e) {
			throw new SAXException(e);
		} catch (ParserConfigurationException e) {
			throw new SAXException(e);
		} catch (XPathExpressionException e) {
			throw new SAXException(e);
		} catch (SecurityException e) {
			throw new SAXException(e);
		} catch (IllegalArgumentException e) {
			throw new SAXException(e);
		}
	}
	
	public boolean isDataSet(File file) {
		File config = new File(getFullPath(file.getPath(), DATA_FILE_NAME));
		return config.isFile();
	}
	
	String getFullPath(String base, String path) {
		return String.format("%s%s%s", base, File.separator, path); //$NON-NLS-1$
	}
	
	protected static String getString(XPath xpath, String query, Node root) throws XPathExpressionException {
		String value = (String) xpath.evaluate(query, root, XPathConstants.STRING);
		if (value == null || value.trim().length() == 0) {
			return null;
		}
		return value;
	}
	
	public void setDataSet(DataSet data, ProgressReporter progress) {
		// Hack for #1451: Clear the cache.  We ought to change this so the
		//                 cache isn't static.
		SoftRefObjectCache.instance().clear();

		dataSet = data;
		notifyDataSetListeners(progress);
		File path;
		if (data != null) {
			path = new File(data.getBasePath());
		} else {
			path = null;
		}
		setDataSourcePath(path);
	}
	
	public DataSet getDataSet() {
		return dataSet;
	}

	public File getDataSourcePath() {
		synchronized (this) {
			return dataSourcePath;
		}
	}
	
	public void setDataSourcePath(File path) {
		synchronized (this) {
			dataSourcePath = path;
			File settingsDirectory = AbstractGeneMania.getSettingsDirectory();
			String lastDataSet = String.format("%s%s%s", settingsDirectory.getPath(), File.separator, LAST_DATA_SET); //$NON-NLS-1$
			try {
				if (path == null) {
					new File(lastDataSet).delete();
					return;
				}
				
				FileWriter writer = new FileWriter(lastDataSet);
				try {
					writer.write(path.getPath());
					writer.write("\n"); //$NON-NLS-1$
				} finally {
					writer.close();
				}
			} catch (IOException e) {
				LogUtils.log(getClass(), e);
			}
		}
	}

	public List<File> getDataSetPaths() {
		List<File> paths = new LinkedList<File>();
		File mostRecent = getMostRecentDataSetFile();
		File root = AbstractGeneMania.getSettingsDirectory();
		for (File file : root.listFiles()) {
			if (!file.isDirectory() || mostRecent != null && file.equals(mostRecent)) {
				continue;
			}
			if (isDataSet(file)) {
				paths.add(file);
			}
		}
		Collections.sort(paths, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return (int) (o1.lastModified() - o2.lastModified());
			}
		});
		if (mostRecent != null) {
			paths.add(0, mostRecent);
		}
		return paths;
	}
	
	public File getDataSetPath(String dataSetName) {
		File mostRecent = getMostRecentDataSetFile();
		File root = AbstractGeneMania.getSettingsDirectory();
		for (File file : root.listFiles()) {
			if (!file.isDirectory() || mostRecent != null && file.equals(mostRecent)) {
				continue;
			}
			if (!isDataSet(file)) {
				continue;
			}
			String name = file.getName();
			if (name.equals(dataSetName)) {
				return file;
			}
		}
		return null;
	}
	
	private File getMostRecentDataSetFile() {
		try {
			File file = AbstractGeneMania.getSettingsDirectory();
			String path = String.format("%s%s%s", file.getPath(), File.separator, LAST_DATA_SET); //$NON-NLS-1$
			BufferedReader reader = new BufferedReader(new FileReader(path));
			try {
				String dataSetPath = reader.readLine();
				if (dataSetPath == null) {
					return null;
				}
				File dataSet = new File(dataSetPath);
				if (dataSet.isDirectory()) {
					return dataSet;
				}
				return null;
			} finally {
				reader.close();
			}
		} catch (IOException e) {
			return null;
		}
	}
	
	public void reloadDataSet(ProgressReporter progress) {
		if (dataSet == null) {
			return;
		}
		try {
			dataSet.reload(progress);
			notifyDataSetListeners(progress);
		} catch (IOException e) {
			dataSet.log(e);
		}
	}

	void notifyDataSetListeners(ProgressReporter progress) {
		for (DataSetChangeListener listener : dataSetListeners) {
			listener.dataSetChanged(dataSet, progress);
		}
	}
	
	public void addDataSetChangeListener(DataSetChangeListener listener) {
		dataSetListeners.add(listener);
	}
	
	public void removeDataSetChangeListener(DataSetChangeListener listener) {
		dataSetListeners.remove(listener);
	}
}
