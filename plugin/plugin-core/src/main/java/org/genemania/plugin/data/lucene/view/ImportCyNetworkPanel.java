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

package org.genemania.plugin.data.lucene.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;
import org.genemania.data.normalizer.DataFileType;
import org.genemania.data.normalizer.DataImportSettings;
import org.genemania.data.normalizer.OrganismClassifier;
import org.genemania.data.normalizer.OrganismClassifier.Match;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.OrganismMediator;
import org.genemania.plugin.Strings;
import org.genemania.plugin.completion.DynamicTableModel;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetChangeListener;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.data.IModelManager;
import org.genemania.plugin.data.Namespace;
import org.genemania.plugin.formatters.IObjectFormatter;
import org.genemania.plugin.formatters.OrganismFormatter;
import org.genemania.plugin.model.ModelElement;
import org.genemania.plugin.model.OrganismComparator;
import org.genemania.plugin.proxies.NetworkProxy;
import org.genemania.plugin.proxies.NodeProxy;
import org.genemania.plugin.task.GeneManiaTask;
import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.plugin.view.components.NetworkGroupComboBox;
import org.genemania.plugin.view.util.UiUtils;
import org.genemania.type.DataLayout;
import org.genemania.type.ImportedDataFormat;
import org.genemania.type.NetworkProcessingMethod;
import org.genemania.util.ProgressReporter;

@SuppressWarnings("serial")
public class ImportCyNetworkPanel<NETWORK, NODE, EDGE> extends JPanel {
	private static final int CHECK_COLUMN = 0;
	private static final int NAME_COLUMN = 1;
	
	private final DataSetManager dataSetManager;
	private final UiUtils uiUtils;
	private final TaskDispatcher taskDispatcher;
	private final CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils;
	
	private JLabel weightLabel;
	private JComboBox weightCombo;
	private JLabel expressionLabel;
	private JTable expressionTable;
	private JScrollPane expressionPane;
	private ExpressionTableModel expressionModel;
	private NetworkGroupComboBox groupCombo;
	private JTextField nameTextField;
	private JTextArea descriptionTextArea;
	private JComboBox organismCombo;
	private JComboBox idCombo;
	private JComboBox networkCombo;
	private JComboBox typeCombo;
	private JLabel helpLabel;
	private JPanel sourcePanel;
	private JPanel destinationPanel;
	private JButton importButton;
	private JTextField groupNameField;

	public ImportCyNetworkPanel(DataSetManager dataSetManager, UiUtils uiUtils, CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils, TaskDispatcher taskDispatcher) {
		this.dataSetManager = dataSetManager;
		this.uiUtils = uiUtils;
		this.cytoscapeUtils = cytoscapeUtils;
		this.taskDispatcher = taskDispatcher;
		
		setOpaque(false);
		setLayout(new GridBagLayout());
		
		Insets insets = new Insets(0, 0, 0, 0);
		int row = 0;
		
		sourcePanel = createSourcePanel();
		sourcePanel.setBorder(BorderFactory.createTitledBorder(Strings.importCyNetworkSource_title));
		destinationPanel = createDestinationPanel();
		destinationPanel.setBorder(BorderFactory.createTitledBorder(Strings.importCyNetworkDestination_title));

		helpLabel = new JLabel();
		add(helpLabel, new GridBagConstraints(0, row, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 5, 5), 0, 0));
		row++;

		add(sourcePanel, new GridBagConstraints(0, row, 1, 1, 1, 1, GridBagConstraints.PAGE_START, GridBagConstraints.BOTH, insets, 0, 0));
		row++;

		add(destinationPanel, new GridBagConstraints(0, row, 1, 1, 1, 1, GridBagConstraints.PAGE_START, GridBagConstraints.BOTH, insets, 0, 0));
		row++;
		
