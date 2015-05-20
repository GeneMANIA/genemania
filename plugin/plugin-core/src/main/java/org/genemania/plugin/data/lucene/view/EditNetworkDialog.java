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

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.plugin.Strings;
import org.genemania.plugin.view.components.NetworkGroupComboBox;
import org.genemania.plugin.view.util.UiUtils;

@SuppressWarnings("serial")
public class EditNetworkDialog extends AbstractEditDialog {
	
	private final NetworkGroupComboBox groupCombo;
	private final JTextField nameField;
	private final JTextArea descriptionField;
	private final JTextField groupNameField;
	
	public EditNetworkDialog(final Frame owner, final boolean modality, final UiUtils uiUtils) {
		super(owner, Strings.editNetwork_title, modality);
		
		final JPanel contents = uiUtils.createJPanel();
		contents.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
		contents.setLayout(new GridBagLayout());

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
		
		groupCombo = new NetworkGroupComboBox();
		groupCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				validateSettings();
			}
		});
		
		nameField = new JTextField(30);
		nameField.getDocument().addDocumentListener(listener);
		
		descriptionField = new JTextArea();
		descriptionField.setRows(3);
		descriptionField.getDocument().addDocumentListener(listener);
		
		JPanel buttonPanel = createButtonPanel(uiUtils);
		JScrollPane scrollPane = new JScrollPane(descriptionField);
		
		JPanel groupPanel = uiUtils.createJPanel();
		groupPanel.setLayout(new GridBagLayout());
		
		FocusListener focusListener = new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				validateSettings();
			}
			@Override
			public void focusGained(FocusEvent e) {
			}
		};

		groupNameField = new JTextField(30);
		groupNameField.getDocument().addDocumentListener(listener);
		groupNameField.addFocusListener(focusListener);
		
		groupPanel.add(groupCombo, new GridBagConstraints(0, 0, 1, 1, Double.MIN_VALUE, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		groupPanel.add(groupNameField, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		int row = 0;
		contents.add(new JLabel(Strings.importNetworkGroup_label), new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0 ,0), 0, 0));
		contents.add(groupPanel, new GridBagConstraints(1, row, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		row++;
		
		contents.add(new JLabel(Strings.importNetworkName_label), new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0 ,0), 0, 0));
		contents.add(nameField, new GridBagConstraints(1, row, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		row++;
		
		contents.add(new JLabel(Strings.importNetworkDescription_label), new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0 ,0), 0, 0));
		contents.add(scrollPane, new GridBagConstraints(1, row, 1, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		row++;
		
		contents.add(buttonPanel, new GridBagConstraints(0, row, 2, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		row++;
		
		setLayout(new GridBagLayout());
		add(contents, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
	}
	
	@Override
	protected boolean isValidForm() {
		int groupIndex = groupCombo.getSelectedIndex();
		groupNameField.setVisible(groupIndex == 0);
		validate();
		
		boolean valid = nameField.getText().trim().length() > 0;
		valid &= descriptionField.getText().trim().length() > 0;
		valid &= groupIndex != -1;
		
		String groupName = groupNameField.getText().trim();
		valid &= groupIndex != 0 || groupName.length() > 0 && !groupCombo.containsGroup(groupName);

		return valid;
	}

	public void setOrganism(Organism organism) {
		groupCombo.updateNetworkGroups(organism.getInteractionNetworkGroups());
	}
	
	public void setGroup(InteractionNetworkGroup group) {
		groupCombo.setGroup(group);
	}
	
	public void setNetworkName(String name) {
		nameField.setText(name);
	}
	
	public void setDescription(String description) {
		descriptionField.setText(description);
	}
	
	public InteractionNetworkGroup getGroup() {
		return groupCombo.getGroup();
	}
	
	public String getNetworkName() {
		return nameField.getText().trim();
	}
	
	public String getDescription() {
		return descriptionField.getText().trim();
	}

	public String getColor() {
		return null;
	}

	public String getGroupName() {
		return groupNameField.getText().trim();
	}
}
