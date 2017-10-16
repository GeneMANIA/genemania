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

package org.genemania.plugin.formatters;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.genemania.domain.Gene;
import org.genemania.plugin.controllers.IGeneProvider;
import org.genemania.plugin.model.SearchResult;
import org.genemania.plugin.model.ViewState;

public class GeneListOutputFormatter implements IOutputFormatter {
	protected final IGeneProvider provider;

	public GeneListOutputFormatter(IGeneProvider provider) {
		this.provider = provider;
	}
	
	public void format(OutputStream out, ViewState viewState) {
		SearchResult result = viewState.getSearchResult();
		PrintWriter writer = new PrintWriter(out);
		try {
			final Map<Gene, Double> scores = result.getScores();
			ArrayList<Gene> genes = new ArrayList<Gene>(scores.keySet());
			Collections.sort(genes, new Comparator<Gene>() {
				public int compare(Gene gene1, Gene gene2) {
					return -Double.compare(scores.get(gene1), scores.get(gene2));
				}
			});
			for (Gene gene : genes) {
				formatGene(writer, result, gene, scores.get(gene));
			}
		} finally {
			writer.close();
		}
	}

	protected void formatGene(PrintWriter writer, SearchResult result, Gene gene, Double double1) {
		if (result.isQueryNode(gene.getNode().getId())) {
			writer.println(gene.getSymbol());
		} else {
			Gene preferredGene = provider.getGene(gene.getNode());
			writer.println(preferredGene.getSymbol());
		}
	}

	public String getExtension() {
		return "genes.txt"; //$NON-NLS-1$
	}
}
