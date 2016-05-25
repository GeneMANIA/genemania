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
 * LuceneNetworkGroupDao: Lucene network group data access object implementation   
 * Created Jun 04, 2010
 * @author Ovi Comes
 */
package org.genemania.dao.impl;

import java.util.List;

import org.genemania.connector.LuceneConnector;
import org.genemania.dao.NetworkGroupDao;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.exception.DataStoreException;
import org.springframework.cache.annotation.Cacheable;

public class LuceneNetworkGroupDao implements NetworkGroupDao {

	// __[fields]______________________________________________________________
	private LuceneConnector connector;

	// __[constructors]________________________________________________________
	public LuceneNetworkGroupDao() {
		connector = LuceneConnector.getInstance();
	}

	// __[interface implementation]____________________________________________
	 @Cacheable("networkGroupsByOrganismCache")
	public List<InteractionNetworkGroup> findNetworkGroupsByOrganism(
			long organismId) throws DataStoreException {
		return connector.findNetworkGroupsByOrganism(organismId);

	}

	// TODO revise when attributes are implemented fully in the engine
	 @Cacheable("networkGroupsByNameCache")
	public InteractionNetworkGroup findNetworkGroupByName(long organismId,
			String name) throws DataStoreException {
		return connector.getNetworkGroupByName(
				organismId, name);
	}
}
