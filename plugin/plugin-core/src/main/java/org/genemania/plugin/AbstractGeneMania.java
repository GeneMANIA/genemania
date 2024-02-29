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
package org.genemania.plugin;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

import org.genemania.exception.ApplicationException;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.data.IConfiguration;
import org.genemania.plugin.data.Version;
import org.genemania.plugin.selection.SessionManager;
import org.genemania.plugin.task.GeneManiaTask;
import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.plugin.view.DownloadDialog;
import org.genemania.plugin.view.DownloadDialog.Action;
import org.genemania.plugin.view.components.WrappedOptionPane;
import org.genemania.plugin.view.util.FileSelectionMode;
import org.genemania.plugin.view.util.UiUtils;
import org.genemania.util.ChildProgressReporter;
import org.genemania.util.ProgressReporter;
import org.xml.sax.SAXException;

public abstract class AbstractGeneMania implements GeneMania {
	
	protected static final String SETTINGS_DIRECTORY = "genemania_plugin"; //$NON-NLS-1$

	public static final String RELEASE_VERSION = "releaseVersion"; //$NON-NLS-1$
	public static final String MIN_DATA_VERSION = "minDataVersion"; //$NON-NLS-1$
	public static final String BUILD_NUMBER = "buildNumber"; //$NON-NLS-1$
	
	protected Component rootMenuItem;
	protected JMenuItem showResultsMenu;

	protected final DataSetManager dataSetManager;
	protected final SessionManager selectionManager;
	protected final Metadata metadata;
	protected final UiUtils uiUtils;
	protected final FileUtils fileUtils;
	protected final CytoscapeUtils cytoscapeUtils;
	protected final NetworkUtils networkUtils;
	protected final TaskDispatcher taskDispatcher;

	protected abstract void startUp();
	protected abstract void shutDown();
	
	public AbstractGeneMania(
			DataSetManager dataSetManager,
			CytoscapeUtils cytoscapeUtils,
			UiUtils uiUtils,
			FileUtils fileUtils,
			NetworkUtils networkUtils,
			TaskDispatcher taskDispatcher,
			SessionManager selectionManager
	) {
		this.dataSetManager = dataSetManager;
		this.cytoscapeUtils = cytoscapeUtils;
		this.uiUtils = uiUtils;
		this.fileUtils = fileUtils;
		this.networkUtils = networkUtils;
		this.taskDispatcher = taskDispatcher;
		this.selectionManager = selectionManager;
		
		metadata = new Metadata();
	}
	
	public static File getSettingsDirectory() {
		String path = System.getProperty(SETTINGS_PROPERTY);
		if (path == null) {
			FileSystemView view = FileSystemView.getFileSystemView();
			File defaultDirectory = view.getDefaultDirectory();
			path = defaultDirectory.getPath();
		}
		File settingsDirectory = new File(String.format("%s%s%s", path, File.separator, SETTINGS_DIRECTORY)); //$NON-NLS-1$
		if (!settingsDirectory.exists()) {
			settingsDirectory.mkdirs();
		}
		return settingsDirectory;
	}
	
	public String getVersion() {
		return SCHEMA_VERSION;
	}
	
	@Override
	public DataSetManager getDataSetManager() {
		return dataSetManager;
	}
	
	public void setDataSet(DataSet data, ProgressReporter progress) {
		if (data != null && !isCompatible(data.getVersion())) {
			if (WrappedOptionPane.showConfirmDialog(taskDispatcher.getTaskDialog(), Strings.incompatibleData_prompt, Strings.default_title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 40) == JOptionPane.NO_OPTION) {
				return;
			}
		}
		dataSetManager.setDataSet(data, progress);
	}
	
	private boolean isCompatible(Version version) {
		String minVersion = metadata.getConfigProperty(MIN_DATA_VERSION);
		if (minVersion == null) {
			// No minimum data version set -- must assume everything is
			// compatible
			return true;
		}
		return version.getBaseVersion().compareTo(minVersion) >= 0;
	}

	@Override
	public void handleCheck() {
		final boolean[] requiresConfiguration = new boolean[1];
		GeneManiaTask task = new GeneManiaTask(Strings.dataUpdateCheck_title) {
			@Override
			protected void runTask() throws Throwable {
				requiresConfiguration[0] = checkForUpdates(progress);
			}
		};
		taskDispatcher.executeTask(task, cytoscapeUtils.getFrame(), true, true);
		handleConfiguration(requiresConfiguration[0]);
	}

	@Override
	public void handleSwitch() {
		try {
			chooseDataSet(cytoscapeUtils.getFrame());
		} catch (ApplicationException e) {
			LogUtils.log(getClass(), e);
		}
	}

	@Override
	public void handleDownload() {
		final boolean[] requiresConfiguration = new boolean[1];
		GeneManiaTask task = new GeneManiaTask(Strings.dataUpdateDownload_title) {
			@Override
			protected void runTask() throws Throwable {
				requiresConfiguration[0] = showDownloadDialog(progress);
			}
		};
		taskDispatcher.executeTask(task, cytoscapeUtils.getFrame(), true, true);
		handleConfiguration(requiresConfiguration[0]);
	}

