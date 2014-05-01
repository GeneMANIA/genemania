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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
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
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.text.DefaultEditorKit;
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
import org.genemania.plugin.view.util.FileSelectionMode;
import org.genemania.plugin.view.util.UiUtils;
import org.genemania.type.CombiningMethod;
import org.genemania.type.ScoringMethod;
import org.genemania.util.ProgressReporter;

public class RetrieveRelatedGenesDialog<NETWORK, NODE, EDGE> extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private JPanel networkSubPanel;
	private Organism selectedOrganism;

	private JComboBox organismComboBox;

	private JTextField limitTextField;

	private JComboBox weightingMethodComboBox;

	private JLabel dataSourceLabel;
	private CompletionPanel genePanel;
	private JLabel totalOrganismsLabel;
	private JLabel totalNetworksLabel;
	private JLabel totalGenesLabel;
	private JLabel totalInteractionsLabel;
	private JButton startButton;
	private JButton removeButton;
	private JButton clearButton;
	private NetworkSelectionPanel selectionPanel;
	private JButton configureButton;
	private JPanel networkPanel;
	private JButton chooseFileButton;

	private Map<Long, List<String>> selectedGenes;
	private RetrieveRelatedGenesController<NETWORK, NODE, EDGE> controller;
	private DataSetManager dataSetManager;

	private TitledBorder networkBorder;
	private TitledBorder geneBorder;
	private TitledBorder organismBorder;
	private TitledBorder loadQueryBorder;

	private final NetworkUtils networkUtils;

	private final UiUtils uiUtils;

	private final CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils;

	private final FileUtils fileUtils;

	private final TaskDispatcher taskDispatcher;

	private final GeneMania<NETWORK, NODE, EDGE> plugin;

	private JFormattedTextField attributeLimitTextField;

    @SuppressWarnings("serial")
	public RetrieveRelatedGenesDialog(Frame owner, boolean modality, RetrieveRelatedGenesController<NETWORK, NODE, EDGE> controller, DataSetManager dataSetManager, NetworkUtils networkUtils, UiUtils uiUtils, CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils, FileUtils fileUtils, TaskDispatcher taskDispatcher, GeneMania<NETWORK, NODE, EDGE> plugin) {
    	super(owner, Strings.default_title, modality);
    	this.controller = controller;
    	this.networkUtils = networkUtils;
    	this.uiUtils = uiUtils;
    	this.cytoscapeUtils = cytoscapeUtils;
    	this.fileUtils = fileUtils;
    	this.taskDispatcher = taskDispatcher;
    	this.plugin = plugin;
    	
    	selectedGenes = new HashMap<Long, List<String>>();
    	this.dataSetManager = dataSetManager;
		dataSetManager.addDataSetChangeListener(new DataSetChangeListener() {
			public void dataSetChanged(DataSet activeDataSet, ProgressReporter progress) {
				handleDataSetChanged(activeDataSet);
			}
		});

		JRootPane root = getRootPane();
		final RetrieveRelatedGenesDialog<NETWORK, NODE, EDGE> dialog = this;
		AbstractAction action = new AbstractAction("Close") { //$NON-NLS-1$
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		};
		String key = (String) action.getValue(Action.NAME);
		root.getActionMap().put(key, action);
		root.getInputMap(JRootPane.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), key);
		
    	Container contents = getContentPane();
        contents.setLayout(new BoxLayout(contents, BoxLayout.PAGE_AXIS));
        addComponents();
        
        addComponentListener(new ComponentListener() {
			public void componentShown(ComponentEvent e) {
			}
			
			public void componentResized(ComponentEvent e) {
				genePanel.handleParentMoved();
			}
			
			public void componentMoved(ComponentEvent e) {
				genePanel.handleParentMoved();
			}
			
			public void componentHidden(ComponentEvent e) {
				genePanel.hideProposals();
			}
		});
        
        addWindowFocusListener(new WindowFocusListener() {
			@Override
			public void windowLostFocus(WindowEvent event) {
				Window window = event.getOppositeWindow();
				if (window == genePanel.getProposalDialog()) {
					return;
				}
				genePanel.hideProposals();
			}
			
			@Override
			public void windowGainedFocus(WindowEvent arg0) {
			}
		});
        
        createMenu();
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();        
        JMenu editMenu = new JMenu(Strings.edit_menuLabel);
        JMenuItem pasteMenu = new JMenuItem(Strings.paste_menuLabel);
        pasteMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				genePanel.requestFocus();
				new DefaultEditorKit.PasteAction().actionPerformed(event);
			}
		});
		editMenu.add(pasteMenu);
        menuBar.add(editMenu);
        
        setJMenuBar(menuBar);
	}

	protected void handleDataSetChanged(DataSet dataSet) {
		try {
			setDataSet(dataSet);
		} catch (ApplicationException e) {
			LogUtils.log(getClass(), e);
		}
	}

	public void setDataSet(DataSet data) throws ApplicationException {
		if (data == null) {
			dataSourceLabel.setText(Strings.retrieveRelatedGenesNoDataSet_label);
		} else {
			dataSourceLabel.setText(data.getDescription());
	    	try {
	    		Vector<ModelElement<Organism>> model = controller.createModel(data);
	    		organismComboBox.setModel(new DefaultComboBoxModel(model));
			} catch (DataStoreException e) {
				throw new ApplicationException(e);
			}
			updateStatistics(data);
			configureButton.setEnabled(data.getConfiguration().hasUi());
		}
		
		handleOrganismSelected();
    }
    
    private JPanel createOrganismSelector() {
    	JPanel header = uiUtils.createJPanel();
    	organismBorder = BorderFactory.createTitledBorder(Strings.retrieveRelatedGenesOrganism_label);
    	header.setBorder(organismBorder);
    	header.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        String tipText = Strings.retrieveRelatedGenesOrganismComboBox_label;
        header.setToolTipText(tipText);
    	
        //organism combo box
        organismComboBox = new JComboBox();
        organismComboBox.setToolTipText(tipText);
        header.add(organismComboBox);
        
        organismComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				handleOrganismSelected();
			}
        });
        
        return header;
    }
    
    @SuppressWarnings("unchecked")
	private void handleOrganismSelected() {
		try {
			ModelElement<Organism> element = (ModelElement<Organism>) organismComboBox.getSelectedItem();
			if (element != null) {
				handleOrganismChange(element.getItem());
			} else {
				handleOrganismChange(null);
			}
		} catch (ApplicationException e) {
			LogUtils.log(getClass(), e);
		}
		validateQuery();
	}

	private JComponent createGeneAndNetworkPane() {
		JPanel panel = uiUtils.createJPanel();
		panel.setLayout(new GridBagLayout());
		
		JPanel organismSelectionPanel = createOrganismSelector();
		panel.add(organismSelectionPanel, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		JPanel loadQueryPanel = uiUtils.createJPanel();
		loadQueryPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		loadQueryBorder = BorderFactory.createTitledBorder(Strings.retrieveRelatedGenesLoadParameters_label);
		loadQueryPanel.setBorder(loadQueryBorder);
		chooseFileButton = new JButton(Strings.retrieveRelatedGenesChooseFileButton_label);
		chooseFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleChooseFile();
			}
		});
		loadQueryPanel.add(chooseFileButton);
		panel.add(loadQueryPanel, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		
		JPanel geneSelectionPanel = uiUtils.createJPanel();
		geneBorder = BorderFactory.createTitledBorder(Strings.retrieveRelatedGenesGenePanel_label);
        geneSelectionPanel.setBorder(geneBorder);
        geneSelectionPanel.setLayout(new GridBagLayout());
        
        genePanel = new CompletionPanel(2, networkUtils, uiUtils, taskDispatcher);
        
        geneSelectionPanel.add(genePanel, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        
        JPanel buttonPanel = uiUtils.createJPanel();
        buttonPanel.setLayout(new GridBagLayout());
        
        final JLabel statusField = new JLabel(""); //$NON-NLS-1$
        statusField.setBorder(BorderFactory.createEmptyBorder());
        buttonPanel.add(statusField, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        
        genePanel.setProgressReporter(new ProgressReporter() {
        	public void setStatus(String status) {
        		statusField.setText(status);
        		statusField.invalidate();
        	}

			public String getStatus() {
				return statusField.getText();
			}

			public void cancel() {
			}

			public int getMaximumProgress() {
				return 0;
			}

			public int getProgress() {
				return 0;
			}

			public boolean isCanceled() {
				return false;
			}

			public void setMaximumProgress(int maximum) {
			}

			public void setProgress(int progress) {
			}

			public String getDescription() {
				return null;
			}

			public void setDescription(String description) {
			}
        });
        
        removeButton = new JButton(Strings.retrieveRelatedGenesRemoveGeneButton_label);
        removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				genePanel.removeSelection();
				validateQuery();
			}
        });
        buttonPanel.add(removeButton, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        
        clearButton = new JButton(Strings.retrieveRelatedGenesClearGenesButton_label);
        clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleClearButton();
			}
        });
        buttonPanel.add(clearButton, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        
        genePanel.addTableModelEventListener(new TableModelListener() {
        	public void tableChanged(TableModelEvent e) {
        		validateQuery();
        	}
        });
        
        genePanel.addListSelectionListener(new ListSelectionListener() {
	        public void valueChanged(ListSelectionEvent e) {
	        	validateQuery();
	        }
        });
        
        geneSelectionPanel.add(buttonPanel, new GridBagConstraints(0, 2, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        
		panel.add(geneSelectionPanel, new GridBagConstraints(0, 1, 2, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

        networkPanel = createNetworkPanel();
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panel, networkPanel);
        splitPane.setAlignmentX(CENTER_ALIGNMENT);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        
        return splitPane;
    }
    
    private void handleChooseFile() {
    	HashSet<String> extensions = new HashSet<String>();
    	extensions.add("json"); //$NON-NLS-1$
		File initialFile = fileUtils.getUserHome();
		final File file;
		try {
			file = uiUtils.getFile(this, Strings.retrieveRelatedGenesChooseFile_title, initialFile, Strings.jsonDescription, extensions, FileSelectionMode.OPEN_FILE);
		} catch (ApplicationException e) {
			LogUtils.log(getClass(), e);
			return;
		}
		if (file == null) {
			return;
		}
    	GeneManiaTask task = new GeneManiaTask(Strings.retrieveRelatedGenesChooseFile_title) {
			@Override
			protected void runTask() throws Throwable {
				progress.setStatus(Strings.retrieveRelatedGenesChooseFile_status);
				DataSet data = dataSetManager.getDataSet();
				IQueryErrorHandler handler = new IQueryErrorHandler() {
					public void warn(String message) {
					}
					
					public void handleUnrecognizedGene(String gene) {
					}
					
					public void handleSynonym(String gene) {
					}
					
					public void handleNetwork(InteractionNetwork network) {
					}
					
					public void handleUnrecognizedNetwork(String network) {
					}
				};
				final Query query = parseQuery(data, file, handler);
				SwingUtilities.invokeAndWait(new Runnable() {
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

	@SuppressWarnings("unchecked")
	private void applyQuery(Query query) throws ApplicationException {
		Organism organism = query.getOrganism();
		ComboBoxModel model = organismComboBox.getModel();		
		for (int i = 0; i < model.getSize(); i++) {
			ModelElement<Organism> element = (ModelElement<Organism>) model.getElementAt(i);
			Organism o = element.getItem();
			if (organism.getId() == o.getId()) {
				model.setSelectedItem(element);
				handleOrganismChange(o);
				break;
			}
		}

		genePanel.setItems(query.getGenes());
		selectionPanel.setSelection(query);
		limitTextField.setText(String.valueOf(query.getGeneLimit()));
		
		ComboBoxModel weightingMethodModel = weightingMethodComboBox.getModel();
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

	private void handleClearButton() {
		genePanel.clear();
		validateQuery();
	}

	private JPanel createNetworkPanel() {
        JPanel networkPanel = uiUtils.createJPanel();
        networkBorder = BorderFactory.createTitledBorder(Strings.retrieveRelatedGenesNetworkPanel_label);
        networkPanel.setBorder(networkBorder);
        networkPanel.setLayout(new GridBagLayout());
        
        JEditorPane selectionLabel = uiUtils.createEditorPane(Strings.retrieveRelatedGenesNetworkPanelSelection_label);
        selectionLabel.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				handleLink(e);
			}
		});
        networkPanel.add(selectionLabel, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        
        networkPanel.setToolTipText(Strings.retrieveRelatedGenesNetworkPanel_tooltip);
        networkSubPanel = uiUtils.createJPanel();
        networkSubPanel.setLayout(new GridBagLayout());
                
        networkPanel.add(networkSubPanel, new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        return networkPanel;
    }
    
	private void handleLink(HyperlinkEvent e) {
		if (e.getEventType() != EventType.ACTIVATED) {
			return;
		}
		if (selectionPanel == null) {
			return;
		}
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

	private void addLabel(Container component, String message, int row, int column) {
    	JLabel label = new JLabel(message);
    	label.setAlignmentX(CENTER_ALIGNMENT);
    	Insets insets = new Insets(0, 2, 0, 2);
		component.add(label, new GridBagConstraints(row, column, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets, 0, 0));
	}

	private void addComponents() {
        Container contents = getContentPane();
        contents.setLayout(new GridBagLayout());
        Insets insets = new Insets(0, 0, 0, 0);
		contents.add(createDataSourcePanel(), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
        contents.add(createGeneAndNetworkPane(), new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
        
        Insets insets2 = new Insets(0, 2, 0, 2);
        JPanel limitPanel = uiUtils.createJPanel();
        
        int column = 0;
        limitPanel.setLayout(new GridBagLayout());
        addLabel(limitPanel, Strings.retrieveRelatedGenes_label, column, 0);
        column++;
        
        limitTextField = new JFormattedTextField(new NumberFormatter(new DecimalFormat("#"))); //$NON-NLS-1$
        limitTextField.setText("20"); //$NON-NLS-1$
        limitTextField.setColumns(4);
        limitTextField.addKeyListener(new KeyAdapter() {
        	@Override
        	public void keyReleased(KeyEvent e) {
        		validateQuery();
        	}
		});
        limitPanel.add(limitTextField, new GridBagConstraints(column, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets2, 0, 0));
        column++;
        
        addLabel(limitPanel, Strings.retrieveRelatedGenes_label2, column, 0);
        column++;
        
        addLabel(limitPanel, Strings.retrieveRelatedGenes_label5, column, 0);
        column++;

        attributeLimitTextField = new JFormattedTextField(new NumberFormatter(new DecimalFormat("#"))); //$NON-NLS-1$
        attributeLimitTextField.setText("20"); //$NON-NLS-1$
        attributeLimitTextField.setColumns(4);
        attributeLimitTextField.addKeyListener(new KeyAdapter() {
        	@Override
        	public void keyReleased(KeyEvent e) {
        		validateQuery();
        	}
		});
        limitPanel.add(attributeLimitTextField, new GridBagConstraints(column, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets2, 0, 0));
        column++;

        addLabel(limitPanel, Strings.retrieveRelatedGenes_label6, column, 0);
        column++;

        
        addLabel(limitPanel, Strings.retrieveRelatedGenes_label3, column, 0);
        column++;
        
        weightingMethodComboBox = new JComboBox();
        
        limitPanel.add(weightingMethodComboBox, new GridBagConstraints(column, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets2, 0, 0));
        column++;
        
        addLabel(limitPanel, Strings.retrieveRelatedGenes_label4, column, 0);
        column++;
        
        startButton = new JButton(Strings.retrieveRelatedGenesStartButton_label);
        startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleStartButton();
			}
        });
        limitPanel.add(startButton, new GridBagConstraints(column, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets2, 0, 0));
        column++;
        
        contents.add(limitPanel, new GridBagConstraints(0, 3, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
        
        validateQuery();
    }

	private Query getQuery() {
		Query query = new Query();
		query.setOrganism(selectedOrganism);
		query.setGenes(genePanel.getItems());
		query.setGeneLimit(getLimit(limitTextField));
		query.setAttributeLimit(getLimit(attributeLimitTextField));
		query.setCombiningMethod(getCombiningMethod());
		query.setScoringMethod(getScoringMethod());
		return query;
	}
	
	private void handleStartButton() {
		Query query = getQuery();
		Collection<Group<?, ?>> groups = selectionPanel.getSelectedGroups();
		NETWORK cyNetwork = controller.runMania(this, query, groups);

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
			boolean hasGenes = genePanel.getItemCount() > 0;
			boolean hasNetworks = selectionPanel.getSelectionCount() > 0;
			
			setControlsEnabled(true);
			Status status = checkQueryStatus();
			startButton.setEnabled(status == Status.Ok);
			clearButton.setEnabled(hasGenes);
			removeButton.setEnabled(genePanel.getSelectionCount() > 0);			
			
			organismBorder.setTitleColor(SystemColor.textText);
			geneBorder.setTitleColor(hasGenes ? SystemColor.textText : Color.red);
			networkBorder.setTitleColor(hasNetworks ? SystemColor.textText : Color.red);
			loadQueryBorder.setTitleColor(SystemColor.textText);
		} else {
			setControlsEnabled(false);
			organismBorder.setTitleColor(SystemColor.textInactiveText);
			networkBorder.setTitleColor(SystemColor.textInactiveText);
			geneBorder.setTitleColor(SystemColor.textInactiveText);
			loadQueryBorder.setTitleColor(SystemColor.textInactiveText);
		}
	}
	
	private void setControlsEnabled(boolean enabled) {
		organismComboBox.setEnabled(enabled);
		genePanel.setEnabled(enabled);
		removeButton.setEnabled(enabled);
		clearButton.setEnabled(enabled);
		startButton.setEnabled(enabled);
		chooseFileButton.setEnabled(enabled);
		repaint();
	}

	private Status checkQueryStatus() {
		if (selectedOrganism == null) {
			return Status.NoOrganismSelected;
		}
		
		if (getLimit(limitTextField) < 0) {
			return Status.LimitViolation;
		}

		if (getLimit(attributeLimitTextField) < 0) {
			return Status.LimitViolation;
		}
		
		List<String> geneNames = genePanel.getItems();
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
		
		networkSubPanel.removeAll();
		
		List<String> genes = null;
		if (selectionChanged) {
			if (selectedOrganism != null) {
				selectedGenes.put(selectedOrganism.getId(), genePanel.getItems());
			}
			if (organism != null) {
				genes = selectedGenes.get(organism.getId());
			}
			genePanel.clear();
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
		genePanel.setProvider(provider);

		if (genes != null) {
			genePanel.setItems(genes);
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
		networkSubPanel.add(selectionPanel, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		selectionPanel.setGroups(sortedGroups);
		
		validate();
        validateQuery();
	}

	private void updateCombiningMethods(Organism organism) {
		weightingMethodComboBox.removeAllItems();
		
		if (organism == null) {
			return;
		}
		
		boolean hasAnnotations = organism.getOntology() != null;
		
        weightingMethodComboBox.addItem(new WeightingMethodEntry(CombiningMethod.AUTOMATIC_SELECT, Strings.default_combining_method));
        weightingMethodComboBox.addItem(new WeightingMethodEntry(CombiningMethod.AUTOMATIC, Strings.automatic));
        if (hasAnnotations) {
	        weightingMethodComboBox.addItem(new WeightingMethodEntry(CombiningMethod.BP, Strings.bp));
	        weightingMethodComboBox.addItem(new WeightingMethodEntry(CombiningMethod.MF, Strings.mf));
	        weightingMethodComboBox.addItem(new WeightingMethodEntry(CombiningMethod.CC, Strings.cc));
        }
        weightingMethodComboBox.addItem(new WeightingMethodEntry(CombiningMethod.AVERAGE, Strings.average));
        weightingMethodComboBox.addItem(new WeightingMethodEntry(CombiningMethod.AVERAGE_CATEGORY, Strings.average_category));
	}

	CombiningMethod getCombiningMethod() {
		return ((WeightingMethodEntry) weightingMethodComboBox.getSelectedItem()).getMethod();
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
	
	private Component createDataSourcePanel() {
		final JPanel panel = uiUtils.createJPanel();
		panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(Strings.retrieveRelatedGenesStatistics_label));
        
		dataSourceLabel = new JLabel();
		dataSourceLabel.setName(Strings.retrieveRelatedGenesStatisticsVersion_label);
		
		Font labelFont = dataSourceLabel.getFont().deriveFont(Font.BOLD);
		Insets insets = new Insets(0, 8, 0 ,8);
		
		panel.add(createLabel(Strings.retrieveRelatedGenesStatisticsOrganisms_label, labelFont), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 5, 0));
		totalOrganismsLabel = new JLabel("0"); //$NON-NLS-1$
		totalOrganismsLabel.setName(Strings.retrieveRelatedGenesStatisticsOrganisms_label);
		panel.add(totalOrganismsLabel, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 2, 0));
		
		panel.add(createLabel(Strings.retrieveRelatedGenesStatisticsNetworks_label, labelFont), new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 5, 0));
		totalNetworksLabel = new JLabel("0"); //$NON-NLS-1$
		totalNetworksLabel.setName(Strings.retrieveRelatedGenesStatisticsNetworks_label);
		panel.add(totalNetworksLabel, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 2, 0));
		
		panel.add(createLabel(Strings.retrieveRelatedGenesStatisticsGenes_label, labelFont), new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 5, 0));
		totalGenesLabel = new JLabel("0"); //$NON-NLS-1$
		totalGenesLabel.setName(Strings.retrieveRelatedGenesStatisticsGenes_label);
		panel.add(totalGenesLabel, new GridBagConstraints(2, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 2, 0));
		
		panel.add(createLabel(Strings.retrieveRelatedGenesStatisticsInteractions_label, labelFont), new GridBagConstraints(3, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 5, 0));
		totalInteractionsLabel = new JLabel("0"); //$NON-NLS-1$
		totalInteractionsLabel.setName(Strings.retrieveRelatedGenesStatisticsInteractions_label);
		panel.add(totalInteractionsLabel, new GridBagConstraints(3, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 5, 0));
		
		panel.add(createLabel(Strings.retrieveRelatedGenesStatisticsVersion_label, labelFont), new GridBagConstraints(4, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 5, 0));
		panel.add(dataSourceLabel, new GridBagConstraints(4, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		
		configureButton = new JButton(Strings.dataSetConfiguration_title);
		configureButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				handleConfigureButton();
			}
		});

		panel.add(configureButton, new GridBagConstraints(6, 0, 1, 2, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		return panel;
	}
	
	private void handleConfigureButton() {
		DataSet data = dataSetManager.getDataSet();
		if (data == null) {
			return;
		}
		IConfiguration config = data.getConfiguration();
		if (config.hasUi()) {
			config.showUi(this);
		}
	}

	private JLabel createLabel(String message, Font font) {
		JLabel label = new JLabel(message);
		label.setFont(font);
		return label;
	}

	public void updateStatistics(DataSet data) {
		StatsMediator mediator = data.getMediatorProvider().getStatsMediator();
		Statistics statistics = mediator.getLatestStatistics();
		totalGenesLabel.setText(String.valueOf(statistics.getGenes()));
		totalInteractionsLabel.setText(String.valueOf(statistics.getInteractions()));
		totalNetworksLabel.setText(String.valueOf(statistics.getNetworks()));
		totalOrganismsLabel.setText(String.valueOf(statistics.getOrganisms()));
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
