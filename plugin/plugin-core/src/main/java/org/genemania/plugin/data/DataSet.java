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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.genemania.data.classification.IGeneClassifier;
import org.genemania.data.normalizer.GeneCompletionProvider2;
import org.genemania.domain.GeneNamingSource;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkById;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.InteractionNetworkGroupById;
import org.genemania.domain.Organism;
import org.genemania.engine.cache.IObjectCache;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.plugin.data.compatibility.AdapterStrategy1450;
import org.genemania.plugin.data.compatibility.AdapterStrategyObjectCache;
import org.genemania.util.ProgressReporter;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public abstract class DataSet {
	
	public static final String USER = "user"; //$NON-NLS-1$
	public static final String BASE = "base"; //$NON-NLS-1$
	public static final String CACHE_PATH = "cache"; //$NON-NLS-1$
	
	public static final String ACCESS_MODE_COMPACT = "compact"; //$NON-NLS-1$
	public static final String ACCESS_MODE_DIRECT = "direct"; //$NON-NLS-1$
	
	protected Version version;
	protected final File path;
	protected String accessMode;
	
	private boolean headlessMode;
	
	public DataSet(File path, Node root) throws SAXException {
		this.path = path;
		
		try {
			processConfiguration(root);
		} catch (XPathExpressionException e) {
			throw new SAXException(e);
		}
	}

	protected void processConfiguration(Node root) throws XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		version = Version.parse(getString(xpath, "data-version", root)); //$NON-NLS-1$
		accessMode = getString(xpath, "access-mode", root); //$NON-NLS-1$
	}
	
	public Version getVersion() {
		return version;
	}
	
	public String getAccessMode() {
		return accessMode;
	}

	protected static String getString(XPath xpath, String query, Node root) throws XPathExpressionException {
		String value = (String) xpath.evaluate(query, root, XPathConstants.STRING);
		if (value == null || value.trim().length() == 0) {
			return null;
		}
		return value;
	}
	
	public String getDescription() {
		return version.toString();
	}

	public IObjectCache getObjectCache(ProgressReporter progress, boolean forceRebuild) throws ApplicationException, DataStoreException {
		String fullCachePath = getFullPath(CACHE_PATH);
		AdapterStrategyObjectCache cache = new AdapterStrategyObjectCache(fullCachePath);
		
		cache.addStrategy(new AdapterStrategy1450(getClassLoader("compatibility/1472/"))); //$NON-NLS-1$
		cache.addStrategy(new AdapterStrategy1450(getClassLoader("compatibility/1450/"))); //$NON-NLS-1$
		
		return cache;
	}
	
	ClassLoader getClassLoader(String root) {
		URL base = getClass().getClassLoader().getResource(root);
		return new URLClassLoader(new URL[] { base }, null);
	}
	
	public String getFullPath(String relativePath) {
		return getFullPath(path.getPath(), relativePath);
	}
	
	static String getFullPath(String base, String path) {
		return String.format("%s%s%s", base, File.separator, path); //$NON-NLS-1$
	}
	
	public String getBasePath() {
		return path.getPath();
	}
	
	public void log(Throwable e) {
		if (e == null) {
			return;
		}
		Logger logger = Logger.getLogger(getClass());
		logger.error("Unexpected error", e); //$NON-NLS-1$
	}
	
	public boolean isHeadless() {
		return headlessMode;
	}

	public void setHeadless(boolean enabled) {
		headlessMode = enabled;
	}
	
	public abstract IConfiguration getConfiguration();
	public abstract IMediatorProvider getMediatorProvider();
	public abstract void reload(ProgressReporter progress) throws IOException;
	public abstract GeneCompletionProvider2 getCompletionProvider(Organism organism);
	public abstract Colour getColor(String code);
	public abstract InteractionNetworkGroup getNetworkGroup(long networkId);
	public abstract InteractionNetworkGroupById getNetworkGroupById(long networkId);
	public abstract List<DataDescriptor> getInstalledDataDescriptors();
	public abstract List<Long> getNodeIds(long organismId);
	public abstract List<GeneNamingSource> getAllNamingSources();
	public abstract IGeneClassifier getGeneClassifier();
	public abstract <T> Long getNextAvailableId(Class<T> modelClass, Namespace namespace) throws ApplicationException;
	public abstract IModelWriter createModelWriter() throws ApplicationException;
	public abstract IModelManager createModelManager(Namespace namespace) throws ApplicationException;
	public abstract Organism getOrganism(long networkGroupId);
	public abstract Collection<InteractionNetwork> getUserNetworks() throws ApplicationException;
}
