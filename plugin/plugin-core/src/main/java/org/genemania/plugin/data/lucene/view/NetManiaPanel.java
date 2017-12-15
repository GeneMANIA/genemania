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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
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

@SuppressWarnings("serial")
public class NetManiaPanel extends JPanel {
	
	private JPanel availablePanel;
	private JPanel installedPanel;
	private DynamicListModel<DataDescriptor> installedModel;
	private DynamicListModel<DataDescriptor> availableModel;
	private JList installedList;
	private JList availableList;
	private JButton deleteButton;
	private JButton installButton;

	private final DataSetManager dataSetManager;
	private final UiUtils uiUtils;
	private final TaskDispatcher taskDispatcher;
	
	public NetManiaPanel(DataSetManager dataSetManager, UiUtils uiUtils, TaskDispatcher taskDispatcher) {
		this.dataSetManager = dataSetManager;
		this.uiUtils = uiUtils;
		this.taskDispatcher = taskDispatcher;
		
		if (uiUtils.isAquaLAF())
			setOpaque(false);
		
		addComponents();
		
		final LuceneDataSet data = (LuceneDataSet) dataSetManager.getDataSet();
		populate(data);
		validateSettings();
	}

	public void doDelete() {
		LuceneDataSet data = (LuceneDataSet) dataSetManager.getDataSet();
		int[] selection = getInstalledList().getSelectedIndices();
		
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
			@Override
			@SuppressWarnings("unchecked")
			protected void runTask() {
				int[] selection = getAvailableList().getSelectedIndices();
				ArrayList<Integer> installed = new ArrayList<Integer>();
				progress.setMaximumProgress(selection.length);
				LuceneDataSet data = (LuceneDataSet) dataSetManager.getDataSet();
				
				for (int index : selection) {
					if (progress.isCanceled())
						break;
					
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
				
				// Remove in reverse order so we don't have to worry about shifting indices.
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
	
	private void addComponents() {
		final JEditorPane descriptionPane =
				uiUtils.createLinkEnabledEditorPane(this, Strings.luceneConfigNetManiaTab_label);
		descriptionPane.setFont(descriptionPane.getFont().deriveFont(UiUtils.INFO_FONT_SIZE));
		descriptionPane.setMargin(new Insets(5, 0, 20, 0));
		
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateGaps(uiUtils.isWinLAF());
		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(descriptionPane, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getAvailablePanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getInstalledPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(descriptionPane, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getAvailablePanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getInstalledPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		
		uiUtils.equalizeSize(getInstallButton(), getDeleteButton());
	}
	
	private JPanel getAvailablePanel() {
		if (availablePanel == null) {
			availablePanel = uiUtils.createJPanel();
			availablePanel.setBorder(uiUtils.createTitledBorder(Strings.netmaniaAvailableDataList_title));
			
			final JScrollPane scrollPane = new JScrollPane(getAvailableList());
			scrollPane.setPreferredSize(uiUtils.computeTextSizeHint(getFontMetrics(getFont()), 10, 10));
			
			final GroupLayout layout = new GroupLayout(availablePanel);
			availablePanel.setLayout(layout);
			layout.setAutoCreateGaps(uiUtils.isWinLAF());
			layout.setAutoCreateContainerGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getInstallButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getInstallButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return availablePanel;
	}
	
	private JPanel getInstalledPanel() {
		if (installedPanel == null) {
			installedPanel = uiUtils.createJPanel();
			installedPanel.setBorder(uiUtils.createTitledBorder(Strings.netmaniaInstalledDataList_title));
			
			final JScrollPane scrollPane = new JScrollPane(getInstalledList());
			scrollPane.setPreferredSize(uiUtils.computeTextSizeHint(getFontMetrics(getFont()), 10, 10));
			
			final GroupLayout layout = new GroupLayout(installedPanel);
			installedPanel.setLayout(layout);
			layout.setAutoCreateGaps(uiUtils.isWinLAF());
			layout.setAutoCreateContainerGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getDeleteButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getDeleteButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return installedPanel;
	}
	
	private JList getAvailableList() {
		if (availableList == null) {
			availableList = new JList();
			availableList.setName(Strings.netmaniaAvailableDataList_title);
			availableList.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					validateSettings();
				}
			});
		}
		
		return availableList;
	}
	
	private JList getInstalledList() {
		if (installedList == null) {
			installedList = new JList();
			installedList.setName(Strings.netmaniaInstalledDataList_title);
			installedList.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					validateSettings();
				}
			});
		}
		
		return installedList;
	}
	
	private JButton getInstallButton() {
		if (installButton == null) {
			installButton = new JButton(Strings.installDataButton_label);
			installButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					doInstall();
				}
			});
		}
		
		return installButton;
	}
	
	private JButton getDeleteButton() {
		if (deleteButton == null) {
			deleteButton = new JButton(Strings.deleteDataButton_label);
			deleteButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					doDelete();
				}
			});
		}
		
		return deleteButton;
	}
	
	private void validateSettings() {
		getInstallButton().setEnabled(
				availableModel != null && availableModel.getSize() > 0 && getAvailableList().getSelectedIndex() != -1);
		getDeleteButton().setEnabled(
				installedModel != null && installedModel.getSize() > 0 && getInstalledList().getSelectedIndex() != -1);
	}
	
	@SuppressWarnings("unchecked")
	private DataDescriptor getInstalledDataDescriptor(String id) {
		LuceneDataSet data = (LuceneDataSet) dataSetManager.getDataSet();
		List<DataDescriptor> installed = data.getInstalledDataDescriptors();
		
		for (DataDescriptor descriptor : installed) {
			if (descriptor.getId().equals(id))
				return descriptor;
		}
		
		return null;
	}
	
	private void populate(LuceneDataSet data) {
		List<DataDescriptor> installed = data.getInstalledDataDescriptors();
		Collections.sort(installed);
		installedModel = new DynamicListModel<DataDescriptor>(installed);
		getInstalledList().setModel(installedModel);
		
		List<DataDescriptor> availableDescriptors = null;
		
		try {
			availableDescriptors = data.getAvailableDataDescriptors();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(
					null,
					e.getMessage(), 
					"GeneMANIA Error",
					JOptionPane.ERROR_MESSAGE
			);
			return;
		}
		
		List<DataDescriptor> result = new ArrayList<DataDescriptor>();
		
		for (DataDescriptor descriptor : availableDescriptors) {
			if (!installed.contains(descriptor))
				result.add(descriptor);
		}
		
		Collections.sort(result);
		
		availableModel = new DynamicListModel<DataDescriptor>(result);
		getAvailableList().setModel(availableModel);
	}
}
