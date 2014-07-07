package org.genemania.controller.rest;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import org.genemania.domain.Gene;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.Organism;
import org.genemania.domain.SearchParameters;
import org.genemania.domain.SearchResults;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.exception.NoUserNetworkException;
import org.genemania.service.AttributeService;
import org.genemania.service.GeneService;
import org.genemania.service.NetworkService;
import org.genemania.service.OrganismService;
import org.genemania.service.SearchService;
import org.genemania.type.CombiningMethod;
import org.genemania.type.SearchResultsErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SearchResultsController {

	@Autowired
	private SearchService searchService;

	@Autowired
	private OrganismService organismService;

	@Autowired
	private GeneService geneService;

	@Autowired
	private NetworkService networkService;

	@Autowired
	private AttributeService attributeService;

	@RequestMapping(method = RequestMethod.POST, value = "/search_results")
	@ResponseBody
	public SearchResults list(
			@RequestParam(value = "organism", required = false, defaultValue = "1") String organismStr,
			@RequestParam(value = "genes", required = true) String genesStr,
			@RequestParam(value = "weighting", required = false, defaultValue = "AUTOMATIC_SELECT") CombiningMethod weighting,
			@RequestParam(value = "geneThreshold", required = false, defaultValue = "20") Integer geneThreshold,
			@RequestParam(value = "attrThreshold", required = false, defaultValue = "10") Integer attrThreshold,
			@RequestParam(value = "networks", required = false) Long[] networkIds,
			@RequestParam(value = "attrgroups", required = false) Long[] attrGroupIds,
			HttpSession session) {

		// set up search params
		//
		SearchParameters params = new SearchParameters();
		String sessionId = session.getId();
		boolean includeUserNetworks = true;

		// set organism
		Organism organism;
		int organismId;
		try {
			organism = organismService.findOrganismByString(organismStr);
			organismId = (int) organism.getId();
			params.setOrganism(organism);
		} catch (DataStoreException e) {
			return new SearchResults(e.getMessage(),
					SearchResultsErrorCode.DATASTORE);
		}

		// set genes
		String[] genesSplit = genesStr.split("\n");
		for( int i = 0; i < genesSplit.length; i++ ){
			genesSplit[i] = genesSplit[i].trim();
		}
		List<String> geneLines = Arrays.asList(genesSplit);
		Collection<Gene> genes;
		try {
			genes = geneService.findGenesForOrganism(organismId, geneLines);
			params.setGenes(genes);
		} catch (DataStoreException e) {
			return new SearchResults(e.getMessage(),
					SearchResultsErrorCode.DATASTORE);
		}

		// set weighting
		params.setWeighting(weighting);

		// set gene threshold
		if (geneThreshold <= 0) {
			return new SearchResults(
					"At least one result gene must be allowed by gene threshold",
					SearchResultsErrorCode.DATASTORE);
		} else {
			params.setResultsSize(geneThreshold);
		}

		// set attr threshold
		if (attrThreshold < 0) {
			return new SearchResults(
					"The attribute threshold must be non-negative",
					SearchResultsErrorCode.DATASTORE);
		} else {
			params.setAttributeResultsSize(attrThreshold);
		}

		// set networks

		Collection<InteractionNetwork> networks;
		try {
			networks = networkService.getNetworks(organismId, networkIds,
					sessionId, includeUserNetworks);
			params.setNetworks(networks);
		} catch (DataStoreException e) {
			return new SearchResults(e.getMessage(),
					SearchResultsErrorCode.DATASTORE);
		}

		//
		// //

		// submit and return the search
		try {
			SearchResults results = searchService.search(params);

			// back reference to parameters b/c params may be auto/default
			results.setParameters(params);

			return results;
		} catch (ApplicationException e) {
			return new SearchResults(e.getMessage(), SearchResultsErrorCode.APP);
		} catch (DataStoreException e) {
			return new SearchResults(e.getMessage(),
					SearchResultsErrorCode.DATASTORE);
		} catch (NoUserNetworkException e) {
			return new SearchResults(e.getMessage(),
					SearchResultsErrorCode.USER_NETWORK);
		}

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

	public GeneService getGeneService() {
		return geneService;
	}

	public void setGeneService(GeneService geneService) {
		this.geneService = geneService;
	}

	public NetworkService getNetworkService() {
		return networkService;
	}

	public void setNetworkService(NetworkService networkService) {
		this.networkService = networkService;
	}

	public AttributeService getAttributeService() {
		return attributeService;
	}

	public void setAttributeService(AttributeService attributeService) {
		this.attributeService = attributeService;
	}

}
