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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.genemania.data.normalizer.DataFileType;
import org.genemania.data.normalizer.DataImportSettings;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.plugin.FileUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.completion.DynamicTableModel;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetChangeListener;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.data.lucene.controllers.ImportedDataController;
import org.genemania.plugin.data.lucene.models.UserNetworkEntry;
import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.plugin.validation.ValidationEventListener;
import org.genemania.plugin.view.util.UiUtils;
import org.genemania.util.ProgressReporter;

@SuppressWarnings("serial")
public class ImportedNetworkDataPanel extends JPanel {
	
	private final ImportedDataController controller;
	private final UiUtils uiUtils;
	private final FileUtils fileUtils;
	private final TaskDispatcher taskDispatcher;
	private final DataSetManager dataSetManager;
	private final DataSetChangeListener listener;
	
	private DynamicTableModel<UserNetworkEntry> installedModel;
	
	private JLabel helpLabel = new JLabel(Strings.luceneConfigUserDefinedTab_label);
	private JPanel importTabPanel;
	private ImportNetworkPanel importPanel;
	private JPanel installedPanel;
	private JTable installedTable;
	private JButton importButton;
	private JButton deleteButton;
	private JButton editButton;

	public ImportedNetworkDataPanel(
			final DataSetManager dataSetManager,
			final UiUtils uiUtils,
			final FileUtils fileUtils,
			final TaskDispatcher taskDispatcher
	) {
		this.dataSetManager = dataSetManager;
		this.uiUtils = uiUtils;
		this.fileUtils = fileUtils;
		this.taskDispatcher = taskDispatcher;
		this.controller = new ImportedDataController(dataSetManager, taskDispatcher);
		
		if (uiUtils.isAquaLAF())
			setOpaque(false);
		
		installedModel = controller.createModel(getDataSet());
		updateOrganisms(getDataSet());
		
		listener = new DataSetChangeListener() {
			@Override
			public void dataSetChanged(DataSet activeDataSet, ProgressReporter progress) {
				handleDataSetChange(activeDataSet);
			}
		};
		dataSetManager.addDataSetChangeListener(listener);
		
		addComponents();
		handleDataSetChange(getDataSet());
	}
	
	private void addComponents() {
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateGaps(uiUtils.isWinLAF());
		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(getImportTabPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getInstalledPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getImportTabPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getInstalledPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
	}

	private JPanel getImportTabPanel() {
		if (importTabPanel == null) {
			importTabPanel = uiUtils.createJPanel();
			importTabPanel.setBorder(uiUtils.createTitledBorder(Strings.importNetworkTab_label));
			
			helpLabel.setFont(helpLabel.getFont().deriveFont(UiUtils.INFO_FONT_SIZE));
			helpLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			
			final GroupLayout layout = new GroupLayout(importTabPanel);
			importTabPanel.setLayout(layout);
			layout.setAutoCreateGaps(uiUtils.isWinLAF());
			layout.setAutoCreateContainerGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
					.addComponent(helpLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getImportPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getImportButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(helpLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getImportPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getImportButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return importTabPanel;
	}
	
	private JPanel getInstalledPanel() {
		if (installedPanel == null) {
			installedPanel = uiUtils.createJPanel();
			installedPanel.setBorder(uiUtils.createTitledBorder(Strings.installedNetworkList_title));
			
			final JScrollPane scrollPane = new JScrollPane(getInstalledTable());
			scrollPane.setPreferredSize(uiUtils.computeTextSizeHint(getFontMetrics(getFont()), 10, 6));
			
			final GroupLayout layout = new GroupLayout(installedPanel);
			installedPanel.setLayout(layout);
			layout.setAutoCreateGaps(uiUtils.isWinLAF());
			layout.setAutoCreateContainerGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(getDeleteButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getEditButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createSequentialGroup()
							.addComponent(getDeleteButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getEditButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
			
			uiUtils.equalizeSize(getDeleteButton(), getEditButton());
		}
		
		return installedPanel;
	}
	
	private ImportNetworkPanel getImportPanel() {
		if (importPanel == null) {
			importPanel = new ImportNetworkPanel(dataSetManager, uiUtils, fileUtils, taskDispatcher);
			
			importPanel.addValidationEventListener(new ValidationEventListener() {
				@Override
				public void validate(boolean isValid) {
					getImportButton().setEnabled(isValid);
				}
			});
			importPanel.validateSettings();
		}
		
		return importPanel;
	}
	
	private JTable getInstalledTable() {
		if (installedTable == null) {
			installedTable = new JTable(installedModel) {
				@Override
				public void addNotify() {
					super.addNotify();
					uiUtils.packColumns(installedTable);
				}
			};
			installedTable.setOpaque(false);
			installedTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					int totalSelected = installedTable.getSelectedRowCount();
					getEditButton().setEnabled(totalSelected == 1);
					getDeleteButton().setEnabled(totalSelected > 0);
				}
			});
		}
		
		return installedTable;
	}
	
	private JButton getDeleteButton() {
		if (deleteButton == null) {
			deleteButton = new JButton(Strings.deleteNetworkButton_label);
			deleteButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					deleteNetworks(getDataSet());
				}
			});
			deleteButton.setEnabled(false);
		}
		
		return deleteButton;
	}
	
