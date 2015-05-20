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

import javax.swing.BorderFactory;
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

	public EditOrganismDialog(Frame frame, boolean modality, UiUtils uiUtils, OrganismValidator validator) {
		super(frame, Strings.editOrganism_title, modality);
		this.validator = validator;

		JPanel contents = uiUtils.createJPanel();
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
		
		Insets insets = new Insets(0, 0, 0, 0);
		int row = 0;
		
		nameField = new JTextField(30);
		nameField.getDocument().addDocumentListener(listener);

		contents.add(new JLabel(Strings.importOrganismName_label), new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, insets, 0, 0));
		contents.add(nameField, new GridBagConstraints(1, row, 2, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		row++;
		
		aliasField = new JTextField(30);
		aliasField.getDocument().addDocumentListener(listener);
		
		contents.add(new JLabel(Strings.importOrganismAlias_label), new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, insets, 0, 0));
		contents.add(aliasField, new GridBagConstraints(1, row, 2, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		row++;

		taxIdField = new JTextField(30);
		taxIdField.getDocument().addDocumentListener(listener);
		
		contents.add(new JLabel(Strings.importOrganismTaxonomyId_label), new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, insets, 0, 0));
		contents.add(taxIdField, new GridBagConstraints(1, row, 2, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		row++;

		descriptionField = new JTextArea();
		descriptionField.getDocument().addDocumentListener(listener);
		
		contents.add(new JLabel(Strings.importOrganismDescription_label), new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
		contents.add(new JScrollPane(descriptionField), new GridBagConstraints(1, row, 2, 1, 1, 1, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, insets, 0, 0));
		row++;
		
		JPanel buttonPanel = createButtonPanel(uiUtils);
		contents.add(buttonPanel, new GridBagConstraints(0, row, 3, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		row++;

		setLayout(new GridBagLayout());
		add(contents, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
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
