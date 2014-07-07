package org.genemania.controller.rest;

import java.util.Collection;

import javax.servlet.http.HttpSession;

import org.genemania.domain.Organism;
import org.genemania.exception.DataStoreException;
import org.genemania.service.OrganismService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class OrganismsController {

	@Autowired
	private OrganismService organismService;

	public OrganismService getOrganismService() {
		return organismService;
	}

	public void setOrganismService(OrganismService organismService) {
		this.organismService = organismService;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/organisms")
	@ResponseBody
	public Collection<Organism> listAll(HttpSession session)
			throws DataStoreException {
		return this.organismService.getOrganisms();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/organisms/{organismId}")
	@ResponseBody
	public Organism list(@PathVariable Long organismId, HttpSession session)
			throws DataStoreException {

		return this.organismService.findOrganismById(organismId);
	}
}
