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
package org.genemania.plugin.controllers;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.genemania.domain.Gene;
import org.genemania.domain.GeneNamingSource;

public class RankedGeneProviderWithUniprotHack extends RankedGeneProvider {

	public static final String UNIPROT_AC = "Uniprot AC"; //$NON-NLS-1$
	public static final String UNIPROT_ID = "Uniprot ID"; //$NON-NLS-1$
	
	Pattern UNIPROT_ACCESSION = Pattern.compile("([A-NR-Z][0-9][A-Z][A-Z0-9][A-Z0-9][0-9])|([O-Q][0-9][A-Z0-9][A-Z0-9][A-Z0-9][0-9])"); //$NON-NLS-1$

	private final int comparisonModifier;
	
	public RankedGeneProviderWithUniprotHack(List<GeneNamingSource> baseNamingSources, List<GeneNamingSource> namingSourcePreferences) {
		super(baseNamingSources, namingSourcePreferences);
		
		int acRank = Integer.MIN_VALUE;
		int idRank = Integer.MIN_VALUE;
		int rank = 0;
		for (GeneNamingSource source : namingSourcePreferences) {
			if (source.getName().equals(UNIPROT_ID)) {
				idRank = rank;
			} else if (source.getName().equals(UNIPROT_AC)) {
				acRank = rank;
			}
			rank++;
		}
		comparisonModifier = idRank >= acRank ? 1 : -1;
	}

	@Override
	protected Comparator<Gene> createComparator() {
		return new Comparator<Gene>() {
			public int compare(Gene gene1, Gene gene2) {
				GeneNamingSource source1 = gene1.getNamingSource();
				GeneNamingSource source2 = gene2.getNamingSource();
				long id1 = source1.getId();
				long id2 = source2.getId();
				
				if (id1 == id2 && source1.getName().equals(UNIPROT_ID)) {
					if (UNIPROT_ACCESSION.matcher(gene1.getSymbol()).matches()) {
						return -comparisonModifier;
					}
					if (UNIPROT_ACCESSION.matcher(gene2.getSymbol()).matches()) {
						return comparisonModifier;
					}
					return 0;
				} else {
					Integer rank1 = rankings.get(id1);
					Integer rank2 = rankings.get(id2);
					return rank2 - rank1;
				}
			}
		};
	}
}
