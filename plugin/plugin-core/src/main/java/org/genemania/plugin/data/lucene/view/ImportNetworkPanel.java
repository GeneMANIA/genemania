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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.genemania.data.normalizer.DataFileClassifier;
import org.genemania.data.normalizer.DataFileType;
import org.genemania.data.normalizer.DataImportSettings;
import org.genemania.data.normalizer.OrganismClassifier;
import org.genemania.data.normalizer.OrganismClassifier.Match;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.exception.ApplicationException;
import org.genemania.plugin.FileUtils;
import org.genemania.plugin.LogUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.task.GeneManiaTask;
import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.plugin.validation.ValidationEventListener;
import org.genemania.plugin.view.components.NetworkGroupComboBox;
import org.genemania.plugin.view.util.FileSelectionMode;
import org.genemania.plugin.view.util.UiUtils;
import org.genemania.type.DataLayout;

public class ImportNetworkPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private List<Organism> organisms;

	private NetworkGroupComboBox groupCombo;
	private JComboBox organismCombo;
	private JTextField nameField;
	private JTextArea descriptionField;

	private JTextField fileField;

	private JButton chooseButton;

	private OrganismClassifier detector;

	private File detectedFile;
	private ButtonGroup typeGroup;

	private JRadioButton interactionRadioButton;

	private JRadioButton profileRadioButton;

	private JTextField groupNameField;

	private final List<ValidationEventListener> validationListeners;
	private final DataImportSettings importSettings;
	private final DataSetManager dataSetManager;
	private final UiUtils uiUtils;
	private final FileUtils fileUtils;
	private final TaskDispatcher taskDispatcher;

	public ImportNetworkPanel(DataSetManager dataSetManager, final UiUtils uiUtils, final FileUtils fileUtils, TaskDispatcher taskDispatcher) {
		this.dataSetManager = dataSetManager;
		this.uiUtils = uiUtils;
		this.fileUtils = fileUtils;
		this.taskDispatcher = taskDispatcher;
		
		importSettings = new DataImportSettings();
		validationListeners = new ArrayList<ValidationEventListener>();
		
		setOpaque(false);
		
		setLayout(new GridBagLayout());
		
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				validateSettings();
			}
		};
		
		organismCombo = new JComboBox();
		organismCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateGroups();
				validateSettings();
			}
		});
		
		groupCombo = new NetworkGroupComboBox();
		groupCombo.addActionListener(actionListener);
		
		DocumentListener documentListener = new DocumentListener() {
			public void removeUpdate(DocumentEvent e) {
				validateSettings();
			}
			
			public void insertUpdate(DocumentEvent e) {
				validateSettings();
			}
			
			public void changedUpdate(DocumentEvent e) {
				validateSettings();
			}
		};
		
		FocusListener focusListener = new FocusListener() {
			public void focusLost(FocusEvent e) {
				validateSettings();
			}
			
			public void focusGained(FocusEvent e) {
			}
		};
		
		nameField = new JTextField(30);
		nameField.getDocument().addDocumentListener(documentListener);
		descriptionField = new JTextArea();
		descriptionField.getDocument().addDocumentListener(documentListener);
		
		fileField = new JTextField(30);
		fileField.getDocument().addDocumentListener(documentListener);
		fileField.addFocusListener(focusListener);
		
		chooseButton = new JButton(Strings.importNetworkBrowseButton_label);
		chooseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				handleChoose();
			}
		});

		JPanel typePanel = uiUtils.createJPanel();
		typePanel.setLayout(new GridBagLayout());
		interactionRadioButton = uiUtils.createRadioButton(Strings.importNetworkNetworkRadioButton_label);
		interactionRadioButton.addActionListener(actionListener);
		profileRadioButton = uiUtils.createRadioButton(Strings.importNetworkProfileRadioButton_label);
		profileRadioButton.addActionListener(actionListener);
		
		typeGroup = new ButtonGroup();
		typeGroup.add(interactionRadioButton);
		typeGroup.add(profileRadioButton);
		
		typePanel.add(interactionRadioButton, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		typePanel.add(profileRadioButton, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		
		JPanel groupPanel = uiUtils.createJPanel();
		groupPanel.setLayout(new GridBagLayout());
		
		groupNameField = new JTextField(30);
		groupNameField.getDocument().addDocumentListener(documentListener);
		groupNameField.addFocusListener(focusListener);
		
		groupPanel.add(groupCombo, new GridBagConstraints(0, 0, 1, 1, Double.MIN_VALUE, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		groupPanel.add(groupNameField, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		
		add(new JLabel(Strings.importNetworkFilePath_label), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(new JLabel(Strings.importNetworkFileType_label), new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(new JLabel(Strings.importNetworkOrganism_label), new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(new JLabel(Strings.importNetworkGroup_label), new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(new JLabel(Strings.importNetworkName_label), new GridBagConstraints(0, 4, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(new JLabel(Strings.importNetworkDescription_label), new GridBagConstraints(0, 5, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		
		add(fileField, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		add(chooseButton, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		add(typePanel, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(organismCombo, new GridBagConstraints(1, 2, 2, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(groupPanel, new GridBagConstraints(1, 3, 2, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		add(nameField, new GridBagConstraints(1, 4, 2, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		add(new JScrollPane(descriptionField), new GridBagConstraints(1, 5, 2, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		
		updateGroups();
	}

	public void setOrganisms(List<Organism> organisms) {
		this.organisms = organisms;
		
		Vector<String> items = new Vector<String>();
		for (Organism organism : organisms) {
			items.add(String.format(Strings.importNetworkOrganism_description, organism.getName(), organism.getDescription()));
		}
		DefaultComboBoxModel organismModel = new DefaultComboBoxModel(items);
		organismCombo.setModel(organismModel);
		updateGroups();
	}
	
	protected void detectOrganism(final File file, final int maximumLinesToSample) {
		GeneManiaTask task = new GeneManiaTask(Strings.importNetworkDetecting_title) {
			@Override
			protected void runTask() throws Throwable {
				progress.setStatus(Strings.importNetworkDetecting_status);
				try {
					if (detectedFile != null && detectedFile.getCanonicalFile().equals(file.getCanonicalFile())) {
						return;
					}
				} catch (IOException e) {
					LogUtils.log(getClass(), e);
				}
				if (!file.exists()) {
					detectedFile = null;
					detector = null;
					return;
				}
				DataSet data = dataSetManager.getDataSet();
				DataFileClassifier classifier = new DataFileClassifier();
				try {
					classifier.classify(importSettings, fileUtils.getUncompressedStream(file), maximumLinesToSample);
					if (importSettings.getDataLayout().equals(DataLayout.GEO_PROFILE)) {
						typeGroup.setSelected(profileRadioButton.getModel(), true);
					} else if (importSettings.getDataLayout().equals(DataLayout.WEIGHTED_NETWORK)) {
						typeGroup.setSelected(interactionRadioButton.getModel(), true);
					} else if (importSettings.getDataLayout().equals(DataLayout.BINARY_NETWORK)) {
						typeGroup.setSelected(interactionRadioButton.getModel(), true);
					} else {
						JOptionPane.showMessageDialog(taskDispatcher.getTaskDialog(), Strings.importNetworkPanelUnrecognizedFile_error, Strings.importNetworkFile_title, JOptionPane.WARNING_MESSAGE);
						fileField.setText(""); //$NON-NLS-1$
						typeGroup.setSelected(profileRadioButton.getModel(), false);
						typeGroup.setSelected(interactionRadioButton.getModel(), false);
						return;
					}
					
					detector = new OrganismClassifier(data.getGeneClassifier());
					detector.classify(importSettings, fileUtils.getUncompressedReader(file), maximumLinesToSample);
					if (importSettings.getOrganism() == null) {
						return;
					}
					List<Match> mostLikelyOrganismIds = detector.getMostLikelyOrganismIds();
					Long id;
					if (mostLikelyOrganismIds.size() > 1) {
						id = disambiguateOrganism(mostLikelyOrganismIds);
					} else {
						id = importSettings.getOrganism().getId();
					}
					
					detectedFile = file;
					if (id == null) {
						return;
					}
					for (int i = 0; i < organisms.size(); i++) {
						Organism organism = organisms.get(i);
						if (organism.getId() == id) {
							organismCombo.setSelectedIndex(i);
							importSettings.setOrganism(organism);
							return;
						}
					}
				} catch (IOException e) {
					LogUtils.log(getClass(), e);
				}
			}
		};
		taskDispatcher.executeTask(task, uiUtils.getFrame(this), true, false);
	}

	private Long disambiguateOrganism(List<Match> mostLikelyOrganismIds) {
		OrganismChoice[] choices = new OrganismChoice[mostLikelyOrganismIds.size()];
		int index = 0;
		for (Match match : mostLikelyOrganismIds) {
			Organism organism = findOrganism(match.organismId);
			choices[index] = new OrganismChoice(organism, match.score);
			index++;
		}
		OrganismChoice choice = (OrganismChoice) JOptionPane.showInputDialog(uiUtils.getFrame(this), Strings.importNetworkDisambiguateMessage, Strings.importNetworkDisambiguateTitle, JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
		if (choice == null) {
			return null;
		}
		return choice.organism.getId();
	}

	private Organism findOrganism(long organismId) {
		for (Organism organism : organisms) {
			if (organism.getId() == organismId) {
				return organism;
			}
		}
		return null;
	}

	protected void updateGroups() {
		int index = organismCombo.getSelectedIndex();
		if (index == -1) {
			groupCombo.updateNetworkGroups(null);
			return;
		}
		Organism organism = organisms.get(index);
		groupCombo.updateNetworkGroups(organism.getInteractionNetworkGroups());
	}

	Organism getOrganism() {
		int index = organismCombo.getSelectedIndex();
		if (index == -1) {
			return null;
		}
		return organisms.get(index);
	}
	
	InteractionNetwork getNetwork() {
		InteractionNetwork network = new InteractionNetwork();
		network.setDefaultSelected(false);
		network.setDescription(descriptionField.getText());
		network.setName(nameField.getText());
		return network;
	}

	public File getNetworkFile() {
		return new File(fileField.getText());
	}

	public DataFileType getType() {
		ButtonModel selection = typeGroup.getSelection();
		if (selection == null) {
			return DataFileType.UNKNOWN;
		}
		if (selection.equals(interactionRadioButton.getModel())) {
			return DataFileType.INTERACTION_NETWORK;
		}
		if (selection.equals(profileRadioButton.getModel())) {
			return DataFileType.EXPRESSION_PROFILE;
		}
		return DataFileType.UNKNOWN;
	}
	
	public void validateSettings() {
		int groupIndex = groupCombo.getSelectedIndex();
		groupNameField.setVisible(groupIndex == 0);
		validate();
		
		ButtonModel selection = typeGroup.getSelection();
		boolean valid = selection != null;
		valid &= nameField.getText().trim().length() > 0;
		valid &= new File(fileField.getText()).isFile();
		valid &= organismCombo.getSelectedIndex() != -1;
		valid &= groupIndex != -1;
		
		String groupName = groupNameField.getText().trim();
		valid &= groupIndex != 0 || groupName.length() > 0 && !groupCombo.containsGroup(groupName);
		
		for (ValidationEventListener listener : validationListeners) {
			listener.validate(valid);
		}
	}
	
	public void clear() {
		nameField.setText(""); //$NON-NLS-1$
		descriptionField.setText(""); //$NON-NLS-1$
		fileField.setText(""); //$NON-NLS-1$
		groupNameField.setText(""); //$NON-NLS-1$
		detectedFile = null;
		detector = null;
	}

	public void addValidationEventListener(ValidationEventListener listener) {
		validationListeners.add(listener);
	}

	public DataImportSettings getImportSettings() {
		importSettings.setOrganism(getOrganism());
		importSettings.setNetwork(getNetwork());
		InteractionNetworkGroup group = groupCombo.getGroup();
		if (group.getId() == -1) {
			String name = groupNameField.getText().trim();
			group.setName(name);
			group.setCode(name);
			group.setDescription(""); //$NON-NLS-1$
		}
		importSettings.setNetworkGroup(group);
		return importSettings;
	}
	

	private void handleChoose() {
		int maximumLinesToSample = 250;
		HashSet<String> extensions = new HashSet<String>();
		extensions.add("csv"); //$NON-NLS-1$
		extensions.add("txt"); //$NON-NLS-1$
		extensions.add("soft"); //$NON-NLS-1$
		extensions.add("soft.gz"); //$NON-NLS-1$
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
		if (file.exists()) {
			detectOrganism(file, maximumLinesToSample);
		}
		validateSettings();
	}

	
	public static class OrganismChoice {
		public Organism organism;
		public double score;
		
		public OrganismChoice(Organism organism, double score) {
			this.organism = organism;
			this.score = score;
		}
		
		@Override
		public String toString() {
			return String.format("%s - %d%%", organism.getName(), (int) (score * 100)); //$NON-NLS-1$
		}
	}
}
