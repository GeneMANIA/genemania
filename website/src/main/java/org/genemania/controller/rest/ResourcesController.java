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

package org.genemania.controller.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpSession;

import org.genemania.domain.AttributeGroup;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.domain.Statistics;
import org.genemania.exception.DataStoreException;
import org.genemania.service.AttributeGroupService;
import org.genemania.service.NetworkGroupService;
import org.genemania.service.OrganismService;
import org.genemania.service.StatsService;
import org.genemania.service.VersionService;
import org.genemania.service.VersionService.VersionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ResourcesController {

	private class Resources {
		public Map<Long, Collection<InteractionNetworkGroup>> networkGroups;
		public Collection<Organism> organisms;
		public Map<Long, Collection<AttributeGroup>> attributeGroups;
		public Statistics stats;
		public VersionInfo versions;
		
	}

	@Autowired
	private NetworkGroupService networkGroupService;

	@Autowired
	private OrganismService organismService;

	@Autowired
	private AttributeGroupService attributeGroupService;

	@Autowired
	private StatsService statsService;

	@Autowired
	VersionService versionService;

	@RequestMapping(method = RequestMethod.GET, value = "/resources")
	@ResponseBody
	public Resources get(@RequestParam(value = "session_id", required = false) String sessionId, HttpSession session)
			throws DataStoreException {

		if (sessionId == null || sessionId.isEmpty()) {
			sessionId = session.getId();
		}

		Resources resources = new Resources();

		// organisms
		//
		
		Collection<Organism> organisms = organismService.getOrganisms();
		
		resources.organisms = organisms;
		
		// networks
		//

		Map<Long, Collection<InteractionNetworkGroup>> idToNetworks = new HashMap<Long, Collection<InteractionNetworkGroup>>();

		for (Organism organism : organisms) {
			Long organismId = organism.getId();

			Collection<InteractionNetworkGroup> groups = networkGroupService.findNetworkGroupsByOrganism(organismId,
					sessionId);

			idToNetworks.put(organismId, groups);
		}
		
		resources.networkGroups = idToNetworks;
		
		// attr groups
		//
		
		Map<Long, Collection<AttributeGroup>> idToAttrs = new HashMap<Long, Collection<AttributeGroup>>();

		for (Organism organism : organisms) {
			Long organismId = organism.getId();

			Collection<AttributeGroup> groups = attributeGroupService
					.findAttributeGroups(organismId);

			idToAttrs.put(organismId, groups);
		}

		resources.attributeGroups = idToAttrs;
		
		// stats
		//
		
		resources.stats = statsService.getStats();
		
		// version
		//
		
		resources.versions = versionService.getVersion();
		

		return resources;
	}

	public NetworkGroupService getNetworkGroupService() {
		return networkGroupService;
	}

	public void setNetworkGroupService(NetworkGroupService networkGroupService) {
		this.networkGroupService = networkGroupService;
	}

	public OrganismService getOrganismService() {
		return organismService;
	}

	public void setOrganismService(OrganismService organismService) {
		this.organismService = organismService;
	}

	public AttributeGroupService getAttributeGroupService() {
		return attributeGroupService;
	}

	public void setAttributeGroupService(AttributeGroupService attributeGroupService) {
		this.attributeGroupService = attributeGroupService;
	}

	public StatsService getStatsService() {
		return statsService;
	}

	public void setStatsService(StatsService statsService) {
		this.statsService = statsService;
	}

	public VersionService getVersionService() {
		return versionService;
	}

	public void setVersionService(VersionService versionService) {
		this.versionService = versionService;
	}

}
