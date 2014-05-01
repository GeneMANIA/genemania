package org.genemania.service.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.genemania.connector.EngineConnector;
import org.genemania.dao.AttributeDao;
import org.genemania.dao.AttributeGroupDao;
import org.genemania.dao.GeneDao;
import org.genemania.domain.Attribute;
import org.genemania.domain.AttributeGroup;
import org.genemania.domain.Gene;
import org.genemania.domain.Interaction;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.OntologyCategory;
import org.genemania.domain.ResultAttribute;
import org.genemania.domain.ResultAttributeGroup;
import org.genemania.domain.ResultGene;
import org.genemania.domain.ResultInteraction;
import org.genemania.domain.ResultInteractionNetwork;
import org.genemania.domain.ResultInteractionNetworkGroup;
import org.genemania.domain.ResultOntologyCategory;
import org.genemania.domain.SearchParameters;
import org.genemania.domain.SearchResults;
import org.genemania.dto.AttributeDto;
import org.genemania.dto.OntologyCategoryDto;
import org.genemania.dto.RelatedGenesWebRequestDto;
import org.genemania.dto.RelatedGenesWebResponseDto;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.exception.NoUserNetworkException;
import org.genemania.service.AttributeGroupService;
import org.genemania.service.NetworkGroupService;
import org.genemania.service.NetworkService;
import org.genemania.service.SearchService;
import org.genemania.type.CombiningMethod;
import org.genemania.util.AttributeLinkoutGenerator;
import org.genemania.util.GeneLinkoutGenerator;
import org.springframework.beans.factory.annotation.Autowired;

public class SearchServiceImpl implements SearchService {

	@Autowired
	EngineConnector engineConnector;

	@Autowired
	GeneDao geneDao;

	@Autowired
	AttributeGroupService attributeGroupService;

	@Autowired
	NetworkGroupService networkGroupService;

	@Autowired
	NetworkService networkService;

	@Autowired
	AttributeDao attributeDao;

