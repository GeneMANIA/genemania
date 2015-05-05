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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.genemania.domain.Organism;
import org.genemania.exception.DataStoreException;
import org.genemania.plugin.FileUtils;
import org.genemania.plugin.LogUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.data.Configuration;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.data.lucene.view.LuceneConfigPanel;
import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.plugin.view.components.WrappedOptionPane;
import org.genemania.plugin.view.util.UiUtils;

public class LuceneConfiguration<NETWORK, NODE, EDGE> extends Configuration {

	private JDialog dialog;
	private final DataSetManager dataSetManager;
	private final UiUtils uiUtils;
	private final FileUtils fileUtils;
	private final CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils;
	private final TaskDispatcher taskDispatcher;

	public LuceneConfiguration(
			final DataSet data,
			final DataSetManager dataSetManager,
			final UiUtils uiUtils,
			final FileUtils fileUtils,
			final CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils,
			final TaskDispatcher taskDispatcher
	) {
		super(data);
		this.dataSetManager = dataSetManager;
		this.uiUtils = uiUtils;
		this.fileUtils = fileUtils;
		this.cytoscapeUtils = cytoscapeUtils;
		this.taskDispatcher = taskDispatcher;
	}
	
	@Override
	public boolean hasUi() {
		return true;
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "serial" })
	public void showUi(Window parent) {
		if (parent instanceof Frame) {
			dialog = new JDialog((Frame) parent, true);
		} else if (parent instanceof Dialog) {
			dialog = new JDialog((Dialog) parent, true);
		} else {
			return;
		}
		
		final LuceneConfigPanel<NETWORK, NODE, EDGE> configPanel = new LuceneConfigPanel<NETWORK, NODE, EDGE>(
				(LuceneDataSet<NETWORK, NODE, EDGE>) data,
				dataSetManager,
				uiUtils,
				fileUtils,
				cytoscapeUtils,
				taskDispatcher
		);
		
		final JButton closeButton = new JButton(new AbstractAction(Strings.closeButton_label) {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				confirmClose(configPanel.getDataSet());
			}
		});
		
		final JPanel buttonPanel = uiUtils.createOkCancelPanel(null, closeButton);
		
		final JPanel contentPane = new JPanel();
		
		final GroupLayout layout = new GroupLayout(contentPane);
		contentPane.setLayout(layout);
		layout.setAutoCreateGaps(uiUtils.isWinLAF());
		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addComponent(configPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(configPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(buttonPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		configPanel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				dialog.pack();
			}
		});
		
		dialog.add(contentPane);
		
		uiUtils.setDefaultOkCancelKeyStrokes(dialog.getRootPane(), closeButton.getAction(), closeButton.getAction());
		dialog.getRootPane().setDefaultButton(closeButton);
		
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				confirmClose(configPanel.getDataSet());
			}
		});
		
		dialog.setTitle(Strings.dataSetConfiguration_title);
		dialog.setLocationByPlatform(true);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setVisible(true);
		
		configPanel.close();
	}
	
	private void confirmClose(DataSet data) {
		try {
			List<Organism> organisms = data.getMediatorProvider().getOrganismMediator().getAllOrganisms();
			
			if (organisms.size() == 0) {
				if (WrappedOptionPane.showConfirmDialog(
						dialog,
						Strings.luceneConfig_error,
						Strings.luceneConfig_title,
						WrappedOptionPane.YES_NO_OPTION,
						WrappedOptionPane.WARNING_MESSAGE,
						WrappedOptionPane.DEFAULT_WIDTH
					) == WrappedOptionPane.YES_OPTION) {
					dialog.setVisible(false);
				}
			} else {
				dialog.setVisible(false);
			}
		} catch (DataStoreException e) {
			LogUtils.log(getClass(), e);
			dialog.setVisible(false);
		}
	}
}
