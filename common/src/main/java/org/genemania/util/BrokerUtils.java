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

/**
 * BrokerUtils: broker-related utility methods  
 * Created Jul 21, 2009
 * @author Ovi Comes
 */
package org.genemania.util;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.genemania.Constants;
import org.genemania.domain.Gene;
import org.genemania.domain.Interaction;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.Node;
import org.genemania.domain.OntologyCategory;
import org.genemania.dto.EnrichmentEngineRequestDto;
import org.genemania.dto.EnrichmentEngineResponseDto;
import org.genemania.dto.InteractionDto;
import org.genemania.dto.NetworkDto;
import org.genemania.dto.NodeDto;
import org.genemania.dto.OntologyCategoryDto;
import org.genemania.dto.RelatedGenesEngineRequestDto;
import org.genemania.dto.RelatedGenesEngineResponseDto;
import org.genemania.dto.RelatedGenesWebRequestDto;
import org.genemania.dto.RelatedGenesWebResponseDto;
import org.genemania.dto.UploadNetworkEngineRequestDto;
import org.genemania.dto.UploadNetworkEngineResponseDto;
import org.genemania.dto.UploadNetworkWebRequestDto;
import org.genemania.dto.UploadNetworkWebResponseDto;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.ValidationException;
import org.genemania.message.RelatedGenesRequestMessage;
import org.genemania.message.RelatedGenesResponseMessage;
import org.genemania.message.UploadNetworkRequestMessage;
import org.genemania.message.UploadNetworkResponseMessage;
import org.genemania.type.CombiningMethod;
import org.genemania.type.DataLayout;
import org.genemania.type.NetworkProcessingMethod;
import org.genemania.type.ScoringMethod;

public class BrokerUtils {

	// __[static]______________________________________________________________
	private static Logger LOG = Logger.getLogger(BrokerUtils.class);

	// __[public helpers]______________________________________________________
	public static RelatedGenesEngineRequestDto msg2dto(RelatedGenesRequestMessage msg) {
		// init & validate
		RelatedGenesEngineRequestDto ret = new RelatedGenesEngineRequestDto();
		if(msg == null) {
			LOG.warn("empty RelatedGenesRequestMessage");
		}
        // build the groups
		Collection<Collection<Long>> interactionNetworks = new ArrayList<Collection<Long>>();
        Map<String, Collection<Long>> groupsMap = new Hashtable<String, Collection<Long>>();
		for(NetworkDto network: msg.getNetworks()) {
			String type = "_";
			if(network.getId() < 0) {
				type = "user";
			} else {
				if(StringUtils.isNotEmpty(network.getType())) {
					type = network.getType().toLowerCase();
				}
			}
			Collection<Long> groupNetworks = groupsMap.get(type);
			if(groupNetworks == null) {
				groupNetworks = new ArrayList<Long>();
				groupsMap.put(type, groupNetworks);
			}
			groupNetworks.add(network.getId());
		}
		LOG.debug("network collections: " + groupsMap);
		// add the network collections to the dto 
		Iterator<String> iterator = groupsMap.keySet().iterator();
		while(iterator.hasNext()) {
			String nextGroup = iterator.next();
			Collection<Long> networks = groupsMap.get(nextGroup);
			if((networks != null) && (networks.size() > 0)) {
				interactionNetworks.add(networks);
			}
		}
		ret.setInteractionNetworks(interactionNetworks);
		// build the rest of the dto
		ret.setCombiningMethod(CombiningMethod.fromCode(msg.getCombiningMethod()));
		ret.setLimitResults(msg.getResultSize());
		ret.setOrganismId(msg.getOrganismId());
		ret.setPositiveNodes(msg.getPositiveNodes());
		ret.setScoringMethod(ScoringMethod.DISCRIMINANT); // TODO: this should be a config prop
		ret.setNamespace(msg.getUserDefinedNetworkNamespace());
        ret.setAttributeGroups(msg.getAttributeGroups());
        ret.setAttributesLimit(msg.getAttributesLimit());
		ret.setProgressReporter(NullProgressReporter.instance());
		// done
		return ret;
	}

	public static UploadNetworkEngineRequestDto msg2dto(UploadNetworkRequestMessage msg) {
		// init & validate
		UploadNetworkEngineRequestDto ret = new UploadNetworkEngineRequestDto();
		if(msg == null) {
			LOG.error("empty UploadNetworkRequestMessage");
		} else {
			// populate the request dto
			ret.setData(new StringReader(msg.getData()));
			ret.setLayout(DataLayout.fromCode(msg.getLayout()));
			ret.setMethod(NetworkProcessingMethod.fromCode(msg.getMethod()));
			ret.setNamespace(msg.getNamespace());
			ret.setNetworkId(msg.getNetworkId());
			ret.setOrganismId(msg.getOrganismId());
			ret.setProgressReporter(NullProgressReporter.instance());
			ret.setSparsification(msg.getSparsification());
		}
		// done
		return ret;
	}
	
