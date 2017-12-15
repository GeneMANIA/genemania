/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2008-2011 University of Toronto.
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
package org.genemania.plugin;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.genemania.data.normalizer.DataImportSettings;
import org.genemania.domain.Gene;
import org.genemania.domain.Interaction;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.NetworkMetadata;
import org.genemania.domain.ResultGene;
import org.genemania.domain.ResultInteraction;
import org.genemania.domain.ResultInteractionNetwork;
import org.genemania.domain.ResultInteractionNetworkGroup;
import org.genemania.domain.SearchRequest;
import org.genemania.domain.SearchResults;
import org.genemania.dto.InteractionDto;
import org.genemania.dto.NetworkDto;
import org.genemania.dto.NodeDto;
import org.genemania.dto.RelatedGenesEngineRequestDto;
import org.genemania.dto.RelatedGenesEngineResponseDto;
import org.genemania.dto.UploadNetworkEngineRequestDto;
import org.genemania.type.NetworkProcessingMethod;
import org.genemania.util.ProgressReporter;

public final class ManiaUtils {
	
	public static final int QUERY_GENE_THRESHOLD = 6;
	private static final int DEFAULT_SPARSIFICATION = 50;

	private ManiaUtils() {
	}
	
	public static UploadNetworkEngineRequestDto createRequest(DataImportSettings settings, Reader sourceData, ProgressReporter progress) {
		UploadNetworkEngineRequestDto request = new UploadNetworkEngineRequestDto();
		request.setLayout(settings.getDataLayout());
		request.setMethod(settings.getProcessingMethod());
		request.setNamespace(GeneMania.DEFAULT_NAMESPACE);
		request.setOrganismId(settings.getOrganism().getId());
		request.setNetworkId(settings.getNetwork().getId());
		request.setProgressReporter(progress);
		request.setData(sourceData);
		
		int sparsification = DEFAULT_SPARSIFICATION;
		switch (settings.getDataLayout()) {
		case GEO_PROFILE:
		case SPARSE_PROFILE:
		case PROFILE:
			request.setSparsification(sparsification);
			break;
		case BINARY_NETWORK:
			if (request.getMethod().equals(NetworkProcessingMethod.LOG_FREQUENCY)) {
				request.setSparsification(sparsification);
			}
			break;
		}
		
		return request;
	}
	
	public static RelatedGenesEngineRequestDto req2dto(SearchRequest req) {
		RelatedGenesEngineRequestDto dto = new RelatedGenesEngineRequestDto();
		
		dto.setAttributesLimit(req.getAttrThreshold());
		dto.setLimitResults(req.getGeneThreshold());
		dto.setCombiningMethod(req.getWeighting());
//		dto.setScoringMethod();
//		dto.setNamespace();
//		dto.setPositiveNodes();
//		dto.setInteractionNetworks(Arrays.asList(req.getNetworks()));
		dto.setAttributeGroups(Arrays.asList(req.getAttrGroups()));
		dto.setOrganismId(req.getOrganism());
		
		return dto;
	}
	
