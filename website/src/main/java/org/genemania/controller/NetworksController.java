package org.genemania.controller;

import javax.servlet.http.HttpSession;

import org.genemania.exception.DataStoreException;
import org.genemania.service.OrganismService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Export all networks as HTML
 */

@Controller
public class NetworksController {
	
	@Autowired
	OrganismService organismService;
	
	public OrganismService getOrganismService() {
		return organismService;
	}

	public void setOrganismService(OrganismService organismService) {
		this.organismService = organismService;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/networks")
	public ModelAndView error(HttpSession session) {

		ModelAndView mv = ModelAndViewFactory.create("/networks");
		
		try {
			mv.addObject("organisms", organismService.getOrganisms());
		} catch (DataStoreException e) {
			// can't do anything
		}
		
		return mv;
	}
}