	/**
	 * @deprecated Use RelatedGenesResponseMessage dto2msg(RelatedGenesEngineResponseDto dto, EnrichmentEngineResponseDto eResponseDto) instead
	 */
	public static RelatedGenesResponseMessage dto2msg(RelatedGenesEngineResponseDto dto) {
		RelatedGenesResponseMessage ret = new RelatedGenesResponseMessage();
		ret.setNetworks(dto.getNetworks());
		return ret;
	}

	public static RelatedGenesResponseMessage dto2msg(RelatedGenesEngineResponseDto rgdto, EnrichmentEngineResponseDto edto) {
		RelatedGenesResponseMessage ret = new RelatedGenesResponseMessage();
		ret.setNetworks(rgdto.getNetworks());
		ret.setAnnotations(edto.getAnnotations());
		ret.setCombiningMethod(rgdto.getCombiningMethodApplied().toString());
		ret.setAttributes(rgdto.getNodeToAttributes());
		return ret;
	}

	public static UploadNetworkResponseMessage dto2msg(UploadNetworkEngineResponseDto dto) {
		UploadNetworkResponseMessage ret = new UploadNetworkResponseMessage();
		ret.setInteractionCount(dto.getNumInteractions());
		return ret;
	}
	
	public static RelatedGenesRequestMessage dto2msg(RelatedGenesWebRequestDto dto) {
		RelatedGenesRequestMessage ret = new RelatedGenesRequestMessage();
		if (dto == null) {
			LOG.error("empty RelatedGenesWebRequestDto");
		}
		if(dto.getCombiningMethod() != null) {
			ret.setCombiningMethod(dto.getCombiningMethod().getCode());
		} else {
			LOG.error("unknown combining method");
			ret.setCombiningMethod(CombiningMethod.UNKNOWN.getCode());
		}
		ret.setOrganismId(dto.getOrganismId());
		ret.setOntologyId(dto.getOntologyId());
		ret.setResultSize(dto.getResultSize());
		ret.setAttributesLimit(dto.getAttributesLimit());
		Collection<Long> positives = new ArrayList<Long>();
		for(Gene gene: dto.getInputGenes()) {
			if(gene != null) {
				if(gene.getNode() != null) {
					positives.add(gene.getNode().getId());
				} else {
					LOG.error("no node for gene " + gene.getId() + "-" + gene.getSymbol());
				}
			} else {
				LOG.error("null gene found in DTO's input gene list");
			}
		}
		ret.setPositiveNodes(positives);
		Collection<NetworkDto> networks = new ArrayList<NetworkDto>();
		for(InteractionNetwork in: dto.getInputNetworks()) {
			NetworkDto network = new NetworkDto();
			network.setId(in.getId());
			if(in.getMetadata() != null) {
				network.setType(in.getMetadata().getNetworkType());
			}
			networks.add(network);
		}
		ret.setNetworks(networks);
		ret.setAttributeGroups(dto.getAttributeGroups());
		ret.setUserDefinedNetworkNamespace(dto.getUserDefinedNetworkNamespace());

		return ret;
	}
	
	public static UploadNetworkRequestMessage dto2msg(UploadNetworkWebRequestDto dto) {
		UploadNetworkRequestMessage ret = new UploadNetworkRequestMessage();
		// populate the request message
		ret.setData(dto.getData());
		ret.setMethod(dto.getProcessingMethod().getCode());
		ret.setNamespace(dto.getNamespace());
		ret.setNetworkId(dto.getNetworkId());
		ret.setOrganismId(dto.getOrganismId());
		ret.setSparsification(dto.getSparsification());
		ret.setLayout(dto.getDataLayout().getCode());
		// done
		return ret;
	}	

	// return a hollow dto
	public static RelatedGenesWebResponseDto msg2dto(RelatedGenesResponseMessage msg) throws ApplicationException {
		//init
		RelatedGenesWebResponseDto ret = new RelatedGenesWebResponseDto();
		if(msg.getErrorCode() != 0) {
			throw new ApplicationException(msg.getErrorMessage(), msg.getErrorCode());
		}
		//networks
		List<InteractionNetwork> networks = new ArrayList<InteractionNetwork>();
		Map<Long, Double> networkWeightsMap = new Hashtable<Long, Double>();
		for(NetworkDto nvo: msg.getNetworks()) {
			networkWeightsMap.put(nvo.getId(), nvo.getWeight());
			InteractionNetwork network = new InteractionNetwork();
			network.setId(nvo.getId());
			Collection<Interaction> interactions = new ArrayList<Interaction>();
			for(InteractionDto ivo: nvo.getInteractions()) {
				Interaction interaction = new Interaction();
				Node fromNode = new Node();
				fromNode.setId(ivo.getNodeVO1().getId());
				interaction.setFromNode(fromNode);
				Node toNode = new Node();
				toNode.setId(ivo.getNodeVO2().getId());
				interaction.setToNode(toNode);
				interaction.setWeight((float)ivo.getWeight());//TODO: switch interaction hbm weight to double
				interactions.add(interaction);
			}
			network.setInteractions(interactions);
			networks.add(network);
		}
		ret.setNetworks(networks);
		ret.setNetworkWeightsMap(networkWeightsMap);
		ret.setNodeScoresMap(buildNodeScoresMap(msg.getNodes()));
		// annotations
		Map<Long, OntologyCategoryDto> ontologyCategories = new Hashtable<Long, OntologyCategoryDto>();
		Map<Long, Collection<OntologyCategory>> annotations = new Hashtable<Long, Collection<OntologyCategory>>();
		Map<Long, Collection<OntologyCategoryDto>> enrichedCategoriesMap = msg.getAnnotations();
		Iterator<Long> nodesIterator = enrichedCategoriesMap.keySet().iterator();
		while(nodesIterator.hasNext()) {
			long nodeId = nodesIterator.next();
			Collection<OntologyCategoryDto> categoryVOs = enrichedCategoriesMap.get(nodeId);
			Collection<OntologyCategory> categories = new ArrayList<OntologyCategory>();
			for(OntologyCategoryDto category: categoryVOs) {
				OntologyCategory cat = new OntologyCategory();
                cat.setId(category.getId());
				cat.setName("TODO: get from db");
				categories.add(cat);
				ontologyCategories.put(cat.getId(), category);
			}
			annotations.put(nodeId, categories);
		}
		ret.setAnnotations(annotations);
		ret.setOntologyCategories(ontologyCategories);
		ret.setOrganismId(msg.getOrganismId());
		ret.setCombiningMethod(CombiningMethod.fromCode(msg.getCombiningMethod()));
		ret.setAttributes(msg.getAttributes()); // this ok or need to take a copy??
		return ret;
	}
	
