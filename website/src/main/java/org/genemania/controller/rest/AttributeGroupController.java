package org.genemania.controller.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.genemania.domain.AttributeGroup;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.exception.DataStoreException;
import org.genemania.service.AttributeGroupService;
import org.genemania.service.OrganismService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AttributeGroupController {

	@Autowired
	private AttributeGroupService attributeGroupService;

	@Autowired
	private OrganismService organismService;

	@RequestMapping(method = RequestMethod.GET, value = "/attribute_groups/{organismId}")
	@ResponseBody
	public Collection<AttributeGroup> list(@PathVariable Long organismId,
			HttpSession session) throws DataStoreException {
		return attributeGroupService.findAttributeGroups(organismId);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/attribute_groups")
	@ResponseBody
	public Map<Long, Collection<AttributeGroup>> listAll(HttpSession session)
			throws DataStoreException {

		Map<Long, Collection<AttributeGroup>> idToAttrs = new HashMap<Long, Collection<AttributeGroup>>();

		Collection<Organism> organisms = organismService.getOrganisms();

		for (Organism organism : organisms) {
			Long organismId = organism.getId();

			Collection<AttributeGroup> groups = attributeGroupService
					.findAttributeGroups(organismId);

			idToAttrs.put(organismId, groups);
		}

		return idToAttrs;

	}

	public AttributeGroupService getAttributeGroupService() {
		return attributeGroupService;
	}

	public void setAttributeGroupService(
			AttributeGroupService attributeGroupService) {
		this.attributeGroupService = attributeGroupService;
	}

	public OrganismService getOrganismService() {
		return organismService;
	}

	public void setOrganismService(OrganismService organismService) {
		this.organismService = organismService;
	}

}
