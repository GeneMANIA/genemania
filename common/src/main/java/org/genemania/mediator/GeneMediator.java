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

/**
 * GeneMediator: TODO add description
 * Created Jul 23, 2008
 * @author Ovi Comes
 */
package org.genemania.mediator;

import java.util.List;
import java.util.Set;

import org.genemania.domain.Gene;
import org.genemania.domain.GeneData;
import org.genemania.domain.GeneNamingSource;
import org.genemania.domain.Organism;
import org.genemania.exception.DataStoreException;

public interface GeneMediator {

	public List<Gene> getAllGenes(long organismId);
	public List<Gene> getGenes(List<String> geneSymbols, long organismId) throws DataStoreException;
	public Gene getGeneForSymbol(Organism organism, String geneSymbol);
	public GeneNamingSource findNamingSourceByName(String namingSourceName);
	public void updateGeneData(Organism organism, String geneSymbol, GeneData geneData);
	public Set<String> getSynonyms(long organismId, String symbol);
	public Set<String> getSynonyms(long organismId, long nodeId);
	public Long getNodeId(long organismId, String symbol);
	public boolean isValid(long organismId, String symbol);
	public String getCanonicalSymbol(long organismId, String symbol);
}