	private JButton getEditButton() {
		if (editButton == null) {
			editButton = new JButton(Strings.editNetworkButton_label);
			editButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					editNetworks(getDataSet());
				}
			});
			editButton.setEnabled(false);
		}

		return editButton;
	}
	
	private JButton getImportButton() {
		if (importButton == null) {
			importButton = new JButton(Strings.importNetworkButton_label);
			importButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					importNetwork(getDataSet());
				}
			});
		}
		
		return importButton;
	}

	private void handleDataSetChange(DataSet activeDataSet) {
		updateOrganisms(activeDataSet);
		
		if (activeDataSet != null) {
			boolean hasData = controller.hasData(activeDataSet);
			getImportTabPanel().setVisible(hasData);
			getInstalledPanel().setVisible(hasData);
			
			if (hasData) {
				helpLabel.setText(Strings.luceneConfigUserDefinedTab_label);
			} else {
				helpLabel.setText(Strings.luceneConfigUserDefinedTab_label2);
			}
		}
		
		installedModel = controller.createModel(activeDataSet);
		getInstalledTable().setModel(installedModel);
		uiUtils.packColumns(getInstalledTable());
	}

	private void deleteNetworks(DataSet data) {
		int[] selection = getInstalledTable().getSelectedRows();
		controller.deleteNetworks(uiUtils.getFrame(this), data, installedModel, selection);
//		installedModel.removeRows(selection);
	}

	private void editNetworks(DataSet data) {
		int[] selection = getInstalledTable().getSelectedRows();
		
		for (int index : selection) {
			final UserNetworkEntry entry = installedModel.get(index);
			final InteractionNetwork network = entry.network;
			final EditNetworkDialog dialog = new EditNetworkDialog(uiUtils.getFrame(this), uiUtils);
			dialog.setLocationByPlatform(true);
			dialog.setOrganism(entry.organism);
			dialog.setGroup(entry.group);
			dialog.setNetworkName(network.getName());
			dialog.setDescription(network.getDescription());
			dialog.validateSettings();
			dialog.pack();
			dialog.setVisible(true);
			
			if (dialog.isCanceled())
				return;
			
			network.setName(dialog.getNetworkName());
			network.setDescription(dialog.getDescription());
			final InteractionNetworkGroup group = dialog.getGroup();
			
			if (group.getId() == -1) {
				final String name = dialog.getGroupName();
				group.setName(name);
				group.setCode(name);
				group.setDescription(""); //$NON-NLS-1$
			}
			
			controller.updateNetwork(uiUtils.getFrame(this), data, network, group, dialog.getColor());
		}
	}

	private void importNetwork(final DataSet data) {
		File file = getImportPanel().getNetworkFile();
		
		if (file == null || !file.exists())
			return;
		
		String networkFile = file.getAbsolutePath();
		DataImportSettings settings = getImportPanel().getImportSettings();
		settings.setSource(file.getName());
		DataFileType type = getImportPanel().getType();
		
		controller.importNetwork(uiUtils.getFrame(this), data, settings, networkFile, type);
		uiUtils.packColumns(getInstalledTable());
		getImportPanel().clear();
	}

	public void close() {
		dataSetManager.removeDataSetChangeListener(listener);
	}
	
	private void updateOrganisms(final DataSet data) {
		final List<Organism> organisms = controller.getOrganisms(data);
		getImportPanel().setOrganisms(organisms);
	}

	public DataSet getDataSet() {
		return dataSetManager.getDataSet();
	}
}
