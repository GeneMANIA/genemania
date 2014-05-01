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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.genemania.domain.Organism;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.OrganismMediator;
import org.genemania.plugin.FileUtils;
import org.genemania.plugin.LogUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.completion.DynamicTableModel;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetChangeListener;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.data.Namespace;
import org.genemania.plugin.data.lucene.LuceneDataSet;
import org.genemania.plugin.data.lucene.controllers.ImportOrganismController;
import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.plugin.validation.OrganismValidator;
import org.genemania.plugin.view.util.FileSelectionMode;
import org.genemania.plugin.view.util.UiUtils;
import org.genemania.util.ProgressReporter;

@SuppressWarnings("serial")
public class ImportOrganismPanel extends JPanel {
	private final DataSetManager dataSetManager;
	private final FileUtils fileUtils;
	private final UiUtils uiUtils;
	private final ImportOrganismController controller;
	private final OrganismValidator validator;

	private JTextField fileField;
	private JTextField nameField;
	private JTextField aliasField;
	private JTextField taxIdField;
	private JTextArea descriptionField;
	private JButton importButton;
	private DataSetChangeListener listener;

	private JTable installedTable;
	private JButton editButton;
	private JButton deleteButton;
	private DynamicTableModel<Organism> installedModel;

	public ImportOrganismPanel(DataSetManager dataSetManager, FileUtils fileUtils, UiUtils uiUtils, TaskDispatcher taskDispatcher) {
		this.dataSetManager = dataSetManager;
		this.fileUtils = fileUtils;
		this.uiUtils = uiUtils;
		
		controller = new ImportOrganismController(dataSetManager, taskDispatcher);
		validator = new OrganismValidator();
		
		DataSet data = dataSetManager.getDataSet();
		listener = new DataSetChangeListener() {
			@Override
			public void dataSetChanged(DataSet activeDataSet, ProgressReporter progress) {
				handleDataSetChange(activeDataSet);
			}
		};
		dataSetManager.addDataSetChangeListener(listener);
		setOpaque(false);
		
		setLayout(new GridBagLayout());
		JPanel importPanel = createImportPanel();
		JPanel installedPanel = createInstalledPanel();
		
		Insets insets = new Insets(0, 0, 0, 0);
		add(importPanel, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
		add(installedPanel, new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
		handleDataSetChange(data);
		validateSettings();
	}

	private void handleDataSetChange(DataSet data) {
		installedModel.clear();
		OrganismMediator mediator = data.getMediatorProvider().getOrganismMediator();
		try {
			List<Organism> organisms = mediator.getAllOrganisms();
			for (Organism organism : organisms) {
				if (organism.getId() >= 0) {
					continue;
				}
				
				installedModel.add(organism);
			}
			uiUtils.packColumns(installedTable);
			validator.setOrganisms(organisms);
		} catch (DataStoreException e) {
			LogUtils.log(getClass(), e);
		}
	}

	@SuppressWarnings("rawtypes")
	private JPanel createInstalledPanel() {
		JPanel panel = uiUtils.createJPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder(Strings.installedOrganismList_title));
		
		final LuceneDataSet data = (LuceneDataSet) dataSetManager.getDataSet();;
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

		panel.add(scrollPane, new GridBagConstraints(0, 0, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0 ,0));
		
		deleteButton = new JButton(Strings.deleteNetworkButton_label);
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				deleteOrganisms(data);
			}
		});
		deleteButton.setEnabled(false);
		
		editButton = new JButton(Strings.editNetworkButton_label);
		editButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				editOrganisms(data);
			}
		});
		editButton.setEnabled(false);
		
		panel.add(deleteButton, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0 ,0));
		panel.add(editButton, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0 ,0));

		return panel;
	}

	private JPanel createImportPanel() {
		JPanel panel = uiUtils.createJPanel();
		panel.setBorder(BorderFactory.createTitledBorder(Strings.importOrganism_title));
		panel.setLayout(new GridBagLayout());
		
		Insets insets = new Insets(0, 0, 0, 0);
		int row = 0;
		int rowWidth = 3;

		JLabel header = new JLabel(Strings.importOrganismHelp_label);
		panel.add(header, new GridBagConstraints(0, row, rowWidth, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, insets , 0, 0));
		row++;

		DocumentListener listener = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				validateSettings();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				validateSettings();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				validateSettings();
			}
		};
		
		fileField = new JTextField();
		fileField.getDocument().addDocumentListener(listener);
		fileField.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				validateSettings();
			}
			
			public void focusGained(FocusEvent e) {
			}
		});

		JButton browseButton = new JButton(Strings.importOrganismBrowseButton_label);
		browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleBrowse();
			}
		});
		
		panel.add(new JLabel(Strings.importOrganismFile_label), new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, insets, 0, 0));
		panel.add(fileField, new GridBagConstraints(1, row, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		panel.add(browseButton, new GridBagConstraints(2, row, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets, 0, 0));
		row++;
		
		nameField = new JTextField(30);
		nameField.getDocument().addDocumentListener(listener);
		
		panel.add(new JLabel(Strings.importOrganismName_label), new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, insets, 0, 0));
		panel.add(nameField, new GridBagConstraints(1, row, 2, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		row++;
		
		aliasField = new JTextField(30);
		aliasField.getDocument().addDocumentListener(listener);
		
		panel.add(new JLabel(Strings.importOrganismAlias_label), new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, insets, 0, 0));
		panel.add(aliasField, new GridBagConstraints(1, row, 2, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		row++;

		taxIdField = new JTextField(30);
		taxIdField.getDocument().addDocumentListener(listener);
		
		panel.add(new JLabel(Strings.importOrganismTaxonomyId_label), new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, insets, 0, 0));
		panel.add(taxIdField, new GridBagConstraints(1, row, 2, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		row++;

		descriptionField = new JTextArea();
		descriptionField.getDocument().addDocumentListener(listener);
		
		panel.add(new JLabel(Strings.importOrganismDescription_label), new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
		panel.add(new JScrollPane(descriptionField), new GridBagConstraints(1, row, 2, 1, 1, 1, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, insets, 0, 0));
		row++;

		importButton = new JButton(Strings.importOrganismImportButton_label);
		importButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleImport();
			}
		});
		
		panel.add(importButton, new GridBagConstraints(0, row, rowWidth, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		row++;

		return panel;
	}

	private void handleBrowse() {
		HashSet<String> extensions = new HashSet<String>();
		extensions.add("csv"); //$NON-NLS-1$
		extensions.add("txt"); //$NON-NLS-1$
		File initialFile = fileUtils.getUserHome();
		File file;
		try {
			file = uiUtils.getFile(uiUtils.getFrame(this), Strings.importNetworkFile_title, initialFile, Strings.importNetworkPanelTypeDescription_label, extensions, FileSelectionMode.OPEN_FILE);
		} catch (ApplicationException e) {
			LogUtils.log(getClass(), e);
			return;
		}
		if (file == null) {
			return;
		}
		fileField.setText(file.getAbsolutePath());
		validateSettings();
	}

	private void handleImport() {
		DataSet data = dataSetManager.getDataSet();
		Organism organism = new Organism();
		try {
			organism.setId(data.getNextAvailableId(organism.getClass(), Namespace.USER));
			organism.setName(nameField.getText());
			organism.setAlias(aliasField.getText());
			organism.setTaxonomyId(getTaxonomyId());
			organism.setDescription(descriptionField.getText());
			
			Reader reader = new FileReader(fileField.getText());
			controller.importOrganism(uiUtils.getFrame(this), data, reader, organism);
			resetFields();
		} catch (ApplicationException e) {
			LogUtils.log(getClass(), e);
		} catch (IOException e) {
			LogUtils.log(getClass(), e);
		}
	}

	private void resetFields() {
		fileField.setText(""); //$NON-NLS-1$
		nameField.setText(""); //$NON-NLS-1$
		aliasField.setText(""); //$NON-NLS-1$
		taxIdField.setText(""); //$NON-NLS-1$
		descriptionField.setText(""); //$NON-NLS-1$
	}

	private long getTaxonomyId() {
		String text = taxIdField.getText();
		if (text == null || text.isEmpty()) {
			return -1;
		}
		try {
			return Long.parseLong(text);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	@SuppressWarnings("rawtypes")
	private void deleteOrganisms(LuceneDataSet data) {
		int[] selection = installedTable.getSelectedRows();
		controller.deleteOrganisms(uiUtils.getFrame(this), data, installedModel, selection);
	}
	
	private void editOrganisms(DataSet data) {
		int[] selection = installedTable.getSelectedRows();
		for (int index : selection) {
			Organism organism = installedModel.get(index);
			EditOrganismDialog dialog;
			try {
				validator.setCurrentOrganism(organism);
				dialog = new EditOrganismDialog(uiUtils.getFrame(this), true, uiUtils, validator);
				dialog.setLocationByPlatform(true);
				dialog.setOrganismName(organism.getName());
				dialog.setAlias(organism.getAlias());
				dialog.setTaxonomyId(organism.getTaxonomyId());
				dialog.setDescription(organism.getDescription());
				dialog.validateSettings();
				dialog.pack();
				dialog.setVisible(true);
			} finally {
				validator.setCurrentOrganism(null);
			}
			
			if (dialog.isCanceled()) {
				return;
			}
			
			organism.setName(dialog.getOrganismName());
			organism.setAlias(dialog.getAlias());
			organism.setTaxonomyId(dialog.getTaxonomyId());
			organism.setDescription(dialog.getDescription());
			controller.updateOrganism(uiUtils.getFrame(this), data, organism);
		}
	}

	private void validateSettings() {
		boolean isValid = true;
		
		isValid &= isValidPath(fileField.getText());
		isValid &= validator.isValidName(nameField.getText());
		isValid &= validator.isValidAlias(aliasField.getText());
		isValid &= validator.isValidTaxonomyId(taxIdField.getText());
		importButton.setEnabled(isValid);
	}

	private boolean isValidPath(String path) {
		if (path == null || path.isEmpty()) {
			return false;
		}
		return new File(path).isFile();
	}

	public void close() {
		dataSetManager.removeDataSetChangeListener(listener);
	}
}
