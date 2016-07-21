package org.genemania.connector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.genemania.broker.SyncWebWorker;
import org.genemania.dao.NetworkDao;
import org.genemania.dao.NodeDao;
import org.genemania.dao.OrganismDao;
import org.genemania.domain.Interaction;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.Node;
import org.genemania.domain.Ontology;
import org.genemania.domain.OntologyCategory;
import org.genemania.domain.Organism;
import org.genemania.dto.InteractionDto;
import org.genemania.dto.NetworkDto;
import org.genemania.dto.OntologyCategoryDto;
import org.genemania.dto.RelatedGenesWebRequestDto;
import org.genemania.dto.RelatedGenesWebResponseDto;
import org.genemania.dto.UploadNetworkWebRequestDto;
import org.genemania.dto.UploadNetworkWebResponseDto;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.util.BrokerUtils;

public class SyncWebWorkerConnector implements EngineConnector {

	private static final Logger LOG = Logger.getLogger(SyncWebWorkerConnector.class);

	private SyncWebWorker worker;
	private NetworkDao networkDao;
	private NodeDao nodeDao;
	private OrganismDao organismDao;

	public SyncWebWorkerConnector() {
		worker = new SyncWebWorker();
	}

	@Override
	public RelatedGenesWebResponseDto getRelatedGenes(RelatedGenesWebRequestDto req) throws ApplicationException {
		SyncWebWorker.SearchResult res = worker.getRelatedGenes(req);
		RelatedGenesWebResponseDto ret = new RelatedGenesWebResponseDto();

		// networks
		List<InteractionNetwork> networks = new ArrayList<InteractionNetwork>();
		Map<Long, Double> networkWeightsMap = new Hashtable<Long, Double>();
		for (NetworkDto nvo : res.response.getNetworks()) {
			networkWeightsMap.put(nvo.getId(), nvo.getWeight());
			InteractionNetwork network = new InteractionNetwork();
			network.setId(nvo.getId());
			Collection<Interaction> interactions = new ArrayList<Interaction>();
			for (InteractionDto ivo : nvo.getInteractions()) {
				Interaction interaction = new Interaction();
				Node fromNode = new Node();
				fromNode.setId(ivo.getNodeVO1().getId());
				interaction.setFromNode(fromNode);
				Node toNode = new Node();
				toNode.setId(ivo.getNodeVO2().getId());
				interaction.setToNode(toNode);
				interaction.setWeight((float) ivo.getWeight());// TODO: switch
																// interaction
																// hbm weight to
																// double
				interactions.add(interaction);
			}
			network.setInteractions(interactions);
			networks.add(network);
		}
		ret.setNetworks(networks);
		ret.setNetworkWeightsMap(networkWeightsMap);
		ret.setNodeScoresMap(BrokerUtils.buildNodeScoresMap(res.response.getNodes()));
		// annotations
		Map<Long, OntologyCategoryDto> ontologyCategories = new Hashtable<Long, OntologyCategoryDto>();
		Map<Long, Collection<OntologyCategory>> annotations = new Hashtable<Long, Collection<OntologyCategory>>();
		Map<Long, Collection<OntologyCategoryDto>> enrichedCategoriesMap = res.enrichment.getAnnotations();
		Iterator<Long> nodesIterator = enrichedCategoriesMap.keySet().iterator();
		while (nodesIterator.hasNext()) {
			long nodeId = nodesIterator.next();
			Collection<OntologyCategoryDto> categoryVOs = enrichedCategoriesMap.get(nodeId);
			Collection<OntologyCategory> categories = new ArrayList<OntologyCategory>();
			for (OntologyCategoryDto category : categoryVOs) {
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
		ret.setOrganismId(res.request.getOrganismId());
		ret.setCombiningMethod(res.request.getCombiningMethod());
		ret.setAttributes(res.response.getNodeToAttributes()); // this ok or
																// need to take
																// a copy?
		try {
			load(ret);
		} catch (DataStoreException e) {
			throw new ApplicationException("data access error processing get related genes request", e);
		}

		return ret;
	}

	private RelatedGenesWebResponseDto load(RelatedGenesWebResponseDto hollowResponseDto) throws DataStoreException {
		RelatedGenesWebResponseDto ret = hollowResponseDto;
		NetworkDao netDao = getNetworkDao();

		for (InteractionNetwork hollowNetwork : hollowResponseDto.getNetworks()) {
			InteractionNetwork network = netDao.findNetwork(hollowNetwork.getId());

			if (network != null) { // predefined networks
				hollowNetwork.setDefaultSelected(network.isDefaultSelected());
				hollowNetwork.setMetadata(network.getMetadata());
				hollowNetwork.setName(network.getName());
				hollowNetwork.setTags(network.getTags());
			}

		}

		// extract organism id
		long organismId = hollowResponseDto.getOrganismId();
		// load ontology categories
		Organism organism = getOrganismDao().findOrganism(organismId);
		Ontology ontology = organism.getOntology();
		Map<Long, Collection<OntologyCategory>> annotations = hollowResponseDto.getAnnotations();
		Iterator<Long> nodeIterator = annotations.keySet().iterator();

		while (nodeIterator.hasNext()) {
			long nodeId = nodeIterator.next();
			Collection<OntologyCategory> categories = annotations.get(nodeId);

			for (OntologyCategory hollowCategory : categories) {
				OntologyCategory category = getOntologyCategory(hollowCategory.getId(), ontology);

				if (category != null) {
					hollowCategory.setDescription(category.getDescription());
					hollowCategory.setName(category.getName());
				} else {
					LOG.warn("Could not load ontology category with id [" + hollowCategory.getId() + "]");
				}
			}
		}
		ret.setCombiningMethod(hollowResponseDto.getCombiningMethod());
		return ret;
	}

	@Override
	public UploadNetworkWebResponseDto uploadNetwork(UploadNetworkWebRequestDto req) throws ApplicationException {
		LOG.info("uploadNetwork request");

        UploadNetworkWebResponseDto ret = new UploadNetworkWebResponseDto();

        SyncWebWorker.UploadResult res = worker.uploadNetwork(req);

        ret.setInteractionCount(res.response.getNumInteractions());
        
        LOG.info("completed uploadNetwork request");
        return ret;
	}

	private OntologyCategory getOntologyCategory(long ontologyCategoryId, Ontology ontology) {
		OntologyCategory ret = null;
		Collection<OntologyCategory> categories = ontology.getCategories();
		for (OntologyCategory cat : categories) {
			if (cat != null) {
				if (cat.getId() == ontologyCategoryId) {
					ret = cat;
					break;
				}
			} else {
				LOG.warn("null ontology category found");
			}
		}
		return ret;
	}

	public NetworkDao getNetworkDao() {
		return this.networkDao;
	}

	public NodeDao getNodeDao() {
		return this.nodeDao;
	}

	public OrganismDao getOrganismDao() {
		return this.organismDao;
	}

	public void setNetworkDao(NetworkDao networkDao) {
		this.networkDao = networkDao;
	}

	public void setNodeDao(NodeDao nodeDao) {
		this.nodeDao = nodeDao;
	}

	public void setOrganismDao(OrganismDao organismDao) {
		this.organismDao = organismDao;
	}

}