	// helper to stuff bits of node object into map Node ID -> Score
	private static Map<Long, Double> buildNodeScoresMap(
            Collection<NodeDto> nodes) {
	    
	    Map<Long, Double> nodeScoresMap = new Hashtable<Long, Double>();
	    for (NodeDto node: nodes) {
	        nodeScoresMap.put(node.getId(), node.getScore());
	    }
	    
	    return nodeScoresMap;
    }

    public static UploadNetworkWebResponseDto msg2dto(UploadNetworkResponseMessage msg) throws ApplicationException {
		UploadNetworkWebResponseDto ret = new UploadNetworkWebResponseDto();
		if(msg.getErrorCode() != 0) {
			throw new ApplicationException(msg.getErrorMessage(), msg.getErrorCode());
		}
		ret.setInteractionCount(msg.getInteractionCount());
		return ret;
	}	

	public static EnrichmentEngineRequestDto buildEnrichmentRequestFrom(RelatedGenesEngineRequestDto rgRequestDto, RelatedGenesEngineResponseDto rgResponseDto, long ontologyId) throws ApplicationException {
		LOG.debug("building enrichment request");
		//init
		EnrichmentEngineRequestDto ret = new EnrichmentEngineRequestDto();
		try {
			//read config data
			int minCategories = Integer.parseInt(ApplicationConfig.getInstance().getProperty(Constants.CONFIG_PROPERTIES.ENRICHMENT_MIN_CATEGORIES));
			double qValueThreshold = Double.parseDouble(ApplicationConfig.getInstance().getProperty(Constants.CONFIG_PROPERTIES.ENRICHMENT_Q_VAL_THRESHOLD));
			// read input DTO data
			Set<Long> uniqueInputNodes = new HashSet<Long>();
			Collection<Long> inputNodes = rgRequestDto.getPositiveNodes();
			uniqueInputNodes.addAll(inputNodes);
			LOG.debug("added " + uniqueInputNodes.size() + "/" + inputNodes.size() + " input nodes");
			List<NetworkDto> outputNetworks = rgResponseDto.getNetworks();
			int outputNodeCounter = 0;
			for(NetworkDto network: outputNetworks) {
				Collection<InteractionDto> interactions = network.getInteractions();
				for(InteractionDto interaction: interactions) {
					uniqueInputNodes.add(interaction.getNodeVO1().getId());
					uniqueInputNodes.add(interaction.getNodeVO2().getId());
					outputNodeCounter += 2;
				}
			}
			LOG.debug("added " + (uniqueInputNodes.size() - inputNodes.size()) + "/" + outputNodeCounter + " input nodes");
			Collection<Long> nodes = new ArrayList<Long>();
			Iterator<Long> uniqueNodesIterator = uniqueInputNodes.iterator();
			while(uniqueNodesIterator.hasNext()) {
				nodes.add(uniqueNodesIterator.next());
			}
			long organismId = rgRequestDto.getOrganismId();
			LOG.debug("organismId=" + organismId + ", ontologyId=" + ontologyId);
			ValidationUtils.validateEnrichmentParameters(minCategories, ontologyId, qValueThreshold);
			// populate the enrichment DTO
			ret.setMinCategories(minCategories);
			ret.setNodes(nodes);
			ret.setOntologyId(ontologyId);
			ret.setOrganismId(organismId);
			ret.setProgressReporter(NullProgressReporter.instance());
			ret.setqValueThreshold(qValueThreshold);
		} catch (NumberFormatException e) {
			throw new ApplicationException(e);			
		} catch (ValidationException e) {
			throw new ApplicationException(e);			
		}
		return ret;
	}
}
