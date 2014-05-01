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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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
	private JTable table;
	private String dataSetId;
	private DownloadController controller;
	private JButton downloadButton;
	private JButton selectButton;
	private Action action;
	private DynamicTableModel<ModelElement> model;
	private ButtonGroup optionGroup;
	private Map<String, JRadioButton> optionMap;
	private JRadioButton noSelectionButton;
	private final UiUtils uiUtils;
	private final DataSetManager dataSetManager;
	private final FileUtils fileUtils;

	public DownloadDialog(Dialog parent, String title, boolean modal, String preamble, DataSetManager dataSetManager, UiUtils uiUtils, FileUtils fileUtils) throws ApplicationException {
		super(parent, title, modal);
		this.uiUtils = uiUtils;
		this.dataSetManager = dataSetManager;
		this.fileUtils = fileUtils;
		initialize(preamble);
	}

	void initialize(String preamble) throws ApplicationException {
		controller = new DownloadController(dataSetManager, fileUtils);
		
		JRootPane pane = getRootPane();
		pane.setLayout(new GridBagLayout());
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		Insets insets = new Insets(0, 0, 0, 0);
		JLabel label = new JLabel(preamble);
		
		int row = 0;
		panel.add(label, new GridBagConstraints(0, row, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 16, 0) , 0, 0));
		row++;
		
		List<DownloadController.ModelElement> items;
		try {
			items = controller.createModel();
		} catch (IOException e) {
			throw new ApplicationException(Strings.checkForUpdates_error);
		} 
		
		model = new DynamicTableModel<ModelElement>() {
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
			
			public int getColumnCount() {
				return 3;
			}
			
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
			
			public Object getValueAt(int rowIndex, int columnIndex) {
				ModelElement element = get(rowIndex);
				switch (columnIndex) {
				case 0:
					return element.isInstalled();
				case 1:
					if (element.isActive()) {
						return String.format(Strings.downloadControllerModelElementActive_label, element.getName());
					}
					return String.format(Strings.downloadControllerModelElement_label, element.getName());
				case 2:
					return element.getDescription();
				default:
					return null;
				}
			}
			
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return false;
			}
			
			public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			}
		};
		
		for (ModelElement item : items) {
			model.add(item);
		}
		
		table = new JTable(model) {
			@Override
			public void addNotify() {
				super.addNotify();
				uiUtils.packColumns(this);
			}
		};
		table.setRowSelectionAllowed(true);
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				updateOptions();
				validateState();
			}
		});
		
		JScrollPane scrollPane = new JScrollPane(table);
		panel.add(scrollPane, new GridBagConstraints(0, row, 1, 1, 1, 1, GridBagConstraints.PAGE_START, GridBagConstraints.BOTH, insets, 0, 0));
		row++;
		
		JPanel optionPanel = createOptionPanel();
		panel.add(optionPanel, new GridBagConstraints(0, row, 1, 1, 1, 0, GridBagConstraints.PAGE_START, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		row++;
		
		JPanel buttonPanel = createButtonPanel();
		panel.add(buttonPanel, new GridBagConstraints(0, row, 1, 1, 1, 0, GridBagConstraints.PAGE_START, GridBagConstraints.NONE, insets, 0, 0));
		row++;
		
		pane.add(panel, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.PAGE_START, GridBagConstraints.BOTH, new Insets(16, 16, 16, 16), 0, 0));
		setMinimumSize(new Dimension(uiUtils.computeTextSizeHint(label.getFontMetrics(label.getFont()), 60, 25)));
		setLocationByPlatform(true);
		updateOptions();
		validateState();
		pack();
	}
	
	private JPanel createButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel .setLayout(new FlowLayout());
		downloadButton = new JButton(Strings.downloadDialogDownloadButton_label);
		downloadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleDownloadButton();
			}
		});
		selectButton = new JButton(Strings.downloadDialogSelectButton_label);
		selectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleSelectButton();
			}
		});
		JButton cancelButton = new JButton(Strings.downloadDialogCancelButton_label);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleCancelButton();
			}
		});
		buttonPanel.add(selectButton);
		buttonPanel.add(downloadButton);
		buttonPanel.add(cancelButton);
		return buttonPanel;
	}

	private JPanel createOptionPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder(Strings.downloadDialogOptionPanel_label));
		
		JRadioButton coreButton = new JRadioButton();
		JRadioButton allButton = new JRadioButton();
		JRadioButton openLicenseButton = new JRadioButton();
		
		optionMap = new HashMap<String, JRadioButton>();
		optionMap.put("-core", coreButton); //$NON-NLS-1$
		optionMap.put("", allButton); //$NON-NLS-1$
		optionMap.put("-open_license", openLicenseButton); //$NON-NLS-1$
		
		ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				validateState();
			}
		};
		
		for (Entry<String, JRadioButton> entry : optionMap.entrySet()) {
			JRadioButton button = entry.getValue();
			button.addActionListener(listener);
			button.setActionCommand(entry.getKey());
		}
		
		optionGroup = new ButtonGroup();
		optionGroup.add(coreButton);
		optionGroup.add(allButton);
		optionGroup.add(openLicenseButton);
		
		noSelectionButton = new JRadioButton();
		noSelectionButton.addActionListener(listener);
		noSelectionButton.setActionCommand("none"); //$NON-NLS-1$
		optionGroup.add(noSelectionButton);
		
		panel.add(allButton, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		panel.add(coreButton, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		panel.add(openLicenseButton, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		
		return panel;
	}

	void deselectAllOptions() {
		noSelectionButton.setSelected(true);
	}
	
	void updateOptions() {
		int row = table.getSelectedRow();
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
		if (isActive) {
			description = Strings.downloadDialogOptionPanelActive_label;
		} else if (isInstalled) {
			description = Strings.downloadDialogOptionPanelInstalled_label;
		} else if (megabytes == 0) {
			description = ""; //$NON-NLS-1$
		} else {
			description = String.format(Strings.downloadDialogOptionSize_label, megabytes);
		}
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
		if (element != null) {
			download = !element.isInstalled();
		}
		downloadButton.setEnabled(download);
		selectButton.setEnabled(element != null && !download);
	}
	
	void handleCancelButton() {
		action = Action.cancel;
		setVisible(false);
	}

	void handleAction(Action action) {
		this.action = action;
		ModelElement element = getSelection();
		if (element != null) {
			dataSetId = element.getName();
		}
		setVisible(false);
	}
	
	private ModelElement getSelection() {
		int row = table.getSelectedRow();
		if (row == -1) {
			return null;
		}
		ModelElement root = (ModelElement) model.get(row);
		ModelElement[] children = root.getChildren();
		if (children.length == 0) {
			return root;
		}
		
		ButtonModel selection = optionGroup.getSelection();
		if (selection == null) {
			return null;
		}
		String selected = selection.getActionCommand();
		for (ModelElement child : children) {
			Matcher matcher = DownloadController.optionPattern.matcher(child.getName());
			if (matcher.matches()) {
				String action = matcher.group(2);
				if (action == null) {
					action = ""; //$NON-NLS-1$
				}
				if (action.equals(selected)) {
					return child;
				}
			}
		}
		return null;
	}

	void handleDownloadButton() {
		handleAction(Action.download);
	}

	void handleSelectButton() {
		handleAction(Action.select);
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
