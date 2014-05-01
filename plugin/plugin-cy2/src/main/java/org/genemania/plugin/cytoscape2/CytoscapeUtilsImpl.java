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

package org.genemania.plugin.cytoscape2;

import java.awt.Color;
import java.awt.Frame;
import java.beans.PropertyVetoException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.JInternalFrame;

import org.genemania.plugin.GeneMania;
import org.genemania.plugin.LogUtils;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.cytoscape.AbstractCytoscapeUtils;
import org.genemania.plugin.cytoscape2.layout.FilteredLayout;
import org.genemania.plugin.cytoscape2.support.Compatibility;
import org.genemania.plugin.cytoscape2.support.Compatibility.MappingType;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.proxies.EdgeProxy;
import org.genemania.plugin.proxies.NetworkProxy;
import org.genemania.plugin.proxies.NodeProxy;
import org.genemania.plugin.selection.NetworkSelectionManager;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.CytoscapeInit;
import cytoscape.actions.GinyUtils;
import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;
import cytoscape.layout.CyLayoutAlgorithm;
import cytoscape.layout.CyLayouts;
import cytoscape.view.CyNetworkView;
import cytoscape.view.NetworkViewManager;
import cytoscape.visual.CalculatorCatalog;
import cytoscape.visual.EdgeAppearanceCalculator;
import cytoscape.visual.NodeAppearanceCalculator;
import cytoscape.visual.NodeShape;
import cytoscape.visual.VisualMappingManager;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.VisualStyle;
import cytoscape.visual.calculators.BasicCalculator;
import cytoscape.visual.calculators.Calculator;
import cytoscape.visual.mappings.BoundaryRangeValues;
import cytoscape.visual.mappings.ContinuousMapping;
import cytoscape.visual.mappings.DiscreteMapping;
import cytoscape.visual.mappings.LinearNumberToNumberInterpolator;
import cytoscape.visual.mappings.PassThroughMapping;

public class CytoscapeUtilsImpl extends AbstractCytoscapeUtils<CyNetwork, CyNode, CyEdge> {

	private final Compatibility compatibility;
	private final Map<String, Reference<CyEdge>> edges;
	private ReferenceQueue<CyEdge> referenceQueue;

