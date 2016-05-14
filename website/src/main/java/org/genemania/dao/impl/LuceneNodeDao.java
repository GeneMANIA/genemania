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
 * LuceneNodeDao: Lucene node data access object implementation   
 * Created Jun 18, 2010
 * @author Ovi Comes
 */
package org.genemania.dao.impl;

import org.genemania.connector.LuceneConnector;
import org.genemania.dao.NodeDao;
import org.genemania.domain.Node;
import org.genemania.exception.DataStoreException;

import com.googlecode.ehcache.annotations.Cacheable;

public class LuceneNodeDao implements NodeDao {

	// __[attributes]__________________________________________________________
	private LuceneConnector connector;
	
	// __[constructors]________________________________________________________
	public LuceneNodeDao() {
		connector = LuceneConnector.getInstance();
	}

	// __[interface implementation]____________________________________________
	// @Cacheable(cacheName="nodeCache")
	public Node findNode(long organismId, long nodeId) throws DataStoreException {
		return connector.findNodeById(nodeId, organismId);
	}

}
