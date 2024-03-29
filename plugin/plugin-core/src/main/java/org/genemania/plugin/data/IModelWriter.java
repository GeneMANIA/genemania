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

package org.genemania.plugin.data;

import org.genemania.domain.Gene;
import org.genemania.domain.GeneNamingSource;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Node;
import org.genemania.domain.Organism;
import org.genemania.exception.ApplicationException;

public interface IModelWriter {
	
	void addNetwork(InteractionNetwork network, InteractionNetworkGroup group) throws ApplicationException;
	void addGroup(InteractionNetworkGroup group, Organism organism, String color) throws ApplicationException;
	void addOrganism(Organism organism) throws ApplicationException;
	void addNode(Node node, Organism organism) throws ApplicationException;
	void addGene(Gene gene) throws ApplicationException;
	void addNamingSource(GeneNamingSource source) throws ApplicationException;
	
	void deleteNetwork(InteractionNetwork network) throws ApplicationException;
	void deleteOrganism(Organism organism) throws ApplicationException;
	void deleteOrganismNodesAndGenes(Organism organism) throws ApplicationException;
	
	void close() throws ApplicationException;
}
