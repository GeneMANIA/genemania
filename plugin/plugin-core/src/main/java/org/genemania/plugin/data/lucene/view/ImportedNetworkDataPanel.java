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
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
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

public class ImportedNetworkDataPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private final ImportedDataController controller;
	private final UiUtils uiUtils;
	private final DataSetManager dataSetManager;
	private final DataSetChangeListener listener;
	
	private DynamicTableModel<UserNetworkEntry> installedModel;
	private JTable installedTable;
	private JButton deleteButton;

	private JButton editButton;
	private ImportNetworkPanel importPanel;
	private JPanel importTabPanel;
	private JLabel helpLabel;
	private JPanel installedPanel;

	@SuppressWarnings("serial")
	public ImportedNetworkDataPanel(DataSetManager dataSetManager, final UiUtils uiUtils, FileUtils fileUtils, TaskDispatcher taskDispatcher) {
		this.uiUtils = uiUtils;
		this.dataSetManager = dataSetManager;
		this.controller = new ImportedDataController(dataSetManager, taskDispatcher);
		
		setLayout(new GridBagLayout());
		setOpaque(false);
		
		installedPanel = uiUtils.createJPanel();
		installedPanel.setLayout(new GridBagLayout());
		installedPanel.setBorder(uiUtils.createTitledBorder(Strings.installedNetworkList_title));
		
		final DataSet data = dataSetManager.getDataSet();
		installedModel = controller.createModel(data);
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
				editButton.setEnabled(totalSelected == 1);
				deleteButton.setEnabled(totalSelected > 0);
			}
		});
		
		JScrollPane scrollPane = new JScrollPane(installedTable);
		scrollPane.setBorder(BorderFactory.createEtchedBorder());
		
		installedPanel.add(scrollPane, new GridBagConstraints(0, 0, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0 ,0));
		
		deleteButton = new JButton(Strings.deleteNetworkButton_label);
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				deleteNetworks(data);
			}
		});
		deleteButton.setEnabled(false);
		
		editButton = new JButton(Strings.editNetworkButton_label);
		editButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				editNetworks(data);
			}
		});
		editButton.setEnabled(false);
		
		installedPanel.add(deleteButton, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0 ,0));
		installedPanel.add(editButton, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0 ,0));
		
		importTabPanel = uiUtils.createJPanel();
		importTabPanel.setLayout(new GridBagLayout());
		importTabPanel.setBorder(uiUtils.createTitledBorder(Strings.importNetworkTab_label));
		
		importPanel = new ImportNetworkPanel(dataSetManager, uiUtils, fileUtils, taskDispatcher);
		updateOrganisms(data);
		
		helpLabel = new JLabel(Strings.luceneConfigUserDefinedTab_label);
		helpLabel.setFont(helpLabel.getFont().deriveFont(UiUtils.INFO_FONT_SIZE));
		helpLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 20, 0));
		
		listener = new DataSetChangeListener() {
			public void dataSetChanged(DataSet activeDataSet, ProgressReporter progress) {
				handleDataSetChange(activeDataSet);
			}
		};
		dataSetManager.addDataSetChangeListener(listener);
		
		int row = 0;
		importTabPanel.add(importPanel, new GridBagConstraints(0, row, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		row++;
		
		final JButton importButton = new JButton(Strings.importNetworkButton_label);
		importButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				importNetwork(data);
			}
		});
		importTabPanel.add(importButton, new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		row++;
		
		importPanel.addValidationEventListener(new ValidationEventListener() {
			public void validate(boolean isValid) {
				importButton.setEnabled(isValid);
			}
		});
		importPanel.validateSettings();
		
		row = 0;
		add(helpLabel, new GridBagConstraints(0, row, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 5, 5), 0, 0));
		row++;
		add(importTabPanel, new GridBagConstraints(0, row, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		row++;
		add(installedPanel, new GridBagConstraints(0, row, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		row++;
		add(uiUtils.createJPanel(), new GridBagConstraints(0, row, 1, 1, 0, Double.MIN_VALUE, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0) , 0, 0));
		row++;
		handleDataSetChange(data);
	}

	private void handleDataSetChange(DataSet activeDataSet) {
		updateOrganisms(activeDataSet);
		if (activeDataSet != null) {
			boolean hasData = controller.hasData(activeDataSet);
			importTabPanel.setVisible(hasData);
			installedPanel.setVisible(hasData);
			if (hasData) {
				helpLabel.setText(Strings.luceneConfigUserDefinedTab_label);
			} else {
				helpLabel.setText(Strings.luceneConfigUserDefinedTab_label2);
			}
		}
		installedModel = controller.createModel(activeDataSet);
		installedTable.setModel(installedModel);
		uiUtils.packColumns(installedTable);
	}

	private void deleteNetworks(DataSet data) {
		int[] selection = installedTable.getSelectedRows();
		controller.deleteNetworks(uiUtils.getFrame(this), data, installedModel, selection);
//		installedModel.removeRows(selection);
	}

	private void editNetworks(DataSet data) {
		int[] selection = installedTable.getSelectedRows();
		for (int index : selection) {
			UserNetworkEntry entry = installedModel.get(index);
			InteractionNetwork network = entry.network;
			EditNetworkDialog dialog = new EditNetworkDialog(uiUtils.getFrame(this), true, uiUtils);
			dialog.setLocationByPlatform(true);
			dialog.setOrganism(entry.organism);
			dialog.setGroup(entry.group);
			dialog.setNetworkName(network.getName());
			dialog.setDescription(network.getDescription());
			dialog.validateSettings();
			dialog.pack();
			dialog.setVisible(true);
			
			if (dialog.isCanceled()) {
				return;
			}
			
			network.setName(dialog.getNetworkName());
			network.setDescription(dialog.getDescription());
			InteractionNetworkGroup group = dialog.getGroup();
			if (group.getId() == -1) {
				String name = dialog.getGroupName();
				group.setName(name);
				group.setCode(name);
				group.setDescription(""); //$NON-NLS-1$
			}
			controller.updateNetwork(uiUtils.getFrame(this), data, network, group, dialog.getColor());
		}
	}

	private void importNetwork(final DataSet data) {
		File file = importPanel.getNetworkFile();
		if (file == null || !file.exists()) {
			return;
		}
		String networkFile = file.getAbsolutePath();
		DataImportSettings settings = importPanel.getImportSettings();
		settings.setSource(file.getName());
		DataFileType type = importPanel.getType();
		
		controller.importNetwork(uiUtils.getFrame(this), data, settings, networkFile, type);
		uiUtils.packColumns(installedTable);
		importPanel.clear();
	}

	public void close() {
		dataSetManager.removeDataSetChangeListener(listener);
	}
	
	private void updateOrganisms(DataSet data) {
		List<Organism> organisms = controller.getOrganisms(data);
		importPanel.setOrganisms(organisms);
	}

	public DataSet getDataSet() {
		return dataSetManager.getDataSet();
	}
}
