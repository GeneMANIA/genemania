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

package org.genemania.plugin.view;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;

import org.genemania.plugin.Strings;
import org.genemania.plugin.controllers.AttributesController;
import org.genemania.plugin.model.AttributeEntry;

@SuppressWarnings("serial")
public class AttributesDialog extends JDialog {
	private List<AttributeEntry> entries;
	private List<JCheckBox> checkBoxes;
	private List<String> selectedAttributes;
	
	public AttributesDialog(Frame parent, boolean modality, AttributesController controller) {
		super(parent, Strings.attributesDialog_title, modality);
		
		this.entries = controller.createModel();
		
		JRootPane root = getRootPane();
		root.setLayout(new GridBagLayout());
		
		JPanel canvas = new JPanel();
		
		canvas.setLayout(new GridBagLayout());
		
		int row = 0;
		Insets insets = new Insets(0, 0, 0, 0);
		
		JTextArea label = new JTextArea(Strings.attributesDialog_description);
		label.setEditable(false);
		label.setOpaque(false);
		label.setWrapStyleWord(true);
		label.setLineWrap(true);
		label.setColumns(40);
		label.setRows(4);
		canvas.add(label, new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets , 0, 0));
		row++;
		
		canvas.add(createSelectPanel(), new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.PAGE_START, GridBagConstraints.NONE, insets, 0, 0));
		row++;

		checkBoxes = new ArrayList<JCheckBox>();
		for (final AttributeEntry entry : entries) {
			final JCheckBox checkBox = new JCheckBox(entry.getDisplayName());
			checkBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					entry.setSelected(checkBox.isSelected());
				}
			});
			canvas.add(checkBox, new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets, 0, 0));
			checkBoxes.add(checkBox);
			row++;
		}
		
		canvas.add(createControlPanel(), new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.PAGE_START, GridBagConstraints.NONE, insets, 0, 0));
		row++;
		
		canvas.add(new JPanel(), new GridBagConstraints(0, row, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
		row++;
		
		root.add(canvas, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(16, 16, 16, 16), 0, 0));
	}
	
	private Component createControlPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		JButton addAttributesButton = new JButton(Strings.attributesDialogAddButton_label);
		addAttributesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleAddAttributesButton();
			}
		});
		panel.add(addAttributesButton);
		
		JButton cancelButton = new JButton(Strings.attributesDialogCancelButton_label);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleCancelButton();
			}
		});
		panel.add(cancelButton);
		return panel;
	}

	private void handleCancelButton() {
		selectedAttributes = Collections.emptyList();
		setVisible(false);
	}

	private void handleAddAttributesButton() {
		selectedAttributes = new ArrayList<String>();
		for (AttributeEntry entry : entries) {
			if (entry.isSelected()) {
				selectedAttributes.add(entry.getAttributeName());
			}
		}
		setVisible(false);
	}

	public List<String> getSelectedAttributes() {
		return selectedAttributes;
	}
	
	private Component createSelectPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		JButton selectAllButton = new JButton(Strings.attributesDialogSelectAllButton_label);
		selectAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleSelectAllButton();
			}
		});
		panel.add(selectAllButton);
		
		JButton selectNoneButton = new JButton(Strings.attributesDialogSelectNoneButton_label);
		selectNoneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleSelectNoneButton();
			}
		});
		panel.add(selectNoneButton);
		return panel;
	}

	private void handleSelectNoneButton() {
		setSelected(false);
	}

	private void handleSelectAllButton() {
		setSelected(true);
	}
	
	private void setSelected(boolean selected) {
		for (JCheckBox checkBox : checkBoxes) {
			checkBox.setSelected(selected);
		}
		for (AttributeEntry entry : entries) {
			entry.setSelected(true);
		}
	}
}
