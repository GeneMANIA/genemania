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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.genemania.domain.Gene;
import org.genemania.plugin.Strings;
import org.genemania.plugin.completion.DynamicTableModel;
import org.genemania.plugin.model.AnnotationEntry;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.selection.SelectionEvent;
import org.genemania.plugin.selection.SelectionListener;
import org.genemania.plugin.view.util.UiUtils;

@SuppressWarnings("serial")
public class FunctionInfoPanel extends JPanel {
	private final JTable table;
	private final DynamicTableModel<AnnotationEntry> model;
	private ViewState options;
	private SelectionListener<Gene> functionListener;
	private final UiUtils uiUtils;

	public FunctionInfoPanel(UiUtils uiUtils) {
		this.uiUtils = uiUtils;
		
		model = new DynamicTableModel<AnnotationEntry>() {
			public Class<?> getColumnClass(int columnIndex) {
				switch (columnIndex) {
				case 0:
					return String.class; 
				case 1:
					return String.class;
				case 2:
					return String.class;
				}
				return null;
			}

			public int getColumnCount() {
				return 3;
			}

			public String getColumnName(int columnIndex) {
				switch (columnIndex) {
				case 0:
					return Strings.functionInfoPanelQValue_label;
				case 1:
					return Strings.functionInfoPanelCoverage_label;
				case 2:
					return Strings.functionInfoPanelGoAnnotation_label; 
				}
				return null;
			}

			public Object getValueAt(int rowIndex, int columnIndex) {
				AnnotationEntry entry = get(rowIndex);
				if (entry == null) {
					return null;
				}
				switch (columnIndex) {
				case 0:
					return String.format("%.2g", entry.getQValue()); //$NON-NLS-1$
				case 1:
					return String.format("%d/%d", entry.getSampleOccurrences(), entry.getTotalOccurrences()); //$NON-NLS-1$
				case 2:
					return entry.getDescription(); 
				}
				return null;
			}

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return false;
			}

			public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			}
		};
		table = new JTable(model);
		JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		setLayout(new GridBagLayout());
		add(scrollPane, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		
		ListSelectionModel selectionModel = table.getSelectionModel();
		selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selectionModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				updateSelectedNodes();
			}
		});
	}
	
	private void updateSelectedNodes() {
		if (functionListener == null) {
			return;
		}
		
		int row = table.getSelectedRow();
		if (row == -1) {
			// Clear selection
			return;
		}
		
		AnnotationEntry entry = model.get(row);
		if (entry == null) {
			return;
		}

		Collection<Gene> genes = options.getSearchResult().getNodesByAnnotation(entry.getName());
		SelectionEvent<Gene> event = new SelectionEvent<Gene>(new HashSet<Gene>(genes), true);
		functionListener.selectionChanged(event);
	}

	public void applyOptions(ViewState options) {
		this.options = options;
		model.clear();
		for (AnnotationEntry entry : options.getSearchResult().getEnrichmentSummary()) {
			model.add(entry);
		}
		uiUtils.packColumns(table);
	}

	public void updateSelection(ViewState options) {
		if (options.getTotalHighlightedGenes() == 0) {
			table.getSelectionModel().clearSelection();
		}
	}

	public void setSelectionListener(SelectionListener<Gene> functionListener) {
		this.functionListener = functionListener;
	}
}
