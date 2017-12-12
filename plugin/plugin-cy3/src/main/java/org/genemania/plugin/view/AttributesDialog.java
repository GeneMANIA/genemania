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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.genemania.plugin.Strings;
import org.genemania.plugin.controllers.AttributesController;
import org.genemania.plugin.model.AttributeEntry;
import org.genemania.plugin.view.util.UiUtils;

@SuppressWarnings("serial")
public class AttributesDialog extends JDialog {
	
	private final List<AttributeEntry> entries;
	private List<String> selectedAttributes;
	
	private JPanel mainPanel;
	private JPanel attributesPanel;
	private JButton selectAllButton;
	private JButton selectNoneButton;
	private JButton addAttributesButton;
	private JButton cancelButton;
	private final List<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
	
	private final UiUtils uiUtils;
	
	public AttributesDialog(final Frame parent, final AttributesController controller, final UiUtils uiUtils) {
		super(parent, Strings.attributesDialog_title, ModalityType.APPLICATION_MODAL);
		this.uiUtils = uiUtils;
		this.entries = controller.createModel();
		
		addComponents();
	}
	
	public List<String> getSelectedAttributes() {
		return selectedAttributes;
	}
	
	private void addComponents() {
		final JPanel buttonPanel = uiUtils.createOkCancelPanel(getAddAttributesButton(), getCancelButton());
		
		final JPanel panel = new JPanel();
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateGaps(uiUtils.isWinLAF());
		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addComponent(getMainPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getMainPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(buttonPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		getContentPane().add(panel, BorderLayout.CENTER);
		
		uiUtils.setDefaultOkCancelKeyStrokes(getRootPane(), getAddAttributesButton().getAction(),
				getCancelButton().getAction());
		getRootPane().setDefaultButton(getAddAttributesButton());
		
		pack();
		setResizable(false);
	}
	
	private JPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = uiUtils.createJPanel();
			mainPanel.setBorder(uiUtils.createPanelBorder());
			
			final JTextArea label = new JTextArea(Strings.attributesDialog_description);
			label.setEditable(false);
			label.setOpaque(false);
			label.setFont(label.getFont().deriveFont(UiUtils.INFO_FONT_SIZE));
			label.setWrapStyleWord(true);
			label.setLineWrap(true);
			
			final GroupLayout layout = new GroupLayout(mainPanel);
			mainPanel.setLayout(layout);
			layout.setAutoCreateGaps(uiUtils.isWinLAF());
			layout.setAutoCreateContainerGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
							.addComponent(label, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addGroup(layout.createSequentialGroup()
									.addComponent(getSelectAllButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
									.addComponent(getSelectNoneButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							)
					)
					.addComponent(getAttributesPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.TRAILING, true)
					.addGroup(layout.createSequentialGroup()
							.addComponent(label, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
									.addComponent(getSelectAllButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
									.addComponent(getSelectNoneButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							)
					)
					.addComponent(getAttributesPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			
			uiUtils.equalizeSize(getSelectAllButton(), getSelectNoneButton());
		}
		
		return mainPanel;
	}
	
	private JPanel getAttributesPanel() {
		if (attributesPanel == null) {
			attributesPanel = uiUtils.createJPanel();
			attributesPanel.setBorder(uiUtils.createPanelBorder());
			
			final GroupLayout layout = new GroupLayout(attributesPanel);
			attributesPanel.setLayout(layout);
			layout.setAutoCreateGaps(uiUtils.isWinLAF());
			layout.setAutoCreateContainerGaps(true);
			
			final ParallelGroup hgroup = layout.createParallelGroup(Alignment.LEADING, true);
			final SequentialGroup vgroup = layout.createSequentialGroup();
			layout.setHorizontalGroup(hgroup);
			layout.setVerticalGroup(vgroup);
			
			for (final AttributeEntry entry : entries) {
				final JCheckBox checkBox = new JCheckBox(entry.getDisplayName());
				checkBox.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						entry.setSelected(checkBox.isSelected());
					}
				});
				checkBoxes.add(checkBox);
				
				hgroup.addComponent(checkBox, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
				vgroup.addComponent(checkBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
			}
		}
		
		return attributesPanel;
	}
	
	private JButton getSelectAllButton() {
		if (selectAllButton == null) {
			selectAllButton = new JButton(new AbstractAction(Strings.selectAllButton_label) {
				@Override
				public void actionPerformed(ActionEvent e) {
					setSelected(true);
				}
			});
			selectAllButton.putClientProperty("JComponent.sizeVariant", "small"); // Mac OS X only
		}
		
		return selectAllButton;
	}
	
	private JButton getSelectNoneButton() {
		if (selectNoneButton == null) {
			selectNoneButton = new JButton(new AbstractAction(Strings.selectNoneButton_label) {
				@Override
				public void actionPerformed(ActionEvent e) {
					setSelected(false);
				}
			});
			selectNoneButton.putClientProperty("JComponent.sizeVariant", "small"); // Mac OS X only
		}
		
		return selectNoneButton;
	}
	
	private JButton getAddAttributesButton() {
		if (addAttributesButton == null) {
			addAttributesButton = new JButton(new AbstractAction(Strings.attributesDialogAddButton_label) {
				@Override
				public void actionPerformed(ActionEvent e) {
					handleAddAttributesButton();
				}
			});
		}
		
		return addAttributesButton;
	}
	
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(new AbstractAction(Strings.attributesDialogCancelButton_label) {
				@Override
				public void actionPerformed(ActionEvent e) {
					handleCancelButton();
				}
			});
		}
		
		return cancelButton;
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

	private void setSelected(boolean selected) {
		for (JCheckBox checkBox : checkBoxes) {
			checkBox.setSelected(selected);
		}
		for (AttributeEntry entry : entries) {
			entry.setSelected(true);
		}
	}
}