	private void handleConfiguration(boolean requiresConfiguration) {
		DataSet data = dataSetManager.getDataSet();
		if (data == null || !requiresConfiguration) {
			return;
		}
		IConfiguration configuration = data.getConfiguration();
		if (configuration.hasUi()) {
			configuration.showUi(cytoscapeUtils.getFrame());
		}
	}
	
	private boolean showDownloadDialog(ProgressReporter progress) throws ApplicationException {
		final DownloadDialog dialog = new DownloadDialog(
				taskDispatcher.getTaskDialog(),
				Strings.downloadDialog_title,
				Strings.dataUpdateDownload_label,
				dataSetManager,
				uiUtils,
				fileUtils
		);
		dialog.setVisible(true);
		String dataId = dialog.getSelectedDataSetId();
		Action action = dialog.getAction();
		
		if (dataId != null && action == Action.download) {
	        File settings = getSettingsDirectory();
			downloadDataSet(dataId, settings, progress);
			return true;
		} else if (dataId != null && action == Action.select) {
			File path = findDataSetById(dataId);
			loadDataSet(path, progress, false, true);
		}
		
		return false;
	}

	protected File findDataSetById(String dataId) {
		Pattern pattern = Pattern.compile(".*?gmdata-(.*?)"); //$NON-NLS-1$

		List<File> paths = dataSetManager.getDataSetPaths();
		for (File file : paths) {
			Matcher matcher = pattern.matcher(file.getName());
			if (matcher.matches() && matcher.group(1).equals(dataId)) {
				return file;
			}
		}
		return null;
	}

	protected void validateMenu() {
		SessionManager manager = getSessionManager();
		showResultsMenu.setEnabled(manager.isGeneManiaNetwork(cytoscapeUtils.getCurrentNetwork()));
	}

	public boolean checkForUpdates(ProgressReporter progress) throws ApplicationException {
		try {
			List<String> dataSets = fileUtils.getCompatibleDataSets(FileUtils.DEFAULT_BASE_URL, SCHEMA_VERSION);
			if (dataSets.size() == 0) {
				throw new ApplicationException(Strings.checkForUpdates_error);
			}
			String url = dataSets.get(0);
			String[] parts = url.split("/"); //$NON-NLS-1$
			String dataId = parts[parts.length - 1];
			
			Pattern pattern = Pattern.compile("gmdata-(.*)"); //$NON-NLS-1$
			Matcher matcher = pattern.matcher(dataId);
			if (!matcher.matches()) {
				// This shouldn't happen...
				throw new ApplicationException(Strings.checkForUpdates_error);
			}
			Version version = Version.parse(matcher.group(1));
			
			DataSet dataSet = dataSetManager.getDataSet();
			if (dataSet == null) {
				ChildProgressReporter childProgress = new ChildProgressReporter(progress);
				initializeData(cytoscapeUtils.getFrame(), childProgress, false);
				childProgress.close();
			}
			
			// Check if the latest version has been downloaded but not active
			File latestDataSet = null;
			for (File path : dataSetManager.getDataSetPaths()) {
				matcher = pattern.matcher(path.getName());
				if (!matcher.matches()) {
					continue;
				}
				Version otherVersion = Version.parse(matcher.group(1));
				if (version.isEquivalentTo(otherVersion)) {
					latestDataSet = path;
					break;
				}
			}
			
			if (latestDataSet != null || dataSet != null && dataSet.getVersion().isEquivalentTo(version)) {
				JOptionPane.showMessageDialog(taskDispatcher.getTaskDialog(), Strings.checkForUpdatesOk_label, Strings.checkForUpdates_title, JOptionPane.INFORMATION_MESSAGE);
				if (dataSet == null) {
					loadDataSet(latestDataSet, progress, false, false);
				}
				return false;
			}
			
			// A new version is available
			return showDownloadDialog(progress);
		} catch (IOException e) {
			throw new ApplicationException(String.format(Strings.checkForUpdates_error2, e.getMessage()), e);
		}
	}
	
	public void downloadDataSet(String dataId, final File path, ProgressReporter progress) throws ApplicationException {
		try {
			String baseUrl;
			
			if (dataId == null) {
				List<String> dataSets = fileUtils.getCompatibleDataSets(FileUtils.DEFAULT_BASE_URL, SCHEMA_VERSION);
				
				if (dataSets.size() == 0)
					throw new ApplicationException(Strings.checkForUpdates_error);
				
				baseUrl = dataSets.get(0);
			} else {
				baseUrl = fileUtils.findDataSetBaseUrl(FileUtils.DEFAULT_BASE_URL, dataId);
			}
			
			URL url = new URL(String.format("%s.zip", baseUrl)); //$NON-NLS-1$
			File dataZipFile = fileUtils.download(url, path, progress);
			
			if (dataZipFile == null)
				return;
			
			File dataPath = unzipDataSet(dataZipFile, progress);
			dataZipFile.delete();
			loadDataSet(dataPath, progress, false, true);
		} catch (IOException e) {
			throw new ApplicationException(String.format(Strings.downloadData_error, e.getMessage()), e);
		}
	}
	
