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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.genemania.service.VisualizationDataService;
import org.genemania.service.VisualizationDataService.Visualization;
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
	private VisualizationDataService visualizationDataService;

	@Autowired
	private NetworkGroupService networkGroupService;

	@Autowired
	private AttributeGroupService attributeGroupService;

	protected Log logger = LogFactory.getLog(getClass());

	/**
	 * Test the error page
	 * 
	 * @throws DataStoreException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/error")
	public ModelAndView error(HttpSession session) throws DataStoreException {

		ModelAndView mv = ModelAndViewFactory.create("/error");
		mv.addObject("exception", new RuntimeException(
				"A fake exception for testing"));
		mv.addObject("organismId", 4);
		mv.addObject("geneLines", "pcna");
		mv.addObject("networkIds", null);
		mv.addObject("attrGroupIds", null);
		mv.addObject("weighting", "AUTOMATIC");
		mv.addObject("threshold", "20");
		mv.addObject("attrThreshold", "20");
		mv.addObject("sessionId", session.getId());
		mv.addObject("dbversion", new java.text.SimpleDateFormat(
				"d MMMMM yyyy HH:mm:ss").format(statsService.getStats()
				.getDate()));

		return mv;
	}

	/**
	 * Display the search page.
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/search")
	public ModelAndView show(HttpSession session) {
		logger.debug("Return Search View...");

		ModelAndView mv;

		try {
			Collection<Organism> organisms = organismService.getOrganisms();
			Organism defaultOrganism = organismService.getDefaultOrganism();
			Statistics stats = statsService.getStats();
			Map<Long, Collection<AttributeGroup>> organismIdToAttributeGroups = new HashMap<Long, Collection<AttributeGroup>>();
			for (Organism organism : organisms) {
				organismIdToAttributeGroups.put(organism.getId(),
						attributeGroupService.findAttributeGroups(organism
								.getId()));
			}

			mv = ModelAndViewFactory.create("/search");
			mv.addObject("organisms", organisms);
			mv.addObject("defaultOrganism", defaultOrganism);
			mv.addObject("organismIdToAttributeGroups",
					organismIdToAttributeGroups);
			mv.addObject("stats", stats);
			mv.addObject("sessionId", session.getId());
			mv.addObject("dbversion", new java.text.SimpleDateFormat(
					"d MMMMM yyyy HH:mm:ss").format(statsService.getStats()
					.getDate()));
		} catch (Exception e) {
			mv = ModelAndViewFactory.create("/search");
			mv.addObject("exception", e);
		}

		return mv;
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

		return getModelAndView("/search", "results.jsp", organismId, geneLines,
				weighting, threshold, attrThreshold, networkIds, attrGroupIds,
				session);
	}

	/**
	 * Perform the search and return a printout
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/print")
	public ModelAndView print(
			@RequestParam("organism") Integer organismId,
			@RequestParam("genes") String geneLines,
			@RequestParam("weighting") CombiningMethod weighting,
			@RequestParam("threshold") Integer threshold,
			@RequestParam("attrThreshold") Integer attrThreshold,
			@RequestParam(value = "networks", required = false) Long[] networkIds,
			@RequestParam(value = "attrgroups", required = false) Long[] attrGroupIds,
			@RequestParam("svg") String svg,
			@RequestParam("golegend") String golegend,
			@RequestParam("netlegend") String netlegend, HttpSession session) {

		ModelAndView mv = getModelAndView("/print", "print.jsp", organismId,
				geneLines, weighting, threshold, attrThreshold, networkIds,
				attrGroupIds, session);

		mv.addObject("svg", svg);
		mv.addObject("golegend", golegend);
		mv.addObject("netlegend", netlegend);

		return mv;

	}

	private void addColors(ModelAndView mv, String colorList,
			String variableName) {
		Map<String, String> map = new HashMap<String, String>();

		if (colorList != "") {
			for (String pair : colorList.split("\\|\\|")) {
				if (pair == null || pair.equals("")) {
					continue;
				}

				String[] entry = pair.split("\\|");

				if (entry == null) {
					continue;
				}

				String id = entry[0];
				String color = entry[1];

				map.put(id, color);
			}
		}

		mv.addObject(variableName, map);
	}

	/**
	 * Perform the search and return a text export
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/export/text")
	public ModelAndView text(
			@RequestParam("organism") Integer organismId,
			@RequestParam("genes") String geneLines,
			@RequestParam("weighting") CombiningMethod weighting,
			@RequestParam("threshold") Integer threshold,
			@RequestParam("attrThreshold") Integer attrThreshold,
			@RequestParam(value = "networks", required = false) Long[] networkIds,
			@RequestParam(value = "attrgroups", required = false) Long[] attrGroupIds,
			HttpSession session) {

		return getModelAndView("/export/text", "text.jsp", organismId,
				geneLines, weighting, threshold, attrThreshold, networkIds,
				attrGroupIds, session);

	}

	/**
	 * Perform the search and return a text export
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/export/params")
	public ModelAndView params(
			@RequestParam("organism") Integer organismId,
			@RequestParam("genes") String geneLines,
			@RequestParam("weighting") CombiningMethod weighting,
			@RequestParam("threshold") Integer threshold,
			@RequestParam("attrThreshold") Integer attrThreshold,
			@RequestParam(value = "networks", required = false) Long[] networkIds,
			@RequestParam(value = "attrgroups", required = false) Long[] attrGroupIds,
			HttpSession session) {

		return getModelAndView("/export/params", "export_params.jsp",
				organismId, geneLines, weighting, threshold, attrThreshold,
				networkIds, attrGroupIds, session);

	}

	/**
	 * Return a json export of params
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/export/params_json")
	public ModelAndView paramsAsJson(
			@RequestParam("organism") Integer organismId,
			@RequestParam("genes") String geneLines,
			@RequestParam("weighting") CombiningMethod weighting,
			@RequestParam("threshold") Integer threshold,
			@RequestParam("attrThreshold") Integer attrThreshold,
			@RequestParam(value = "networks", required = false) Long[] networkIds,
			@RequestParam(value = "attrgroups", required = false) Long[] attrGroupIds,
			HttpSession session) {

		return getModelAndView("/export/params_json", "export_params_json.jsp",
				organismId, geneLines, weighting, threshold, attrThreshold,
				networkIds, attrGroupIds, session);

	}

	/**
	 * Perform the search and return a text export
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/export/networks")
	public ModelAndView networks(
			@RequestParam("organism") Integer organismId,
			@RequestParam("genes") String geneLines,
			@RequestParam("weighting") CombiningMethod weighting,
			@RequestParam("threshold") Integer threshold,
			@RequestParam("attrThreshold") Integer attrThreshold,
			@RequestParam(value = "networks", required = false) Long[] networkIds,
			@RequestParam(value = "attrgroups", required = false) Long[] attrGroupIds,
			HttpSession session) {

		return getModelAndView("/export/networks", "export_networks.jsp",
				organismId, geneLines, weighting, threshold, attrThreshold,
				networkIds, attrGroupIds, session);

	}

	/**
	 * Perform the search and return a text export
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/export/genes")
	public ModelAndView genes(
			@RequestParam("organism") Integer organismId,
			@RequestParam("genes") String geneLines,
			@RequestParam("weighting") CombiningMethod weighting,
			@RequestParam("threshold") Integer threshold,
			@RequestParam("attrThreshold") Integer attrThreshold,
			@RequestParam(value = "networks", required = false) Long[] networkIds,
			@RequestParam(value = "attrgroups", required = false) Long[] attrGroupIds,
			HttpSession session) {

		return getModelAndView("/export/genes", "export_genes.jsp", organismId,
				geneLines, weighting, threshold, attrThreshold, networkIds,
				attrGroupIds, session);

	}

	/**
	 * Perform the search and return a text export
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/export/go")
	public ModelAndView go(
			@RequestParam("organism") Integer organismId,
			@RequestParam("genes") String geneLines,
			@RequestParam("weighting") CombiningMethod weighting,
			@RequestParam("threshold") Integer threshold,
			@RequestParam("attrThreshold") Integer attrThreshold,
			@RequestParam(value = "networks", required = false) Long[] networkIds,
			@RequestParam(value = "attrgroups", required = false) Long[] attrGroupIds,
			HttpSession session) {

		return getModelAndView("/export/go", "export_go.jsp", organismId,
				geneLines, weighting, threshold, attrThreshold, networkIds,
				attrGroupIds, session);

	}

	/**
	 * Perform the search and return a text export
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/export/interactions")
	public ModelAndView interactions(
			@RequestParam("organism") Integer organismId,
			@RequestParam("genes") String geneLines,
			@RequestParam("weighting") CombiningMethod weighting,
			@RequestParam("threshold") Integer threshold,
			@RequestParam("attrThreshold") Integer attrThreshold,
			@RequestParam(value = "networks", required = false) Long[] networkIds,
			@RequestParam(value = "attrgroups", required = false) Long[] attrGroupIds,
			HttpSession session) {

		return getModelAndView("/export/interactions",
				"export_interactions.jsp", organismId, geneLines, weighting,
				threshold, attrThreshold, networkIds, attrGroupIds, session);

	}

	/**
	 * Perform the search and return an attributes list
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/export/attributes")
	public ModelAndView attributes(
			@RequestParam("organism") Integer organismId,
			@RequestParam("genes") String geneLines,
			@RequestParam("weighting") CombiningMethod weighting,
			@RequestParam("threshold") Integer threshold,
			@RequestParam("attrThreshold") Integer attrThreshold,
			@RequestParam(value = "networks", required = false) Long[] networkIds,
			@RequestParam(value = "attrgroups", required = false) Long[] attrGroupIds,
			HttpSession session) {

		return getModelAndView("/export/attributes", "export_attributes.jsp",
				organismId, geneLines, weighting, threshold, attrThreshold,
				networkIds, attrGroupIds, session);

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
	 * @param threshold
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

		try {
			Integer organismId = null;
			try {
				organismId = Integer.parseInt(organism);

				for (Organism org : organismService.getOrganisms()) {
					long id = org.getTaxonomyId();

					if (id == organismId) {
						organismId = Integer.parseInt("" + org.getId());
						break;
					}
				}
			} catch (NumberFormatException e) {
				for (Organism org : organismService.getOrganisms()) {
					String alias = organismNameToUrl(org.getDescription());
					String name = organismNameToUrl(org.getAlias());

					if (alias.equals(organism) || name.equals(organism)) {
						organismId = new Long(org.getId()).intValue();
						break;
					}
				}
			}

			if (organismId == null) {
				throw new RuntimeException("organism");
			}

			if (pipeSeparatedGenes == null) {
				throw new RuntimeException("genes");
			}

			String[] genes = pipeSeparatedGenes.split("\\|");
			String geneLines = "";
			for (String gene : genes) {
				geneLines += gene + "\n";
			}

			// validate genes
			GeneNames geneNames = geneService.getGeneNames(organismId,
					geneLines);
			Collection<Gene> validGenes = geneService.findGenesForOrganism(
					organismId, geneNames.getValidGenes());
			if (validGenes.size() == 0) {
				throw new RuntimeException("genes");
			}

			CombiningMethod weighting;
			if (weightingAsString == null) {
				weighting = CombiningMethod.AUTOMATIC_SELECT;
			} else {
				weighting = CombiningMethod.fromCode(weightingAsString);
			}

			if (weighting == null || weighting == CombiningMethod.UNKNOWN) {
				throw new RuntimeException("weighting");
			}

			int threshold;
			if (thresholdAsString == null || thresholdAsString.equals("")) {
				threshold = 20;
			} else {
				try {
					threshold = Integer.parseInt(thresholdAsString);
				} catch (NumberFormatException e) {
					throw new RuntimeException("threshold");
				}
			}

			Long[] networkIds = null;

			Long[] attrGrIds = null;

			ModelAndView mv = getModelAndView("/link", "results.jsp",
					organismId, geneLines, weighting, threshold, 20,
					networkIds, attrGrIds, session, false);

			return mv;

		} catch (Exception e) {
			ModelAndView mv = ModelAndViewFactory.create("/link",
					"linkError.jsp");
			mv.addObject("error", e.getMessage());
			mv.addObject("o", organism);
			mv.addObject("g", pipeSeparatedGenes);
			mv.addObject("m", weightingAsString);
			mv.addObject("r", thresholdAsString);
			return mv;
		}

	}

	/**
	 * /** Gets search results from the engine
	 * 
	 * @param organismId
	 *            The ID of the organism
	 * @param geneLines
	 *            The gene names separated by newlines
	 * @param weighting
	 *            The weighting method
	 * @param threshold
	 *            The number of result genes to return
	 * @param networkIds
	 *            The IDs of networks to use in the search
	 * @param session
	 *            The HTTP session
	 * @return The results page
	 */
	private ModelAndView getModelAndView(String url, String jsp,
			Integer organismId, String geneLines,
			org.genemania.type.CombiningMethod weighting, Integer threshold,
			Integer attrThreshold, Long[] networkIds, Long[] attributeGroupIds,
			HttpSession session) {
		return getModelAndView(url, jsp, organismId, geneLines, weighting,
				threshold, attrThreshold, networkIds, attributeGroupIds,
				session, true);
	}

	/**
	 * /** Gets search results from the engine
	 * 
	 * @param organismId
	 *            The ID of the organism
	 * @param geneLines
	 *            The gene names separated by newlines
	 * @param weighting
	 *            The weighting method
	 * @param threshold
	 *            The number of result genes to return
	 * @param networkIds
	 *            The IDs of networks to use in the search
	 * @param session
	 *            The HTTP session
	 * @param includeUserNetworks
	 *            Whether to include user networks in the search
	 * @return The results page
	 */
	private ModelAndView getModelAndView(String url, String jsp,
			Integer organismId, String geneLines,
			org.genemania.type.CombiningMethod weighting, Integer threshold,
			Integer attrThreshold, Long[] networkIds, Long[] attrGroupIds,
			HttpSession session, boolean includeUserNetworks) {

		logger.debug("Return results view...");

		ModelAndView mv;

		try {
			// validate organism
			// ============================================

			Organism organism = organismService.findOrganismById(new Long(
					organismId));

			// validate networks
			// ============================================

			Collection<InteractionNetwork> networks = networkService
					.getNetworks(organismId, networkIds, session.getId(),
							includeUserNetworks);

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

			// search objects
			// ============================================

			Collection<Organism> organisms = organismService.getOrganisms();
			Organism defaultOrganism = organismService.getDefaultOrganism();
			Statistics stats = statsService.getStats();
			Map<Long, Collection<AttributeGroup>> organismIdToAttributeGroups = new HashMap<Long, Collection<AttributeGroup>>();
			for (Organism org : organisms) {
				organismIdToAttributeGroups.put(org.getId(),
						attributeGroupService.findAttributeGroups(org.getId()));
			}

			try {
				SearchResults results = searchService.search(params);

				Visualization vis = visualizationDataService
						.getVisualizationData(results);
				Collection<InteractionNetworkGroup> networkGroups = networkGroupService
						.findNetworkGroupsByOrganism(new Long(organismId),
								session.getId());

				// network groups for printing since they need to be sorted and
				// the
				// model is bad and does not support getting them sorted :(

				Collection<NetworkGroup> groups = new LinkedList<NetworkGroup>();
				Map<Long, Boolean> networkIdIncluded = new HashMap<Long, Boolean>();
				Map<String, NetworkGroup> nameToGroup = new HashMap<String, NetworkGroup>();

				Collection<InteractionNetwork> paramNetworks = params
						.getNetworks();

				if (paramNetworks.isEmpty()) {
					paramNetworks = networkService
							.findDefaultNetworksForOrganism(params
									.getOrganism().getId(), session.getId(),
									true);
				}

				for (InteractionNetwork network : paramNetworks) {
					networkIdIncluded.put(network.getId(), true);
				}

				String[] groupNames = new String[networkGroups.size()];
				int i = 0;
				for (InteractionNetworkGroup group : networkGroups) {
					boolean empty = true;
					if (group.getInteractionNetworks() != null
							&& group.getInteractionNetworks().size() != 0) {
						NetworkGroup g = new NetworkGroup(group.getName());
						nameToGroup.put(group.getName(), g);

						for (InteractionNetwork network : group
								.getInteractionNetworks()) {
							if (networkIdIncluded.containsKey(network.getId())) {
								g.add(network.getName());
								empty = false;
							}
						}

						if (empty) {
							groupNames[i++] = "";
						} else {
							groupNames[i++] = group.getName();
						}

					} else {
						groupNames[i++] = "";
					}
				}
				Arrays.sort(groupNames);

				for (String name : groupNames) {
					if (name == null || name.equals("")) {
						continue;
					}

					NetworkGroup group = nameToGroup.get(name);
					groups.add(group);
				}

				// add objects to the page
				// ============================================

				mv = ModelAndViewFactory.create(url, jsp);
				mv.addObject("organisms", organisms);
				mv.addObject("defaultOrganism", defaultOrganism);
				mv.addObject("organismIdToAttributeGroups",
						organismIdToAttributeGroups);
				mv.addObject("stats", stats);
				mv.addObject("dbversion", new java.text.SimpleDateFormat(
						"d MMMMM yyyy HH:mm:ss").format(statsService.getStats()
						.getDate()));
				mv.addObject("dbversionstd", new java.text.SimpleDateFormat(
						"yyyy-MM-dd").format(statsService.getStats().getDate()));

				// results objects

				mv.addObject("invalidGeneNames", geneNames.getInvalidGenes());
				mv.addObject("params", params);
				mv.addObject("results", results);
				mv.addObject("vis", vis);
				mv.addObject("organismId", organismId);
				mv.addObject("geneLines", geneLines);
				mv.addObject("networkIds", networkIds);
				mv.addObject("attrGroupIds", attrGroupIds);
				mv.addObject("weighting", weighting);
				mv.addObject("threshold", threshold);
				mv.addObject("sessionId", session.getId());
				mv.addObject("networkGroups", groups);
				mv.addObject("attributeGroups", attributeGroups);

			} catch (NoUserNetworkException e) {
				mv = ModelAndViewFactory.create(url, "search.jsp");
				mv.addObject("organisms", organisms);
				mv.addObject("defaultOrganism", defaultOrganism);
				mv.addObject("stats", stats);
				mv.addObject("dbversion", new java.text.SimpleDateFormat(
						"d MMMMM yyyy HH:mm:ss").format(statsService.getStats()
						.getDate()));
				mv.addObject("dbversionstd", new java.text.SimpleDateFormat(
						"yyyy-MM-dd").format(statsService.getStats().getDate()));

				// results objects

				mv.addObject("params", params);
				mv.addObject("organismId", organismId);
				mv.addObject("geneLines", geneLines);
				mv.addObject("networkIds", networkIds);
				mv.addObject("attrGroupIds", attrGroupIds);
				mv.addObject("weighting", weighting);
				mv.addObject("threshold", threshold);
				mv.addObject("sessionId", session.getId());
				mv.addObject("userNetworkException", e);
			}

		} catch (Exception e) {
			mv = ModelAndViewFactory.create(url, "error.jsp");
			mv.addObject("exception", e);
			mv.addObject("organismId", organismId);
			mv.addObject("geneLines", geneLines);
			mv.addObject("networkIds", networkIds);
			mv.addObject("attrGroupIds", attrGroupIds);
			mv.addObject("weighting", weighting);
			mv.addObject("threshold", threshold);
			mv.addObject("sessionId", session.getId());

			this.logger.error(e.getMessage(), e);

		}

		return mv;

	}

	public static class NetworkGroup {
		String name;
		Collection<String> networks = new LinkedList<String>();

		public NetworkGroup(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public String[] getNetworks() {
			String[] sorted = networks.toArray(new String[0]);
			Arrays.sort(sorted);

			return sorted;
		}

		public void add(String network) {
			networks.add(network);
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

	public Log getLogger() {
		return logger;
	}

	public void setLogger(Log logger) {
		this.logger = logger;
	}

	public NetworkGroupService getNetworkGroupService() {
		return networkGroupService;
	}

	public void setNetworkGroupService(NetworkGroupService networkGroupService) {
		this.networkGroupService = networkGroupService;
	}

}