	public static RelatedGenesEngineResponseDto res2dto(SearchResults res) {
		RelatedGenesEngineResponseDto dto = new RelatedGenesEngineResponseDto();

		// Convert ResultGenes to NodeDtos
		Collection<ResultGene> resGenes = res.getResultGenes();
		List<NodeDto> nodeDtoList = new ArrayList<>();
		
		for (ResultGene rg : resGenes) {
			Gene gene = rg.getGene();
			NodeDto nodeDto = new NodeDto(gene.getId(), rg.getScore());
			nodeDtoList.add(nodeDto);
		}
		
		/*
		 * 'data': {\n",
    "                'id'         : str(gene['gene']['id']),\n",
    "                'name'       : gene['gene']['symbol'],\n",
    "                'shared_name': gene['gene']['symbol'],\n",
    "                'queryGene'  : gene['queryGene'],\n",
    "                'score'      : gene['score'],\n",
    "            }\n",
		*/
		
		
		// Convert ResultInteractionNetworkGroup to NetworkDto
		Collection<ResultInteractionNetworkGroup> resNetGroups = res.getResultNetworkGroups();
		List<NetworkDto> netDtoList = new ArrayList<>();
		Map<Long, NodeDto> nodeDtoLookup = new HashMap<>(); // To reuse NodeDto instances with same id...
		
		for (ResultInteractionNetworkGroup rng : resNetGroups) {
			InteractionNetworkGroup netGroup = rng.getNetworkGroup();
			Collection<ResultInteractionNetwork> resNetworks = rng.getResultNetworks();
			
			for (ResultInteractionNetwork rn : resNetworks) {
				InteractionNetwork in = rn.getNetwork();
				NetworkMetadata metadata = in.getMetadata();
				
				List<InteractionDto> interactionDtoList = new ArrayList<>();
				
				for (ResultInteraction ri: rn.getResultInteractions()) {
					Interaction interaction = ri.getInteraction();
					long nodeId1 = ri.getFromGene().getGene().getId();
					long nodeId2 = ri.getToGene().getGene().getId();
					
					// TODO create lookup and reuse instances with same id!!!
					NodeDto nodeDto1 = nodeDtoLookup.get(nodeId1);
					NodeDto nodeDto2 = nodeDtoLookup.get(nodeId2);
					
					if (nodeDto1 == null) {
						nodeDto1 = new NodeDto(nodeId1, ri.getFromGene().getScore());
						nodeDtoLookup.put(nodeId1, nodeDto1);
					}
					if (nodeDto2 == null) {
						nodeDto2 = new NodeDto(nodeId2, ri.getToGene().getScore());
						nodeDtoLookup.put(nodeId2, nodeDto2);
					}
					
					InteractionDto interactionDto = new InteractionDto(nodeDto1, nodeDto2, interaction.getWeight());
					interactionDtoList.add(interactionDto);
				}
				
				NetworkDto netDto = new NetworkDto(metadata.getId(), rn.getWeight());
				netDto.setType(metadata.getNetworkType());
				netDto.setInteractions(interactionDtoList);
				netDtoList.add(netDto);
			}
		}
	
	/*	
		 for net_gr in res_json['resultNetworkGroups']:\n",
    "        gr_name = net_gr['networkGroup']['name']\n",
    "        for net in net_gr['resultNetworks']:\n",
    "            for inter in net['resultInteractions']:\n",
    "                edge = {\n",
    "                    'group': 'edges',\n",
    "                    'data': {\n",
    "                        'name'       : gr_name,\n",
    "                        'shared_name': gr_name,\n",
    "                        'source'     : str(inter['fromGene']['gene']['id']),\n",
    "                        'target'     : str(inter['toGene']['gene']['id']),\n",
    "                        'weight'     : inter['interaction']['weight'],\n",
    "                    }\n",
    "                }\n",
    "                count = count + 1\n",
    "                cy_elements.append(edge)\n",
    "                edges.append(edge)\n",
		 */
		
		
// ___ RES:
//		Collection<ResultGene> resultGenes;
//		Collection<ResultInteractionNetworkGroup> resultNetworkGroups;
//		Collection<ResultOntologyCategory> resultOntologyCategories;
//		CombiningMethod weighting;
//		Collection<ResultAttributeGroup> resultAttributeGroupss;

//		String error;
//		SearchResultsErrorCode errorCode;
		
// --- DTO:
//		List<NodeDto> nodes;
//		List<NetworkDto> networks;
//		CombiningMethod combiningMethodApplied;
//		Collection<AttributeDto> attributes; // redundant
//		Map<Long, Collection<AttributeDto>> nodeToAttributes;  // maps Node ID -> Collection<AttributeDto>
		
		return dto;
	}
}
