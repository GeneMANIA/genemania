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

import java.io.PrintWriter;

import org.genemania.domain.Gene;
import org.genemania.plugin.controllers.IGeneProvider;
import org.genemania.plugin.model.SearchResult;

public class GeneScoresOutputFormatter extends GeneListOutputFormatter {

	public GeneScoresOutputFormatter(IGeneProvider geneProvider) {
		super(geneProvider);
	}

	@Override
	protected void formatGene(PrintWriter writer, SearchResult result, Gene gene, Double score) {
		Gene preferredGene = provider.getGene(gene.getNode());
		if (result.isQueryNode(gene.getNode().getId())) {
			writer.printf("%s\t\n", preferredGene.getSymbol()); //$NON-NLS-1$
		} else {
			writer.printf("%s\t%s\n", preferredGene.getSymbol(), Double.toString(score * 100)); //$NON-NLS-1$
		}
	}

	@Override
	public String getExtension() {
		return "scores.txt"; //$NON-NLS-1$
	}
}
