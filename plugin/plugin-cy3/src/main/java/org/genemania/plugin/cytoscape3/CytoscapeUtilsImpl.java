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
package org.genemania.plugin.cytoscape3;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Paint;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.genemania.exception.ApplicationException;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.LogUtils;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.cytoscape.AbstractCytoscapeUtils;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.delegates.SelectionDelegate;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.proxies.EdgeProxy;
import org.genemania.plugin.proxies.NetworkProxy;
import org.genemania.plugin.proxies.NodeProxy;
import org.genemania.plugin.selection.NetworkSelectionManager;

public class CytoscapeUtilsImpl extends AbstractCytoscapeUtils<CyNetwork, CyNode, CyEdge> implements CytoscapeUtils<CyNetwork, CyNode, CyEdge>, RowsSetListener {

	private CySwingApplication application;
	private CyApplicationManager applicationManager;
	private CyNetworkManager networkManager;
	private CyNetworkFactory networkFactory;
	private CyNetworkViewFactory viewFactory;
	private CyNetworkViewManager viewManager;
	
	private final VisualStyleFactory styleFactory;
	private final VisualMappingManager mappingManager;
	private final VisualMappingFunctionFactory discreteFactory;
	private final VisualMappingFunctionFactory passthroughFactory;
	private final VisualMappingFunctionFactory continuousFactory;
	
	private final TaskManager<?, ?> taskManager;
	private final ApplyPreferredLayoutTaskFactory applyPreferredLayoutTaskFactory;
	private final RenderingEngineManager renderingEngineManager;

	Map<CyNetwork, Map<String, Reference<CyNode>>> nodes;
	Map<CyNetwork, Reference<CyNetworkView>> networkViews;
	Map<CyNetwork, Map<String, Reference<CyEdge>>> edges;
	Map<CyNetwork, VisualStyle> visualStyles;
	
	Map<CyTable, Reference<CyNetwork>> networksByNodeTable;
	Map<CyTable, Reference<CyNetwork>> networksByEdgeTable;
	
	private Object selectionMutex = new Object();
	private SelectionHandler selectionHandler;
	private CyEventHelper eventHelper;
	
