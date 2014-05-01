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

package org.genemania.plugin.data.lucene.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.genemania.exception.ApplicationException;
import org.genemania.plugin.LogUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.completion.DynamicListModel;
import org.genemania.plugin.data.DataDescriptor;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.data.lucene.LuceneDataSet;
import org.genemania.plugin.task.GeneManiaTask;
import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.plugin.view.util.UiUtils;
import org.genemania.util.ChildProgressReporter;

public class NetManiaPanel<NETWORK, NODE, EDGE> extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private DynamicListModel<DataDescriptor> installedModel;
	private DynamicListModel<DataDescriptor> availableModel;
	private JList installedList;
	private JList availableList;
	private JButton deleteButton;
	private JButton installButton;

	private final DataSetManager dataSetManager;
	private final UiUtils uiUtils;
	private final TaskDispatcher taskDispatcher;
	
	@SuppressWarnings("unchecked")
	public NetManiaPanel(DataSetManager dataSetManager, UiUtils uiUtils, TaskDispatcher taskDispatcher) {
		this.dataSetManager = dataSetManager;
		this.uiUtils = uiUtils;
		this.taskDispatcher = taskDispatcher;
		
		setLayout(new GridBagLayout());
		setOpaque(false);
		
		ListSelectionListener listener = new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				validateSettings();
			}
		};
	
		JPanel installed = uiUtils.createJPanel();
		installed.setBorder(BorderFactory.createTitledBorder(Strings.netmaniaInstalledDataList_title));
		
		installed.setLayout(new GridBagLayout());
		installedList = new JList();
		installedList.setName(Strings.netmaniaInstalledDataList_title);
		installedList.setBorder(BorderFactory.createEtchedBorder());
		installedList.addListSelectionListener(listener);
		installed.add(new JScrollPane(installedList), new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		
		JPanel installedButtons = uiUtils.createJPanel();
		installedButtons.setLayout(new GridBagLayout());
		installed.add(installedButtons, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		
		deleteButton = new JButton(Strings.deleteDataButton_label);
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				doDelete();
			}
		});
		installedButtons.add(deleteButton, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0 ,0));
		
		JPanel available = uiUtils.createJPanel();
		available.setBorder(BorderFactory.createTitledBorder(Strings.netmaniaAvailableDataList_title));
		
		available.setLayout(new GridBagLayout());
		availableList = new JList();
		availableList.setName(Strings.netmaniaAvailableDataList_title);
		availableList.setBorder(BorderFactory.createEtchedBorder());
		availableList.addListSelectionListener(listener);
		available.add(new JScrollPane(availableList), new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		
		JPanel availableButtons = uiUtils.createJPanel();
		availableButtons.setLayout(new GridBagLayout());
		available.add(availableButtons, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		
		installButton = new JButton(Strings.installDataButton_label);
		installButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				doInstall();
			}
		});
		availableButtons.add(installButton, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		
		JEditorPane descriptionPane = uiUtils.createLinkEnabledEditorPane(this, Strings.luceneConfigNetManiaTab_label);
		add(descriptionPane, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 5, 5), 0, 0));
		add(available, new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		add(installed, new GridBagConstraints(0, 2, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		LuceneDataSet<NETWORK, NODE, EDGE> data = (LuceneDataSet<NETWORK, NODE, EDGE>) dataSetManager.getDataSet();
		populate(data);
		validateSettings();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void doDelete() {
		LuceneDataSet<NETWORK, NODE, EDGE> data = (LuceneDataSet) dataSetManager.getDataSet();
		int[] selection = installedList.getSelectedIndices();
		for (int i : selection) {
			DataDescriptor descriptor = (DataDescriptor) installedModel.getElementAt(i);

			try {
				data.deleteIndex(descriptor.getId());
			} catch (ApplicationException e) {
				data.log(e);
			}
		}
		validateSettings();
		GeneManiaTask task = new GeneManiaTask(Strings.deleteData_status) {
			@Override
			protected void runTask() throws Throwable {
				dataSetManager.reloadDataSet(progress);
			}
		};
		taskDispatcher.executeTask(task, uiUtils.getFrame(this), true, true);
		LogUtils.log(getClass(), task.getLastError());
		
		populate(data);
	}
	
	public void doInstall() {
		GeneManiaTask task = new GeneManiaTask(Strings.installData_status) {
			@SuppressWarnings("unchecked")
			@Override
			protected void runTask() {
				int[] selection = availableList.getSelectedIndices();
				ArrayList<Integer> installed = new ArrayList<Integer>();
				progress.setMaximumProgress(selection.length);
				LuceneDataSet<NETWORK, NODE, EDGE> data = (LuceneDataSet<NETWORK, NODE, EDGE>) dataSetManager.getDataSet();
				for (int index : selection) {
					if (progress.isCanceled()) {
						break;
					}
					DataDescriptor descriptor = (DataDescriptor) availableModel.getElementAt(index);
					String name = descriptor.getId();

					ChildProgressReporter childProgress = new ChildProgressReporter(progress);
					try {
						data.installIndex(name, descriptor.getDescription(), childProgress);
						if (childProgress.isCanceled()) {
							data.deleteIndex(name);
							break;
						}
						DataDescriptor installedDescriptor = getInstalledDataDescriptor(descriptor.getId());
						if (installedDescriptor != null) {
							installedModel.add(installedDescriptor);
						} else {
							installedModel.add(descriptor);
						}
						installed.add(index);
					} catch (ApplicationException e) {
						// Something bad happened -- clean up the mess.
						data.log(e);
						try {
							data.deleteIndex(name);
						} catch (ApplicationException e1) {
						}
					} finally {
						childProgress.close();
					}
				}
				// Remove in reverse order so we don't have to worry about
				// shifting indicies.
				Collections.sort(installed);
				for (int i = installed.size() - 1; i >= 0; i--) {
					availableModel.remove(installed.get(i));
				}
				installedModel.sort();
				validateSettings();
				dataSetManager.reloadDataSet(progress);
			}
		};
		taskDispatcher.executeTask(task, uiUtils.getFrame(this), true, true);
		LogUtils.log(getClass(), task.getLastError());
	}
	
	private void validateSettings() {
		installButton.setEnabled(availableModel.getSize() > 0 && availableList.getSelectedIndex() != -1);
		deleteButton.setEnabled(installedModel.getSize() > 0 && installedList.getSelectedIndex() != -1);
	}
	
	@SuppressWarnings("unchecked")
	private DataDescriptor getInstalledDataDescriptor(String id) {
		LuceneDataSet<NETWORK, NODE, EDGE> data = (LuceneDataSet<NETWORK, NODE, EDGE>) dataSetManager.getDataSet();
		List<DataDescriptor> installed = data.getInstalledDataDescriptors();
		for (DataDescriptor descriptor : installed) {
			if (descriptor.getId().equals(id)) {
				return descriptor;
			}
		}
		return null;
	}
	
	private void populate(LuceneDataSet<NETWORK, NODE, EDGE> data) {
		List<DataDescriptor> installed = data.getInstalledDataDescriptors();
		Collections.sort(installed);
		installedModel = new DynamicListModel<DataDescriptor>(installed);
		installedList.setModel(installedModel);
		
		List<DataDescriptor> availableDescriptors = data.getAvailableDataDescriptors();
		List<DataDescriptor> result = new ArrayList<DataDescriptor>();
		
		for (DataDescriptor descriptor : availableDescriptors) {
			if (!installed.contains(descriptor)) {
				result.add(descriptor);
			}
		}
		Collections.sort(result);
		
		availableModel = new DynamicListModel<DataDescriptor>(result);
		availableList.setModel(availableModel);
	}

}
