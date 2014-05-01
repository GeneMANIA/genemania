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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
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

	public LuceneConfiguration(DataSet data, DataSetManager dataSetManager, UiUtils uiUtils, FileUtils fileUtils, CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils, TaskDispatcher taskDispatcher) {
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
	
	@SuppressWarnings("unchecked")
	@Override
	public void showUi(Window parent) {
		if (parent instanceof Frame) {
			dialog = new JDialog((Frame) parent, true);
		} else if (parent instanceof Dialog) {
			dialog = new JDialog((Dialog) parent, true);
		} else {
			return;
		}
		
		final LuceneConfigPanel<NETWORK, NODE, EDGE> configPanel = new LuceneConfigPanel<NETWORK, NODE, EDGE>((LuceneDataSet<NETWORK, NODE, EDGE>) data, dataSetManager, uiUtils, fileUtils, cytoscapeUtils, taskDispatcher);
		
		dialog.setTitle(Strings.dataSetConfiguration_title);
		dialog.setPreferredSize(new Dimension(650, 600));
		dialog.setLayout(new GridBagLayout());
		dialog.add(configPanel, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		
		JButton closeButton = new JButton(Strings.closeButton_label);
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				confirmClose(configPanel.getDataSet());
			}
		});
		dialog.add(closeButton, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				confirmClose(configPanel.getDataSet());
			}
		});
		
		dialog.setLocationByPlatform(true);
		dialog.pack();
		dialog.setVisible(true);
		configPanel.close();
	}
	
	private void confirmClose(DataSet data) {
		try {
			List<Organism> organisms = data.getMediatorProvider().getOrganismMediator().getAllOrganisms();
			if (organisms.size() == 0) {
				if (WrappedOptionPane.showConfirmDialog(dialog, Strings.luceneConfig_error, Strings.luceneConfig_title, WrappedOptionPane.YES_NO_OPTION, WrappedOptionPane.WARNING_MESSAGE, WrappedOptionPane.DEFAULT_WIDTH) == WrappedOptionPane.YES_OPTION) {
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
