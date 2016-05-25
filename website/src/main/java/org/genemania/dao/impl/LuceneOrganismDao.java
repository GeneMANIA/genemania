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
 * LuceneOrganismDao: Lucene organism data access object implementation   
 * Created Jun 04, 2010
 * @author Ovi Comes
 */
package org.genemania.dao.impl;

import java.util.List;

import org.genemania.connector.LuceneConnector;
import org.genemania.dao.OrganismDao;
import org.genemania.domain.Gene;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.Organism;
import org.genemania.exception.DataStoreException;
import org.springframework.cache.annotation.Cacheable;

public class LuceneOrganismDao implements OrganismDao {

	// __[attributes]__________________________________________________________
	private LuceneConnector connector;
	
	// __[constructors]________________________________________________________
	public LuceneOrganismDao() {
		connector = LuceneConnector.getInstance();
	}

	// __[interface implementation]____________________________________________
	@Cacheable("allOrganismsCache")
	public List<Organism> getAllOrganisms() throws DataStoreException {
		return connector.retrieveAllOrganisms();
	}

	@Cacheable("organismCache")
	public Organism findOrganism(long organismId) throws DataStoreException {
		return connector.findOrganismById(organismId);
	}

	@Cacheable("defaultGenesCache")
	public List<Gene> getDefaultGenes(long organismId) throws DataStoreException {
		return connector.retrieveDefaultGenesFor(organismId);
	}

	@Cacheable("defaultNetworksCache")
	public List<InteractionNetwork> getDefaultNetworks(long organismId) throws DataStoreException {
		return connector.retrieveDefaultNetworksFor(organismId);
	}

}
