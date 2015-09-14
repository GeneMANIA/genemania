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

package org.genemania.controller;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.genemania.Constants;
import org.genemania.domain.AttributeGroup;
import org.genemania.domain.Gene;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.domain.SearchParameters;
import org.genemania.domain.SearchResults;
import org.genemania.domain.Statistics;
import org.genemania.exception.DataStoreException;
import org.genemania.exception.NoUserNetworkException;
import org.genemania.service.AttributeGroupService;
import org.genemania.service.GeneService;
import org.genemania.service.GeneService.GeneNames;
import org.genemania.service.NetworkGroupService;
import org.genemania.service.NetworkService;
import org.genemania.service.OrganismService;
import org.genemania.service.SearchService;
import org.genemania.service.StatsService;
import org.genemania.type.CombiningMethod;
import org.genemania.util.ApplicationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SearchController {

	@Autowired
	private OrganismService organismService;

	@Autowired
	private StatsService statsService;

	@Autowired
	private NetworkService networkService;

	@Autowired
	private GeneService geneService;

	@Autowired
	private SearchService searchService;

	@Autowired
	private NetworkGroupService networkGroupService;

	@Autowired
	private AttributeGroupService attributeGroupService;

	protected Logger logger = Logger.getLogger(getClass());

	/**
	 * Display the search page.
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/search")
	public ModelAndView show(HttpSession session) {
		return ModelAndViewFactory.create("/search", "index.jsp");
	}

	/**
	 * Perform the search and return the page.
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/search")
	public ModelAndView search(
			@RequestParam("organism") Integer organismId,
			@RequestParam("genes") String geneLines,
			@RequestParam("weighting") CombiningMethod weighting,
			@RequestParam("threshold") Integer threshold,
			@RequestParam("attrThreshold") Integer attrThreshold,
			@RequestParam(value = "networks", required = false) Long[] networkIds,
			@RequestParam(value = "attrgroups", required = false) Long[] attrGroupIds,
			HttpSession session) {

		// the main page handles this
		return ModelAndViewFactory.create("/search", "index.jsp");
	}

	private String organismNameToUrl(String name) {
		return name.replace("'", "").replace(" ", "_").toLowerCase();
	}

	/**
	 * Deep linking with a detailed API with more control
	 *
	 * @param organism
	 *            The organism ID or alias
	 * @param pipeSeparatedGenes
	 *            The genes separated by pipes
	 * @param weightingAsString
	 *            The name of the weighting method
	 * @param thresholdAsString
	 *            The number of genes to return
	 * @param session
	 *            The HTTP session
	 * @return The results page
	 */

	@RequestMapping(method = RequestMethod.GET, value = "/link")
	public ModelAndView link(
			@RequestParam(value = "o", required = true) String organism,
			@RequestParam(value = "g", required = false) String pipeSeparatedGenes,
			@RequestParam(value = "m", required = false) String weightingAsString,
			@RequestParam(value = "r", required = false) String thresholdAsString,
			HttpSession session) {

		// the main page handles this
		return ModelAndViewFactory.create("/link", "index.jsp");
	}


	public SearchService getSearchService() {
		return searchService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public OrganismService getOrganismService() {
		return organismService;
	}

	public void setOrganismService(OrganismService organismService) {
		this.organismService = organismService;
	}

	public StatsService getStatsService() {
		return statsService;
	}

	public void setStatsService(StatsService statsService) {
		this.statsService = statsService;
	}

	public NetworkService getNetworkService() {
		return networkService;
	}

	public void setNetworkService(NetworkService networkService) {
		this.networkService = networkService;
	}

	public GeneService getGeneService() {
		return geneService;
	}

	public void setGeneService(GeneService geneService) {
		this.geneService = geneService;
	}

	public NetworkGroupService getNetworkGroupService() {
		return networkGroupService;
	}

	public void setNetworkGroupService(NetworkGroupService networkGroupService) {
		this.networkGroupService = networkGroupService;
	}

}
