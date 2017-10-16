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

package org.genemania.plugin.completion;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.TransferHandler;

import org.genemania.completion.CompletionConsumer;
import org.genemania.data.normalizer.GeneCompletionProvider2;
import org.genemania.domain.Gene;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.task.GeneManiaTask;
import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.plugin.view.util.UiUtils;
import org.genemania.util.ProgressReporter;

@SuppressWarnings("serial")
public class CompletionTransferHandler extends TransferHandler {
	
	private final GeneCompletionProvider2 provider;
	private final CompletionConsumer consumer;
	private final NetworkUtils networkUtils;
	private final UiUtils uiUtils;
	private final TaskDispatcher taskDispatcher;

	public CompletionTransferHandler(
			final GeneCompletionProvider2 provider,
			final CompletionConsumer consumer,
			final NetworkUtils networkUtils,
			final UiUtils uiUtils,
			final TaskDispatcher taskDispatcher
	) {
		this.provider = provider;
		this.consumer = consumer;
		this.networkUtils = networkUtils;
		this.uiUtils = uiUtils;
		this.taskDispatcher = taskDispatcher;
	}
	
	@Override
	public boolean importData(final JComponent comp, final Transferable t) {
		final PasteResult[] result = new PasteResult[1];
		
		GeneManiaTask task = new GeneManiaTask(Strings.completionTransferHandler_title2) {
			@Override
			protected void runTask() throws Throwable {
				String data = (String) t.getTransferData(DataFlavor.stringFlavor);
				result[0] = validate(data, progress);
			}
		};
		
		Frame parent = uiUtils.getFrame(comp);
		taskDispatcher.executeTask(task, parent, true, true);
		
		if (result[0] != null && result[0].hasIssues()) {
			showIssues(result[0], parent);
		}
		
		return super.importData(comp, t);
	}

	private JDialog createDialog(Container parent) {
		JDialog dialog = null;
		
		while (parent != null) {
			if (parent instanceof Frame) {
				dialog = new JDialog((Frame) parent, true);
				break;
			} else if (parent instanceof Dialog) {
				dialog = new JDialog((Dialog) parent, true);
				break;
			}
			parent = parent.getParent();
		}
		
		if (dialog == null)
			dialog = new JDialog();
		
		return dialog;
	}
	
	private void showIssues(PasteResult result, Container parent) {
		final JDialog dialog = createDialog(parent);
		dialog.setTitle(Strings.completionTransferHandler_title);
		
		final JButton button = new JButton(new AbstractAction(Strings.completionTransferHandlerOkButton_label) {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		});
		
		final JPanel buttonPanel = uiUtils.createOkCancelPanel(null, button);
		
		final JPanel panel = uiUtils.createJPanel();
		final GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		final SequentialGroup hgroup = layout.createSequentialGroup();
		final ParallelGroup vgroup = layout.createParallelGroup(Alignment.LEADING, true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addGroup(hgroup)
				.addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(vgroup)
				.addComponent(buttonPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);

		final Map<String, Set<String>> synonyms = result.getSynonyms();
		
		if (synonyms.size() > 0) {
			final JLabel label = new JLabel(Strings.completionTransferHandlerSynonyms_label);
			final List<String> symbols = new ArrayList<String>();
			
			for (Entry<String, Set<String>> entry : synonyms.entrySet()) {
				String preferredSymbol = entry.getKey();
				
				for (String symbol : entry.getValue())
					symbols.add(String.format("%s (%s)", symbol, preferredSymbol)); //$NON-NLS-1$
			}
			
			Collections.sort(symbols);
			final JScrollPane scrollPane = new JScrollPane(new JList(new DynamicListModel<String>(symbols)));
			
			hgroup.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(label, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			vgroup.addGroup(layout.createSequentialGroup()
					.addComponent(label, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
		}
		
		final List<String> unrecognizedGenes = new ArrayList<String>(result.getUnrecognizedSymbols());
		
		if (unrecognizedGenes.size() > 0) {
			final JLabel label = new JLabel(Strings.completionTransferHandlerUnrecognizedGenes_label);
			
			Collections.sort(unrecognizedGenes);
			final JScrollPane scrollPane = new JScrollPane(new JList(new DynamicListModel<String>(unrecognizedGenes)));
			
			hgroup.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(label, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			vgroup.addGroup(layout.createSequentialGroup()
					.addComponent(label, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
		}
		
		Map<String, Integer> duplicates = result.getDuplicates();
		if (duplicates.size() > 0) {
			final JLabel label = new JLabel(Strings.completionTransferHandlerDuplicateGenes_label);
			final List<String> duplicateGenes = new ArrayList<String>();
			
			for (Entry<String, Integer> entry : duplicates.entrySet()) {
				duplicateGenes.add(String.format("%s (%d)", entry.getKey(), entry.getValue())); //$NON-NLS-1$
			}
			
			Collections.sort(duplicateGenes);
			final JScrollPane scrollPane = new JScrollPane(new JList(new DynamicListModel<String>(duplicateGenes)));
			
			hgroup.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(label, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			vgroup.addGroup(layout.createSequentialGroup()
					.addComponent(label, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
		}

		dialog.getContentPane().add(panel);
		
		uiUtils.setDefaultOkCancelKeyStrokes(dialog.getRootPane(), button.getAction(), button.getAction());
		dialog.getRootPane().setDefaultButton(button);
		
		dialog.setMinimumSize(new Dimension(300, 300));
		dialog.pack();
		dialog.setLocationByPlatform(true);
		dialog.setVisible(true);
	}

	private PasteResult validate(String data, ProgressReporter progress) {
		StringTokenizer tokenizer = new StringTokenizer(data);
		PasteResult result = new PasteResult(networkUtils);
		int valid = 0;
		
		try {
			while (tokenizer.hasMoreTokens()) {
				if (progress.isCanceled())
					return result;
				
				String token = tokenizer.nextToken();
				result.addDuplicate(token);
				Gene gene = provider.getGene(token);
				
				if (gene != null) {
					result.addSynonym(gene);
					consumer.consume(gene.getSymbol());
					valid += 1;
				} else {
					result.addUnrecognizedSymbol(token);
				}
				
				progress.setStatus(String.format(Strings.completionTransferHandler_status, valid, result.getUnrecognizedSymbols().size()));
			}
		} finally {
			consumer.finish();
		}
		
		return result;
	}
}
