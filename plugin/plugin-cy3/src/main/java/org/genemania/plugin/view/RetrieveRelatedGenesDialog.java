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
import static org.genemania.plugin.view.util.UiUtils.MISSING_FIELD_COLOR;
import static org.genemania.plugin.view.util.UiUtils.MISSING_FIELD_ICON_CODE;
import static org.genemania.plugin.view.util.UiUtils.MISSING_FIELD_ICON_SIZE;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.text.NumberFormatter;

import org.genemania.data.normalizer.GeneCompletionProvider2;
import org.genemania.domain.AttributeGroup;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.domain.Statistics;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.AttributeMediator;
import org.genemania.mediator.StatsMediator;
import org.genemania.plugin.FileUtils;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.LogUtils;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.apps.IQueryErrorHandler;
import org.genemania.plugin.completion.CompletionPanel;
import org.genemania.plugin.controllers.RetrieveRelatedGenesController;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetChangeListener;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.data.IConfiguration;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.ModelElement;
import org.genemania.plugin.model.Network;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.model.impl.InteractionNetworkGroupImpl;
import org.genemania.plugin.model.impl.QueryAttributeGroupImpl;
import org.genemania.plugin.model.impl.QueryAttributeNetworkImpl;
import org.genemania.plugin.parsers.IQueryParser;
import org.genemania.plugin.parsers.JsonQueryParser;
import org.genemania.plugin.parsers.Query;
import org.genemania.plugin.parsers.WebsiteQueryParser;
import org.genemania.plugin.selection.NetworkSelectionManager;
import org.genemania.plugin.selection.SelectionEvent;
import org.genemania.plugin.selection.SelectionListener;
import org.genemania.plugin.task.GeneManiaTask;
import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.plugin.view.util.CollapsiblePanel;
import org.genemania.plugin.view.util.CollapsiblePanel.CollapseListener;
import org.genemania.plugin.view.util.FileSelectionMode;
import org.genemania.plugin.view.util.UiUtils;
import org.genemania.type.CombiningMethod;
import org.genemania.type.ScoringMethod;
import org.genemania.util.ProgressReporter;

@SuppressWarnings("serial")
public class RetrieveRelatedGenesDialog<NETWORK, NODE, EDGE> extends JDialog {
	
	private JPanel dataPanel;
	private JPanel basicPanel;
	private CollapsiblePanel advancedPanel;
	private CompletionPanel genePanel;
	private JPanel networkPanel;
	private JPanel networkSubPanel;
	private NetworkSelectionPanel selectionPanel;
	private JPanel limitPanel;
	
	private JLabel organismLabel;
	private JLabel geneLabel;
	private JLabel networkLabel;
	private JLabel organismMissingLabel;
	private JLabel geneMissingLabel;
	private JLabel networkMissingLabel;
	
	private Organism selectedOrganism;
	private JComboBox organismComboBox;
	private JTextField limitTextField;
	private JComboBox weightingMethodComboBox;
	
	private JLabel totalOrganismsLabel;
	private JLabel totalNetworksLabel;
	private JLabel totalGenesLabel;
	private JLabel totalInteractionsLabel;
	private JLabel dataVersionLabel;
	
	private JButton startButton;
	private JButton removeGenesButton;
	private JButton clearGenesButton;
	private JButton dataConfigButton;
	private JButton loadParamsButton;
	
	private JFormattedTextField attributeLimitTextField;

	private Map<Long, List<String>> selectedGenes;
	private RetrieveRelatedGenesController<NETWORK, NODE, EDGE> controller;
	private DataSetManager dataSetManager;

	private final NetworkUtils networkUtils;
	private final UiUtils uiUtils;

	private final CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils;

	private final FileUtils fileUtils;
	private final TaskDispatcher taskDispatcher;
	private final GeneMania<NETWORK, NODE, EDGE> plugin;

	public RetrieveRelatedGenesDialog(
			Frame owner,
			boolean modality,
			RetrieveRelatedGenesController<NETWORK, NODE, EDGE> controller,
			DataSetManager dataSetManager,
			NetworkUtils networkUtils,
			UiUtils uiUtils,
			CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils,
			FileUtils fileUtils,
			TaskDispatcher taskDispatcher,
			GeneMania<NETWORK, NODE, EDGE> plugin
	) {
    	super(owner, Strings.default_title, modality);
    	this.controller = controller;
    	this.networkUtils = networkUtils;
    	this.uiUtils = uiUtils;
    	this.cytoscapeUtils = cytoscapeUtils;
    	this.fileUtils = fileUtils;
    	this.taskDispatcher = taskDispatcher;
    	this.plugin = plugin;
    	this.dataSetManager = dataSetManager;
    	
    	selectedGenes = new HashMap<Long, List<String>>();
    	
    	createLabels();
    	
		dataSetManager.addDataSetChangeListener(new DataSetChangeListener() {
			@Override
			public void dataSetChanged(final DataSet dataSet, final ProgressReporter progress) {
				try {
					setDataSet(dataSet);
				} catch (ApplicationException e) {
					LogUtils.log(getClass(), e);
				}
			}
		});

		final JRootPane root = getRootPane();
		final RetrieveRelatedGenesDialog<NETWORK, NODE, EDGE> dialog = this;
		final AbstractAction action = new AbstractAction("Close") { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		};
		final String key = (String) action.getValue(Action.NAME);
		root.getActionMap().put(key, action);
		root.getInputMap(JRootPane.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), key);
		