	private File unzipDataSet(final File path, ProgressReporter progress) throws IOException {
		fileUtils.unzip(path, path.getParentFile(), progress);
		String fileName = path.getName();
		int index = fileName.lastIndexOf("."); //$NON-NLS-1$
		
		if (index > 0)
			fileName = fileName.substring(0, index);
		
		return new File(String.format("%s%s%s", path.getParent(), File.separator, fileName)); //$NON-NLS-1$
	}
	
	public void chooseDataSet(Container parent) throws ApplicationException {
    	HashSet<String> extensions = new HashSet<>();
    	extensions.add("xml"); //$NON-NLS-1$
    	
		File dataSourcePath = dataSetManager.getDataSourcePath();
    	File initialFile = (dataSourcePath != null && dataSourcePath.exists()) ? dataSourcePath : fileUtils.getUserHome();
		File path = uiUtils.getFile(parent, Strings.changeData_title, initialFile, DataSetManager.DATA_FILE_NAME, extensions, FileSelectionMode.OPEN_FILE);
		
		if (path == null)
			return;

		if (DataSetManager.DATA_FILE_NAME.equals(path.getName()))
			path = path.getParentFile();
		
		final File dataPath = path;
		GeneManiaTask task = new GeneManiaTask(Strings.loadData_title) {
			@Override
			protected void runTask() throws Throwable {
				loadDataSet(dataPath, progress, false, true);
			}
		};
		taskDispatcher.executeTask(task, uiUtils.getWindow(parent), true, true);
		LogUtils.log(getClass(), task.getLastError());
	}
	
	@Override
	public void loadDataSet(File path, ProgressReporter progress, boolean promptToUpdate, boolean reportErrors) throws ApplicationException {
		try {
			if (path == null) {
				setDataSet(null, progress);
			} else {
				DataSet loadedData = dataSetManager.open(path);
				
				if (!isCompatible(loadedData.getVersion()) && promptToUpdate) {
					try {
						if (WrappedOptionPane.showConfirmDialog(taskDispatcher.getTaskDialog(), Strings.incompatibleData2_prompt, Strings.default_title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, WrappedOptionPane.DEFAULT_WIDTH) == JOptionPane.YES_OPTION) {
					        File settings = getSettingsDirectory();
							downloadDataSet(null, settings, progress);
						}
					} catch (Exception e) {
						setDataSet(null, progress);
					}
				} else {
					setDataSet(loadedData, progress);
				}
			}
		} catch (SAXException e) {
			if (reportErrors)
				WrappedOptionPane.showConfirmDialog(taskDispatcher.getTaskDialog(), String.format(Strings.loadData_error, path.getPath()), Strings.loadData_title, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, WrappedOptionPane.DEFAULT_WIDTH);
		}
	}

	@Override
	public SessionManager getSessionManager() {
		return selectionManager;
	}

	@Override
	public boolean initializeData(ProgressReporter progress, boolean reportErrors) throws ApplicationException {
		File path = dataSetManager.getDataSourcePath();
		
		if (path == null || !path.exists()) {
			List<File> paths = dataSetManager.getDataSetPaths();
			
			if (paths.size() == 0) {
				final DownloadDialog dialog = new DownloadDialog(
						taskDispatcher.getTaskDialog(),
						Strings.downloadDialog_title,
						Strings.missingData_prompt,
						dataSetManager,
						uiUtils,
						fileUtils
				);
				dialog.setVisible(true);
				String dataId = dialog.getSelectedDataSetId();
				
				if (dataId != null) {
			        File settings = getSettingsDirectory();
					downloadDataSet(dataId, settings, progress);
					
					return true;
				} else {
					loadDataSet(null, progress, false, false);
				}
			} else {
				loadDataSet(paths.get(0), progress, true, reportErrors);
			}
		} else {
			loadDataSet(path, progress, true, reportErrors);
		}
		
		return false;
	}

	@Override
	public void initializeData(Window parent, final boolean reportErrors) {
		final boolean[] requiresConfiguration = new boolean[1];
		GeneManiaTask task = new GeneManiaTask(Strings.loadData_title) {
			@Override
			protected void runTask() throws Throwable {
				requiresConfiguration[0] = initializeData(progress, reportErrors);
			}
		};
		taskDispatcher.executeTask(task, uiUtils.getWindow(parent), true, true);
		LogUtils.log(getClass(), task.getLastError());
		
		if (requiresConfiguration[0]) {
			DataSet data = dataSetManager.getDataSet();
			
			if (data == null)
				return;
			
			IConfiguration config = data.getConfiguration();
			
			if (config.hasUi())
				config.showUi(parent);
		}
	}
	
	public void initializeData(Window parent, ProgressReporter progress, boolean reportErrors) throws ApplicationException {
		boolean requiresConfiguration = initializeData(progress, reportErrors);
		if (requiresConfiguration) {
			DataSet data = dataSetManager.getDataSet();
			
			if (data == null)
				return;
			
			IConfiguration config = data.getConfiguration();
			
			if (config.hasUi())
				config.showUi(parent);
		}
	}
}
