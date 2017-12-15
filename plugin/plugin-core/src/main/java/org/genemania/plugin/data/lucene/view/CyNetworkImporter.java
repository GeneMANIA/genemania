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

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.genemania.data.normalizer.NormalizationResult;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.util.ProgressReporter;

public class CyNetworkImporter {
	
	private final CytoscapeUtils cytoscapeUtils;

	public CyNetworkImporter(CytoscapeUtils cytoscapeUtils) {
		this.cytoscapeUtils = cytoscapeUtils;
	}
	
	public void process(CyNetwork network, String idAttribute, String weightAttribute, Writer output,
			ProgressReporter progress) {
		PrintWriter writer = new PrintWriter(output);
		
		try {
			Context context = new Context();
			Collection<CyEdge> edges = network.getEdgeList();
			progress.setMaximumProgress(edges.size());
			
			for (CyEdge edge : edges) {
				if (progress.isCanceled())
					return;
				
				progress.setProgress(context.totalInteractions);
				context.totalInteractions++;
				
				String sourceSymbol = getSymbol(edge.getSource(), network, idAttribute);
				String targetSymbol = getSymbol(edge.getTarget(), network, idAttribute);
				
				if (sourceSymbol == null || targetSymbol == null) {
					context.droppedInteractions++;
					continue;
				}
				
				Double weight = null;
				
				if (weightAttribute != null) {
					Object rawValue = cytoscapeUtils.getAttribute(network, edge, weightAttribute, Object.class);
					
					if (!(rawValue instanceof Double) && !(rawValue instanceof Integer)) {
						context.droppedInteractions++;
						continue;
					}
	
					if (rawValue instanceof Double)
						weight = (Double) rawValue;
					else
						weight = ((Integer) rawValue).doubleValue();
				}
				
				if (weightAttribute != null) {
					// Weighted interaction
					writer.printf("%s\t%s\t%s\n", sourceSymbol, targetSymbol, Double.toString(weight)); //$NON-NLS-1$
				} else {
					// Unweighted interaction
					writer.printf("%s\t%s\n", sourceSymbol, targetSymbol); //$NON-NLS-1$
				}
			}
		} finally {
			writer.close();
		}
	}
	
	public void process(CyNetwork network, String idAttribute, List<String> expressionAttributes, Writer output,
			ProgressReporter progress) {
		PrintWriter writer = new PrintWriter(output);
		
		try {
			writer.print("IDENTIFIER"); //$NON-NLS-1$
			
			for (String attribute : expressionAttributes) {
				writer.print("\t"); //$NON-NLS-1$
				writer.print(attribute);
			}
			
			writer.println();
			
			Context context = new Context();
			Collection<CyNode> nodes = network.getNodeList();
			progress.setMaximumProgress(nodes.size());
			
			for (CyNode node : nodes) {
				if (progress.isCanceled())
					return;
				
				progress.setProgress(context.totalInteractions);
				context.totalInteractions++;
				
				String symbol = getSymbol(node, network, idAttribute);
				
				if (symbol == null) {
					context.droppedInteractions++;
					continue;
				}

				writer.print(symbol);
				writer.print("\t"); //$NON-NLS-1$
				
				for (String attribute : expressionAttributes) {
					Double value = null;
					Class<?> type = cytoscapeUtils.getAttributeType(network, node, attribute);
					Object rawValue = cytoscapeUtils.getAttribute(network, node, attribute, type);
					
					if (rawValue instanceof Integer)
						value = ((Integer) rawValue).doubleValue();
					else if (rawValue instanceof Double)
						value = (Double) rawValue;
					
					if (value != null)
						writer.print(value);
					
					writer.print("\t"); //$NON-NLS-1$
				}
				
				writer.println();
			}
		} finally {
			writer.close();
		}
	}

	private String getSymbol(CyNode node, CyNetwork network, String idAttribute) {
		Class<?> type = cytoscapeUtils.getAttributeType(network, node, idAttribute);
		Object value = cytoscapeUtils.getAttribute(network, node, idAttribute, type);
		
		if (value instanceof String)
			return cytoscapeUtils.getAttribute(network, node, idAttribute, String.class);
		
		if (value instanceof List) {
			for (Object item : (List<?>) value) {
				if (item instanceof String)
					return (String) item;
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

	public NormalizationResult normalize(CyNetwork network, String idAttribute, List<String> expressionAttributes,
			Writer writer, ProgressReporter progress) {
		Context context = new Context();
		return createResult(context);
	}
	
	static class Context {
		
		public int droppedInteractions;
		public int totalInteractions;
		public Set<String> invalidSymbols;
		public Set<String> conflictingSymbols;
		
		public Context() {
			invalidSymbols = new HashSet<>();
			conflictingSymbols = new HashSet<>();
		}
	}
}
