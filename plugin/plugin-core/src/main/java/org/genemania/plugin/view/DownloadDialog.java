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
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.cytoscape.util.swing.IconManager;
import org.genemania.exception.ApplicationException;
import org.genemania.plugin.FileUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.completion.DynamicTableModel;
import org.genemania.plugin.controllers.DownloadController;
import org.genemania.plugin.controllers.DownloadController.ModelElement;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.view.util.UiUtils;

@SuppressWarnings("serial")
public class DownloadDialog extends JDialog {
	
	private String dataSetId;
	private final DownloadController controller;
	private final DynamicTableModel<ModelElement> model;
	private Action action;
	
	private JPanel optionPanel;
	private JTable table;
	private JButton downloadButton;
	private JButton selectButton;
	private JButton cancelButton;
	private final ButtonGroup optionGroup;
	private Map<String, JRadioButton> optionMap;
	private JRadioButton noSelectionButton;
	
	private final UiUtils uiUtils;

	public DownloadDialog(
			final Dialog parent,
			final String title,
			final String preamble,
			final DataSetManager dataSetManager,
			final UiUtils uiUtils,
			final FileUtils fileUtils
	) throws ApplicationException {
		super(parent, title, ModalityType.APPLICATION_MODAL);
		this.uiUtils = uiUtils;
		
		optionGroup = new ButtonGroup();
		controller = new DownloadController(dataSetManager, fileUtils);
		
		List<DownloadController.ModelElement> items = null;
		
		try {
			items = controller.createModel();
		} catch (IOException e) {
			throw new ApplicationException(Strings.checkForUpdates_error);
		} 
		
		model = new DynamicTableModel<ModelElement>() {
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				switch (columnIndex) {
				case 0:
					return Boolean.class;
				case 1:
					return String.class;
				case 2:
					return String.class;
				default:
					return null;
				}
			}
			
			@Override
			public int getColumnCount() {
				return 3;
			}
			
			@Override
			public String getColumnName(int columnIndex) {
				switch (columnIndex) {
				case 0:
					return Strings.downloadDialogInstalledColumn_label;
				case 1:
					return Strings.downloadDialogNameColumn_label;
				case 2:
					return Strings.downloadDialogDescriptionColumn_label;
				default:
					return null;
				}
			}
			
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				ModelElement element = get(rowIndex);
				switch (columnIndex) {
				case 0:
					return element.isInstalled();
				case 1:
					if (element.isActive())
						return String.format(Strings.downloadControllerModelElementActive_label, element.getName());
					return String.format(Strings.downloadControllerModelElement_label, element.getName());
				case 2:
					return element.getDescription();
				default:
					return null;
				}
			}
			
			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return false;
			}
			
			@Override
			public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			}
		};
		
		for (ModelElement item : items)
			model.add(item);
		
		addComponents(preamble);
	}

	private void addComponents(final String preamble) throws ApplicationException {
		final JLabel label = new JLabel(preamble);
		final JScrollPane scrollPane = new JScrollPane(getTable());
		
		final JPanel buttonPanel = uiUtils.createOkCancelPanel(getDownloadButton(), getCancelButton(), getSelectButton());
		
		final JPanel panel = new JPanel();
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addComponent(label, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getOptionPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(label, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getOptionPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(buttonPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		getContentPane().add(panel, BorderLayout.CENTER);
		
		uiUtils.setDefaultOkCancelKeyStrokes(getRootPane(), getDownloadButton().getAction(),
				getCancelButton().getAction());
		getRootPane().setDefaultButton(getDownloadButton());
		
		setMinimumSize(new Dimension(uiUtils.computeTextSizeHint(label.getFontMetrics(label.getFont()), 60, 25)));
		setLocationByPlatform(true);
		
		updateOptions();
		validateState();
		
		pack();
	}
	
	private JTable getTable() {
		if (table == null) {
			table = new JTable(model) {
				@Override
				public void addNotify() {
					super.addNotify();
					uiUtils.packColumns(this);
				}
			};
			table.setRowSelectionAllowed(true);
			
			table.setDefaultRenderer(Boolean.class, new DefaultTableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
						boolean hasFocus, int row, int column) {
					super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
					
					if (Boolean.TRUE == value) {
						this.setFont(uiUtils.getIconFont(14.0f));
						this.setText(IconManager.ICON_CHECK);
						this.setHorizontalAlignment(CENTER);
					} else {
						this.setText(null);
					}
					
					return this;
				}
			});
			
			table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.getSelectionModel().addListSelectionListener(evt -> {
				updateOptions();
				validateState();
			});
		}
		
		return table;
	}
	
	private JPanel getOptionPanel() {
		if (optionPanel == null) {
			optionPanel = new JPanel();
			optionPanel.setBorder(uiUtils.createTitledBorder(Strings.downloadDialogOptionPanel_label));
			
			final JRadioButton coreButton = new JRadioButton();
			final JRadioButton allButton = new JRadioButton();
			final JRadioButton openLicenseButton = new JRadioButton();
			
			optionMap = new HashMap<>();
			optionMap.put("-core", coreButton); //$NON-NLS-1$
			optionMap.put("", allButton); //$NON-NLS-1$
			optionMap.put("-open_license", openLicenseButton); //$NON-NLS-1$
			
			for (Entry<String, JRadioButton> entry : optionMap.entrySet()) {
				final JRadioButton button = entry.getValue();
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent event) {
						validateState();
					}
				});
				button.setActionCommand(entry.getKey());
			}
			
			optionGroup.add(coreButton);
			optionGroup.add(allButton);
			optionGroup.add(openLicenseButton);
			optionGroup.add(getNoSelectionButton());
			
			final GroupLayout layout = new GroupLayout(optionPanel);
			optionPanel.setLayout(layout);
			layout.setAutoCreateGaps(uiUtils.isWinLAF());
			layout.setAutoCreateContainerGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(allButton)
					.addComponent(coreButton)
					.addComponent(openLicenseButton)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(allButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(coreButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(openLicenseButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return optionPanel;
	}
	
	private JRadioButton getNoSelectionButton() {
		if (noSelectionButton == null) {
			noSelectionButton = new JRadioButton();
			noSelectionButton.addActionListener(evt -> validateState());
			noSelectionButton.setActionCommand("none"); //$NON-NLS-1$
		}
		
		return noSelectionButton;
	}
	
	private JButton getSelectButton() {
		if (selectButton == null) {
			selectButton = new JButton(new AbstractAction(Strings.downloadDialogSelectButton_label) {
				@Override
				public void actionPerformed(ActionEvent e) {
					handleSelectButton();
				}
			});
		}
		
		return selectButton;
	}
	
	private JButton getDownloadButton() {
		if (downloadButton == null) {
			downloadButton = new JButton(new AbstractAction(Strings.downloadDialogDownloadButton_label) {
				@Override
				public void actionPerformed(ActionEvent e) {
					handleDownloadButton();
				}
			});
		}
		
		return downloadButton;
	}
	
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(new AbstractAction(Strings.downloadDialogCancelButton_label) {
				@Override
				public void actionPerformed(ActionEvent e) {
					handleCancelButton();
				}
			});
		}
		
		return cancelButton;
	}

	void deselectAllOptions() {
		getNoSelectionButton().setSelected(true);
	}
	
	void updateOptions() {
		int row = table != null ? table.getSelectedRow() : -1;
		
		if (row == -1) {
			disableOptions();
			return;
		}
		
		ModelElement root = (ModelElement) model.get(row);
		ModelElement[] children = root.getChildren();
		enableOptions(root, children);
	}

	String getLabel(String option, boolean isActive, boolean isInstalled, double megabytes) {
		String description;
		
		if (isActive)
			description = Strings.downloadDialogOptionPanelActive_label;
		else if (isInstalled)
			description = Strings.downloadDialogOptionPanelInstalled_label;
		else if (megabytes == 0)
			description = ""; //$NON-NLS-1$
		else
			description = String.format(Strings.downloadDialogOptionSize_label, megabytes);
		
		String template = Strings.get(String.format("downloadDialogOption%s", option)); //$NON-NLS-1$
		
		return String.format(template, description);
	}
	
	private void disableOptions() {
		for (Entry<String, JRadioButton> entry : optionMap.entrySet()) {
			JRadioButton button = entry.getValue();
			String key = entry.getKey();
			button.setText(getLabel(key, false, false, 0));
			button.setEnabled(false);
			button.invalidate();
		}
		
		deselectAllOptions();
		invalidate();
		pack();
	}

	private void enableOptions(ModelElement parent, ModelElement[] children) {
		deselectAllOptions();
		
		if (children.length == 0) {
			for (Entry<String, JRadioButton> entry : optionMap.entrySet()) {
				JRadioButton button = entry.getValue();
				String key = entry.getKey();
				double size;
				
				if ("".equals(key)) { //$NON-NLS-1$
					size = "".equals(key) ? computeSize(parent.getSize()) : 0; //$NON-NLS-1$
					button.setEnabled(true);
					button.setSelected(true);
				} else {
					button.setEnabled(false);
					size = 0;
				}
				
				button.setText(getLabel(key, false, false, size));
				button.invalidate();
			}
			
			invalidate();
			pack();
			
			return;
		}

		// Disable all options first
		for (Entry<String, JRadioButton> entry : optionMap.entrySet()) {
			JRadioButton button = entry.getValue();
			String key = entry.getKey();
			button.setText(getLabel(key, false, false, 0));
			button.setEnabled(false);
			button.invalidate();
		}

		// Only enable options that are actually available
		for (ModelElement child : children) {
			for (Entry<String, JRadioButton> entry : optionMap.entrySet()) {
				String key = entry.getKey();
				
				if (child.getName().endsWith(key)) {
					JRadioButton button = entry.getValue();
					button.setText(getLabel(key, child.isActive(), child.isInstalled(), computeSize(child.getSize())));
					button.setEnabled(true);
					button.setSelected(child.isInstalled());
					button.invalidate();
				}
			}
		}
		
		invalidate();
		pack();
	}

	private double computeSize(long size) {
		return size / 1024.0;
	}

	void validateState() {
		boolean download = false;
		ModelElement element = getSelection();
		
		if (element != null)
			download = !element.isInstalled();
		
		getDownloadButton().setEnabled(download);
		getSelectButton().setEnabled(element != null && !download);
	}
	
	private void handleCancelButton() {
		action = Action.cancel;
		setVisible(false);
	}
	
	private void handleDownloadButton() {
		handleAction(Action.download);
	}

	private void handleSelectButton() {
		handleAction(Action.select);
	}

	private void handleAction(Action action) {
		this.action = action;
		ModelElement element = getSelection();
		
		if (element != null)
			dataSetId = element.getName();
		
		setVisible(false);
	}
	
	private ModelElement getSelection() {
		int row = getTable().getSelectedRow();
		
		if (row == -1)
			return null;
		
		ModelElement root = (ModelElement) model.get(row);
		ModelElement[] children = root.getChildren();
		
		if (children.length == 0)
			return root;
		
		ButtonModel selection = optionGroup.getSelection();
		
		if (selection == null)
			return null;
		
		String selected = selection.getActionCommand();
		
		for (ModelElement child : children) {
			Matcher matcher = DownloadController.optionPattern.matcher(child.getName());
			
			if (matcher.matches()) {
				String action = matcher.group(2);
				
				if (action == null)
					action = ""; //$NON-NLS-1$
				if (action.equals(selected))
					return child;
			}
		}
		
		return null;
	}

	public String getSelectedDataSetId() {
		return dataSetId;
	}
	
	public Action getAction() {
		return action;
	}
	
	public enum Action {
		download,
		select,
		cancel,
	}
}