		importButton = new JButton(Strings.importCyNetworkImport_label);
		importButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleImport();
			}
		});
		
		add(importButton , new GridBagConstraints(0, row, 2, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		row++;
		
		add(uiUtils.createJPanel(), new GridBagConstraints(0, row, 2, 1, 0, Double.MIN_VALUE, GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets , 0, 0));
		row++;
		
		dataSetManager.addDataSetChangeListener(new DataSetChangeListener() {
			@Override
			public void dataSetChanged(DataSet activeDataSet, ProgressReporter progress) {
				handleDataSetChange(activeDataSet);
			}
		});
		
		handleDataSetChange(dataSetManager.getDataSet());
		validateSettings();
	}
	
	private JPanel createDestinationPanel() {
		JPanel panel = uiUtils.createJPanel();
		panel.setLayout(new GridBagLayout());

		Insets insets = new Insets(0, 0, 0, 0);
		int row = 0;
		
		organismCombo = new JComboBox();
		panel.add(new JLabel(Strings.importCyNetworkOrganism_label), new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets , 0, 0));
		panel.add(organismCombo, new GridBagConstraints(1, row, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets , 0, 0));
		row++;
		organismCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleOrganismChange();
			}
		});
		
		DocumentListener documentListener = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				validateSettings();
			}
			
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				validateSettings();
			}
			
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				validateSettings();
			}
		};
		
		FocusListener focusListener = new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				validateSettings();
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
			}
		};
		
		JPanel groupPanel = uiUtils.createJPanel();
		groupPanel.setLayout(new GridBagLayout());
		
		groupCombo = new NetworkGroupComboBox();
		groupCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				validateSettings();
			}
		});

		groupNameField = new JTextField(30);
		groupNameField.getDocument().addDocumentListener(documentListener);
		groupNameField.addFocusListener(focusListener);
		
		groupPanel.add(groupCombo, new GridBagConstraints(0, 0, 1, 1, Double.MIN_VALUE, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		groupPanel.add(groupNameField, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		panel.add(new JLabel(Strings.importCyNetworkNetworkGroup_label), new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets , 0, 0));
		panel.add(groupPanel, new GridBagConstraints(1, row, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, insets , 0, 0));
		row++;

		nameTextField = new JTextField(30);
		nameTextField.getDocument().addDocumentListener(documentListener);
		nameTextField.addFocusListener(focusListener);
		panel.add(new JLabel(Strings.importCyNetworkNetworkName_label) , new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets , 0, 0));
		panel.add(nameTextField, new GridBagConstraints(1, row, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, insets , 0, 0));
		row++;
		
		descriptionTextArea = new JTextArea();
		descriptionTextArea.setRows(4);
		descriptionTextArea.getDocument().addDocumentListener(documentListener);
		descriptionTextArea.addFocusListener(focusListener);
		
		JScrollPane descriptionPane = new JScrollPane(descriptionTextArea);
		panel.add(new JLabel(Strings.importCyNetworkNetworkDescription_label) , new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets , 0, 0));
		panel.add(descriptionPane, new GridBagConstraints(1, row, 1, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, insets , 0, 0));
		row++;
		
		panel.add(uiUtils.createJPanel(), new GridBagConstraints(0, row, 2, 1, 0, Double.MIN_VALUE, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
		row++;

		return panel;
	}

	void validateSettings() {
		int groupIndex = groupCombo.getSelectedIndex();
		groupNameField.setVisible(groupIndex == 0);
		validate();

		boolean valid = nameTextField.getText().trim().length() > 0;
		valid &= organismCombo.getSelectedIndex() != -1;
		valid &= groupIndex != -1;
		
		String groupName = groupNameField.getText().trim();
		valid &= groupIndex != 0 || groupName.length() > 0 && !groupCombo.containsGroup(groupName);
		
		importButton.setEnabled(valid);
	}

	@SuppressWarnings("unchecked")
	private JPanel createSourcePanel() {
		JPanel panel = uiUtils.createJPanel();
		panel.setLayout(new GridBagLayout());

		Insets insets = new Insets(0, 0, 0, 0);
		int row = 0;
		
		networkCombo = new JComboBox();
		panel.add(new JLabel(Strings.importCyNetworkSourceNetwork_label), new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets , 0, 0));
		panel.add(networkCombo, new GridBagConstraints(1, row, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets , 0, 0));
		row++;

		idCombo = new JComboBox();
		panel.add(new JLabel(Strings.importCyNetworkNodeIdentifier_label), new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets , 0, 0));
		panel.add(idCombo, new GridBagConstraints(1, row, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets , 0, 0));
		row++;
		
		networkCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleNetworkChange();
				handleSourceChange();
			}
		});
		idCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleSourceChange();
			}
		});
		
		Comparator<NetworkType> comparator = new Comparator<NetworkType>() {
			public int compare(NetworkType o1, NetworkType o2) {
				return o1.compareTo(o2);
			}
		};
		IObjectFormatter<NetworkType> formatter = new IObjectFormatter<NetworkType>() {
			public String format(NetworkType type) {
				switch (type) {
				case COEXPRESSION:
					return Strings.importCyNetworkTypeCoexpression_label;
				case UNWEIGHTED:
					return Strings.importCyNetworkTypeUnweighted_label;
				case WEIGHTED:
					return Strings.importCyNetworkTypeWeighted_label;
				default:
					return Strings.importCyNetworkTypeUnknown_label;
				}
			};
		};
	
		NetworkType[] allTypes = NetworkType.values();
		ModelElement<NetworkType>[] typeModel = new ModelElement[allTypes.length];
		for (int i = 0; i < typeModel.length; i++) {
			typeModel[i] = new ModelElement<NetworkType>(allTypes[i], comparator, formatter); 
		}
		typeCombo = new JComboBox(typeModel);
		typeCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleTypeChange(typeCombo);
			}
		});
		
		panel.add(new JLabel(Strings.importCyNetworkType_label), new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets , 0, 0));
		panel.add(typeCombo, new GridBagConstraints(1, row, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets , 0, 0));
		row++;
		
		// Weighted components
		weightLabel = new JLabel(Strings.importCyNetworkWeight_label);
		weightCombo = new JComboBox();
		panel.add(weightLabel , new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets , 0, 0));
		panel.add(weightCombo, new GridBagConstraints(1, row, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets , 0, 0));
		
		// Co-expression components
		expressionLabel = new JLabel(Strings.importCyNetworkExpressionValues_label);
		expressionModel = new ExpressionTableModel();
		
		expressionTable = new JTable(expressionModel) {
			@Override
			public void addNotify() {
				super.addNotify();
				uiUtils.packColumns(this);
			}
		};
		expressionPane = new JScrollPane(expressionTable);
		
		panel.add(expressionLabel, new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, insets , 0, 0));
		panel.add(expressionPane, new GridBagConstraints(1, row, 1, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, insets , 0, 0));
		
		row++;
		
		panel.add(uiUtils.createJPanel(), new GridBagConstraints(0, row, 2, 1, 0, Double.MIN_VALUE, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
		row++;
		
		handleTypeChange(typeCombo);
		return panel;
	}

	@SuppressWarnings("unchecked")
	private void handleOrganismChange() {
		ModelElement<Organism> element = (ModelElement<Organism>) organismCombo.getSelectedItem();
		if (element == null) {
			groupCombo.updateNetworkGroups(null);
			return;
		}
		Organism organism = element.getItem();
		Collection<InteractionNetworkGroup> groups = organism.getInteractionNetworkGroups();
		groupCombo.updateNetworkGroups(groups);
	}

	@SuppressWarnings("unchecked")
	private void handleSourceChange() {
		String idAttribute = (String) idCombo.getSelectedItem();
		if (idAttribute == null) {
			return;
		}
		NetworkProxy<NETWORK, NODE, EDGE> networkProxy = ((ModelElement<NetworkProxy<NETWORK, NODE, EDGE>>) networkCombo.getSelectedItem()).getItem();
		NETWORK network = networkProxy.getProxied();
		
		// Attempt to autodetect organism
		DataSet data = dataSetManager.getDataSet();
		OrganismClassifier classifier = new OrganismClassifier(data.getGeneClassifier());
		for (NODE node : networkProxy.getNodes()) {
			NodeProxy<NODE> nodeProxy = cytoscapeUtils.getNodeProxy(node, network);
			Class<?> type = nodeProxy.getAttributeType(idAttribute);
			if (type.equals(String.class)) {
				String symbol = nodeProxy.getAttribute(idAttribute, String.class);
				try {
					classifier.addGene(symbol, 0);
				} catch (ApplicationException e) {
					log(e);
				}
			} else if (type.equals(List.class)) {
				List<?> list = nodeProxy.getAttribute(idAttribute, List.class);
				for (Object item : list) {
					if (!(item instanceof String)) {
						continue;
					}
					try {
						classifier.addGene((String) item, 0);
					} catch (ApplicationException e) {
						log(e);
					}
				}
			}
		}
		List<Match> organismIds = classifier.getMostLikelyOrganismIds();
		if (organismIds.size() > 0) {
			Match match = organismIds.get(0);
			ComboBoxModel model = organismCombo.getModel();
			for (int i = 0; i < model.getSize(); i++) {
				ModelElement<Organism> element = (ModelElement<Organism>) model.getElementAt(i);
				if (element.getItem().getId() == match.organismId) {
					organismCombo.setSelectedItem(element);
					return;
				}
			}
		}
	}

	private void log(Throwable t) {
		Logger logger = Logger.getLogger(getClass());
		logger.error(t.getMessage(), t);
	}

	private void handleDataSetChange(DataSet data) {
		populateNetworks();
		populateOrganisms(data);
	}

	@SuppressWarnings("unchecked")
	private void populateNetworks() {
		ArrayList<ModelElement<NetworkProxy<NETWORK, NODE, EDGE>>> networkIds = new ArrayList<ModelElement<NetworkProxy<NETWORK, NODE, EDGE>>>();
		Comparator<NetworkProxy<NETWORK, NODE, EDGE>> comparator = new Comparator<NetworkProxy<NETWORK, NODE, EDGE>>() {
			public int compare(NetworkProxy<NETWORK, NODE, EDGE> o1, NetworkProxy<NETWORK, NODE, EDGE> o2) {
				return o1.getTitle().compareToIgnoreCase(o2.getTitle());
			}
		};
		IObjectFormatter<NetworkProxy<NETWORK, NODE, EDGE>> formatter = new IObjectFormatter<NetworkProxy<NETWORK, NODE, EDGE>>() {
			public String format(NetworkProxy<NETWORK, NODE, EDGE> object) {
				return object.getTitle();
			}
		};
		for (NETWORK network : cytoscapeUtils.getNetworks()) {
			NetworkProxy<NETWORK, NODE, EDGE> networkProxy = cytoscapeUtils.getNetworkProxy(network);
			networkIds.add(new ModelElement<NetworkProxy<NETWORK, NODE, EDGE>>(networkProxy, comparator, formatter));
		}
		ModelElement<NetworkProxy<NETWORK, NODE, EDGE>>[] model = new ModelElement[networkIds.size()];
		model = networkIds.toArray(model);
		Arrays.sort(model);
		networkCombo.setModel(new DefaultComboBoxModel(model));
		
		boolean hasNetworks = model.length > 0;
		sourcePanel.setVisible(hasNetworks);
		destinationPanel.setVisible(hasNetworks);
		importButton.setVisible(hasNetworks);
		
		if (hasNetworks) {
			helpLabel.setText(Strings.importCyNetworkHelp_label);
			handleNetworkChange();
		} else {
			helpLabel.setText(Strings.importCyNetworkHelpEmpty_label);
		}
	}

	@SuppressWarnings("unchecked")
	private void handleNetworkChange() {
		NetworkProxy<NETWORK, NODE, EDGE> networkProxy = ((ModelElement<NetworkProxy<NETWORK, NODE, EDGE>>) networkCombo.getSelectedItem()).getItem();
		populateAttributes(networkProxy);
	}

	@SuppressWarnings("unchecked")
	private void populateOrganisms(DataSet data) {
		try {
			OrganismMediator organismMediator = data.getMediatorProvider().getOrganismMediator();
			List<Organism> allOrganisms = organismMediator.getAllOrganisms();
			ModelElement<Organism>[] elements = new ModelElement[allOrganisms.size()];
			int i = 0;
			for (Organism organism : allOrganisms) {
				elements[i] = new ModelElement<Organism>(organism, OrganismComparator.getInstance(), OrganismFormatter.getInstance());
				i++;
			}
			Arrays.sort(elements);
			organismCombo.setModel(new DefaultComboBoxModel(elements));
			handleOrganismChange();
		} catch (DataStoreException e) {
			log(e);
		}
	}

	private void populateAttributes(NetworkProxy<NETWORK, NODE, EDGE> networkProxy) {
		String[] nodeAttributes = sort(networkProxy.getNodeAttributeNames());
		String[] edgeAttributes = sort(networkProxy.getEdgeAttributeNames());
		
		idCombo.setModel(new DefaultComboBoxModel(nodeAttributes));
		weightCombo.setModel(new DefaultComboBoxModel(edgeAttributes));
		
		expressionModel.clear();
		for (String name : nodeAttributes) {
			expressionModel.add(new ExpressionTableElement(name));
		}
	}

	private String[] sort(Collection<String> names) {
		ArrayList<String> list = new ArrayList<String>(names);
		Collections.sort(list, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareToIgnoreCase(o2);
			}
		});
		return list.toArray(new String[list.size()]);
	}

	@SuppressWarnings("unchecked")
	private void handleTypeChange(JComboBox combo) {
		ModelElement<NetworkType> element = (ModelElement<NetworkType>) combo.getSelectedItem();
		boolean showWeight = element.getItem() == NetworkType.WEIGHTED;
		boolean showNone = element.getItem() == NetworkType.UNWEIGHTED;
		
		weightLabel.setVisible(showWeight && !showNone);
		weightCombo.setVisible(showWeight && !showNone);
		expressionLabel.setVisible(!showWeight && !showNone);
		expressionPane.setVisible(!showWeight && !showNone);
	}

	@SuppressWarnings("unchecked")
	private void handleImport() {
		ModelElement<Organism> element = (ModelElement<Organism>) organismCombo.getSelectedItem();
		final Organism organism = element.getItem();
		
		final NetworkProxy<NETWORK, NODE, EDGE> networkProxy = ((ModelElement<NetworkProxy<NETWORK, NODE, EDGE>>) networkCombo.getSelectedItem()).getItem();
		final NETWORK cyNetwork = networkProxy.getProxied();
		
		final String idAttribute = (String) idCombo.getSelectedItem();
		final ModelElement<NetworkType> typeElement = (ModelElement<NetworkType>) typeCombo.getSelectedItem();

		GeneManiaTask task = new GeneManiaTask(Strings.importCyNetworkTask_title) {
			@Override
			protected void runTask() throws Throwable {
				File outputFile = File.createTempFile("temp", "cynetwork.txt");  //$NON-NLS-1$//$NON-NLS-2$
				try {
					DataImportSettings settings = new DataImportSettings();
	
					DataSet data = dataSetManager.getDataSet();
					long networkId = data.getNextAvailableId(InteractionNetwork.class, Namespace.USER);
					InteractionNetwork network = new InteractionNetwork();
					network.setId(networkId);
					network.setName(nameTextField.getText());
					network.setDescription(descriptionTextArea.getText());
					
					InteractionNetworkGroup group = groupCombo.getGroup();
					
					settings.setNetwork(network);
					settings.setNetworkGroup(group);
					settings.setOrganism(organism);
	
					Writer writer = new FileWriter(outputFile);
					DataFileType fileType = null;
					try {
						switch (typeElement.getItem()) {
						case UNWEIGHTED:
							fileType = DataFileType.INTERACTION_NETWORK;
							handleUnweightedNetwork(settings, cyNetwork, idAttribute, writer, progress);
							break;
						case WEIGHTED:
							fileType = DataFileType.INTERACTION_NETWORK;
							String weightAttribute = (String) weightCombo.getSelectedItem();
							handleWeightedNetwork(settings, cyNetwork, idAttribute, weightAttribute, writer, progress);
							break;
						case COEXPRESSION:
							fileType = DataFileType.EXPRESSION_PROFILE;
							List<String> expressionAttributes = getExpressionAttributes();
							handleCoexpressionNetwork(settings, cyNetwork, idAttribute, expressionAttributes, writer, progress);
							break;
						}
					} finally {
						writer.close();
					}
	
					IModelManager manager = data.createModelManager(Namespace.USER);
					try {
						manager.installNetwork(settings, outputFile.getPath(), fileType, progress);
					} finally {
						manager.close();
					}
					dataSetManager.reloadDataSet(progress);
					resetForm();
				} finally {
					outputFile.delete();
				}
			}

		};
		taskDispatcher.executeTask(task, uiUtils.getFrame(this), true, true);
	}
	
	protected void handleCoexpressionNetwork(DataImportSettings settings, NETWORK cyNetwork, String idAttribute, List<String> expressionAttributes, Writer writer, ProgressReporter progress) {
		List<Integer> idColumns = new ArrayList<Integer>();
		idColumns.add(0);
		settings.setDataFormat(ImportedDataFormat.PROFILE_DATA_TAB_DELIMITED);
		settings.setDataLayout(DataLayout.PROFILE);
		settings.setDelimiter("\t"); //$NON-NLS-1$
		settings.setIdColumns(idColumns);
		settings.setProcessingMethod(NetworkProcessingMethod.PEARSON);
		CyNetworkImporter<NETWORK, NODE, EDGE> importer = new CyNetworkImporter<NETWORK, NODE, EDGE>(cytoscapeUtils);
		importer.process(cyNetwork, idAttribute, expressionAttributes, writer, progress);							
	}

	protected void handleWeightedNetwork(DataImportSettings settings, NETWORK cyNetwork, String idAttribute, String weightAttribute, Writer writer, ProgressReporter progress) {
		List<Integer> idColumns = new ArrayList<Integer>();
		idColumns.add(0);
		idColumns.add(1);
		settings.setDataFormat(ImportedDataFormat.NETWORK_DATA_TAB_DELIMITED);
		settings.setDataLayout(DataLayout.WEIGHTED_NETWORK);
		settings.setDelimiter("\t"); //$NON-NLS-1$
		settings.setIdColumns(idColumns);
		settings.setProcessingMethod(NetworkProcessingMethod.DIRECT);
		CyNetworkImporter<NETWORK, NODE, EDGE> importer = new CyNetworkImporter<NETWORK, NODE, EDGE>(cytoscapeUtils);
		importer.process(cyNetwork, idAttribute, weightAttribute, writer, progress);
	}

	private void handleUnweightedNetwork(DataImportSettings settings, NETWORK cyNetwork, String idAttribute, Writer writer, ProgressReporter progress) {
		List<Integer> idColumns = new ArrayList<Integer>();
		idColumns.add(0);
		idColumns.add(1);
		settings.setDataFormat(ImportedDataFormat.NETWORK_DATA_TAB_DELIMITED);
		settings.setDataLayout(DataLayout.BINARY_NETWORK);
		settings.setDelimiter("\t"); //$NON-NLS-1$
		settings.setIdColumns(idColumns);
		settings.setProcessingMethod(NetworkProcessingMethod.DIRECT);
		CyNetworkImporter<NETWORK, NODE, EDGE> importer = new CyNetworkImporter<NETWORK, NODE, EDGE>(cytoscapeUtils);
		importer.process(cyNetwork, idAttribute, (String) null, writer, progress);
	}

	private void resetForm() {
		nameTextField.setText(""); //$NON-NLS-1$
		descriptionTextArea.setText(""); //$NON-NLS-1$
	}
	
	List<String> getExpressionAttributes() {
		List<String> names = new ArrayList<String>();
		for (int row = 0; row < expressionTable.getRowCount(); row++) {
			if ((Boolean) expressionTable.getValueAt(row, CHECK_COLUMN)) {
				names.add((String) expressionTable.getValueAt(row, NAME_COLUMN));
			}
		}
		return names;
	}
	
	enum NetworkType {
		UNWEIGHTED,
		WEIGHTED,
		COEXPRESSION,
	}
	
	static class ExpressionTableModel extends DynamicTableModel<ExpressionTableElement> {

		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case CHECK_COLUMN:
				return Boolean.class;
			case NAME_COLUMN:
				return String.class;
			default:
				return null;	
			}
		}

		public int getColumnCount() {
			return 2;
		}

		public String getColumnName(int columnIndex) {
			switch (columnIndex) {
			case CHECK_COLUMN:
				return ""; //$NON-NLS-1$
			case NAME_COLUMN:
				return Strings.importCyNetworkExpressionNameColumn_label;
			default:
				return null;
			}
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			ExpressionTableElement element = get(rowIndex);
			if (element == null) {
				return null;
			}
			switch (columnIndex) {
			case 0:
				return element.selected;
			case 1:
				return element.name;
			default:
				return null;
			}
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 0;
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			ExpressionTableElement element = get(rowIndex);
			if (element == null) {
				return;
			}
			if (columnIndex != 0) {
				return;
			}
			if (aValue instanceof Boolean) {
				element.selected = (Boolean) aValue;
			}
		}
	}
	
	static class ExpressionTableElement {
		boolean selected;
		String name;
		
		public ExpressionTableElement(String name) {
			this.name = name;
		}
	}
}