	@Override
	public SearchResults search(SearchParameters params)
			throws ApplicationException, DataStoreException,
			NoUserNetworkException {

		Collection<Gene> genes = new HashSet<Gene>();
		for (Gene gene : params.getGenes()) {
			genes.add(gene);
		}

		Collection<InteractionNetwork> userNetworks = networkService
				.getUserNetworks(params.getOrganism().getId(),
						params.getNamespace());

		if (params.getNetworks() != null) {
			for (InteractionNetwork network : params.getNetworks()) {

				boolean networkBelongsToSession = false;

				if (network.getId() >= 0) {
					continue;
				}

				if (userNetworks != null) {
					for (InteractionNetwork userNetwork : userNetworks) {

						if (network.getId() == userNetwork.getId()) {
							networkBelongsToSession = true;
							break;
						}

					}
				}

				if (!networkBelongsToSession) {
					throw new NoUserNetworkException(network.getId(),
							network.getName());
				}
			}
		}

		Collection<InteractionNetwork> networks = new HashSet<InteractionNetwork>();
		Collection<Long> attributeGroupIds = new LinkedList<Long>();

		boolean noNetworks = params.getNetworks() == null
				|| params.getNetworks().size() == 0;
		boolean noAttributes = params.getAttributeGroups() == null
				|| params.getAttributeGroups().size() == 0;

		if (noNetworks && noAttributes) {

			for (InteractionNetwork network : networkService
					.findDefaultNetworksForOrganism(params.getOrganism()
							.getId(), params.getNamespace())) {
				networks.add(network);
			}

			for (AttributeGroup group : attributeGroupService
					.findDefaultAttributeGroups(params.getOrganism().getId())) {
				attributeGroupIds.add(group.getId());
			}
		} else {
			for (InteractionNetwork network : params.getNetworks()) {
				networks.add(network);
			}

			for (AttributeGroup group : params.getAttributeGroups()) {
				attributeGroupIds.add(group.getId());
			}
		}

		// dto param object and search
		RelatedGenesWebRequestDto requestDto = new RelatedGenesWebRequestDto();
		requestDto.setOrganismId(params.getOrganism().getId());
		requestDto.setCombiningMethod(params.getWeighting());
		requestDto.setResultSize(params.getResultsSize());
		requestDto.setUserDefinedNetworkNamespace(params.getNamespace());
		requestDto.setInputNetworks(networks);
		requestDto.setInputGenes(genes);
		requestDto.setOntologyId(params.getOrganism().getOntology().getId());
		requestDto.setAttributeGroups(attributeGroupIds);
		requestDto.setAttributesLimit(params.getAttributeResultsSize());

		RelatedGenesWebResponseDto responseDto = engineConnector
				.getRelatedGenes(requestDto);

		// hold onto this map to build the unique list of result go terms
		Map<String, ResultOntologyCategory> descrToROcat = new HashMap<String, ResultOntologyCategory>();

		// handle genes results
		// ==================================================

		Map<Long, Gene> idToQueryGene = new HashMap<Long, Gene>();
		Collection<ResultGene> rGenes = new LinkedList<ResultGene>();
		Map<Long, ResultGene> idToResultGene = new HashMap<Long, ResultGene>();

		// whether a gene is a query param gene or not
		Map<Boolean, Set<Long>> isQueryToId = new HashMap<Boolean, Set<Long>>();

		for (Gene gene : params.getGenes()) {
			idToQueryGene.put(gene.getNode().getId(), gene);
		}

		isQueryToId.put(true, new HashSet<Long>());
		isQueryToId.put(false, new HashSet<Long>());

		for (Long id : responseDto.getNodeScoresMap().keySet()) {
			isQueryToId.get(false).add(id);
		}
		for (Gene gene : params.getGenes()) {
			Long id = gene.getNode().getId();
			isQueryToId.get(false).remove(id);
			isQueryToId.get(true).add(id);
		}

		// add genes
		// ==================================================

		for (boolean isQuery : isQueryToId.keySet()) {
			for (Long id : isQueryToId.get(isQuery)) {
				Gene gene = geneDao.findGeneForId(responseDto.getOrganismId(),
						id);

				double score;
				if (responseDto.getNodeScoresMap().isEmpty()) {
					score = 0.0;
				} else if (responseDto.getNodeScoresMap().containsKey(id)) {
					score = responseDto.getNodeScoresMap().get(id);
				} else {
					score = 0.0;
				}

				Map<String, String> nameToUrl = GeneLinkoutGenerator.instance()
						.getLinkouts(params.getOrganism(), gene.getNode());
				Collection<ResultGene.Link> links = new LinkedList<ResultGene.Link>();

				for (String name : nameToUrl.keySet()) {
					String url = nameToUrl.get(name);
					ResultGene.Link link = new ResultGene.Link(name, url);
					links.add(link);
				}

				Map<String, OntologyCategory> descrToOcat = new HashMap<String, OntologyCategory>();
				Gene queryGene = idToQueryGene.get(gene.getNode().getId());
				String typedName = (queryGene == null ? "" : queryGene
						.getSymbol());
				ResultGene rGene = new ResultGene(gene, score, isQuery, links,
						typedName);
				Collection<OntologyCategory> oCats = responseDto
						.getAnnotations().get(id);
				String[] descrs = new String[oCats == null ? 0 : oCats.size()];

				int i = 0;
				if (oCats != null) {
					for (OntologyCategory oCat : oCats) {
						descrs[i++] = oCat.getDescription();
						descrToOcat.put(oCat.getDescription(), oCat);
					}
				}
				Arrays.sort(descrs, new Comparator<String>() {

					@Override
					public int compare(String arg0, String arg1) {
						return arg0.compareToIgnoreCase(arg1);
					}

				});

				for (String descr : descrs) {
					OntologyCategory oCat = descrToOcat.get(descr);
					long oId = oCat.getId();
					OntologyCategoryDto oCatDto = responseDto
							.getOntologyCategories().get(oId);
					ResultOntologyCategory rOCat = new ResultOntologyCategory(
							oCat, oCatDto.getpValue(), oCatDto.getqValue(),
							oCatDto.getNumAnnotatedInSample(),
							oCatDto.getNumAnnotatedInTotal());

					if (!descrToROcat.containsKey(descr)) {
						descrToROcat.put(descr, rOCat);
					}
					rGene.getResultOntologyCategories().add(rOCat);
				}

				rGenes.add(rGene);
				idToResultGene.put(rGene.getGene().getNode().getId(), rGene);
			}
		}

		// sort genes by score
		Collections.sort((LinkedList) rGenes);

		// create networks results
		// ==================================================

		Collection<ResultInteractionNetworkGroup> rNetworkGroups = new LinkedList<ResultInteractionNetworkGroup>();
		Map<String, ResultInteractionNetworkGroup> nameToRNetworkGroup = new HashMap<String, ResultInteractionNetworkGroup>();
		for (InteractionNetwork network : responseDto.getNetworks()) {

			if (network.getId() < 0) {
				InteractionNetwork userNetwork = networkService
						.findNetwork(network.getId());

				if (userNetwork == null) {
					throw new NoUserNetworkException(network.getId(),
							network.getName());
				}

				network.setDefaultSelected(userNetwork.isDefaultSelected());
				network.setDescription(userNetwork.getDescription());
				network.setMetadata(userNetwork.getMetadata());
				network.setName(userNetwork.getName());
				network.setTags(userNetwork.getTags());
			}

			Collection<ResultInteraction> rInteractions = new LinkedList<ResultInteraction>();
			for (Interaction interaction : network.getInteractions()) {
				ResultGene fromGene = idToResultGene.get(interaction
						.getFromNode().getId());
				ResultGene toGene = idToResultGene.get(interaction.getToNode()
						.getId());
				ResultInteraction rInteraction = new ResultInteraction(
						interaction, fromGene, toGene);

				rInteractions.add(rInteraction);
			}

			long id = network.getId();
			double weight = responseDto.getNetworkWeightsMap().get(id);
			ResultInteractionNetwork rNetwork = new ResultInteractionNetwork(
					rInteractions, network, weight);
			String groupName = network.getMetadata().getNetworkType();

			// add result network to result group
			ResultInteractionNetworkGroup rGroup;
			if (nameToRNetworkGroup.containsKey(groupName)) {
				// get existing group
				rGroup = nameToRNetworkGroup.get(groupName);
			} else {
				// create the group
				InteractionNetworkGroup networkGroup = networkGroupService
						.findNetworkGroupByName(params.getOrganism().getId(),
								groupName, params.getNamespace());

				rGroup = new ResultInteractionNetworkGroup();
				rGroup.setNetworkGroup(networkGroup);
				nameToRNetworkGroup.put(groupName, rGroup);
				rNetworkGroups.add(rGroup);
			}
			rGroup.getResultNetworks().add(rNetwork);
		}

		// sort network groups
		Collections.sort((LinkedList) rNetworkGroups);

		// sort networks
		for (ResultInteractionNetworkGroup group : rNetworkGroups) {
			Collections.sort((List) group.getResultNetworks());
		}

		// create GO results
		// ==================================================

		Collection<ResultOntologyCategory> rOCats = new LinkedList<ResultOntologyCategory>();
		for (String descr : descrToROcat.keySet()) {
			ResultOntologyCategory rOCat = descrToROcat.get(descr);
			rOCats.add(rOCat);
		}

		// sort GO results
		Collections.sort((LinkedList) rOCats);

		// actual weighting type
		// ==================================================

		CombiningMethod method = responseDto.getCombiningMethod();

		// create attributes results
		// ==================================================

		Map<Long, Collection<AttributeDto>> nodeIdToAttributeDto = responseDto
				.getAttributes();
		Map<Long, ResultAttributeGroup> idToRAttrGroup = new HashMap<Long, ResultAttributeGroup>();
		Collection<ResultAttributeGroup> rAttrGroups = new LinkedList<ResultAttributeGroup>();
		Map<Long, ResultAttribute> idToRAttr = new HashMap<Long, ResultAttribute>();
		Map<Long, Set<Long>> attrIdToNodeIds = new HashMap<Long, Set<Long>>();

		for (Long nodeId : nodeIdToAttributeDto.keySet()) {
			ResultGene rGene = idToResultGene.get(nodeId);
			Collection<AttributeDto> attrDtos = nodeIdToAttributeDto
					.get(nodeId);

			for (AttributeDto attrDto : attrDtos) {
				Attribute attr = attributeDao.findAttribute(params.getOrganism().getId(), attrDto.getId());
				double weight = attrDto.getWeight();
				long groupId = attrDto.getGroupId();
				long attrId = attrDto.getId();

				Set<Long> nodeIds;
				if (attrIdToNodeIds.containsKey(attrId)) {
					nodeIds = attrIdToNodeIds.get(attrId);
				} else {
					nodeIds = new HashSet<Long>();
					attrIdToNodeIds.put(attrId, nodeIds);
				}
				nodeIds.add(nodeId);

				ResultAttributeGroup rAttrGroup;

				// add to collections
				if (idToRAttrGroup.containsKey(groupId)) {
					rAttrGroup = idToRAttrGroup.get(groupId);
				} else {
					AttributeGroup attrGroup = this.attributeGroupService
							.findAttributeGroup(params.getOrganism().getId(),
									attrDto.getGroupId());
					rAttrGroup = new ResultAttributeGroup(attrGroup);
					rAttrGroup.setAttributeGroup(attrGroup);

					idToRAttrGroup.put(groupId, rAttrGroup);
					rAttrGroups.add(rAttrGroup);
				}

				ResultAttribute rAttr;
				if (idToRAttr.containsKey(attrId)) {
					rAttr = idToRAttr.get(attrId);
				} else {
					rAttr = new ResultAttribute();
					rAttr.setAttribute(attr);
					rAttr.setResultAttributeGroup(rAttrGroup);
					rAttr.setWeight(weight);
					rAttr.setNumAnnotatedInSample(0);
					rAttr.setNumAnnotatedInTotal(0);

					// get linkouts for attr
					Map<String, String> nameToUrl = AttributeLinkoutGenerator
							.instance().getLinkouts(params.getOrganism(),
									rAttrGroup.getAttributeGroup(), attr);
					Collection<ResultAttribute.Link> links = new LinkedList<ResultAttribute.Link>();

					for (String name : nameToUrl.keySet()) {
						String url = nameToUrl.get(name);
						ResultAttribute.Link link = new ResultAttribute.Link(
								name, url);
						links.add(link);
					}
					rAttr.setLinks(links);

					idToRAttr.put(attrId, rAttr);
				}

				// add references to the attr for the group and the gene
				if (!rAttrGroup.getResultAttributes().contains(rAttr)) {
					rAttrGroup.getResultAttributes().add(rAttr);
				}
				rGene.getResultAttributes().add(rAttr);
			}
		}

		for (long id : idToRAttr.keySet()) {
			ResultAttribute rAttr = idToRAttr.get(id);
			rAttr.setNumAnnotatedInSample(attrIdToNodeIds.get(
					rAttr.getAttribute().getId()).size());
		}

		// return
		SearchResults results = new SearchResults(rNetworkGroups, rGenes,
				rOCats, rAttrGroups, method);
		return results;
	}

	public EngineConnector getEngineConnector() {
		return engineConnector;
	}

	public void setEngineConnector(EngineConnector engineConnector) {
		this.engineConnector = engineConnector;
	}

	public GeneDao getGeneDao() {
		return geneDao;
	}

	public void setGeneDao(GeneDao geneDao) {
		this.geneDao = geneDao;
	}

	public NetworkGroupService getNetworkGroupService() {
		return networkGroupService;
	}

	public void setNetworkGroupService(NetworkGroupService networkGroupService) {
		this.networkGroupService = networkGroupService;
	}

	public NetworkService getNetworkService() {
		return networkService;
	}

	public void setNetworkService(NetworkService networkService) {
		this.networkService = networkService;
	}

	public AttributeGroupService getAttributeGroupService() {
		return attributeGroupService;
	}

	public void setAttributeGroupService(
			AttributeGroupService attributeGroupService) {
		this.attributeGroupService = attributeGroupService;
	}

	public AttributeDao getAttributeDao() {
		return attributeDao;
	}

	public void setAttributeDao(AttributeDao attributeDao) {
		this.attributeDao = attributeDao;
	}

}
