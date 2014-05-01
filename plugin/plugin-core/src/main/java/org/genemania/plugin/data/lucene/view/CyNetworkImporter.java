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

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.genemania.data.normalizer.NormalizationResult;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.proxies.EdgeProxy;
import org.genemania.plugin.proxies.NetworkProxy;
import org.genemania.plugin.proxies.NodeProxy;
import org.genemania.util.ProgressReporter;

public class CyNetworkImporter<NETWORK, NODE, EDGE> {
	private final CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils;

	public CyNetworkImporter(CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils) {
		this.cytoscapeUtils = cytoscapeUtils;
	}
	
	public void process(NETWORK network, String idAttribute, String weightAttribute, Writer output, ProgressReporter progress) {
		PrintWriter writer = new PrintWriter(output);
		try {
			Context context = new Context();
			NetworkProxy<NETWORK, NODE, EDGE> networkProxy = cytoscapeUtils.getNetworkProxy(network);
			Collection<EDGE> edges = networkProxy.getEdges();
			progress.setMaximumProgress(edges.size());
			for (EDGE edge : edges) {
				if (progress.isCanceled()) {
					return;
				}
				progress.setProgress(context.totalInteractions);
				context.totalInteractions++;
				
				EdgeProxy<EDGE, NODE> edgeProxy = cytoscapeUtils.getEdgeProxy(edge, network);
				String sourceSymbol = getSymbol(cytoscapeUtils.getNodeProxy(edgeProxy.getSource(), network), idAttribute);
				String targetSymbol = getSymbol(cytoscapeUtils.getNodeProxy(edgeProxy.getTarget(), network), idAttribute);
				if (sourceSymbol == null || targetSymbol == null) {
					context.droppedInteractions++;
					continue;
				}
				
				Double weight = null;
				if (weightAttribute != null) {
					Object rawValue = edgeProxy.getAttribute(weightAttribute, Object.class);
					if (!(rawValue instanceof Double) && !(rawValue instanceof Integer)) {
						context.droppedInteractions++;
						continue;
					}
	
					if (rawValue instanceof Double) {
						weight = (Double) rawValue;
					} else {
						weight = ((Integer) rawValue).doubleValue();
					}
				}
				
				if (weightAttribute != null) {
					// Weighted interaction
					writer.printf("%s\t%s\t%f\n", sourceSymbol, targetSymbol, weight); //$NON-NLS-1$
				} else {
					// Unweighted interaction
					writer.printf("%s\t%s\n", sourceSymbol, targetSymbol); //$NON-NLS-1$
				}
			}
		} finally {
			writer.close();
		}
	}
	
	public void process(NETWORK network, String idAttribute, List<String> expressionAttributes, Writer output, ProgressReporter progress) {
		PrintWriter writer = new PrintWriter(output);
		try {
			writer.print("IDENTIFIER"); //$NON-NLS-1$
			for (String attribute : expressionAttributes) {
				writer.print("\t"); //$NON-NLS-1$
				writer.print(attribute);
			}
			writer.println();
			
			Context context = new Context();
			
			NetworkProxy<NETWORK, NODE, EDGE> networkProxy = cytoscapeUtils.getNetworkProxy(network);
			Collection<NODE> nodes = networkProxy.getNodes();
			progress.setMaximumProgress(nodes.size());
			for (NODE node : nodes) {
				if (progress.isCanceled()) {
					return;
				}
				progress.setProgress(context.totalInteractions);
				context.totalInteractions++;
				
				NodeProxy<NODE> nodeProxy = cytoscapeUtils.getNodeProxy(node, network);
				String symbol = getSymbol(nodeProxy, idAttribute);
				if (symbol == null) {
					context.droppedInteractions++;
					continue;
				}

				writer.print(symbol);
				writer.print("\t"); //$NON-NLS-1$
				
				for (String attribute : expressionAttributes) {
					Double value = null;
					Class<?> type = nodeProxy.getAttributeType(attribute);
					Object rawValue = nodeProxy.getAttribute(attribute, type);
					if (rawValue instanceof Integer) {
						value = ((Integer) rawValue).doubleValue();
					} else if (rawValue instanceof Double) {
						value = (Double) rawValue;
					}
					if (value != null) {
						writer.print(value);
					}
					writer.print("\t"); //$NON-NLS-1$
				}
				writer.println();
			}
		} finally {
			writer.close();
		}
	}

	@SuppressWarnings("rawtypes")
	private String getSymbol(NodeProxy<NODE> nodeProxy, String idAttribute) {
		Class<?> type = nodeProxy.getAttributeType(idAttribute);
		Object value = nodeProxy.getAttribute(idAttribute, type);
		if (value instanceof String) {
			return nodeProxy.getAttribute(idAttribute, String.class);
		}
		if (value instanceof List) {
			for (Object item : (List) value) {
				if (item instanceof String) {
					return (String) item;
				}
			}
		}
		return null;
	}

	private NormalizationResult createResult(Context context) {
		NormalizationResult result = new NormalizationResult();
		result.setDroppedEntries(context.droppedInteractions);
		result.setTotalEntries(context.totalInteractions);
		result.setInvalidSymbols(context.invalidSymbols);
		return result;
	}

	public NormalizationResult normalize(NETWORK network, String idAttribute, List<String> expressionAttributes, Writer writer, ProgressReporter progress) {
		Context context = new Context();
		return createResult(context);
	}
	
	static class Context {
		public int droppedInteractions;
		public int totalInteractions;
		public Set<String> invalidSymbols;
		public Set<String> conflictingSymbols;
		
		public Context() {
			invalidSymbols = new HashSet<String>();
			conflictingSymbols = new HashSet<String>();
		}
	}
}
