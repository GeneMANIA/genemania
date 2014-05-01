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

import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;

import org.genemania.plugin.view.util.UiUtils;

public class TextReportDialog<T> extends JDialog {
	private static final long serialVersionUID = 1L;
	private Map<String, T> values;
	private String selectedOption;
	private final UiUtils uiUtils;

	public TextReportDialog(Dialog parent, String message, List<String> options, Map<String, T> values, String report, boolean modal, UiUtils uiUtils) {
		super(parent, modal);
		this.uiUtils = uiUtils;
		initialize(message, options, values, report);
	}
	
	public TextReportDialog(Frame parent, String message, List<String> options, Map<String, T> values, String report, boolean modal, UiUtils uiUtils) {
		super(parent, modal);
		this.uiUtils = uiUtils;
		initialize(message, options, values, report);
	}

	private void initialize(String message, List<String> options, Map<String, T> values, String report) {
		this.values = values;
		
		JRootPane root = getRootPane();
		root.setLayout(new GridBagLayout());
		
		Insets insets = new Insets(4, 4, 4, 4);

		JEditorPane reportPane = uiUtils.createEditorPane(report);
		reportPane.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(reportPane);
		root.add(scrollPane, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));

		JLabel label = new JLabel(message);
		root.add(label, new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));

		JPanel buttonPanel = createButtonPanel(root, options);
		root.add(buttonPanel, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		
		setLocationByPlatform(true);
		pack();
	}
	
	private JPanel createButtonPanel(JComponent component, List<String> options) {
		JPanel buttonPanel = uiUtils.createJPanel();
		buttonPanel.setLayout(new FlowLayout());
		for (final String label : options) {
			JButton button = new JButton(label);
			final TextReportDialog<T> dialog = this;
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					selectedOption = label;
					dialog.setVisible(false);
				}
			});
			buttonPanel.add(button);
		}
		return buttonPanel;
	}
	
	public T getSelectedValue() {
		if (selectedOption == null) {
			return null;
		}
		return values.get(selectedOption);
	}
}
