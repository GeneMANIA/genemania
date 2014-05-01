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

package org.genemania.plugin.data.lucene;

import java.awt.Dialog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.genemania.data.normalizer.DataImportSettings;
import org.genemania.data.normalizer.NormalizationResult;
import org.genemania.plugin.Strings;
import org.genemania.plugin.data.IConfirmationHandler;
import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.plugin.view.TextReportDialog;
import org.genemania.plugin.view.util.UiUtils;
import org.genemania.type.DataLayout;

public class LuceneConfirmationHandler implements IConfirmationHandler {
	private final UiUtils uiUtils;
	private final TaskDispatcher taskDispatcher;

	public LuceneConfirmationHandler(UiUtils uiUtils, TaskDispatcher taskDispatcher) {
		this.uiUtils = uiUtils;
		this.taskDispatcher = taskDispatcher;
	}
	
	public boolean acceptPartialImport(DataImportSettings settings, NormalizationResult result) {
		String report;
		String message;
		if (settings.getDataLayout().equals(DataLayout.PROFILE)) {
			report = generateProfileReport(result);
			message = Strings.reportInvalidProfileData;
		} else {
			report = generateNetworkReport(result);
			message = Strings.reportInvalidNetworkData;
		}
		List<String> options = new ArrayList<String>();
		Map<String, Integer> values = new HashMap<String, Integer>();
		
		if (result.getTotalEntries() > 0) {
			message = Strings.reportPrompt_label;
			options.add(Strings.yes_label);
			options.add(Strings.no_label);
			values.put(Strings.yes_label, JOptionPane.YES_OPTION);
			values.put(Strings.no_label, JOptionPane.NO_OPTION);
		} else {
			options.add(Strings.ok_label);
			values.put(Strings.ok_label, JOptionPane.OK_OPTION);
		}
		TextReportDialog<Integer> dialog = new TextReportDialog<Integer>((Dialog) taskDispatcher.getTaskDialog(), message, options, values, report, true, uiUtils);
		dialog.setTitle(Strings.report_title);
		dialog.setVisible(true);
		
		Integer selection = dialog.getSelectedValue();
		if (selection == null) {
			return false;
		}
		return selection == JOptionPane.YES_OPTION && result.getTotalEntries() > 0;
	}

	private String generateNetworkReport(NormalizationResult result) {
		List<String> invalidSymbols = new ArrayList<String>(result.getInvalidSymbols());
		Collections.sort(invalidSymbols);

		StringBuilder builder = new StringBuilder();
		builder.append("<b>"); //$NON-NLS-1$
		builder.append(Strings.reportValidInteractions);
		builder.append("</b> "); //$NON-NLS-1$
		builder.append(result.getTotalEntries());
		builder.append("<br><b>"); //$NON-NLS-1$
		builder.append(Strings.reportInvalidInteractions);
		builder.append("</b> "); //$NON-NLS-1$
		builder.append(result.getDroppedEntries());
		builder.append("<hr>"); //$NON-NLS-1$
		builder.append("<b>"); //$NON-NLS-1$
		builder.append(Strings.reportUnrecognizedGenes);
		builder.append("</b> "); //$NON-NLS-1$
		builder.append(invalidSymbols.size());
		builder.append("<br>"); //$NON-NLS-1$
		builder.append("<ul>"); //$NON-NLS-1$
		for (String symbol : invalidSymbols) {
			builder.append("<li>"); //$NON-NLS-1$
			builder.append(symbol);
			builder.append("</li>"); //$NON-NLS-1$
		}
		builder.append("</ul>"); //$NON-NLS-1$
		return builder.toString();
	}

	private String generateProfileReport(NormalizationResult result) {
		List<String> invalidSymbols = new ArrayList<String>(result.getInvalidSymbols());
		Collections.sort(invalidSymbols);
		
		StringBuilder builder = new StringBuilder();
		builder.append("<b>"); //$NON-NLS-1$
		builder.append(Strings.reportValidRows);
		builder.append("</b> "); //$NON-NLS-1$
		builder.append(result.getTotalEntries());
		builder.append("<br><b>"); //$NON-NLS-1$
		builder.append(Strings.reportInvalidRows);
		builder.append("</b> "); //$NON-NLS-1$
		builder.append(result.getDroppedEntries());
		builder.append("<hr>"); //$NON-NLS-1$
		builder.append("<b>"); //$NON-NLS-1$
		builder.append(Strings.reportUnrecognizedGenes);
		builder.append("</b>"); //$NON-NLS-1$
		builder.append(result.getInvalidSymbols().size());
		builder.append("<br>"); //$NON-NLS-1$
		builder.append("<ul>"); //$NON-NLS-1$
		for (String symbol : invalidSymbols) {
			builder.append("<li>"); //$NON-NLS-1$
			builder.append(symbol);
			builder.append("</li>"); //$NON-NLS-1$
		}
		builder.append("</ul>"); //$NON-NLS-1$
		return builder.toString();
	}

}
