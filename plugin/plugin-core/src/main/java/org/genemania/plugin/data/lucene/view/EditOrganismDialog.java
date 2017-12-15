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

import java.awt.Frame;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.genemania.plugin.Strings;
import org.genemania.plugin.validation.OrganismValidator;
import org.genemania.plugin.view.util.UiUtils;

@SuppressWarnings("serial")
public class EditOrganismDialog extends AbstractEditDialog {

	private final JTextField nameField;
	private final JTextField aliasField;
	private final JTextField taxIdField;
	private final JTextArea descriptionField;
	private final OrganismValidator validator;

	public EditOrganismDialog(Frame frame, UiUtils uiUtils, OrganismValidator validator) {
		super(frame, Strings.editOrganism_title, true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		
		this.validator = validator;

		final JLabel label1 = new JLabel(Strings.importOrganismName_label);
		final JLabel label2 = new JLabel(Strings.importOrganismAlias_label);
		final JLabel label3 = new JLabel(Strings.importOrganismTaxonomyId_label);
		final JLabel label4 = new JLabel(Strings.importOrganismDescription_label);
		
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
		
		nameField = new JTextField(30);
		nameField.getDocument().addDocumentListener(listener);
		
		aliasField = new JTextField(30);
		aliasField.getDocument().addDocumentListener(listener);
		
		taxIdField = new JTextField(30);
		taxIdField.getDocument().addDocumentListener(listener);
		
		descriptionField = new JTextArea();
		descriptionField.getDocument().addDocumentListener(listener);
		final JScrollPane scrollPane = new JScrollPane(descriptionField);

		final JPanel buttonPanel = createButtonPanel(uiUtils);

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
								.addComponent(label4)
						)
						.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
								.addComponent(nameField)
								.addComponent(aliasField)
								.addComponent(taxIdField)
								.addComponent(scrollPane)
						)
				)
				.addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(label1)
						.addComponent(nameField)
				)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(label2)
						.addComponent(aliasField)
				)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(label3)
						.addComponent(taxIdField)
				)
				.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
						.addComponent(label4, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(scrollPane, DEFAULT_SIZE, 160, Short.MAX_VALUE)
				)
				.addComponent(buttonPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		getContentPane().add(contents);
	}

	public void setDescription(String description) {
		descriptionField.setText(description);
	}

	@Override
	protected boolean isValidForm() {
		boolean isValid = true;
		isValid &= validator.isValidName(nameField.getText());
		isValid &= validator.isValidAlias(aliasField.getText());
		isValid &= validator.isValidTaxonomyId(taxIdField.getText());
		
		return isValid;
	}

	public void setOrganismName(String name) {
		nameField.setText(name);
	}

	public String getOrganismName() {
		return nameField.getText();
	}

	public String getDescription() {
		return descriptionField.getText();
	}

	public void setAlias(String alias) {
		aliasField.setText(alias);
	}

	public void setTaxonomyId(long taxonomyId) {
		taxIdField.setText(String.valueOf(taxonomyId));
	}

	public String getAlias() {
		return aliasField.getText();
	}

	public long getTaxonomyId() {
		return Long.parseLong(taxIdField.getText());
	}
}