        addComponents();
        
        try {
        	setDataSet(dataSetManager.getDataSet());
		} catch (ApplicationException e) {
			LogUtils.log(getClass(), e);
		}
        
        setMinimumSize(new Dimension(480, 680));
        pack();
        
        addComponentListener(new ComponentListener() {
        	@Override
			public void componentShown(ComponentEvent e) {
			}
			@Override
			public void componentResized(ComponentEvent e) {
				getGenePanel().handleParentMoved();
			}
			@Override
			public void componentMoved(ComponentEvent e) {
				getGenePanel().handleParentMoved();
			}
			@Override
			public void componentHidden(ComponentEvent e) {
				getGenePanel().hideProposals();
			}
		});
        
        addWindowFocusListener(new WindowFocusListener() {
			@Override
			public void windowLostFocus(WindowEvent event) {
				Window window = event.getOppositeWindow();
				if (window == getGenePanel().getProposalDialog()) {
					return;
				}
				getGenePanel().hideProposals();
			}
			
			@Override
			public void windowGainedFocus(WindowEvent arg0) {
			}
		});
    }

	private void setDataSet(final DataSet data) throws ApplicationException {
		if (data == null) {
			dataVersionLabel.setText(Strings.retrieveRelatedGenesNoDataSet_label);
		} else {
			dataVersionLabel.setText(data.getDescription());
			
	    	try {
	    		Vector<ModelElement<Organism>> model = controller.createModel(data);
	    		getOrganismComboBox().setModel(new DefaultComboBoxModel(model));
			} catch (DataStoreException e) {
				throw new ApplicationException(e);
			}
	    	
			updateStatistics(data);
			getDataConfigButton().setEnabled(data.getConfiguration().hasUi());
		}
		
		handleOrganismSelected();
    }
	
	private void createLabels() {
		organismLabel = new JLabel(Strings.retrieveRelatedGenesOrganism_label);
		organismLabel.setToolTipText(Strings.retrieveRelatedGenesOrganismComboBox_label);
		
		geneLabel = new JLabel(Strings.retrieveRelatedGenesGenePanel_label);
		
		networkLabel = new JLabel(Strings.retrieveRelatedGenesNetworkPanel_label);
		
		organismMissingLabel = uiUtils.createIconLabel(MISSING_FIELD_ICON_CODE, MISSING_FIELD_ICON_SIZE, MISSING_FIELD_COLOR);
		geneMissingLabel = uiUtils.createIconLabel(MISSING_FIELD_ICON_CODE, MISSING_FIELD_ICON_SIZE, MISSING_FIELD_COLOR);
		networkMissingLabel = uiUtils.createIconLabel(MISSING_FIELD_ICON_CODE, MISSING_FIELD_ICON_SIZE, MISSING_FIELD_COLOR);
	}
    
	private void addComponents() {
        final JButton closeButton = new JButton(new AbstractAction(Strings.closeButton_label) {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
        
        final JPanel buttonPanel = uiUtils.createOkCancelPanel(getStartButton(), closeButton);
        
        final JPanel panel = new JPanel();
        panel.setDoubleBuffered(true);
        
        final GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
		layout.setAutoCreateGaps(uiUtils.isWinLAF());
		layout.setAutoCreateContainerGaps(true);
		
		final ParallelGroup hgroup = layout.createParallelGroup(Alignment.CENTER, true);
		final SequentialGroup vgroup = layout.createSequentialGroup();
		
		layout.setHorizontalGroup(hgroup
				.addComponent(getDataPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getBasicPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getAdvancedPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(vgroup
				.addComponent(getDataPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getBasicPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getAdvancedPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(buttonPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		getAdvancedPanel().addCollapseListener(new CollapseListener() {
			@Override
			public void expanded() {
				addMeAgain(false);
				resizeDialog();
			}
			@Override
			public void collapsed() {
				addMeAgain(true);
			}
			private void addMeAgain(boolean collapsed) {
				panel.remove(getAdvancedPanel());
				panel.remove(buttonPanel);
				
				hgroup.addComponent(getAdvancedPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
				hgroup.addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
				
				if (collapsed)
					vgroup.addComponent(getAdvancedPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
				else
					vgroup.addComponent(getAdvancedPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
				
				vgroup.addComponent(buttonPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
			}
		});
		
		getContentPane().add(panel, BorderLayout.CENTER);
		
		uiUtils.setDefaultOkCancelKeyStrokes(getRootPane(), getStartButton().getAction(), closeButton.getAction());
		getRootPane().setDefaultButton(getStartButton());
    }
	
	private JPanel getDataPanel() {
		if (dataPanel == null) {
			dataPanel = uiUtils.createJPanel();
			dataPanel.setToolTipText(Strings.retrieveRelatedGenesStatistics_label);
		    
			Font labelFont = new JLabel().getFont().deriveFont(UiUtils.INFO_FONT_SIZE);
			
			totalOrganismsLabel = createLabel("0", labelFont); //$NON-NLS-1$
			totalOrganismsLabel.setName(Strings.retrieveRelatedGenesStatisticsOrganisms_label);
			
			totalNetworksLabel = createLabel("0", labelFont); //$NON-NLS-1$
			totalNetworksLabel.setName(Strings.retrieveRelatedGenesStatisticsNetworks_label);
			
			totalGenesLabel = createLabel("0", labelFont); //$NON-NLS-1$
			totalGenesLabel.setName(Strings.retrieveRelatedGenesStatisticsGenes_label);
			
			totalInteractionsLabel = createLabel("0", labelFont); //$NON-NLS-1$
			totalInteractionsLabel.setName(Strings.retrieveRelatedGenesStatisticsInteractions_label);
			
			dataVersionLabel = createLabel(Strings.retrieveRelatedGenesStatisticsOrganisms_label, labelFont);
			dataVersionLabel.setName(Strings.retrieveRelatedGenesStatisticsVersion_label);
			
			labelFont = labelFont.deriveFont(Font.BOLD);
			
			final JLabel organismsLabel = createLabel(Strings.retrieveRelatedGenesStatisticsOrganisms_label, labelFont);
			final JLabel networksLabel = createLabel(Strings.retrieveRelatedGenesStatisticsNetworks_label, labelFont);
			final JLabel genesLabel = createLabel(Strings.retrieveRelatedGenesStatisticsGenes_label, labelFont);
			final JLabel interactionsLabel = createLabel(Strings.retrieveRelatedGenesStatisticsInteractions_label, labelFont);
			final JLabel versionLabel = createLabel(Strings.retrieveRelatedGenesStatisticsVersion_label, labelFont);
			
			final JSeparator sep = new JSeparator(JSeparator.VERTICAL);
			
			final GroupLayout layout = new GroupLayout(dataPanel);
			dataPanel.setLayout(layout);
			layout.setAutoCreateGaps(false);
			layout.setAutoCreateContainerGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(organismsLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(totalOrganismsLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(networksLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(totalNetworksLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(genesLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(totalGenesLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(interactionsLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(totalInteractionsLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(versionLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(dataVersionLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(getDataConfigButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(getLoadParamsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, false)
					.addGroup(layout.createSequentialGroup()
							.addComponent(organismsLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(totalOrganismsLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createSequentialGroup()
							.addComponent(networksLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(totalNetworksLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createSequentialGroup()
							.addComponent(genesLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(totalGenesLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createSequentialGroup()
							.addComponent(interactionsLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(totalInteractionsLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createSequentialGroup()
							.addComponent(versionLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(dataVersionLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addComponent(getDataConfigButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(sep, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getLoadParamsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return dataPanel;
	}
	
	private JPanel getBasicPanel() {
		if (basicPanel == null) {
			basicPanel = uiUtils.createJPanel();
			basicPanel.setBorder(uiUtils.createPanelBorder());
			basicPanel.setDoubleBuffered(true);
			
			final JLabel statusField = new JLabel(""); //$NON-NLS-1$
	        statusField.setBorder(BorderFactory.createEmptyBorder());
	        
	        getGenePanel().setProgressReporter(new ProgressReporter() {
	        	@Override
	        	public void setStatus(String status) {
	        		statusField.setText(status);
	        		statusField.invalidate();
	        	}
	        	@Override
				public String getStatus() {
					return statusField.getText();
				}
	        	@Override
				public void cancel() {
				}
	        	@Override
				public int getMaximumProgress() {
					return 0;
				}
	        	@Override
				public int getProgress() {
					return 0;
				}
	        	@Override
				public boolean isCanceled() {
					return false;
				}
	        	@Override
				public void setMaximumProgress(int maximum) {
				}
	        	@Override
				public void setProgress(int progress) {
				}
	        	@Override
				public String getDescription() {
					return null;
				}
	        	@Override
				public void setDescription(String description) {
				}
	        });
			
	        final GroupLayout layout = new GroupLayout(basicPanel);
	        basicPanel.setLayout(layout);
			layout.setAutoCreateGaps(uiUtils.isWinLAF());
			layout.setAutoCreateContainerGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addGroup(layout.createSequentialGroup()
							.addComponent(organismMissingLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(organismLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addComponent(getOrganismComboBox(), 320, 420, PREFERRED_SIZE)
					.addGroup(layout.createSequentialGroup()
							.addComponent(geneMissingLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(geneLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addComponent(getGenePanel(), DEFAULT_SIZE, 800, Short.MAX_VALUE)
					.addGroup(layout.createSequentialGroup()
							.addComponent(statusField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(getRemoveGenesButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getClearGenesButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(organismMissingLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(organismLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addComponent(getOrganismComboBox(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(geneMissingLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(geneLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addComponent(getGenePanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(statusField)
							.addComponent(getRemoveGenesButton())
							.addComponent(getClearGenesButton())
					)
			);
			
			uiUtils.equalizeSize(getRemoveGenesButton(), getClearGenesButton());
		}
		
		return basicPanel;
	}
	
	private CollapsiblePanel getAdvancedPanel() {
		if (advancedPanel == null) {
			advancedPanel = new CollapsiblePanel(Strings.advancedOptionsPanel_title, true, uiUtils);
			
			final JSeparator sep = new JSeparator();
			
			final GroupLayout layout = new GroupLayout(advancedPanel.getContentPane());
			advancedPanel.getContentPane().setLayout(layout);
			layout.setAutoCreateGaps(uiUtils.isWinLAF());
			layout.setAutoCreateContainerGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(getNetworkPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(sep, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getLimitPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getNetworkPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getLimitPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return advancedPanel;
	}
    
	private CompletionPanel getGenePanel() {
		if (genePanel == null) {
			genePanel = new CompletionPanel(2, networkUtils, uiUtils, taskDispatcher);
			genePanel.setDoubleBuffered(true);
			
			final int maxWidth = Math.max(genePanel.getPreferredSize().width, getLimitPanel().getPreferredSize().width);
			genePanel.setMinimumSize(new Dimension(maxWidth, genePanel.getMinimumSize().height));
	        
	        genePanel.addTableModelEventListener(new TableModelListener() {
	        	@Override
	        	public void tableChanged(TableModelEvent e) {
	        		validateQuery();
	        	}
	        });
	        genePanel.addListSelectionListener(new ListSelectionListener() {
	        	@Override
		        public void valueChanged(ListSelectionEvent e) {
		        	validateQuery();
		        }
	        });
		}
		
		return genePanel;
	}
	
	private JPanel getNetworkPanel() {
		if (networkPanel == null) {
	        final JEditorPane selectionLabel = uiUtils.createEditorPane(Strings.retrieveRelatedGenesNetworkPanelSelection_label);
	        selectionLabel.addHyperlinkListener(new HyperlinkListener() {
	        	@Override
				public void hyperlinkUpdate(HyperlinkEvent e) {
					handleLink(e);
				}
			});
	        
	        networkPanel = uiUtils.createJPanel();
	        networkPanel.setToolTipText(Strings.retrieveRelatedGenesNetworkPanel_tooltip);
	        networkPanel.setDoubleBuffered(true);
	        
	        final GroupLayout layout = new GroupLayout(networkPanel);
	        networkPanel.setLayout(layout);
			layout.setAutoCreateGaps(uiUtils.isWinLAF());
			layout.setAutoCreateContainerGaps(false);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addGroup(layout.createSequentialGroup()
							.addComponent(networkMissingLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(networkLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addComponent(getNetworkSubPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(selectionLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(networkMissingLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(networkLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addComponent(getNetworkSubPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(selectionLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
        return networkPanel;
    }
	
	private JPanel getNetworkSubPanel() {
		if (networkSubPanel == null) {
			networkSubPanel = uiUtils.createJPanel();
	        networkSubPanel.setLayout(new GridBagLayout());
		}
		
		return networkSubPanel;
	}
	
	private JPanel getLimitPanel() {
		if (limitPanel == null) {
			limitPanel = uiUtils.createJPanel();
			
			final JLabel lbl1 = new JLabel(Strings.retrieveRelatedGenes_label);
			final JLabel lbl2 = new JLabel(Strings.retrieveRelatedGenes_label2);
			final JLabel lbl5 = new JLabel(Strings.retrieveRelatedGenes_label5);
			final JLabel lbl6 = new JLabel(Strings.retrieveRelatedGenes_label6);
			final JLabel lbl3 = new JLabel(Strings.retrieveRelatedGenes_label3);
			final JLabel lbl4 = new JLabel(Strings.retrieveRelatedGenes_label4);
	        
	        final GroupLayout layout = new GroupLayout(limitPanel);
	        limitPanel.setLayout(layout);
			layout.setAutoCreateGaps(uiUtils.isWinLAF());
			layout.setAutoCreateContainerGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addComponent(lbl1)
					.addComponent(getLimitTextField(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(lbl2)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lbl5)
					.addComponent(getAttributeLimitTextField(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(lbl6)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lbl3)
					.addComponent(getWeightingMethodComboBox(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(lbl4)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, false)
					.addComponent(lbl1)
					.addComponent(getLimitTextField())
					.addComponent(lbl2)
					.addComponent(lbl5)
					.addComponent(getAttributeLimitTextField())
					.addComponent(lbl6)
					.addComponent(lbl3)
					.addComponent(getWeightingMethodComboBox())
					.addComponent(lbl4)
			);
		}
		
		return limitPanel;
	}
	
	private JComboBox getOrganismComboBox() {
		if (organismComboBox == null) {
	        organismComboBox = new JComboBox();
	        organismComboBox.setToolTipText(Strings.retrieveRelatedGenesOrganismComboBox_label);
	        
	        organismComboBox.addActionListener(new ActionListener() {
	        	@Override
				public void actionPerformed(ActionEvent event) {
					handleOrganismSelected();
				}
	        });
		}
		
		return organismComboBox;
	}
	
	private JButton getRemoveGenesButton() {
		if (removeGenesButton == null) {
			removeGenesButton = new JButton(Strings.retrieveRelatedGenesRemoveGeneButton_label);
	        removeGenesButton.addActionListener(new ActionListener() {
	        	@Override
				public void actionPerformed(ActionEvent e) {
					genePanel.removeSelection();
					validateQuery();
				}
	        });
	        removeGenesButton.putClientProperty("JComponent.sizeVariant", "small"); // Mac OS X only
		}
		
		return removeGenesButton;
	}
	
	private JButton getClearGenesButton() {
		if (clearGenesButton == null) {
			clearGenesButton = new JButton(Strings.retrieveRelatedGenesClearGenesButton_label);
	        clearGenesButton.addActionListener(new ActionListener() {
	        	@Override
				public void actionPerformed(ActionEvent e) {
					handleClearButton();
				}
	        });
	        clearGenesButton.putClientProperty("JComponent.sizeVariant", "small"); // Mac OS X only
		}
		
		return clearGenesButton;
	}
	
	private JButton getDataConfigButton() {
		if (dataConfigButton == null) {
			dataConfigButton = new JButton(Strings.dataSetConfigurationButton_label);
			dataConfigButton.putClientProperty("JComponent.sizeVariant", "small"); // Mac OS X only
			
			dataConfigButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					handleConfigureButton();
				}
			});
		}
		
		return dataConfigButton;
	}
	
	private JButton getLoadParamsButton() {
		if (loadParamsButton == null) {
			loadParamsButton = new JButton(Strings.retrieveRelatedGenesLoadParametersButton_label);
			loadParamsButton.putClientProperty("JComponent.sizeVariant", "small"); // Mac OS X only
			
			loadParamsButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					handleChooseFile();
				}
			});
		}
		
		return loadParamsButton;
	}
	
	private JTextField getLimitTextField() {
		if (limitTextField == null) {
			limitTextField = new JFormattedTextField(new NumberFormatter(new DecimalFormat("#"))); //$NON-NLS-1$
	        limitTextField.setText("20"); //$NON-NLS-1$
	        limitTextField.setColumns(4);
	        limitTextField.addKeyListener(new KeyAdapter() {
	        	@Override
	        	public void keyReleased(KeyEvent e) {
	        		validateQuery();
	        	}
			});
		}
		
		return limitTextField;
	}
	
	private JFormattedTextField getAttributeLimitTextField() {
		if (attributeLimitTextField == null) {
			attributeLimitTextField = new JFormattedTextField(new NumberFormatter(new DecimalFormat("#"))); //$NON-NLS-1$
	        attributeLimitTextField.setText("20"); //$NON-NLS-1$
	        attributeLimitTextField.setColumns(4);
	        attributeLimitTextField.addKeyListener(new KeyAdapter() {
	        	@Override
	        	public void keyReleased(KeyEvent e) {
	        		validateQuery();
	        	}
			});
		}
		
		return attributeLimitTextField;
	}
	
	private JButton getStartButton() {
		if (startButton == null) {
			 startButton = new JButton(new AbstractAction(Strings.retrieveRelatedGenesStartButton_label) {
					@Override
					public void actionPerformed(ActionEvent e) {
						handleStartButton();
					}
				});
			 startButton.setMinimumSize(new Dimension(180, startButton.getMinimumSize().height));
		}
		
 		return startButton;
	}
	
	private JComboBox getWeightingMethodComboBox() {
		if (weightingMethodComboBox == null) {
			weightingMethodComboBox = new JComboBox();
		}
		
		return weightingMethodComboBox;
	}
    
	protected Query parseQuery(DataSet data, File file, IQueryErrorHandler handler) throws ApplicationException {
		IQueryParser[] parsers = new IQueryParser[] { new JsonQueryParser(), new WebsiteQueryParser() };
		for (IQueryParser parser : parsers) {
			try {
				// TODO: Assume UTF-8 for now
				Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8"); //$NON-NLS-1$
				return parser.parse(data, reader, handler);
			} catch (Exception e) {
			}
		}
		throw new ApplicationException(Strings.retrieveRelatedGenesChooseFile_error);
	}

	private void handleClearButton() {
		getGenePanel().clear();
		validateQuery();
	}
	
    @SuppressWarnings("unchecked")
	private void handleOrganismSelected() {
		try {
			final ModelElement<Organism> element = (ModelElement<Organism>) getOrganismComboBox().getSelectedItem();
			
			if (element != null)
				handleOrganismChange(element.getItem());
			else
				handleOrganismChange(null);
		} catch (ApplicationException e) {
			LogUtils.log(getClass(), e);
		}
		
		validateQuery();
	}

	private void handleLink(HyperlinkEvent e) {
		if (e.getEventType() != EventType.ACTIVATED)
			return;
		if (selectionPanel == null)
			return;
		
		String reference = e.getDescription();
		
		if ("#en-all".equals(reference)) { //$NON-NLS-1$
			selectionPanel.selectAllNetworks(true);
		} else if ("#en-none".equals(reference)) { //$NON-NLS-1$
			selectionPanel.selectAllNetworks(false);
		} else if ("#en-default".equals(reference)) { //$NON-NLS-1$
			selectionPanel.selectDefaultNetworks();
		}
		
		validateQuery();
	}

	private Query getQuery() {
		final Query query = new Query();
		query.setOrganism(selectedOrganism);
		query.setGenes(getGenePanel().getItems());
		query.setGeneLimit(getLimit(getLimitTextField()));
		query.setAttributeLimit(getLimit(getAttributeLimitTextField()));
		query.setCombiningMethod(getCombiningMethod());
		query.setScoringMethod(getScoringMethod());
		
		return query;
	}
	
	private void handleStartButton() {
		Query query = getQuery();
		Collection<Group<?, ?>> groups = selectionPanel.getSelectedGroups();
		NETWORK cyNetwork = controller.runMania(this, query, groups, true);

		cytoscapeUtils.handleNetworkPostProcessing(cyNetwork);
		cytoscapeUtils.performLayout(cyNetwork);
		cytoscapeUtils.maximize(cyNetwork);
		
		NetworkSelectionManager<NETWORK, NODE, EDGE> manager = plugin.getNetworkSelectionManager();
		ViewState options = manager.getNetworkConfiguration(cyNetwork);
		plugin.applyOptions(options);
		plugin.showResults();
	}

	private void validateQuery() {
		if (selectedOrganism != null) {
			boolean hasGenes = getGenePanel().getItemCount() > 0;
			boolean hasNetworks = selectionPanel.getSelectionCount() > 0;
			
			setControlsEnabled(true);
			Status status = checkQueryStatus();
			getStartButton().setEnabled(status == Status.Ok);
			getClearGenesButton().setEnabled(hasGenes);
			getRemoveGenesButton().setEnabled(getGenePanel().getSelectionCount() > 0);			

			organismMissingLabel.setVisible(false);
			geneMissingLabel.setVisible(hasGenes ? false : true);
			networkMissingLabel.setVisible(hasNetworks ? false : true);
		} else {
			setControlsEnabled(false);
			organismMissingLabel.setVisible(true);
			networkMissingLabel.setVisible(true);
			geneMissingLabel.setVisible(true);
		}
	}
	
	private void setControlsEnabled(boolean enabled) {
		getOrganismComboBox().setEnabled(enabled);
		getGenePanel().setEnabled(enabled);
		getRemoveGenesButton().setEnabled(enabled);
		getClearGenesButton().setEnabled(enabled);
		getStartButton().setEnabled(enabled);
		getLoadParamsButton().setEnabled(enabled);
		repaint();
	}

	private Status checkQueryStatus() {
		if (selectedOrganism == null) {
			return Status.NoOrganismSelected;
		}
		
		if (getLimit(getLimitTextField()) < 0) {
			return Status.LimitViolation;
		}

		if (getLimit(getAttributeLimitTextField()) < 0) {
			return Status.LimitViolation;
		}
		
		List<String> geneNames = getGenePanel().getItems();
		if (geneNames.size() < 1) {
			return Status.MinimumQuerySizeViolation;
		}
		
		int selectedNetworks = selectionPanel.getSelectionCount();
		if (selectedNetworks == 0) {
			return Status.MinimumNetworkSelectionViolation;
		}
		
		return Status.Ok;
	}

	private void handleOrganismChange(Organism organism) throws ApplicationException {
		boolean selectionChanged = (selectedOrganism != null && organism != null && selectedOrganism.getId() != organism.getId())
			|| (selectedOrganism == null && organism != null)
			|| (selectedOrganism != null && organism == null);

		updateCombiningMethods(organism);
		
		getNetworkSubPanel().removeAll();
		
		List<String> genes = null;
		if (selectionChanged) {
			if (selectedOrganism != null) {
				selectedGenes.put(selectedOrganism.getId(), getGenePanel().getItems());
			}
			if (organism != null) {
				genes = selectedGenes.get(organism.getId());
			}
			getGenePanel().clear();
		}

		selectedOrganism = organism;
		if (organism == null) {
			return;
		}
		
		DataSet data = dataSetManager.getDataSet();

		if (data == null) {
			return;
		}
		
		GeneCompletionProvider2 provider = data.getCompletionProvider(selectedOrganism);
		getGenePanel().setProvider(provider);

		if (genes != null) {
			getGenePanel().setItems(genes);
		}
		
		List<Group<?, ?>> sortedGroups = new ArrayList<Group<?, ?>>();

		for (InteractionNetworkGroup group : organism.getInteractionNetworkGroups()) {
			sortedGroups.add(new InteractionNetworkGroupImpl(group));
		}
		
		AttributeMediator mediator = data.getMediatorProvider().getAttributeMediator();
		Collection<Network<AttributeGroup>> networks = new ArrayList<Network<AttributeGroup>>();
		for (AttributeGroup group : mediator.findAttributeGroupsByOrganism(organism.getId())) {
			networks.add(new QueryAttributeNetworkImpl(group, 0));
		}
		if (networks.size() > 0) {
			sortedGroups.add(new QueryAttributeGroupImpl(networks));
		}
		
		Collections.sort(sortedGroups, networkUtils.getNetworkGroupComparator());

		selectionPanel = new NetworkSelectionPanel(networkUtils, uiUtils);
		selectionPanel.addListener(new SelectionListener<Object>() {
			@Override
			public void selectionChanged(SelectionEvent<Object> event) {
				validateQuery();
			}
		});
		getNetworkSubPanel().add(selectionPanel, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		selectionPanel.setGroups(sortedGroups);
		
		validate();
        validateQuery();
	}

	private void handleConfigureButton() {
		DataSet data = dataSetManager.getDataSet();
		
		if (data == null)
			return;
		
		IConfiguration config = data.getConfiguration();
		
		if (config.hasUi())
			config.showUi(this);
	}
	
	private void updateCombiningMethods(Organism organism) {
		final JComboBox cmb = getWeightingMethodComboBox();
		cmb.removeAllItems();
		
		if (organism == null)
			return;
		
		boolean hasAnnotations = organism.getOntology() != null;
		
        cmb.addItem(new WeightingMethodEntry(CombiningMethod.AUTOMATIC_SELECT, Strings.default_combining_method));
        cmb.addItem(new WeightingMethodEntry(CombiningMethod.AUTOMATIC, Strings.automatic));
        
        if (hasAnnotations) {
	        cmb.addItem(new WeightingMethodEntry(CombiningMethod.BP, Strings.bp));
	        cmb.addItem(new WeightingMethodEntry(CombiningMethod.MF, Strings.mf));
	        cmb.addItem(new WeightingMethodEntry(CombiningMethod.CC, Strings.cc));
        }
        
        cmb.addItem(new WeightingMethodEntry(CombiningMethod.AVERAGE, Strings.average));
        cmb.addItem(new WeightingMethodEntry(CombiningMethod.AVERAGE_CATEGORY, Strings.average_category));
	}

	CombiningMethod getCombiningMethod() {
		return ((WeightingMethodEntry) getWeightingMethodComboBox().getSelectedItem()).getMethod();
	}
	
	ScoringMethod getScoringMethod() {
		return ScoringMethod.DISCRIMINANT;
	}

	int getLimit(JTextField textField) {
		try {
			return Integer.parseInt(textField.getText());
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	
    private void handleChooseFile() {
    	HashSet<String> extensions = new HashSet<String>();
    	extensions.add("json"); //$NON-NLS-1$
		File initialFile = fileUtils.getUserHome();
		final File file;
		
		try {
			file = uiUtils.getFile(
					this,
					Strings.retrieveRelatedGenesChooseFile_title,
					initialFile,
					Strings.jsonDescription,
					extensions,
					FileSelectionMode.OPEN_FILE
			);
		} catch (ApplicationException e) {
			LogUtils.log(getClass(), e);
			return;
		}
		
		if (file == null)
			return;
		
    	GeneManiaTask task = new GeneManiaTask(Strings.retrieveRelatedGenesChooseFile_title) {
			@Override
			protected void runTask() throws Throwable {
				progress.setStatus(Strings.retrieveRelatedGenesChooseFile_status);
				DataSet data = dataSetManager.getDataSet();
				
				IQueryErrorHandler handler = new IQueryErrorHandler() {
					@Override
					public void warn(String message) {
					}
					@Override
					public void handleUnrecognizedGene(String gene) {
					}
					@Override
					public void handleSynonym(String gene) {
					}
					@Override
					public void handleNetwork(InteractionNetwork network) {
					}
					@Override
					public void handleUnrecognizedNetwork(String network) {
					}
				};
				
				final Query query = parseQuery(data, file, handler);
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						try {
							applyQuery(query);
						} catch (ApplicationException e) {
							throw new RuntimeException(e);
						}
					}
				});
			}
		};
		taskDispatcher.executeTask(task, this, true, false);
	}
	
	@SuppressWarnings("unchecked")
	private void applyQuery(Query query) throws ApplicationException {
		Organism organism = query.getOrganism();
		ComboBoxModel model = getOrganismComboBox().getModel();		
		
		for (int i = 0; i < model.getSize(); i++) {
			ModelElement<Organism> element = (ModelElement<Organism>) model.getElementAt(i);
			Organism o = element.getItem();
			
			if (organism.getId() == o.getId()) {
				model.setSelectedItem(element);
				handleOrganismChange(o);
				break;
			}
		}

		getGenePanel().setItems(query.getGenes());
		selectionPanel.setSelection(query);
		getLimitTextField().setText(String.valueOf(query.getGeneLimit()));
		
		ComboBoxModel weightingMethodModel = getWeightingMethodComboBox().getModel();
		CombiningMethod combiningMethod = query.getCombiningMethod();
		
		for (int i = 0; i < weightingMethodModel.getSize(); i++) {
			WeightingMethodEntry entry = (WeightingMethodEntry) weightingMethodModel.getElementAt(i);
			
			if (entry.getMethod().equals(combiningMethod)) {
				weightingMethodModel.setSelectedItem(entry);
				break;
			}
		}
		
		validateQuery();
	}
	
	private JLabel createLabel(String message, Font font) {
		final JLabel label = new JLabel(message);
		label.setFont(font);
		
		return label;
	}
	
	public void updateStatistics(DataSet data) {
		final StatsMediator mediator = data.getMediatorProvider().getStatsMediator();
		final Statistics statistics = mediator.getLatestStatistics();
		totalGenesLabel.setText(String.valueOf(statistics.getGenes()));
		totalInteractionsLabel.setText(String.valueOf(statistics.getInteractions()));
		totalNetworksLabel.setText(String.valueOf(statistics.getNetworks()));
		totalOrganismsLabel.setText(String.valueOf(statistics.getOrganisms()));
	}
	
	private void resizeDialog() {
		final Dimension size = this.getSize();
		final Dimension prefSize = this.getPreferredSize();
		this.pack();
		
		int w = Math.max((size != null ? size.width : 0), prefSize.width);
		int h = Math.max((size != null ? size.height : 0), prefSize.height);
		this.setSize(new Dimension(w, h));
	}
	
	static class WeightingMethodEntry {
		CombiningMethod method;
		String description;
		
		public WeightingMethodEntry(CombiningMethod method, String description) {
			this.method = method;
			this.description = description;
		}
		
		public CombiningMethod getMethod() {
			return method;
		}

		@Override
		public String toString() {
			return description;
		}
	}
	
	enum Status {
		Ok,
		NoOrganismSelected,
		MinimumQuerySizeViolation,
		MinimumNetworkSelectionViolation,
		LimitViolation,
	}
}
