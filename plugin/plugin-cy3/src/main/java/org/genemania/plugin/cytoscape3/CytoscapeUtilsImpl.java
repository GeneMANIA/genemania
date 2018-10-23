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

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_VISIBLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_FILL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_SIZE;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Paint;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineManager;
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
import org.genemania.plugin.Strings;
import org.genemania.plugin.cytoscape.AbstractCytoscapeUtils;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.cytoscape3.layout.GeneManiaFDLayout;
import org.genemania.plugin.delegates.SelectionDelegate;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.selection.SessionManager;
import org.genemania.util.ProgressReporter;

public class CytoscapeUtilsImpl extends AbstractCytoscapeUtils implements CytoscapeUtils, RowsSetListener {

	private final CySwingApplication application;
	private final CyApplicationManager applicationManager;
	private final CyTableManager tableManager;
	private final CyTableFactory tableFactory;
	private final CyNetworkManager networkManager;
	private final CyNetworkFactory networkFactory;
	private final CyNetworkViewFactory viewFactory;
	private final CyNetworkViewManager viewManager;
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
	
	private final Properties sessionProperties = new Properties();
	
	private final Object selectionMutex = new Object();
	private final Object propsMutex = new Object();
	private final Object prefsMutex = new Object();
	
	private SelectionHandler selectionHandler;
	private final CyEventHelper eventHelper;
	private final CyServiceRegistrar serviceRegistrar;
	
	public CytoscapeUtilsImpl(
			NetworkUtils networkUtils,
			CySwingApplication application,
			CyApplicationManager applicationManager,
			CyTableManager tableManager,
			CyTableFactory tableFactory,
			CyNetworkManager networkManager,
			CyNetworkViewManager viewManager,
			CyNetworkFactory networkFactory,
			CyNetworkViewFactory viewFactory,
			VisualStyleFactory styleFactory,
			VisualMappingManager mappingManager,
			VisualMappingFunctionFactory discreteFactory,
			VisualMappingFunctionFactory passthroughFactory,
			VisualMappingFunctionFactory continuousFactory,
			TaskManager<?, ?> taskManager,
			CyEventHelper eventHelper,
			ApplyPreferredLayoutTaskFactory applyPreferredLayoutTaskFactory,
			RenderingEngineManager renderingEngineManager,
			CyServiceRegistrar serviceRegistrar
	) {
		super(networkUtils);
		this.application = application;
		this.applicationManager = applicationManager;
		this.tableManager = tableManager;
		this.tableFactory = tableFactory;
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
		this.serviceRegistrar = serviceRegistrar;
		
		nodes = new WeakHashMap<>();
		edges = new WeakHashMap<>();
		networkViews = new WeakHashMap<>();
		networksByNodeTable = new WeakHashMap<>();
		networksByEdgeTable = new WeakHashMap<>();
		visualStyles = new WeakHashMap<>();
	}
	
