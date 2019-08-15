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
package org.genemania.plugin.controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.genemania.plugin.FileUtils;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetManager;

public class DownloadController {
	
	public static Pattern optionPattern = Pattern.compile("(\\d+-\\d+-\\d+)(-.+)?"); //$NON-NLS-1$

	private final DataSetManager dataSetManager;

	private final FileUtils fileUtils;

	public DownloadController(DataSetManager dataSetManager, FileUtils fileUtils) {
		this.dataSetManager = dataSetManager;
		this.fileUtils = fileUtils;
	}
	
	public List<ModelElement> createModel() throws IOException {
		Pattern pattern = Pattern.compile(".*?gmdata-(.*?)"); //$NON-NLS-1$

		List<File> paths = dataSetManager.getDataSetPaths();
		Set<String> alreadyInstalled = new HashSet<>();
		
		for (File file : paths) {
			Matcher matcher = pattern.matcher(file.getName());
			if (matcher.matches()) {
				alreadyInstalled.add(matcher.group(1));
			}
		}
		
		String activeDataId = null;
		DataSet data = dataSetManager.getDataSet();
		if (data != null) {
			activeDataId = data.getVersion().toString();
		}
		
		Map<String, String> descriptions = fileUtils.getDataSetDescriptions();
		Map<String, Long> sizes = fileUtils.getDataSetSizes();
		List<String> dataSets = fileUtils.getCompatibleDataSets();
		List<ModelElement> model = new ArrayList<>();
		Map<String, List<ModelElement>> parents = new HashMap<>();
		
		for (String url : dataSets) {
			Matcher matcher = pattern.matcher(url);
			
			if (matcher.matches()) {
				String dataId = matcher.group(1);
				String description = descriptions.get(dataId);
				Long size = sizes.get(dataId);
				if (size == null) {
					size = 0L;
				}
				ModelElement element = new ModelElement(dataId, description, alreadyInstalled.contains(dataId), dataId.equals(activeDataId), size);
				
				Matcher childMatcher = optionPattern.matcher(dataId);
				if (childMatcher.matches() && childMatcher.group(2) != null) {
					String parent = childMatcher.group(1);
					List<ModelElement> children;
					if (parents.containsKey(parent)) {
						children = parents.get(parent);
					} else {
						children = new ArrayList<>();
						parents.put(parent, children);
					}
					children.add(element);
				} else {
					model.add(element);
				}
			}
		}
		
		for (ModelElement parent : model) {
			String dataId = parent.getName();
			List<ModelElement> children = parents.get(dataId);
			if (children == null) {
				continue;
			}
			children.add(new ModelElement(parent.getName(), parent.getDescription(), parent.isInstalled(), parent.isActive(), parent.getSize()));
			ModelElement[] elements = children.toArray(new ModelElement[children.size()]);
			parent.setChildren(elements);
		}
		
		return model;
	}
	
	public static class ModelElement implements Comparable<ModelElement> {
		
		private static final ModelElement[] NO_CHILDREN = new ModelElement[0];
		private String name;
		private String description;
		private boolean installed;
		private boolean active;
		private ModelElement[] children;
		private long size;
		
		public ModelElement(String name, String description, boolean installed, boolean active, long size) {
			this.name = name;
			this.description = description == null ? "" : description; //$NON-NLS-1$
			this.installed = installed;
			this.active = active;
			this.size = size;
			children = NO_CHILDREN;
		}
		
		public void setChildren(ModelElement[] children) {
			this.children = children == null ? NO_CHILDREN : children;
		}

		public String getName() {
			return name;
		}
		
		public String getDescription() {
			return description;
		}
		
		public boolean isInstalled() {
			if (children.length == 0) { 
				return installed;
			}
			for (ModelElement element : children) {
				if (element.installed) {
					return true;
				}
			}
			return false;
		}

		public boolean isActive() {
			if (children.length == 0) { 
				return active;
			}
			for (ModelElement element : children) {
				if (element.active) {
					return true;
				}
			}
			return false;
		}
		
		public long getSize() {
			return size;
		}
		
		public ModelElement[] getChildren() {
			return children;
		}
		
		public int compareTo(ModelElement o) {
			return name.compareTo(o.name);
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
}
