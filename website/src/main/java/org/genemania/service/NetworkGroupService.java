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

package org.genemania.service;

import java.util.Collection;

import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.exception.DataStoreException;

/**
 * Provides access of network group domain data
 */
public interface NetworkGroupService {

	/**
	 * Gets all the network groups for an organism
	 * 
	 * @param organismId
	 *            The ID of the organism
	 * @return All network groups for the organism
	 * @throws DataStoreException
	 *             on database error
	 */
	public Collection<InteractionNetworkGroup> findNetworkGroupsByOrganism(
			Long organismId, String sessionId) throws DataStoreException;

	/**
	 * Gets the network group with the given name
	 * 
	 * @param organismId
	 *            The organism ID of the group
	 * @param name
	 *            The name of the group
	 * @param sessionId
	 *            The user's session ID
	 * @return The network group
	 * @throws DataStoreException
	 *             if the group can not be found
	 */
	InteractionNetworkGroup findNetworkGroupByName(long organismId,
			String name, String sessionId) throws DataStoreException;
}