	@Override
	public void applyVisualization(
			CyNetwork network,
			Map<Long, Double> scores,
			Map<String, Color> netColors,
			double[] extrema
	) {
		VisualStyle style = styleFactory.createVisualStyle(getVisualStyleName(network));
		style.setDefaultValue(NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
		style.setDefaultValue(NODE_FILL_COLOR, RESULT_COLOR);
		style.setDefaultValue(EDGE_TRANSPARENCY, DEF_EDGE_TRANSPARENCY);

		// Node mappings
		style.addVisualMappingFunction(passthroughFactory.createVisualMappingFunction(GENE_NAME_ATTRIBUTE, String.class, NODE_LABEL));

		double[] values = networkUtils.sortScores(scores);
		ContinuousMapping<Double, Double> mapping = (ContinuousMapping<Double, Double>) continuousFactory.createVisualMappingFunction(SCORE_ATTRIBUTE, Double.class, NODE_SIZE);
		mapping.addPoint(values[0], new BoundaryRangeValues<>(MINIMUM_NODE_SIZE, MINIMUM_NODE_SIZE, MINIMUM_NODE_SIZE));
		mapping.addPoint(values[values.length - 1], new BoundaryRangeValues<>(MAXIMUM_NODE_SIZE, MAXIMUM_NODE_SIZE, MAXIMUM_NODE_SIZE));
		style.addVisualMappingFunction(mapping);
		
		DiscreteMapping<String, Paint> nodeFillMapping = (DiscreteMapping<String, Paint>) discreteFactory.createVisualMappingFunction(NODE_TYPE_ATTRIBUTE, String.class, NODE_FILL_COLOR);
		nodeFillMapping.putMapValue(NODE_TYPE_QUERY, QUERY_COLOR);
		nodeFillMapping.putMapValue(NODE_TYPE_RESULT, RESULT_COLOR);
		nodeFillMapping.putMapValue(NODE_TYPE_ATTRIBUTE_NODE, RESULT_COLOR);
		style.addVisualMappingFunction(nodeFillMapping);

		DiscreteMapping<String, NodeShape> nodeShapeMapping = (DiscreteMapping<String, NodeShape>) discreteFactory.createVisualMappingFunction(NODE_TYPE_ATTRIBUTE, String.class, NODE_SHAPE);
		nodeShapeMapping.putMapValue(NODE_TYPE_ATTRIBUTE_NODE, NodeShapeVisualProperty.DIAMOND);
		style.addVisualMappingFunction(nodeShapeMapping);

		// Edge mappings
		DiscreteMapping<String, Paint> edgeColorMapping = (DiscreteMapping<String, Paint>) discreteFactory.createVisualMappingFunction(NETWORK_GROUP_NAME_ATTRIBUTE, String.class, EDGE_STROKE_UNSELECTED_PAINT);
		edgeColorMapping.putAll(netColors);
		style.addVisualMappingFunction(edgeColorMapping);
		
		ContinuousMapping<Double, Double> edgeWidthMapping = (ContinuousMapping<Double, Double>) continuousFactory.createVisualMappingFunction(MAX_WEIGHT_ATTRIBUTE, Double.class, EDGE_WIDTH);
		edgeWidthMapping.addPoint(extrema[0], new BoundaryRangeValues<>(MINIMUM_EDGE_WIDTH, MINIMUM_EDGE_WIDTH, MINIMUM_EDGE_WIDTH));
		edgeWidthMapping.addPoint(extrema[1], new BoundaryRangeValues<>(MAXIMUM_EDGE_WIDTH, MAXIMUM_EDGE_WIDTH, MAXIMUM_EDGE_WIDTH));
		style.addVisualMappingFunction(edgeWidthMapping);
	
		visualStyles.put(network, style);
		
		CytoPanel panel = application.getCytoPanel(CytoPanelName.EAST);
		
		if (panel.getState() == CytoPanelState.HIDE)
			panel.setState(CytoPanelState.DOCK);
	}
	
	@Override
	public CyServiceRegistrar getServiceRegistrar() {
		return serviceRegistrar;
	}
	
	@Override
	public String getSessionProperty(String key) {
		synchronized (propsMutex) {
			return sessionProperties.getProperty(key);
		}
	}
	
	@Override
	public void setSessionProperty(String key, String value) {
		synchronized (propsMutex) {
			sessionProperties.setProperty(key, value);
		}
	}
	
	@Override
	public void removeSessionProperty(String key) {
		synchronized (propsMutex) {
			sessionProperties.remove(key);
		}
	}
	
	@Override
	public String getPreference(String key) {
		synchronized (prefsMutex) {
			CyProperty<Properties> prefs = getPreferences();
			
			return prefs != null ? prefs.getProperties().getProperty(key) : null;
		}
	}
	
	@Override
	public void setPreference(String key, String value) {
		synchronized (prefsMutex) {
			CyProperty<Properties> prefs = getPreferences();
			
			if (prefs != null)
				prefs.getProperties().setProperty(key, value);
		}
	}
	
	@SuppressWarnings("unchecked")
	private CyProperty<Properties> getPreferences() {
		CyProperty<Properties> cyProps = null;
		
		try {
			cyProps = serviceRegistrar.getService(CyProperty.class,
					"(cyPropertyName=" + GeneMania.APP_CYPROPERTY_NAME + ")");
		} catch (Exception e) {
			// Ignore...
		}
		
		return cyProps;
	}
	
	@Override
	public void setHighlighted(final ViewState config, final CyNetwork network, final boolean visible) {
		final Collection<CyNetworkView> netViews = viewManager.getNetworkViews(network);
		
		for (final CyEdge edge : network.getEdgeList()) {
			for (final CyNetworkView nv : netViews) {
				final View<CyEdge> ev = nv.getEdgeView(edge);
				
				if (ev != null)
					ev.setLockedValue(EDGE_VISIBLE, visible);
			}
		}
		
		repaint();
	}	
	
	@Override
	public void setHighlight(final ViewState config, final Group<?, ?> source, final CyNetwork network,
			final boolean visible) {
		final Set<String> edgeIds = config.getEdgeIds(source);
		
		if (edgeIds == null)
			return;
		
		config.setEnabled(source, visible);
		final Collection<CyNetworkView> netViews = viewManager.getNetworkViews(network);
		
		for (final String edgeId : edgeIds) {
			final CyEdge edge = getEdge(edgeId, network);
			if (edge == null) continue;
			
			for (final CyNetworkView nv : netViews) {
				final View<CyEdge> ev = nv.getEdgeView(edge);
				
				if (ev != null)
					ev.setLockedValue(EDGE_VISIBLE, visible);
			}
		}
		
		repaint();
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
	public CyNetwork getNetwork(long suid) {
		return networkManager.getNetwork(suid);
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
		Collection<CyNetworkView> views = viewManager.getNetworkViews(network);
		GeneManiaFDLayout gmLayout = (GeneManiaFDLayout) serviceRegistrar.getService(CyLayoutAlgorithmManager.class)
				.getLayout(GeneManiaFDLayout.ALGORITHM_ID);
		
		if (gmLayout != null) {
			Object context = gmLayout.createLayoutContext();
			
			for (CyNetworkView nv : views) {
				Set<View<CyNode>> nodesToLayOut = new HashSet<>(nv.getNodeViews());
				taskManager.execute(gmLayout.createTaskIterator(nv, context, nodesToLayOut, null));
			}
		} else {
			TaskIterator taskIterator = applyPreferredLayoutTaskFactory.createTaskIterator(views);
			taskManager.execute(taskIterator);
		}
	}

	@Override
	public void registerSelectionListener(
			CyNetwork cyNetwork,
			SessionManager manager,
			GeneMania plugin
	) {
		networksByNodeTable.put(cyNetwork.getDefaultNodeTable(), new WeakReference<>(cyNetwork));
		networksByEdgeTable.put(cyNetwork.getDefaultEdgeTable(), new WeakReference<>(cyNetwork));
		
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

	@Override
	public Set<CyEdge> getSelectedEdges(CyNetwork network) {
		Set<CyEdge> results = new HashSet<>();
		
		for (CyEdge edge : network.getEdgeList()) {
			if (network.getRow(edge).get(CyNetwork.SELECTED, Boolean.class))
				results.add(edge);
		}
		
		return results;
	}
	
	@Override
	public Set<CyNode> getSelectedNodes(CyNetwork network) {
		Set<CyNode> results = new HashSet<>();
		
		for (CyNode node :network.getNodeList()) {
			if (network.getRow(node).get(CyNetwork.SELECTED, Boolean.class))
				results.add(node);
		}
		
		return results;
	}

	@Override
	public String getTitle(CyNetwork network) {
		return network.getRow(network).get(CyNetwork.NAME, String.class);
	}

	@Override
	public void setSelectedEdge(CyNetwork network, CyEdge edge, boolean selected) {
		network.getRow(edge).set(CyNetwork.SELECTED, selected);
		eventHelper.flushPayloadEvents();
	}

	@Override
	public void setSelectedEdges(CyNetwork network, Collection<CyEdge> edges, boolean selected) {
		for (CyEdge edge : edges)
			network.getRow(edge).set(CyNetwork.SELECTED, selected);
		
		eventHelper.flushPayloadEvents();
	}

	@Override
	public void setSelectedNode(CyNetwork network, CyNode node, boolean selected) {
		network.getRow(node).set(CyNetwork.SELECTED, selected);
		eventHelper.flushPayloadEvents();
	}

	@Override
	public void setSelectedNodes(CyNetwork network, Collection<CyNode> nodes, boolean selected) {
		for (CyNode node : nodes)
			network.getRow(node).set(CyNetwork.SELECTED, selected);
		
		eventHelper.flushPayloadEvents();
	}

	@Override
	public void unselectAllEdges(CyNetwork network) {
		setSelectedEdges(network, network.getEdgeList(), false);
	}

	@Override
	public void unselectAllNodes(CyNetwork network) {
		setSelectedNodes(network, network.getNodeList(), false);
	}
	
	@Override
	public Collection<String> getNodeAttributeNames(CyNetwork network) {
		CyTable table = network.getDefaultNodeTable();
		
		return getNames(table.getColumns(), network);
	}
	
	@Override
	public Collection<String> getEdgeAttributeNames(CyNetwork network) {
		CyTable table = network.getDefaultEdgeTable();
		
		return getNames(table.getColumns(), network);
	}

	@Override
	public Collection<String> getNames(Collection<CyColumn> columns, CyNetwork network) {
		Set<String> names = new HashSet<>();
		
		for (CyColumn column : columns)
			names.add(column.getName());
		
		return names;
	}
	
	@Override
	public boolean isGeneManiaNetwork(CyNetwork network) {
		return network != null && getDataVersion(network) != null;
	}
	
	@Override
	public String getDataVersion(CyNetwork network) {
		return network.getRow(network).get(DATA_VERSION_ATTRIBUTE, String.class);
	}
	
	@Override
	public Collection<CyNode> getNeighbours(CyNode node, CyNetwork network) {
		return network.getNeighborList(node, Type.ANY);
	}
	
	@Override
	protected CyEdge getEdge(CyNode from, CyNode to, String type, String label, CyNetwork network) {
		Map<String, Reference<CyEdge>> networkEdges = edges.get(network);
		
		if (networkEdges == null) {
			networkEdges = new HashMap<>();
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
		networkEdges.put(key, new WeakReference<>(edge));
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
			nodeMap = new HashMap<>();
			nodes.put(network, nodeMap);
		}
		
		
		nodeMap.put(id, new WeakReference<>(node));
		return node;
	}
	
	@Override
	protected CyNode getNode(String id, CyNetwork network) {
		Map<String, Reference<CyNode>> nodeMap = nodes.get(network);
		
		if (nodeMap == null)
			return null;
		
		Reference<CyNode> reference = nodeMap.get(id);
		
		if (reference == null)
			return null;
		
		return reference.get();
	}

	@Override
	protected CyEdge getEdge(String id, CyNetwork network) {
		Map<String, Reference<CyEdge>> edgeMap = edges.get(network);
		
		if (edgeMap == null) {
			if (!isGeneManiaNetwork(network))
				return null;
			
			edgeMap = cacheEdges(network);
			edges.put(network, edgeMap);
		}
		
		Reference<CyEdge> reference = edgeMap.get(id);
		
		if (reference == null)
			return null;
		
		return reference.get();
	}

	private Map<String, Reference<CyEdge>> cacheEdges(CyNetwork network) {
		HashMap<String, Reference<CyEdge>> cachedEdges = new HashMap<>();
		
		for (CyEdge edge : network.getEdgeList()) {
			CyRow row = network.getRow(edge);
			
			if (row.get(MAX_WEIGHT_ATTRIBUTE, Double.class) == null)
				continue;
			
			String name = row.get(CyNetwork.NAME, String.class);
			cachedEdges.put(name, new WeakReference<>(edge));
		}
		
		return cachedEdges;
	}

	CyNetworkView getView(CyNetwork network) {
		Reference<CyNetworkView> reference = networkViews.get(network);
		
		if (reference == null) {
			CyNetworkView view = viewFactory.createNetworkView(network);
			networkViews.put(network, new WeakReference<>(view));
			return view;
		}
		
		return reference.get();
	}
	
	@Override
	public void saveSessionState(Map<Long, ViewState> states) {
		// Serialize the ViewStates of genemania networks that were searched on the server
		// (i.e. not from locally installed data)...
		
		// First recreate GeneMANIA's hidden global table
		CyTable table = getGlobalTable(GENEMANIA_VIEWS_TABLE);
		
		if (table != null)
			tableManager.deleteTable(table.getSUID());
		
		table = tableFactory.createTable(GENEMANIA_VIEWS_TABLE, GENEMANIA_VIEWS_PK_ATTRIBUTE, Integer.class, false, true);
		table.createColumn(NETWORK_SUID_ATTRIBUTE, Long.class, true);
		table.createColumn(STATE_ATTRIBUTE, String.class, true);
		tableManager.addTable(table);
		
		// Serialize ViewState objects as Base 64
		int id = 1;
		
		for (Entry<Long, ViewState> entry : states.entrySet()) {
			Long netId = entry.getKey();
			ViewState options = entry.getValue();
			CyNetwork net = getNetwork(netId);
			
			if (net == null || options == null)
				continue;
			
			String dataVersion = getDataVersion(net);
			
			if (dataVersion != null && dataVersion.endsWith(WEB_VERSION_TAG)) {
		        try (
		        		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		        		ObjectOutputStream out = new ObjectOutputStream(baos)
		        	) {
		            out.writeObject(options);
		            out.flush();
		            byte[] bytes = baos.toByteArray();
		            String hex = Base64.getEncoder().encodeToString(bytes);
		            
		            CyRow row = table.getRow(id++);
					row.set(NETWORK_SUID_ATTRIBUTE, netId);
					row.set(STATE_ATTRIBUTE, hex);
		        } catch (Exception ex) {
		        		ex.printStackTrace();
		            LogUtils.log(getClass(), ex);
		        }
			}
		}
	}

	@Override
	public Map<CyNetwork, ViewState> restoreSessionState(ProgressReporter progress) {
		Map<CyNetwork, ViewState> states = new HashMap<>();
		CyTable table = getGlobalTable(GENEMANIA_VIEWS_TABLE);
		
		if (table != null && table.getRowCount() > 0) {
			progress.setStatus(Strings.resultReconstructor_status);
			int currentProgress = 0;
			progress.setMaximumProgress(table.getRowCount());
			progress.setProgress(currentProgress);
			
			for (CyRow row : table.getAllRows()) {
				Long netId = row.get(NETWORK_SUID_ATTRIBUTE, Long.class);
				String hex = row.get(STATE_ATTRIBUTE, String.class);
				
				if (netId != null && hex != null) {
					// The network SUID changes when the session is reloaded!
					CyNetwork network = getNetwork(netId);
					byte[] bytes = Base64.getDecoder().decode(hex);
					
					if (network != null) {
						try (
							ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
							ObjectInputStream in = new ObjectInputStream(bais)
						) {
				            ViewState options = (ViewState) in.readObject();
				            
							if (options != null)
								states.put(network, options);
				        } catch (Exception ex) {
				        		ex.printStackTrace();
				            LogUtils.log(getClass(), ex);
				        }
					}
				}
				
				progress.setProgress(++currentProgress);
			}
			
			// Always delete the serialization table to save RAM!
			tableManager.deleteTable(table.getSUID());
		}
		
		return states;
	}
	
	@Override
	public void removeSavedSessionState(Long networkId) {
		CyTable table = getGlobalTable(GENEMANIA_VIEWS_TABLE);
		
		if (table != null) {
			Collection<CyRow> rows = table.getMatchingRows(NETWORK_SUID_ATTRIBUTE, networkId);
			rows.forEach(r -> table.deleteRows(Collections.singleton(r.get(GENEMANIA_VIEWS_PK_ATTRIBUTE, Integer.class))));
		}
	}
	
	@Override
	public void clearSavedSessionState() {
		CyTable table = getGlobalTable(GENEMANIA_VIEWS_TABLE);
		
		if (table != null)
			tableManager.deleteTable(table.getSUID());
	}
	
	private CyTable getGlobalTable(String title) {
		Set<CyTable> globalTables = tableManager.getGlobalTables();
		
		for (CyTable table : globalTables) {
			if (table.getTitle().equals(title))
				return table;
		}
		
		return null;
	}

	public class SelectionHandler extends SelectionDelegate {

		private RowsSetEvent event;
		private Class<? extends CyIdentifiable> type;

		public SelectionHandler(SessionManager manager,
				GeneMania plugin) {
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
		protected void handleSelection(ViewState options) throws ApplicationException {
			if (!manager.isSelectionListenerEnabled())
				return;
			
			if (type.equals(CyNode.class)) {
				for (RowSetRecord record : event.getPayloadCollection()) {
					if (!record.getColumn().equals(CyNetwork.SELECTED))
						continue;
					
					boolean selected = (Boolean) record.getValue();
					String id = record.getRow().get(CyNetwork.NAME, String.class);
					CyNode node = getNode(id, network);
					
					String name = getAttribute(network, node, CytoscapeUtils.GENE_NAME_ATTRIBUTE, String.class);
					options.setGeneHighlighted(name, selected);
				}
			} else if (type.equals(CyEdge.class)) {
				Set<CyEdge> previousSelection = getSelectedEdges(network);
				
				for (RowSetRecord record : event.getPayloadCollection()) {
					if (!record.getColumn().equals(CyNetwork.SELECTED))
						continue;
					
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

		public SessionManager getSelectionManager() {
			return manager;
		}
	}
}