	public CytoscapeUtilsImpl(NetworkUtils networkUtils,
							  CySwingApplication application, CyApplicationManager applicationManager,
							  CyNetworkManager networkManager, CyNetworkViewManager viewManager,
							  CyNetworkFactory networkFactory, CyNetworkViewFactory viewFactory,
							  VisualStyleFactory styleFactory, VisualMappingManager mappingManager,
							  VisualMappingFunctionFactory discreteFactory,
							  VisualMappingFunctionFactory passthroughFactory,
							  VisualMappingFunctionFactory continuousFactory, TaskManager<?, ?> taskManager,
							  CyEventHelper eventHelper, ApplyPreferredLayoutTaskFactory applyPreferredLayoutTaskFactory,
							  RenderingEngineManager renderingEngineManager) {
		super(networkUtils);
		this.application = application;
		this.applicationManager = applicationManager;
		this.networkManager = networkManager;
		this.networkFactory = networkFactory;
		this.viewFactory = viewFactory;
		this.viewManager = viewManager;
		
		this.styleFactory = styleFactory;
		this.mappingManager = mappingManager;
		this.discreteFactory = discreteFactory;
		this.passthroughFactory = passthroughFactory;
		this.continuousFactory = continuousFactory;

		this.applyPreferredLayoutTaskFactory = applyPreferredLayoutTaskFactory;
		this.taskManager = taskManager;
		this.eventHelper = eventHelper;
		this.renderingEngineManager = renderingEngineManager;
		
		nodes = new WeakHashMap<CyNetwork, Map<String, Reference<CyNode>>>();
		edges = new WeakHashMap<CyNetwork, Map<String, Reference<CyEdge>>>();
		networkViews = new WeakHashMap<CyNetwork, Reference<CyNetworkView>>();
		networksByNodeTable = new WeakHashMap<CyTable, Reference<CyNetwork>>();
		networksByEdgeTable = new WeakHashMap<CyTable, Reference<CyNetwork>>();
		visualStyles = new WeakHashMap<CyNetwork, VisualStyle>();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void applyVisualization(
			CyNetwork network,
			Map<Long, Double> scores,
			Map<String, Color> colors, double[] extrema) {
		
		VisualStyle style = styleFactory.createVisualStyle(getVisualStyleName(network));
		style.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
		style.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, RESULT_COLOR);

		// Node mappings
		style.addVisualMappingFunction(passthroughFactory.createVisualMappingFunction(GENE_NAME_ATTRIBUTE, String.class, BasicVisualLexicon.NODE_LABEL));

		double[] values = networkUtils.sortScores(scores);
		ContinuousMapping<Double, Double> mapping = (ContinuousMapping<Double, Double>) continuousFactory.createVisualMappingFunction(SCORE_ATTRIBUTE, Double.class, BasicVisualLexicon.NODE_SIZE);
		mapping.addPoint(values[0], new BoundaryRangeValues<Double>(MINIMUM_NODE_SIZE, MINIMUM_NODE_SIZE, MINIMUM_NODE_SIZE));
		mapping.addPoint(values[values.length - 1], new BoundaryRangeValues<Double>(MAXIMUM_NODE_SIZE, MAXIMUM_NODE_SIZE, MAXIMUM_NODE_SIZE));
		style.addVisualMappingFunction(mapping);
		
		DiscreteMapping<String, Paint> nodeFillMapping = (DiscreteMapping<String, Paint>) discreteFactory.createVisualMappingFunction(NODE_TYPE_ATTRIBUTE, String.class, BasicVisualLexicon.NODE_FILL_COLOR);
		nodeFillMapping.putMapValue(NODE_TYPE_QUERY, QUERY_COLOR);
		nodeFillMapping.putMapValue(NODE_TYPE_RESULT, RESULT_COLOR);
		nodeFillMapping.putMapValue(NODE_TYPE_ATTRIBUTE_NODE, RESULT_COLOR);
		style.addVisualMappingFunction(nodeFillMapping);

		DiscreteMapping<String, NodeShape> nodeShapeMapping = (DiscreteMapping<String, NodeShape>) discreteFactory.createVisualMappingFunction(NODE_TYPE_ATTRIBUTE, String.class, BasicVisualLexicon.NODE_SHAPE);
		nodeShapeMapping.putMapValue(NODE_TYPE_ATTRIBUTE_NODE, NodeShapeVisualProperty.DIAMOND);
		style.addVisualMappingFunction(nodeShapeMapping);

		// Edge mappings
		DiscreteMapping<String, Paint> edgeColorMapping = (DiscreteMapping<String, Paint>) discreteFactory.createVisualMappingFunction(NETWORK_GROUP_NAME_ATTRIBUTE, String.class, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
		edgeColorMapping.putAll(colors);
		style.addVisualMappingFunction(edgeColorMapping);
		
		ContinuousMapping<Double, Double> edgeWidthMapping = (ContinuousMapping<Double, Double>) continuousFactory.createVisualMappingFunction(MAX_WEIGHT_ATTRIBUTE, Double.class, BasicVisualLexicon.EDGE_WIDTH);
		edgeWidthMapping.addPoint(extrema[0], new BoundaryRangeValues<Double>(MINIMUM_EDGE_WIDTH, MINIMUM_EDGE_WIDTH, MINIMUM_EDGE_WIDTH));
		edgeWidthMapping.addPoint(extrema[1], new BoundaryRangeValues<Double>(MAXIMUM_EDGE_WIDTH, MAXIMUM_EDGE_WIDTH, MAXIMUM_EDGE_WIDTH));
		style.addVisualMappingFunction(edgeWidthMapping);
		
		DiscreteMapping<Integer, Integer> edgeTransparencyMapping = (DiscreteMapping<Integer, Integer>) discreteFactory.createVisualMappingFunction(HIGHLIGHT_ATTRIBUTE, Integer.class, BasicVisualLexicon.EDGE_TRANSPARENCY);
		edgeTransparencyMapping.putMapValue(0, 64);
		edgeTransparencyMapping.putMapValue(1, 255);
		style.addVisualMappingFunction(edgeTransparencyMapping);
	
		visualStyles.put(network, style);
		
		CytoPanel panel = application.getCytoPanel(CytoPanelName.EAST);
		if (panel.getState() == CytoPanelState.HIDE) {
			panel.setState(CytoPanelState.DOCK);
		}
	}

	private void setLabelPosition(CyNetworkView view, VisualStyle style) {
		// HACK: Label positions are specific to Ding's lexicon so we need
		// to jump through hoops to set them.
		for (RenderingEngine<?> engine : renderingEngineManager.getRenderingEngines(view)) {
			try {
				VisualLexicon lexicon = engine.getVisualLexicon();
				@SuppressWarnings("unchecked")
				VisualProperty<Object> property = (VisualProperty<Object>) lexicon.lookup(CyNode.class, "NODE_LABEL_POSITION");
				if (property == null) {
					return;
				}
				
				Object value = property.parseSerializableString("S,N,c,0.00,0.00");
				if (value == null) {
					return;
				}
				style.setDefaultValue(property, value);
				return;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public CyNetwork getCurrentNetwork() {
		return applicationManager.getCurrentNetwork();
	}

	@Override
	public Frame getFrame() {
		return application.getJFrame();
	}

	@Override
	public Set<CyNetwork> getNetworks() {
		return networkManager.getNetworkSet();
	}

	@Override
	public void handleNetworkPostProcessing(CyNetwork network) {
		CyNetworkView view = getView(network);

		VisualStyle style = visualStyles.get(network);
		mappingManager.addVisualStyle(style);
		mappingManager.setVisualStyle(style, view);

		networkManager.addNetwork(network);
		viewManager.addNetworkView(view);
		
		style.apply(view);
		setLabelPosition(view, style);
		view.updateView();
		repaint();
	}

	@Override
	public void maximize(CyNetwork network) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void performLayout(CyNetwork network) {
		CyNetworkView view = viewManager.getNetworkViews(network).iterator().next();		
		TaskIterator taskIterator = applyPreferredLayoutTaskFactory.createTaskIterator(Collections.singletonList(view));
		taskManager.execute(taskIterator);
	}

	@Override
	public void registerSelectionListener(
			CyNetwork cyNetwork,
			NetworkSelectionManager<CyNetwork, CyNode, CyEdge> manager,
			GeneMania<CyNetwork, CyNode, CyEdge> plugin) {
		networksByNodeTable.put(cyNetwork.getDefaultNodeTable(), new WeakReference<CyNetwork>(cyNetwork));
		networksByEdgeTable.put(cyNetwork.getDefaultEdgeTable(), new WeakReference<CyNetwork>(cyNetwork));
		
		synchronized (selectionMutex ) {
			if (selectionHandler == null) {
				selectionHandler = new SelectionHandler(manager, plugin);
			}
		}
	}
	
	@Override
	public void handleEvent(RowsSetEvent event) {
		synchronized (selectionMutex) {
			if (selectionHandler == null) {
				return;
			}
			
			if (!selectionHandler.getSelectionManager().isSelectionListenerEnabled()) {
				return;
			}
		}
		
		CyTable table = event.getSource();
		try {
			Reference<CyNetwork> reference = networksByNodeTable.get(table);
			if (reference != null) {
				CyNetwork network = reference.get();
				if (network != null) {
					selectionHandler.invoke(network, CyNode.class, event);
					return;
				}
			}
			reference = networksByEdgeTable.get(table);
			if (reference != null) {
				CyNetwork network = reference.get();
				if (network != null) {
					selectionHandler.invoke(network, CyEdge.class, event);
					return;
				}
			}
		} catch (ApplicationException e) {
			LogUtils.log(getClass(), e);
		}
	}

	@Override
	public void repaint() {
		application.getJFrame().repaint();
	}

	@Override
	public void updateVisualStyles(CyNetwork network) {
		boolean hasChanges = false;
		eventHelper.flushPayloadEvents();
		for (CyNetworkView view : viewManager.getNetworkViews(network)) {
			view.updateView();
			hasChanges = true;
		}
		if (hasChanges) {
			repaint();
		}
	}

	public Properties getGlobalProperties() {
		// TODO Auto-generated method stub
		return new Properties();
	}
	
	@Override
	protected NetworkProxy<CyNetwork, CyNode, CyEdge> createNetworkProxy(CyNetwork network) {
		return new NetworkProxyImpl(network, eventHelper);
	}
	
	@Override
	protected NodeProxy<CyNode> createNodeProxy(CyNode node, CyNetwork network) {
		return new NodeProxyImpl(node, network);
	}
	
	@Override
	protected EdgeProxy<CyEdge, CyNode> createEdgeProxy(CyEdge edge, CyNetwork network) {
		return new EdgeProxyImpl(edge, network);
	}
	
	@Override
	protected CyEdge getEdge(CyNode from, CyNode to, String type, String label, CyNetwork network) {
		Map<String, Reference<CyEdge>> networkEdges = edges.get(network);
		if (networkEdges == null) {
			networkEdges = new HashMap<String, Reference<CyEdge>>();
			edges.put(network, networkEdges);
		}
		
		String key = getEdgeKey(network, from, to, label);
		Reference<CyEdge> reference = networkEdges.get(key);
		if (reference != null) {
			CyEdge edge = reference.get();
			if (edge != null) {
				return edge;
			}
		}
		
		CyEdge edge = network.addEdge(from, to, false);
		network.getRow(edge).set(CyNetwork.NAME, key);
		networkEdges.put(key, new WeakReference<CyEdge>(edge));
		return edge;
	}
	
	protected String getEdgeKey(CyNetwork network, CyNode from, CyNode to, String label) {
		String fromLabel = network.getRow(from).get(CyNetwork.NAME, String.class);
		String toLabel = network.getRow(to).get(CyNetwork.NAME, String.class);
		return String.format("%s|%s|%s", fromLabel, toLabel, label);
	}
	
	@Override
	protected CyNetwork createNetwork(String title) {
		CyNetwork network = networkFactory.createNetwork();
		network.getRow(network).set(CyNetwork.NAME, title);
		return network;
	}
	
	@Override
	protected CyNode createNode(String id, CyNetwork network) {
		CyNode node = network.addNode();
		network.getRow(node).set(CyNetwork.NAME, id);
		
		Map<String, Reference<CyNode>> nodeMap = nodes.get(network);
		if (nodeMap == null) {
			nodeMap = new HashMap<String, Reference<CyNode>>();
			nodes.put(network, nodeMap);
		}
		nodeMap.put(id, new WeakReference<CyNode>(node));
		return node;
	}
	
	@Override
	protected CyNode getNode(String id, CyNetwork network) {
		Map<String, Reference<CyNode>> nodeMap = nodes.get(network);
		if (nodeMap == null) {
			return null;
		}
		Reference<CyNode> reference = nodeMap.get(id);
		if (reference == null) {
			return null;
		}
		return reference.get();
	}

	@Override
	protected CyEdge getEdge(String id, CyNetwork network) {
		Map<String, Reference<CyEdge>> edgeMap = edges.get(network);
		if (edgeMap == null) {
			return null;
		}
		Reference<CyEdge> reference = edgeMap.get(id);
		if (reference == null) {
			return null;
		}
		return reference.get();
	}

	CyNetworkView getView(CyNetwork network) {
		Reference<CyNetworkView> reference = networkViews.get(network);
		if (reference == null) {
			CyNetworkView view = viewFactory.createNetworkView(network);
			networkViews.put(network, new WeakReference<CyNetworkView>(view));
			return view;
		}
		return reference.get();
	}
	
	public class SelectionHandler extends SelectionDelegate<CyNetwork, CyNode, CyEdge> {

		private RowsSetEvent event;
		private Class<? extends CyIdentifiable> type;

		public SelectionHandler(NetworkSelectionManager<CyNetwork, CyNode, CyEdge> manager,
				GeneMania<CyNetwork, CyNode, CyEdge> plugin) {
			super(true, null, manager, plugin, CytoscapeUtilsImpl.this);
		}

		public void invoke(CyNetwork network, Class<? extends CyIdentifiable> type, RowsSetEvent event) throws ApplicationException {
			synchronized (manager) {
				this.event = event;
				this.type = type;
				this.network = network;
				
				for (RowSetRecord record : event.getPayloadCollection()) {
					if (CyNetwork.SELECTED.equals(record.getColumn())) {
						invoke();
						return;
					}
				}
			}
		}
		
		@Override
		protected void handleSelection(
				ViewState options)
				throws ApplicationException {
			
			if (!manager.isSelectionListenerEnabled()) {
				return;
			}
			
			if (type.equals(CyNode.class)) {
				for (RowSetRecord record : event.getPayloadCollection()) {
					if (!record.getColumn().equals(CyNetwork.SELECTED)) {
						continue;
					}
					boolean selected = (Boolean) record.getValue();
					String id = record.getRow().get(CyNetwork.NAME, String.class);
					CyNode node = getNode(id, network);
					NodeProxy<CyNode> nodeProxy = getNodeProxy(node, network);
					
					String name = nodeProxy.getAttribute(CytoscapeUtils.GENE_NAME_ATTRIBUTE, String.class);
					options.setGeneHighlighted(name, selected);
				}
			} else if (type.equals(CyEdge.class)) {
				NetworkProxy<CyNetwork, CyNode, CyEdge> networkProxy = getNetworkProxy(network);
				Set<CyEdge> previousSelection = networkProxy.getSelectedEdges();
				for (RowSetRecord record : event.getPayloadCollection()) {
					if (!record.getColumn().equals(CyNetwork.SELECTED)) {
						continue;
					}
					boolean selected = (Boolean) record.getValue();
					String id = record.getRow().get(CyNetwork.NAME, String.class);
					CyEdge edge = getEdge(id, network);
					if (selected || manager.checkSelectionState(edge, previousSelection, network)) {
						String groupName = network.getRow(edge).get(CytoscapeUtils.NETWORK_GROUP_NAME_ATTRIBUTE, String.class);
						Group<?, ?> group = options.getGroup(groupName);
						options.setGroupHighlighted(group, selected);
					}
				}
			}
		}

		public NetworkSelectionManager<CyNetwork, CyNode, CyEdge> getSelectionManager() {
			return manager;
		}
	}
}

