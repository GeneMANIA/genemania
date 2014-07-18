package org.genemania.controller.rest;

import java.util.Collection;

import javax.servlet.http.HttpSession;

import org.genemania.domain.AttributeGroup;
import org.genemania.domain.Gene;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.Organism;
import org.genemania.domain.SearchParameters;
import org.genemania.domain.SearchResults;
import org.genemania.exception.DataStoreException;
import org.genemania.service.AttributeGroupService;
import org.genemania.service.GeneService;
import org.genemania.service.NetworkService;
import org.genemania.service.OrganismService;
import org.genemania.service.SearchService;
import org.genemania.service.StatsService;
import org.genemania.service.VisualizationDataService;
import org.genemania.service.GeneService.GeneNames;
import org.genemania.service.VisualizationDataService.Visualization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

// NOTE: used on old website to generate cytoscape web data
@Controller
public class VisualizationDataController {

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

	private @Autowired
	VisualizationDataService visualizationDataService;

	@Autowired
	private AttributeGroupService attributeGroupService;

	@RequestMapping(method = RequestMethod.POST, value = "/visualization")
	@ResponseBody
	public Visualization create(
			@RequestParam("organism") Integer organismId,
			@RequestParam("genes") String geneLines,
			@RequestParam("weighting") org.genemania.type.CombiningMethod weighting,
			@RequestParam("threshold") Integer threshold,
			@RequestParam("attrThreshold") Integer attrThreshold,
			@RequestParam(value = "networks", required = false) Long[] networkIds,
			@RequestParam(value = "attrgroups", required = false) Long[] attrGroupIds,
			HttpSession session) throws DataStoreException {

		try {

			// validate organism
			// ============================================

			Organism organism = organismService.findOrganismById(new Long(
					organismId));

			// validate networks
			// ============================================

			Collection<InteractionNetwork> networks = networkService
					.getNetworks(organismId, networkIds, session.getId());

			// validate genes
			// ============================================

			GeneNames geneNames = geneService.getGeneNames(organismId,
					geneLines);
			Collection<Gene> validGenes = geneService.findGenesForOrganism(
					organismId, geneNames.getValidGenes());

			// validate attributes
			// ============================================

			Collection<AttributeGroup> attributeGroups = attributeGroupService
					.findAttributeGroups(organismId, attrGroupIds);

			// search param object
			// ============================================

			SearchParameters params = new SearchParameters(organism,
					validGenes, networks, attributeGroups, weighting,
					threshold, attrThreshold, session.getId());

			SearchResults results = searchService.search(params);

			return visualizationDataService.getVisualizationData(results);

		} catch (Exception e) {
			throw new RuntimeException(e.toString());
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

	public VisualizationDataService getVisualizationDataService() {
		return visualizationDataService;
	}

	public void setVisualizationDataService(
			VisualizationDataService visualizationDataService) {
		this.visualizationDataService = visualizationDataService;
	}

	public AttributeGroupService getAttributeGroupService() {
		return attributeGroupService;
	}

	public void setAttributeGroupService(
			AttributeGroupService attributeGroupService) {
		this.attributeGroupService = attributeGroupService;
	}

}
