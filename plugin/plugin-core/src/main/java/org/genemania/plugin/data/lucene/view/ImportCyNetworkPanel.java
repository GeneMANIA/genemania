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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

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
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
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
	
	private final JLabel helpLabel = new JLabel();
	private final JLabel weightLabel = new JLabel(Strings.importCyNetworkWeight_label);
	private final JLabel expressionLabel = new JLabel(Strings.importCyNetworkExpressionValues_label);
	private final JLabel sourceNetLabel = new JLabel(Strings.importCyNetworkSourceNetwork_label);
	private final JLabel nodeIdLabel = new JLabel(Strings.importCyNetworkNodeIdentifier_label);
	private final JLabel typeLabel = new JLabel(Strings.importCyNetworkType_label);
	private final JLabel organismLabel = new JLabel(Strings.importCyNetworkOrganism_label);
	private final JLabel netGroupLabel = new JLabel(Strings.importCyNetworkNetworkGroup_label);
	private final JLabel netNameLabel = new JLabel(Strings.importCyNetworkNetworkName_label);
	private final JLabel netDescLabel = new JLabel(Strings.importCyNetworkNetworkDescription_label);
	
	private JComboBox weightCombo;
	private JTable expressionTable;
	private JScrollPane expressionPane;
	private NetworkGroupComboBox groupCombo;
	private JTextField nameTextField;
	private JTextArea descriptionTextArea;
	private JComboBox organismCombo;
	private JComboBox idCombo;
	private JComboBox networkCombo;
	private JComboBox typeCombo;
	private JPanel sourcePanel;
	private JPanel destinationPanel;
	private JButton importButton;
	private JTextField groupNameField;

	private final ExpressionTableModel expressionModel;
	private final DocumentListener documentListener;
	private final FocusListener focusListener;
	
	public ImportCyNetworkPanel(
			final DataSetManager dataSetManager,
			final UiUtils uiUtils,
			final CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils,
			final TaskDispatcher taskDispatcher
	) {
		this.dataSetManager = dataSetManager;
		this.uiUtils = uiUtils;
		this.cytoscapeUtils = cytoscapeUtils;
		this.taskDispatcher = taskDispatcher;
		
		if (uiUtils.isAquaLAF())
			setOpaque(false);
		
		expressionModel = new ExpressionTableModel();
		documentListener = new DocumentListener() {
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
		focusListener = new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				validateSettings();
			}
			@Override
			public void focusGained(FocusEvent arg0) {
			}
		};
		
		addComponents();
		
		handleTypeChange(getTypeCombo());
		
		dataSetManager.addDataSetChangeListener(new DataSetChangeListener() {
			@Override
			public void dataSetChanged(DataSet activeDataSet, ProgressReporter progress) {
				handleDataSetChange(activeDataSet);
			}
		});
		
		handleDataSetChange(dataSetManager.getDataSet());
		validateSettings();
	}
	
	private void addComponents() {
		helpLabel.setFont(helpLabel.getFont().deriveFont(UiUtils.INFO_FONT_SIZE));
		helpLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 20, 0));
		
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateGaps(uiUtils.isWinLAF());
		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(helpLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getSourcePanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getDestinationPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getImportButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(helpLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getSourcePanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getDestinationPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getImportButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		// To proper align all left-side labels and
		// prevent the source panel fields from shifting when the typeCombo value changes:
		uiUtils.fixHorizontalAlignment(
				SwingConstants.RIGHT,
				sourceNetLabel, nodeIdLabel, typeLabel, weightLabel, expressionLabel,
				organismLabel, netGroupLabel, netNameLabel, netDescLabel);
	}
	
	private JPanel getSourcePanel() {
		if (sourcePanel == null) {
			sourcePanel = uiUtils.createJPanel();
			sourcePanel.setBorder(uiUtils.createTitledBorder(Strings.importCyNetworkSource_title));
	
			final GroupLayout layout = new GroupLayout(sourcePanel);
			sourcePanel.setLayout(layout);
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.TRAILING, false)
							.addComponent(sourceNetLabel)
							.addComponent(nodeIdLabel)
							.addComponent(typeLabel)
							.addComponent(weightLabel)
							.addComponent(expressionLabel)
					)
					.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
							.addComponent(getNetworkCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getIdCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getTypeCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getWeightCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getExpressionPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(sourceNetLabel)
							.addComponent(getNetworkCombo())
					)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(nodeIdLabel)
							.addComponent(getIdCombo())
					)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(typeLabel)
							.addComponent(getTypeCombo())
					)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(weightLabel)
							.addComponent(getWeightCombo())
					)
					.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
							.addComponent(expressionLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getExpressionPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					)
			);
		}
		
		return sourcePanel;
	}
	
	private JPanel getDestinationPanel() {
		if (destinationPanel == null) {
			destinationPanel = uiUtils.createJPanel();
			destinationPanel.setBorder(uiUtils.createTitledBorder(Strings.importCyNetworkDestination_title));
	
			final JScrollPane descriptionPane = new JScrollPane(getDescriptionTextArea());
			
			final GroupLayout layout = new GroupLayout(destinationPanel);
			destinationPanel.setLayout(layout);
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.TRAILING, false)
							.addComponent(organismLabel)
							.addComponent(netGroupLabel)
							.addComponent(netNameLabel)
							.addComponent(netDescLabel)
					)
					.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
							.addComponent(getOrganismCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addGroup(layout.createSequentialGroup()
									.addComponent(getGroupCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
									.addComponent(getGroupNameField(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							)
							.addComponent(getNameTextField(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(descriptionPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(organismLabel)
							.addComponent(getOrganismCombo())
					)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(netGroupLabel)
							.addComponent(getGroupCombo())
							.addComponent(getGroupNameField())
					)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(netNameLabel)
							.addComponent(getNameTextField())
					)
					.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
							.addComponent(netDescLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(descriptionPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					)
			);
		}

		return destinationPanel;
	}
	
	private JButton getImportButton() {
		if (importButton == null) {
			importButton = new JButton(Strings.importCyNetworkImport_label);
			importButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					handleImport();
				}
			});
		}
		
		return importButton;
	}
	
	private JComboBox getNetworkCombo() {
		if (networkCombo == null) {
			networkCombo = new JComboBox();
			networkCombo.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					handleNetworkChange();
					handleSourceChange();
				}
			});
		}
		
		return networkCombo;
	}
	
	private JComboBox getIdCombo() {
		if (idCombo == null) {
			idCombo = new JComboBox();
			idCombo.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					handleSourceChange();
				}
			});
		}
		
		return idCombo;
	}
	
	@SuppressWarnings("unchecked")
	private JComboBox getTypeCombo() {
		if (typeCombo == null) {
			final Comparator<NetworkType> comparator = new Comparator<NetworkType>() {
				@Override
				public int compare(NetworkType o1, NetworkType o2) {
					return o1.compareTo(o2);
				}
			};
			final IObjectFormatter<NetworkType> formatter = new IObjectFormatter<NetworkType>() {
				@Override
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
			
			final NetworkType[] allTypes = NetworkType.values();
			final ModelElement<NetworkType>[] typeModel = new ModelElement[allTypes.length];
			
			for (int i = 0; i < typeModel.length; i++) {
				typeModel[i] = new ModelElement<NetworkType>(allTypes[i], comparator, formatter); 
			}
			
			typeCombo = new JComboBox(typeModel);
			typeCombo.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					handleTypeChange(typeCombo);
				}
			});
		}
		
		return typeCombo;
	}
	
	private JComboBox getWeightCombo() {
		if (weightCombo == null) {
			weightCombo = new JComboBox();
		}
		
		return weightCombo;
	}
	
	private JTable getExpressionTable() {
		if (expressionTable == null) {
			expressionTable = new JTable(expressionModel) {
				@Override
				public void addNotify() {
					super.addNotify();
					uiUtils.packColumns(this);
				}
			};
		}
		
		return expressionTable;
	}
	
	private JScrollPane getExpressionPane() {
		if (expressionPane == null) {
			expressionPane = new JScrollPane(getExpressionTable());
			expressionPane.setPreferredSize(uiUtils.computeTextSizeHint(getFontMetrics(getFont()), 10, 5));
		}
		
		return expressionPane;
	}
	
	private JComboBox getOrganismCombo() {
		if (organismCombo == null) {
			organismCombo = new JComboBox();
			organismCombo.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					handleOrganismChange();
				}
			});
		}
		
		return organismCombo;
	}
	
	private NetworkGroupComboBox getGroupCombo() {
		if (groupCombo == null) {
			groupCombo = new NetworkGroupComboBox();
			groupCombo.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					validateSettings();
				}
			});
		}
		
		return groupCombo;
	}
	
	private JTextField getGroupNameField() {
		if (groupNameField == null) {
			groupNameField = new JTextField(30);
			groupNameField.getDocument().addDocumentListener(documentListener);
			groupNameField.addFocusListener(focusListener);
		}
		
		return groupNameField;
	}
	
	private JTextField getNameTextField() {
		if (nameTextField == null) {
			nameTextField = new JTextField(30);
			nameTextField.getDocument().addDocumentListener(documentListener);
			nameTextField.addFocusListener(focusListener);
		}

		return nameTextField;
	}
	
	private JTextArea getDescriptionTextArea() {
		if (descriptionTextArea == null) {
			descriptionTextArea = new JTextArea();
			descriptionTextArea.setRows(4);
			descriptionTextArea.getDocument().addDocumentListener(documentListener);
			descriptionTextArea.addFocusListener(focusListener);
		}
		
		return descriptionTextArea;
	}

	void validateSettings() {
		int groupIndex = getGroupCombo().getSelectedIndex();
		getGroupNameField().setVisible(groupIndex == 0);
		validate();

		boolean valid =  getNameTextField().getText().trim().length() > 0;
		valid &= getOrganismCombo().getSelectedIndex() != -1;
		valid &= groupIndex != -1;
		
		String groupName = getGroupNameField().getText().trim();
		valid &= groupIndex != 0 || groupName.length() > 0 && !getGroupCombo().containsGroup(groupName);
		
		getImportButton().setEnabled(valid);
	}

	@SuppressWarnings("unchecked")
	private void handleOrganismChange() {
		ModelElement<Organism> element = (ModelElement<Organism>) getOrganismCombo().getSelectedItem();
		if (element == null) {
			getGroupCombo().updateNetworkGroups(null);
			return;
		}
		Organism organism = element.getItem();
		Collection<InteractionNetworkGroup> groups = organism.getInteractionNetworkGroups();
		getGroupCombo().updateNetworkGroups(groups);
	}

	@SuppressWarnings("unchecked")
	private void handleSourceChange() {
		String idAttribute = (String) getIdCombo().getSelectedItem();
		if (idAttribute == null) {
			return;
		}
		NetworkProxy<NETWORK, NODE, EDGE> networkProxy = ((ModelElement<NetworkProxy<NETWORK, NODE, EDGE>>) getNetworkCombo().getSelectedItem()).getItem();
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
			ComboBoxModel model = getOrganismCombo().getModel();
			for (int i = 0; i < model.getSize(); i++) {
				ModelElement<Organism> element = (ModelElement<Organism>) model.getElementAt(i);
				if (element.getItem().getId() == match.organismId) {
					getOrganismCombo().setSelectedItem(element);
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
		getNetworkCombo().setModel(new DefaultComboBoxModel(model));
		
		boolean hasNetworks = model.length > 0;
		getSourcePanel().setVisible(hasNetworks);
		getDestinationPanel().setVisible(hasNetworks);
		getImportButton().setVisible(hasNetworks);
		
		if (hasNetworks) {
			helpLabel.setText(Strings.importCyNetworkHelp_label);
			handleNetworkChange();
		} else {
			helpLabel.setText(Strings.importCyNetworkHelpEmpty_label);
		}
	}

	@SuppressWarnings("unchecked")
	private void handleNetworkChange() {
		NetworkProxy<NETWORK, NODE, EDGE> networkProxy = ((ModelElement<NetworkProxy<NETWORK, NODE, EDGE>>) getNetworkCombo().getSelectedItem()).getItem();
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
			getOrganismCombo().setModel(new DefaultComboBoxModel(elements));
			handleOrganismChange();
		} catch (DataStoreException e) {
			log(e);
		}
	}

	private void populateAttributes(NetworkProxy<NETWORK, NODE, EDGE> networkProxy) {
		String[] nodeAttributes = sort(networkProxy.getNodeAttributeNames());
		String[] edgeAttributes = sort(networkProxy.getEdgeAttributeNames());
		
		getIdCombo().setModel(new DefaultComboBoxModel(nodeAttributes));
		getWeightCombo().setModel(new DefaultComboBoxModel(edgeAttributes));
		
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
		getWeightCombo().setVisible(showWeight && !showNone);
		expressionLabel.setVisible(!showWeight && !showNone);
		getExpressionPane().setVisible(!showWeight && !showNone);
	}

	@SuppressWarnings("unchecked")
	private void handleImport() {
		ModelElement<Organism> element = (ModelElement<Organism>) getOrganismCombo().getSelectedItem();
		final Organism organism = element.getItem();
		
		final NetworkProxy<NETWORK, NODE, EDGE> networkProxy = ((ModelElement<NetworkProxy<NETWORK, NODE, EDGE>>) getNetworkCombo().getSelectedItem()).getItem();
		final NETWORK cyNetwork = networkProxy.getProxied();
		
		final String idAttribute = (String) getIdCombo().getSelectedItem();
		final ModelElement<NetworkType> typeElement = (ModelElement<NetworkType>) getTypeCombo().getSelectedItem();

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
					network.setName( getNameTextField().getText());
					network.setDescription(getDescriptionTextArea().getText());
					
					InteractionNetworkGroup group = getGroupCombo().getGroup();
					
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
							String weightAttribute = (String) getWeightCombo().getSelectedItem();
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
		 getNameTextField().setText(""); //$NON-NLS-1$
		getDescriptionTextArea().setText(""); //$NON-NLS-1$
	}
	
	List<String> getExpressionAttributes() {
		final List<String> names = new ArrayList<String>();
		final JTable table = getExpressionTable();
		final int rowCount = table.getRowCount();
		
		for (int row = 0; row < rowCount; row++) {
			if ((Boolean) table.getValueAt(row, CHECK_COLUMN)) {
				names.add((String) table.getValueAt(row, NAME_COLUMN));
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

		@Override
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

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
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

		@Override
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

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 0;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			ExpressionTableElement element = get(rowIndex);
			
			if (element == null)
				return;
			if (columnIndex != 0)
				return;
			
			if (aValue instanceof Boolean)
				element.selected = (Boolean) aValue;
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