	public CytoscapeUtilsImpl(NetworkUtils networkUtils, Compatibility compatibility) {
		super(networkUtils);
		edges = new WeakHashMap<String, Reference<CyEdge>>();
		this.compatibility = compatibility;
		
		referenceQueue = new ReferenceQueue<CyEdge>();
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Reference<? extends CyEdge> reference = referenceQueue.remove();
						edges.values().remove(reference);
					} catch (InterruptedException e) {
						throw new RuntimeException();
					}
				}
			}
		}).start();
	}
	
	@SuppressWarnings("rawtypes")
	static void setAttributeInternal(CyAttributes attributes, String identifier, String key, Object value) {
		if (value == null) {
			if (!attributes.hasAttribute(identifier, key)) {
				return;
			}
			attributes.deleteAttribute(identifier, key);
			return;
		}

		if (value instanceof Long) {
			attributes.setAttribute(identifier, key, ((Long) value).intValue());
		} else if (value instanceof Integer) {
			attributes.setAttribute(identifier, key, (Integer) value);
		} else if (value instanceof String) {
			attributes.setAttribute(identifier, key, (String) value);
		} else if (value instanceof Double) {
			attributes.setAttribute(identifier, key, (Double) value);
		} else if (value instanceof Boolean) {
			attributes.setAttribute(identifier, key, (Boolean) value);
		} else if (value instanceof List) {
			attributes.setListAttribute(identifier, key, (List) value);
		}
	}

	private Calculator createEdgeWidthCalculator(CyNetwork network, double minWeight, double maxWeight, double minSize, double maxSize) {
		VisualPropertyType type = VisualPropertyType.EDGE_LINE_WIDTH;
		Object defaultObject = type.getDefault(Cytoscape.getVisualMappingManager().getVisualStyle());
		ContinuousMapping mapping = compatibility.createContinuousMapping(defaultObject, MAX_WEIGHT_ATTRIBUTE, network, MappingType.EDGE);
		mapping.setInterpolator(new LinearNumberToNumberInterpolator());
		mapping.addPoint(minWeight, new BoundaryRangeValues(minSize, minSize, minSize));
		mapping.addPoint(maxWeight, new BoundaryRangeValues(maxSize, maxSize, maxSize));
		return new BasicCalculator(String.format("edgeWidth-%s", escapeTitle(network.getTitle())), mapping, type); //$NON-NLS-1$
	}
	
	private Calculator createEdgeOpacityCalculator(CyNetwork network) {
		VisualPropertyType type = VisualPropertyType.EDGE_OPACITY;
		Object defaultObject = type.getDefault(Cytoscape.getVisualMappingManager().getVisualStyle());
		DiscreteMapping mapping = compatibility.createDiscreteMapping(defaultObject, HIGHLIGHT_ATTRIBUTE, network, MappingType.EDGE); 
		mapping.putMapValue(1, 255);
		mapping.putMapValue(0, 64);
		return new BasicCalculator("Dynamic calculator", mapping, type); //$NON-NLS-1$
	}
	
	private Calculator createEdgeColourCalculator(CyNetwork network, Map<String, Color> colors) {
		VisualPropertyType type = VisualPropertyType.EDGE_COLOR;
		Object defaultObject = type.getDefault(Cytoscape.getVisualMappingManager().getVisualStyle());
		DiscreteMapping mapping = compatibility.createDiscreteMapping(defaultObject, NETWORK_GROUP_NAME_ATTRIBUTE, network, MappingType.EDGE);
		for (Entry<String, Color> entry : colors.entrySet()) {
			mapping.putMapValue(entry.getKey(), entry.getValue());
		}
		return new BasicCalculator(String.format("edgeColour-%s", escapeTitle(network.getTitle())), mapping, type); //$NON-NLS-1$
	}
	
	private String escapeTitle(String title) {
		return title.replaceAll("[.]", "_"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Calculator createNodeColourCalculator(CyNetwork network) {
		VisualPropertyType type = VisualPropertyType.NODE_FILL_COLOR;
		Object defaultObject = type.getDefault(Cytoscape.getVisualMappingManager().getVisualStyle());
		DiscreteMapping mapping = compatibility.createDiscreteMapping(defaultObject, NODE_TYPE_ATTRIBUTE, network, MappingType.NODE);

		mapping.putMapValue(NODE_TYPE_QUERY, QUERY_COLOR);
		mapping.putMapValue(NODE_TYPE_RESULT, RESULT_COLOR);
		mapping.putMapValue(NODE_TYPE_ATTRIBUTE_NODE, RESULT_COLOR);
		return new BasicCalculator("Type-based calculator", mapping, type); //$NON-NLS-1$
	}

	private Calculator createNodeSizeCalculator(CyNetwork network, Map<Long, Double> scores, double minSize, double maxSize) {
		VisualPropertyType type = VisualPropertyType.NODE_SIZE;
		Object defaultObject = type.getDefault(Cytoscape.getVisualMappingManager().getVisualStyle());
		ContinuousMapping mapping = compatibility.createContinuousMapping(defaultObject, SCORE_ATTRIBUTE, network, MappingType.NODE);

		double[] values = networkUtils.sortScores(scores);
		mapping.setInterpolator(new LinearNumberToNumberInterpolator());
		mapping.addPoint(values[0], new BoundaryRangeValues(minSize, minSize, minSize));
		mapping.addPoint(values[values.length - 1], new BoundaryRangeValues(maxSize, maxSize, maxSize));
		return new BasicCalculator(String.format("nodeSize-%s", escapeTitle(network.getTitle())), mapping, type); //$NON-NLS-1$
	}
	
	public void applyVisualization(CyNetwork network, Map<Long, Double> scores, Map<String, Color> colors, double[] extrema) {
		VisualMappingManager manager = Cytoscape.getVisualMappingManager();
		CalculatorCatalog catalog = manager.getCalculatorCatalog();
		String styleName = getVisualStyleName(network);
		VisualStyle style = catalog.getVisualStyle(styleName);
		if (style == null) {
			style = new VisualStyle(styleName);
		}
		
		NodeAppearanceCalculator nodeAppearance = style.getNodeAppearanceCalculator();
		nodeAppearance.setCalculator(createNodeColourCalculator(network));
		nodeAppearance.setCalculator(createNodeLabelCalculator());
		nodeAppearance.setCalculator(createNodeShapeCalculator(network));
		
		nodeAppearance.setCalculator(createNodeSizeCalculator(network, scores, MINIMUM_NODE_SIZE, MAXIMUM_NODE_SIZE));
		
		EdgeAppearanceCalculator edgeAppearance = style.getEdgeAppearanceCalculator();
		edgeAppearance.setCalculator(createEdgeColourCalculator(network, colors));
		edgeAppearance.setCalculator(createEdgeOpacityCalculator(network));
		
		edgeAppearance.setCalculator(createEdgeWidthCalculator(network, extrema[0], extrema[1], MINIMUM_EDGE_WIDTH, MAXIMUM_EDGE_WIDTH));
		
		VisualPropertyType.NODE_LABEL_POSITION.setDefault(style, compatibility.createDefaultNodeLabelPosition());
		VisualPropertyType.NODE_SHAPE.setDefault(style, NodeShape.ELLIPSE);
		
		catalog.removeVisualStyle(styleName);
		catalog.addVisualStyle(style);
		manager.setVisualStyle(style);
		
		CyNetworkView networkView = getNetworkView(network);
		networkView.setVisualStyle(styleName);
		
		manager.applyNodeAppearances(network, networkView);
	}

	private Calculator createNodeShapeCalculator(CyNetwork network) {
		VisualPropertyType type = VisualPropertyType.NODE_SHAPE;
		Object defaultObject = type.getDefault(Cytoscape.getVisualMappingManager().getVisualStyle());
		DiscreteMapping mapping = compatibility.createDiscreteMapping(defaultObject, NODE_TYPE_ATTRIBUTE, network, MappingType.NODE);
		mapping.putMapValue(NODE_TYPE_ATTRIBUTE_NODE, NodeShape.DIAMOND);
		return new BasicCalculator("Gene name calculator", mapping, type); //$NON-NLS-1$
	}

	private CyNetworkView getNetworkView(CyNetwork network) {
		String id = network.getIdentifier();
		if (!Cytoscape.viewExists(id)) {
			return Cytoscape.createNetworkView(network);
		}
		return Cytoscape.getNetworkView(id);
	}

	public void performLayout(CyNetwork network) {
		CyNetworkView view = getNetworkView(network);
		CyLayoutAlgorithm layout = CyLayouts.getLayout(FilteredLayout.ID);
		if (layout == null) {
			layout = CyLayouts.getDefaultLayout();
		}
		layout.doLayout(view);
	}
	
	private Calculator createNodeLabelCalculator() {
		VisualPropertyType type = VisualPropertyType.NODE_LABEL;
		Object defaultObject = type.getDefault(Cytoscape.getVisualMappingManager().getVisualStyle());
		PassThroughMapping mapping = compatibility.createPassThroughMapping(defaultObject, GENE_NAME_ATTRIBUTE);
		return new BasicCalculator("Gene name calculator", mapping, type); //$NON-NLS-1$
	}

	void setVisibility(GeneMania<CyNetwork, CyNode, CyEdge> plugin, ViewState config, Group<?, ?> source, CyNetwork network, boolean selected) {
		NetworkSelectionManager<CyNetwork, CyNode, CyEdge> manager = plugin.getNetworkSelectionManager();
		boolean listenerEnabled = manager.isSelectionListenerEnabled();
		manager.setSelectionListenerEnabled(false);

		CyNetworkView view = getNetworkView(network);
		if (!selected && source == null) {
			GinyUtils.hideAllEdges(view);
			return;
		}
		if (selected && source == null) {
			GinyUtils.unHideAllEdges(view);
			return;
		}
		
		try {
			config.setEnabled(source, selected);
			
			if (selected) {
				for (Object edgeId : config.getEdgeIds(source)) {
					CyEdge edge = Cytoscape.getRootGraph().getEdge((String) edgeId);
					view.showGraphObject(view.getEdgeView(edge));
				}
			} else {
				for (Object edgeId : config.getEdgeIds(source)) {
					CyEdge edge = Cytoscape.getRootGraph().getEdge((String) edgeId);
					view.hideGraphObject(view.getEdgeView(edge));
				}
			}
		} finally {
			manager.setSelectionListenerEnabled(listenerEnabled);
		}
		view.updateView();
		Cytoscape.getVisualMappingManager().applyEdgeAppearances(network, view);
	}

	@Override
	public void registerSelectionListener(CyNetwork network, NetworkSelectionManager<CyNetwork, CyNode, CyEdge> manager, GeneMania<CyNetwork, CyNode, CyEdge> plugin) {
		NetworkSelectEventListener listener = new NetworkSelectEventListener(network, manager, plugin, this);
		network.addSelectEventListener(listener);
	}
	
	@Override
	public CyNetwork getCurrentNetwork() {
		return Cytoscape.getCurrentNetwork();
	}
	
	@Override
	public void repaint() {
		Cytoscape.getDesktop().repaint();
	}
	
	@Override
	public void updateVisualStyles(CyNetwork network) {
		VisualMappingManager manager = Cytoscape.getVisualMappingManager();
		CyNetworkView networkView = getNetworkView(network);
		manager.applyEdgeAppearances(network, networkView);
	}
	
	@Override
	public void maximize(CyNetwork network) {
		NetworkViewManager viewManager = Cytoscape.getDesktop().getNetworkViewManager();
		CyNetworkView view = getNetworkView(network);
		JInternalFrame frame = viewManager.getInternalFrame(view);
		try {
			frame.setMaximum(true);
		} catch (PropertyVetoException e) {
			LogUtils.log(getClass(), e);
		}
	}
	
	@Override
	public Frame getFrame() {
		return Cytoscape.getDesktop();
	}
	
	@Override
	public Set<CyNetwork> getNetworks() {
		return Cytoscape.getNetworkSet();
	}
	
	@Override
	public void handleNetworkPostProcessing(CyNetwork network) {
		// HACK: For some odd reason, firing the events below as part
		// of a task prevents the events from firing.  Not sure
		// why this is happening but that's why these property changes
		// are here.
		Cytoscape.firePropertyChange(Cytoscape.NETWORK_MODIFIED, null, null);
		Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, null);
	}
	
	@SuppressWarnings("unchecked")
	static <T> T getAttributeInternal(CyAttributes attributes, String id, String name, Class<T> type) {
		if (type.equals(String.class)) {
			return (T) attributes.getStringAttribute(id, name);
		} else if (type.equals(Long.class)) {
			return (T) attributes.getIntegerAttribute(id, name);
		} else if (type.equals(Integer.class)) {
			return (T) attributes.getIntegerAttribute(id, name);
		} else if (type.equals(Double.class)) {
			return (T) attributes.getDoubleAttribute(id, name);
		} else if (type.equals(Boolean.class)) {
			return (T) attributes.getBooleanAttribute(id, name);
		} else if (type.equals(List.class)) {
			return (T) attributes.getListAttribute(id, name);
		}
		return (T) attributes.getAttribute(id, name);
	}

	@Override
	protected NetworkProxy<CyNetwork, CyNode, CyEdge> createNetworkProxy(CyNetwork network) {
		return new NetworkProxyImpl(network);
	}
	
	@Override
	protected NodeProxy<CyNode> createNodeProxy(CyNode node, CyNetwork network) {
		return new NodeProxyImpl(node);
	}
	
	@Override
	protected EdgeProxy<CyEdge, CyNode> createEdgeProxy(CyEdge edge, CyNetwork network) {
		return new EdgeProxyImpl(edge);
	}
	
	@Override
	public Properties getGlobalProperties() {
		return CytoscapeInit.getProperties();
	}
	
	@Override
	protected CyNode getNode(String id, CyNetwork network) {
		return Cytoscape.getCyNode(id);
	}

	@Override
	protected CyNode createNode(String id, CyNetwork network) {
		CyNode node = Cytoscape.getCyNode(id, true);
		network.addNode(node);
		return node;
	}
	
	@Override
	protected CyNetwork createNetwork(String title) {
		return Cytoscape.createNetwork(title, false);
	}

	@Override
	protected CyEdge getEdge(String id, CyNetwork network) {
		Reference<CyEdge> reference = edges.get(id);
		if (reference == null) {
			// We haven't cached this edge yet.  Eagerly cache entire network
			// just in case.
			cacheNetwork(network);
			
			reference = edges.get(id);
			if (reference == null) {
				// This edge really doesn't exist.
				return null;
			}
		}
		return reference.get();
	}
	
	private void cacheNetwork(CyNetwork network) {
		@SuppressWarnings("unchecked")
		Iterator<CyEdge> iterator = network.edgesIterator();
		while (iterator.hasNext()) {
			CyEdge edge = iterator.next();
			edges.put(edge.getIdentifier(), new WeakReference<CyEdge>(edge, referenceQueue));
		}
	}

	@Override
	protected CyEdge getEdge(CyNode from, CyNode to, String type, String label, CyNetwork network) {
		CyEdge edge = Cytoscape.getCyEdge(from, to, Semantics.INTERACTION, label, true);
		network.addEdge(edge);
		edges.put(edge.getIdentifier(), new WeakReference<CyEdge>(edge, referenceQueue));
		return edge;
	}
}
