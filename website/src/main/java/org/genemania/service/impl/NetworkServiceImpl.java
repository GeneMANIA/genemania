package org.genemania.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.genemania.dao.NetworkDao;
import org.genemania.dao.OrganismDao;
import org.genemania.domain.InteractionNetwork;
import org.genemania.exception.DataStoreException;
import org.genemania.service.NetworkService;
import org.genemania.util.UserNetworkGroupIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;

public class NetworkServiceImpl implements NetworkService {

	// session id => user networks
	private Map<String, Collection<InteractionNetwork>> idToUserNetworks = new HashMap<String, Collection<InteractionNetwork>>();

	// user network id => user network
	private Map<Long, InteractionNetwork> userNetworks = new HashMap<Long, InteractionNetwork>();

	@Autowired
	private OrganismDao organismDao;

	@Autowired
	private NetworkDao networkDao;

	public OrganismDao getOrganismDao() {
		return organismDao;
	}

	public void setOrganismDao(OrganismDao organismDao) {
		this.organismDao = organismDao;
	}

	public NetworkDao getNetworkDao() {
		return networkDao;
	}

	public void setNetworkDao(NetworkDao networkDao) {
		this.networkDao = networkDao;
	}

	@Override
	public void addUserNetwork(long organismId, String sessionId,
			InteractionNetwork network) {

		String id = UserNetworkGroupIdGenerator.generateId(organismId,
				sessionId);
		if (!idToUserNetworks.containsKey(id)) {
			idToUserNetworks.put(id, new LinkedList<InteractionNetwork>());
		}
		idToUserNetworks.get(id).add(network);

		userNetworks.put(network.getId(), network);
	}

	@Override
	public Collection<InteractionNetwork> getUserNetworks(long organismId,
			String sessionId) {
		String id = UserNetworkGroupIdGenerator.generateId(organismId,
				sessionId);

		return idToUserNetworks.get(id);
	}

	@Override
	public Collection<InteractionNetwork> findDefaultNetworksForOrganism(
			Long id, String sessionId) throws DataStoreException {
		return findDefaultNetworksForOrganism(id, sessionId, true);
	}

	@Override
	public Collection<InteractionNetwork> findDefaultNetworksForOrganism(
			Long id, String sessionId, boolean includeUserNetworks)
			throws DataStoreException {
		Collection<InteractionNetwork> retNetworks = new LinkedList<InteractionNetwork>();

		Collection<InteractionNetwork> networks = organismDao
				.getDefaultNetworks(id);
		for (InteractionNetwork network : networks) {
			retNetworks.add(network);
		}

		if (includeUserNetworks) {
			Collection<InteractionNetwork> userNetworks = getUserNetworks(id,
					sessionId);
			if (userNetworks != null) {
				for (InteractionNetwork network : userNetworks) {
					retNetworks.add(network);
				}
			}
		}

		return retNetworks;
	}

	@Override
	public InteractionNetwork findNetwork(long id) throws DataStoreException {
		if (userNetworks.containsKey(id)) {
			return userNetworks.get(id);
		} else {
			return networkDao.findNetwork(id);
		}
	}

	@Override
	public Collection<InteractionNetwork> getNetworks(Integer organismId,
			Long[] networkIds, String sessionId) throws DataStoreException {
		return getNetworks(organismId, networkIds, sessionId, true);
	}

	@Override
	public Collection<InteractionNetwork> getNetworks(Integer organismId,
			Long[] networkIds, String sessionId, boolean includeUserNetworks)
			throws DataStoreException {
		Collection<InteractionNetwork> networks = new LinkedList<InteractionNetwork>();

		boolean empty = true;
		if (networkIds != null) {
			for (Long id : networkIds) {
				if (id != null) {
					empty = false;
				}
			}
		}

		if (!empty) {
			for (Long id : networkIds) {
				if (id != null) {
					InteractionNetwork network = findNetwork(id);

					if (includeUserNetworks
							&& (userNetworks.containsKey(id) || (network != null && networkDao
									.isValidNetwork(organismId, network.getId())))) {
						networks.add(network);
					}
				}
			}
		}

		return networks;
	}

	@Override
	public void deleteUserNetwork(long organismId, long networkId,
			String sessionId) throws DataStoreException {

		if (!this.userNetworks.containsKey(networkId)) {
			throw new DataStoreException("No user network with ID " + networkId
					+ " exists");
		} else {
			this.userNetworks.remove(networkId);

			String id = UserNetworkGroupIdGenerator.generateId(organismId,
					sessionId);

			Collection<InteractionNetwork> userNetworksInSession = this.idToUserNetworks
					.get(id);
			if (userNetworksInSession != null) {
				InteractionNetwork networkToRemove = null;
				for (InteractionNetwork network : userNetworksInSession) {
					if (networkId == network.getId()) {
						networkToRemove = network;
						break;
					}
				}
				if (networkToRemove != null) {
					userNetworksInSession.remove(networkToRemove);
				}

			}
		}

	}
}
