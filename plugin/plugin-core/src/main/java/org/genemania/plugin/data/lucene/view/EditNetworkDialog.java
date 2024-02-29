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

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
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
	
	public EditNetworkDialog(Window owner, UiUtils uiUtils) {
		super(owner, Strings.editNetwork_title, true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		
		final JLabel label1 = new JLabel(Strings.importNetworkGroup_label);
		final JLabel label2 = new JLabel(Strings.importNetworkName_label);
		final JLabel label3 = new JLabel(Strings.importNetworkDescription_label);
		
		final DocumentListener listener = new DocumentListener() {
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
		final JScrollPane scrollPane = new JScrollPane(descriptionField);
		
		final JPanel buttonPanel = createButtonPanel(uiUtils);
		
		final FocusListener focusListener = new FocusListener() {
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
		
		final JPanel contents = uiUtils.createJPanel();
		final GroupLayout layout = new GroupLayout(contents);
		contents.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(Alignment.TRAILING, false)
								.addComponent(label1)
								.addComponent(label2)
								.addComponent(label3)
						)
						.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
								.addGroup(layout.createSequentialGroup()
										.addComponent(groupCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
										.addComponent(groupNameField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								)
								.addComponent(nameField)
								.addComponent(scrollPane)
						)
				)
				.addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(label1)
						.addComponent(groupCombo)
						.addComponent(groupNameField)
				)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(label2)
						.addComponent(nameField)
				)
				.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
						.addComponent(label3, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(scrollPane, DEFAULT_SIZE, 160, Short.MAX_VALUE)
				)
				.addComponent(buttonPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		getContentPane().add(contents);
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
