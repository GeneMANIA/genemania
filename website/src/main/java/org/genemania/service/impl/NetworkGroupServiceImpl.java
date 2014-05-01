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

package org.genemania.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.genemania.dao.NetworkGroupDao;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.exception.DataStoreException;
import org.genemania.service.NetworkGroupService;
import org.genemania.service.NetworkService;
import org.genemania.util.UserNetworkConstants;
import org.genemania.util.UserNetworkGroupIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Default implementation for the service.
 */
@Service
public class NetworkGroupServiceImpl implements NetworkGroupService {

	@Autowired
	private NetworkGroupDao networkGroupDao; // injected

	@Autowired
	NetworkService networkService;

	private Map<String, InteractionNetworkGroup> idToUserGroup = new HashMap<String, InteractionNetworkGroup>();

	public NetworkGroupDao getNetworkGroupDao() {
		return networkGroupDao;
	}

	public void setNetworkGroupDao(NetworkGroupDao networkGroupDao) {
		this.networkGroupDao = networkGroupDao;
	}

	public NetworkService getNetworkService() {
		return networkService;
	}

	public void setNetworkService(NetworkService networkService) {
		this.networkService = networkService;
	}

	private void createUserGroup(long organismId, String sessionId) {
		InteractionNetworkGroup userGroup = new InteractionNetworkGroup();
		userGroup.setCode(UserNetworkConstants.CODE);
		userGroup.setId(0);
		userGroup.setDescription(UserNetworkConstants.GROUP_DESCRIPTION);
		userGroup.setName(UserNetworkConstants.GROUP_NAME);

		Collection<InteractionNetwork> networks = networkService
				.getUserNetworks(organismId, sessionId);
		userGroup.setInteractionNetworks(networks);

		String id = UserNetworkGroupIdGenerator.generateId(organismId,
				sessionId);

		this.idToUserGroup.put(id, userGroup);
	}

	private boolean userGroupExists(long organismId, String sessionId) {
		String id = UserNetworkGroupIdGenerator.generateId(organismId,
				sessionId);

		return this.idToUserGroup.containsKey(id)
				&& this.idToUserGroup.get(id) != null;
	}

	private InteractionNetworkGroup getUserGroup(long organismId,
			String sessionId) {

		if (!userGroupExists(organismId, sessionId)) {
			createUserGroup(organismId, sessionId);
		}

		String id = UserNetworkGroupIdGenerator.generateId(organismId,
				sessionId);

		// always use latest list of user networks
		InteractionNetworkGroup group = this.idToUserGroup.get(id);
		Collection<InteractionNetwork> networks = networkService
				.getUserNetworks(organismId, sessionId);
		group.setInteractionNetworks(networks);

		return this.idToUserGroup.get(id);
	}

	public Collection<InteractionNetworkGroup> findNetworkGroupsByOrganism(
			Long organismId, String sessionId) throws DataStoreException {
		List<InteractionNetworkGroup> groups = networkGroupDao
				.findNetworkGroupsByOrganism(organismId);

		List<InteractionNetworkGroup> ret = new LinkedList<InteractionNetworkGroup>();
		for (InteractionNetworkGroup group : groups) {
			ret.add(group);
		}

		InteractionNetworkGroup userGroup = getUserGroup(organismId, sessionId);
		ret.add(userGroup);

		return ret;
	}

	@Override
	public InteractionNetworkGroup findNetworkGroupByName(long organismId,
			String name, String sessionId) throws DataStoreException {

		if (name.equals(UserNetworkConstants.CODE)) {
			return this.getUserGroup(organismId, sessionId);
		} else {
			return this.networkGroupDao
					.findNetworkGroupByName(organismId, name);
		}

	}

}
