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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
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

	private JPanel importPanel;
	private JPanel installedPanel;
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
		
		final DataSet data = dataSetManager.getDataSet();
		listener = new DataSetChangeListener() {
			@Override
			public void dataSetChanged(DataSet activeDataSet, ProgressReporter progress) {
				handleDataSetChange(activeDataSet);
			}
		};
		dataSetManager.addDataSetChangeListener(listener);
		
		if (uiUtils.isAquaLAF())
			setOpaque(false);
		
		installedModel = controller.createModel(dataSetManager.getDataSet());
		
		addComponents();
		handleDataSetChange(data);
		validateSettings();
	}

	private void addComponents() {
		final DocumentListener docListener = new DocumentListener() {
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
		
		fileField = new JTextField(20);
		fileField.getDocument().addDocumentListener(docListener);
		fileField.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				validateSettings();
			}
			@Override
			public void focusGained(FocusEvent e) {
			}
		});
		
		nameField = new JTextField(30);
		nameField.getDocument().addDocumentListener(docListener);
		
		aliasField = new JTextField(30);
		aliasField.getDocument().addDocumentListener(docListener);
		
		taxIdField = new JTextField(30);
		taxIdField.getDocument().addDocumentListener(docListener);
		
		descriptionField = new JTextArea();
		descriptionField.getDocument().addDocumentListener(docListener);
		
		importButton = new JButton(Strings.importOrganismImportButton_label);
		importButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleImport();
			}
		});
		
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateGaps(uiUtils.isWinLAF());
		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(getImportPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getInstalledPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getImportPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getInstalledPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
	}
	
	private JPanel getImportPanel() {
		if (importPanel == null) {
			importPanel = uiUtils.createJPanel();
			importPanel.setBorder(uiUtils.createTitledBorder(Strings.importOrganism_title));
			
			final JLabel helpLabel = new JLabel(Strings.importOrganismHelp_label);
			helpLabel.setFont(helpLabel.getFont().deriveFont(UiUtils.INFO_FONT_SIZE));
			helpLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			
			final JLabel label1 = new JLabel(Strings.importOrganismFile_label);
			final JLabel label2 = new JLabel(Strings.importOrganismName_label);
			final JLabel label3 = new JLabel(Strings.importOrganismAlias_label);
			final JLabel label4 = new JLabel(Strings.importOrganismTaxonomyId_label);
			final JLabel label5 = new JLabel(Strings.importOrganismDescription_label);
			
			final JButton browseButton = new JButton(Strings.importOrganismBrowseButton_label);
			browseButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					handleBrowse();
				}
			});
			
			final JScrollPane scrollPane = new JScrollPane(descriptionField);
			scrollPane.setPreferredSize(uiUtils.computeTextSizeHint(getFontMetrics(getFont()), 10, 4));
			
			final GroupLayout layout = new GroupLayout(importPanel);
			importPanel.setLayout(layout);
			layout.setAutoCreateGaps(false);
			layout.setAutoCreateContainerGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
					.addComponent(helpLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(Alignment.TRAILING, false)
								.addComponent(label1)
								.addComponent(label2)
								.addComponent(label3)
								.addComponent(label4)
								.addComponent(label5)
						)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
								.addGroup(layout.createSequentialGroup()
										.addComponent(fileField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(browseButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
								.addComponent(nameField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(aliasField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(taxIdField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						)
					)
					.addComponent(importButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(helpLabel)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(label1)
							.addComponent(fileField)
							.addComponent(browseButton)
					)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(label2)
							.addComponent(nameField)
					)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(label3)
							.addComponent(aliasField)
					)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(label4)
							.addComponent(taxIdField)
					)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
							.addComponent(label5, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(importButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}

		return importPanel;
	}
	
	private JPanel getInstalledPanel() {
		if (installedPanel == null) {
			installedPanel = uiUtils.createJPanel();
			installedPanel.setBorder(uiUtils.createTitledBorder(Strings.installedOrganismList_title));
			
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
				@Override
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
				@SuppressWarnings("rawtypes")
				@Override
				public void actionPerformed(ActionEvent event) {
					deleteOrganisms((LuceneDataSet) dataSetManager.getDataSet());
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
					editOrganisms(dataSetManager.getDataSet());
				}
			});
			editButton.setEnabled(false);
		}

		return editButton;
	}

	private void handleDataSetChange(DataSet data) {
		installedModel.clear();
		OrganismMediator mediator = data.getMediatorProvider().getOrganismMediator();
		
		try {
			List<Organism> organisms = mediator.getAllOrganisms();
			
			for (Organism organism : organisms) {
				if (organism.getId() >= 0)
					continue;
				
				installedModel.add(organism);
			}
			
			uiUtils.packColumns(getInstalledTable());
			validator.setOrganisms(organisms);
		} catch (DataStoreException e) {
			LogUtils.log(getClass(), e);
		}
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
		
		if (file == null)
			return;
		
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
		int[] selection = getInstalledTable().getSelectedRows();
		controller.deleteOrganisms(uiUtils.getFrame(this), data, installedModel, selection);
	}
	
	private void editOrganisms(DataSet data) {
		int[] selection = getInstalledTable().getSelectedRows();
		
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
		if (path == null || path.isEmpty())
			return false;
		
		return new File(path).isFile();
	}

	public void close() {
		dataSetManager.removeDataSetChangeListener(listener);
	}
}
