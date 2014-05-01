package org.genemania.controller.rest;

import java.util.Collection;

import javax.servlet.http.HttpSession;

import org.genemania.domain.AttributeGroup;
import org.genemania.exception.DataStoreException;
import org.genemania.service.AttributeGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AttributeGroupController {

	private @Autowired
	AttributeGroupService attributeGroupService;

	@RequestMapping(method = RequestMethod.GET, value = "/attribute_groups/{organismId}")
	@ResponseBody
	public Collection<AttributeGroup> list(@PathVariable Long organismId,
			HttpSession session) throws DataStoreException {
		return attributeGroupService.findAttributeGroups(organismId);
	}

	public AttributeGroupService getAttributeGroupService() {
		return attributeGroupService;
	}

	public void setAttributeGroupService(
			AttributeGroupService attributeGroupService) {
		this.attributeGroupService = attributeGroupService;
	}

}
