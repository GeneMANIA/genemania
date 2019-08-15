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
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.genemania.exception.ApplicationException;
import org.genemania.plugin.FileUtils;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.data.DataDescriptor;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.data.lucene.LuceneDataSet;
import org.genemania.util.ChildProgressReporter;
import org.genemania.util.ProgressReporter;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.xml.sax.SAXException;

public class DataAdmin extends AbstractPluginApp {
	
	@Argument
	protected List<String> fArguments = new ArrayList<>();

	protected List<String> getArguments() {
		return fArguments;
	}

	public static void main(String[] args) throws Exception {
		DataAdmin admin = new DataAdmin();
		CmdLineParser parser = new CmdLineParser(admin);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			printUsage(parser);
			return;
		}
		admin.initialize();
		
		try {
			admin.handleArguments();
		} catch (IllegalArgumentException e) {
			String message = e.getMessage();
			if (message != null) {
				System.err.println(message);
			}
			printUsage(parser);
		}
	}

	private static void printUsage(CmdLineParser parser) {
		System.err.println(String.format("\nGeneral usage: %s (list | install | list-data | install-data | uninstall-data) options\n", DataAdmin.class.getSimpleName())); //$NON-NLS-1$
		parser.printUsage(System.err);
	}
	
	private void handleArguments() throws InstantiationException, IllegalAccessException, IllegalArgumentException, SecurityException, InvocationTargetException, NoSuchMethodException {
		Logger.getLogger("org.genemania.plugin").setLevel(Level.WARN); //$NON-NLS-1$
		Logger.getLogger("org.genemania").setLevel(Level.ERROR); //$NON-NLS-1$

		Map<String, Class<? extends Command>> commands = new HashMap<String, Class<? extends Command>>();
		commands.put("list", ListDataSetCommand.class); //$NON-NLS-1$
		commands.put("install", InstallDataSetCommand.class); //$NON-NLS-1$
		commands.put("list-data", ListDescriptorsCommand.class); //$NON-NLS-1$
		commands.put("install-data", InstallDescriptorsCommand.class); //$NON-NLS-1$
		commands.put("uninstall-data", UninstallDescriptorsCommand.class); //$NON-NLS-1$
		
		List<String> arguments = getArguments();
		if (arguments.size() == 0) {
			throw new IllegalArgumentException();
		}
		
		String commandName = arguments.get(0).toLowerCase();
		Class<? extends Command> commandClass = commands.get(commandName);
		
		if (commandClass == null) {
			throw new IllegalArgumentException(String.format("Unrecognized command: %s", commandName)); //$NON-NLS-1$
		}
		
		Command command = commandClass.getConstructor(String.class).newInstance(commandName);
		if (command == null) {
			throw new IllegalArgumentException();
		}
		
		command.setArguments(arguments);
		command.validate();
		command.run();
	}

	private void initialize() {
	}
	
	static abstract class Command {
		static final Pattern DATA_SET_PATTERN = Pattern.compile(".*?gmdata-(.*?)"); //$NON-NLS-1$

		protected List<String> fArguments;
		protected DataSetManager fManager;
		protected FileUtils fFileUtils;
		protected LuceneDataSet fData;
		protected String fName;
		protected ProgressReporter fProgress;
		
		public Command(String name) {
			fName = name;
			fManager = AbstractPluginApp.createDataSetManager();
			fProgress = new ConsoleProgressReporter();
			fFileUtils = new FileUtils();
		}
		
		public void setArguments(List<String> arguments) {
			fArguments = arguments;
		}

		protected void validateDataSet(File file) {
			if (!fManager.isDataSet(file)) {
				throw new IllegalArgumentException(String.format("%s is not a GeneMANIA data set", file.toString())); //$NON-NLS-1$
			}
		}
		
		protected String getDataSetId(String url) {
			Matcher matcher = DATA_SET_PATTERN.matcher(url);
			if (!matcher.matches()) {
				return null;
			}
			return matcher.group(1); 
		}

		public abstract void run();
		public abstract void validate() throws IllegalArgumentException;
	}
	
	static class ListDataSetCommand extends Command {
		public ListDataSetCommand(String name) {
			super(name);
		}
		
		@Override
		public void run() {
			try {
				Map<String, String> descriptions = fFileUtils.getDataSetDescriptions();
				Map<String, Long> sizes = fFileUtils.getDataSetSizes();
				List<String> dataSets = fFileUtils.getCompatibleDataSets();
				
				System.out.printf("Data Set ID\tTotal Size\tDatabase Version\n"); //$NON-NLS-1$
				for (String url : dataSets) {
					String name = getDataSetId(url);
					double size = sizes.get(name) / 1024.0;
					String description = descriptions.get(name);
					System.out.printf("%s\t%.2f MB\t%s\n", name, size, description); //$NON-NLS-1$
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void validate() throws IllegalArgumentException {
		}
	}
	
	public static class InstallDataSetCommand extends Command {
		private String fId;

		public InstallDataSetCommand(String name) {
			super(name);
		}

		@Override
		public void run() {
			try {
				String baseUrl = fFileUtils.findDataSetBaseUrl(fId);
				URL url = new URL(String.format("%s.zip", baseUrl)); //$NON-NLS-1$
				fProgress.setStatus("Installing..."); //$NON-NLS-1$
				fProgress.setMaximumProgress(2);
				int progress = 0;
				fProgress.setProgress(progress);
				ChildProgressReporter childProgress = new ChildProgressReporter(fProgress);
				File dataZipFile;
				try {
					childProgress.setStatus(String.format("Downloading %s", url)); //$NON-NLS-1$
					dataZipFile = fFileUtils.download(url, new File("."), childProgress); //$NON-NLS-1$
				} finally {
					childProgress.close();
				}
				if (dataZipFile == null) {
					return;
				}
				
				childProgress = new ChildProgressReporter(fProgress);
				try {
					fFileUtils.unzip(dataZipFile, dataZipFile.getParentFile(), childProgress);
				} finally {
					childProgress.close();
				}
				dataZipFile.delete();
				fProgress.setStatus("Done."); //$NON-NLS-1$
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void validate() throws IllegalArgumentException {
			try {
				if (fArguments.size() < 2) {
					throw new IllegalArgumentException(String.format("Usage: %s data-set-id", fName)); //$NON-NLS-1$
				}
				String targetId = fArguments.get(1);
				List<String> dataSets = fFileUtils.getCompatibleDataSets();
				
				for (String url : dataSets) {
					String id = getDataSetId(url);
					if (targetId.equals(id)) {
						fId = id;
						return;
					}
				}
				throw new IllegalArgumentException(String.format("Data set with ID='%s' doesn't exist", targetId)); //$NON-NLS-1$
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}

	static class ListDescriptorsCommand extends Command {
		public ListDescriptorsCommand(String name) {
			super(name);
		}

		@Override
		public void run() {
			String path = fArguments.get(1); 
			try {
				fData = (LuceneDataSet) fManager.open(new File(path));
				List<DataDescriptor> available = null;
				
				try {
					available = fData.getAvailableDataDescriptors();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(
							null,
							e.getMessage(), 
							"GeneMANIA Error",
							JOptionPane.ERROR_MESSAGE
					);
					return;
				}
				
				List<DataDescriptor> installed = fData.getInstalledDataDescriptors();
				
				System.out.println("Data ID\tDescription\tStatus"); //$NON-NLS-1$
				
				for (DataDescriptor descriptor : available) {
					boolean isInstalled = installed.contains(descriptor);
					String status = isInstalled ? "Installed" : ""; //$NON-NLS-1$ //$NON-NLS-2$
					System.out.printf("%s\t%s\t%s\n", descriptor.getId(), descriptor.getDescription(), status); //$NON-NLS-1$
				}
			} catch (SAXException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void validate() throws IllegalArgumentException {
			if (fArguments.size() < 2) {
				throw new IllegalArgumentException(String.format("Usage: %s path/to/data-set", fName)); //$NON-NLS-1$
			}

			File file = new File(fArguments.get(1));
			validateDataSet(file);
		}
		
	}
	
	static class InstallDescriptorsCommand extends Command {
		Set<DataDescriptor> fDescriptors;
		
		public InstallDescriptorsCommand(String name) {
			super(name);
			fDescriptors = new HashSet<DataDescriptor>();
		}

		@Override
		public void run() {
			fProgress.setStatus("Installing..."); //$NON-NLS-1$
			fProgress.setMaximumProgress(fDescriptors.size());
			List<DataDescriptor> sorted = new ArrayList<DataDescriptor>(fDescriptors);
			Collections.sort(sorted);
			for (DataDescriptor descriptor : sorted) {
				try {
					ChildProgressReporter childProgress = new ChildProgressReporter(fProgress);
					try {
						fData.installIndex(descriptor.getId(), descriptor.getDescription(), childProgress);
					} finally {
						childProgress.close();
					}
				} catch (ApplicationException e) {
					throw new RuntimeException(e);
				}
			}
			fProgress.setProgress(fDescriptors.size());
			fProgress.setStatus("Done."); //$NON-NLS-1$
		}

		@Override
		public void validate() throws IllegalArgumentException {
			if (fArguments.size() < 3) {
				throw new IllegalArgumentException(String.format("Usage: %s path/to/data-set data-id [ data-id2 ... ]", fName)); //$NON-NLS-1$
			}
			
			File file = new File(fArguments.get(1));
			validateDataSet(file);

			try {
				fData = (LuceneDataSet) fManager.open(file);
				List<DataDescriptor> available = null;
				
				try {
					available = fData.getAvailableDataDescriptors();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(
							null,
							e.getMessage(), 
							"GeneMANIA Error",
							JOptionPane.ERROR_MESSAGE
					);
					return;
				}
				
				List<DataDescriptor> installed = fData.getInstalledDataDescriptors();
				
				for (int i = 2; i < fArguments.size(); i++) {
					String id = fArguments.get(i);
					if ("all".equals(id)) { //$NON-NLS-1$
						addAll(available, installed);
						break;
					}
					DataDescriptor target = new DataDescriptor(id, null);
					int index = available.indexOf(target);
					if (index == -1) {
						throw new IllegalArgumentException(String.format("This data set does not contain any data for ID='%s'", id)); //$NON-NLS-1$
					}
					
					if (installed.contains(target)) {
						System.err.printf("Data with ID='%s' is already installed\n", id); //$NON-NLS-1$
						continue;
					}
					
					fDescriptors.add(available.get(index));
				}
			} catch (SAXException e) {
				throw new RuntimeException(e);
			}
		}

		private void addAll(List<DataDescriptor> available, List<DataDescriptor> installed) {
			for (DataDescriptor descriptor : available) {
				if (installed.contains(descriptor)) {
					continue;
				}
				fDescriptors.add(descriptor);
			}
		}
	}
	
	static class UninstallDescriptorsCommand extends Command {
		private List<DataDescriptor> fDescriptors;

		public UninstallDescriptorsCommand(String name) {
			super(name);
			fDescriptors = new ArrayList<DataDescriptor>();
		}

		@Override
		public void run() {
			fProgress.setStatus("Uninstalling..."); //$NON-NLS-1$
			fProgress.setMaximumProgress(fDescriptors.size());
			for (DataDescriptor descriptor : fDescriptors) {
				try {
					fProgress.setStatus(String.format("Uninstalling %s", descriptor.getDescription())); //$NON-NLS-1$
					fData.deleteIndex(descriptor.getId());
				} catch (ApplicationException e) {
					throw new RuntimeException(e);
				}
			}
			fProgress.setProgress(fDescriptors.size());
			fProgress.setStatus("Done."); //$NON-NLS-1$
		}

		@Override
		public void validate() throws IllegalArgumentException {
			if (fArguments.size() < 3) {
				throw new IllegalArgumentException(String.format("Usage: %s path/to/data-set data-id [ data-id2 ... ]", fName)); //$NON-NLS-1$
			}
			
			File file = new File(fArguments.get(1));
			validateDataSet(file);

			try {
				fData = (LuceneDataSet) fManager.open(file);
				List<DataDescriptor> installed = fData.getInstalledDataDescriptors();
				for (int i = 2; i < fArguments.size(); i++) {
					String id = fArguments.get(i);
					DataDescriptor target = new DataDescriptor(id, null);
					int index = installed.indexOf(target);
					if (index == -1) {
						System.err.printf("Data with ID='%s' isn't installed\n", id); //$NON-NLS-1$
						continue;
					}
					
					fDescriptors.add(installed.get(index));
				}
			} catch (SAXException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	static class ConsoleProgressReporter implements ProgressReporter {
		private String fDescription;
		private boolean fCanceled;
		private int fProgress;
		private int fMaximumProgress;
		private String fStatus;
		private int lastValue = Integer.MIN_VALUE;
		private String lastStatus;

		@Override
		public void cancel() {
			fCanceled = true;
		}

		@Override
		public String getDescription() {
			return fDescription;
		}

		@Override
		public int getMaximumProgress() {
			return fMaximumProgress;
		}

		@Override
		public int getProgress() {
			return fProgress;
		}

		@Override
		public String getStatus() {
			return fStatus;
		}

		@Override
		public boolean isCanceled() {
			return fCanceled;
		}

		@Override
		public void setDescription(String description) {
			fDescription = description;
		}

		@Override
		public void setMaximumProgress(int maximum) {
			fMaximumProgress = maximum;
		}

		@Override
		public void setProgress(int progress) {
			fProgress = progress;
			double percent = (double) progress / fMaximumProgress;
			int value = (int) Math.floor(percent * 100);
			if (value == lastValue ) {
				return;
			}
			lastValue = value;
			System.err.printf("\r[%3.0f%%]", percent * 100); //$NON-NLS-1$
			if (value == 100) {
				System.err.println();
			}
		}

		@Override
		public void setStatus(String status) {
			if (status == null) {
				lastStatus = null;
				return;
			}
			
			if (status.equals(lastStatus)) {
				return;
			}
			lastStatus = status;
			System.err.printf("\r%s\n", status); //$NON-NLS-1$
			fStatus = status;
		}
		
	}
}