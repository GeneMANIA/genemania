/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2010 University of Toronto.
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

package org.genemania.data.normalizer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;


public class WeightedNetworkNormalizer extends BaseNormalizer {
	int fromColumn;
	int toColumn;
	private int weightColumn;
	
	@Override
	protected void handleBeforeNormalize(ParsingContext context) throws IOException {
		List<Integer> idColumns = context.idColumns;
		if (idColumns.size() < 2) {
			throw new IOException();
		}
		
		// Assume first two identifier columns are the interacting pair.
		fromColumn = idColumns.get(0);
		toColumn = idColumns.get(1);
		
		for (int i = 0; i < 3; i++) {
			if (i != fromColumn && i != toColumn) {
				weightColumn = i;
				break;
			}
		}
	}
	
	protected void handleLine(ParsingContext context, String line, GeneCompletionProvider2 genes, PrintWriter writer) {
		String[] parts = line.split(context.delimiter);
		if (parts.length != getColumnCount()) {
			return;
		}
		String fromSymbol = parts[fromColumn];
		String toSymbol = parts[toColumn];
		
		Double weight = null;
		
		if (isWeighted()) {
			weight = parseWeight(parts[weightColumn]);
			if (weight == null) {
				context.droppedInteractions++;
				return;
			}
		}
		
		Long fromId = genes.getNodeId(fromSymbol);
		Long toId = genes.getNodeId(toSymbol);
		
		validateSymbol(fromId, fromSymbol, context.invalidSymbols);
		validateSymbol(toId, toSymbol, context.invalidSymbols);
		
		if (fromId == null || toId == null || fromId == toId) {
			context.droppedInteractions++;
			return;
		}
		
		writer.print(fromId);
		writer.print("\t"); //$NON-NLS-1$
		writer.print(toId);
		writer.print("\t"); //$NON-NLS-1$
		if (isWeighted()) {
			writer.print(Double.toString(weight));
		}
		writer.print("\n"); //$NON-NLS-1$
		context.totalInteractions++;
	}

	protected Double parseWeight(String value) {
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	protected int getColumnCount() {
		return 3;
	}

	protected void validateSymbol(Long id, String symbol, Set<String> invalidSymbols) {
		if (id == null) {
			invalidSymbols.add(symbol);
		}
	}
	
	protected boolean isWeighted() {
		return true;
	}
}
