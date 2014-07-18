package org.genemania.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.genemania.dao.AttributeGroupDao;
import org.genemania.domain.AttributeGroup;
import org.genemania.domain.Interaction;
import org.genemania.domain.ResultAttribute;
import org.genemania.domain.ResultAttributeGroup;
import org.genemania.domain.ResultGene;
import org.genemania.domain.ResultInteractionNetwork;
import org.genemania.domain.ResultInteractionNetworkGroup;
import org.genemania.domain.ResultOntologyCategory;
import org.genemania.domain.SearchResults;
import org.genemania.service.AttributeGroupService;
import org.genemania.service.VisualizationDataService;
import org.springframework.beans.factory.annotation.Autowired;

//NOTE: used on old website to generate cytoscape web data
public class VisualizationDataServiceImpl implements VisualizationDataService {

	@Override
	public Visualization getVisualizationData(SearchResults results) {

		Visualization vis = new Visualization();
		Data data = new Data();
		Map<Long, ResultGene> idToResultGene = new HashMap<Long, ResultGene>();
		Map<Long, Node> idToAttrNode = new HashMap<Long, Node>();
		Collection<Node> nodes = new LinkedList<Node>();
		Collection<Edge> edges = new LinkedList<Edge>();
		boolean haveNonQueryGenes = false;

		for (ResultGene rGene : results.getResultGenes()) {
			Node node = new Node();
			long nodeId = rGene.getGene().getNode().getId();

			haveNonQueryGenes = haveNonQueryGenes || !rGene.isQueryGene();
			
			node.setId("" + nodeId);
			node.setQueryGene(rGene.isQueryGene());
			node.setRawScore(rGene.getScore());
			node.setSymbol(rGene.getGene().getSymbol());
			idToResultGene.put(rGene.getGene().getNode().getId(), rGene);

			Collection<Long> ocids = new LinkedList<Long>();
			for (ResultOntologyCategory rOCat : rGene
					.getResultOntologyCategories()) {
				ocids.add(rOCat.getId());
			}
			node.setOcids(ocids);

			Collection<Long> attrids = new LinkedList<Long>();
			for (ResultAttribute rAttr : rGene.getResultAttributes()) {
				long attrId = rAttr.getAttribute().getId();
				long attrGrId = rAttr.getResultAttributeGroup()
						.getAttributeGroup().getId();
				attrids.add(attrId);
				String code = rAttr.getResultAttributeGroup()
						.getAttributeGroup().getCode();

				Node attrNode;
				if (idToAttrNode.containsKey(attrId)) {
					attrNode = idToAttrNode.get(attrId);
				} else {
					attrNode = new Node();
					attrNode.setId("attr" + attrId);
					attrNode.setQueryGene(false);
					attrNode.setAttributeId(attrId);
					attrNode.setAttribute(true);
					attrNode.setSymbol(rAttr.getAttribute().getName());
					attrNode.setCode(code);
					nodes.add(attrNode);
					idToAttrNode.put(attrId, attrNode);
				}

				Edge edge = new Edge();
				edge.setSource(attrNode.getId());
				edge.setTarget("" + nodeId);
				edge.setId(attrNode.getId() + "-" + nodeId + ":" + code);
				edge.setWeight(0); // attributes are binary
				edge.setCode(code);
				edge.setAttributeId(attrId);
				edge.setAttributeGroupId(attrGrId);
				edge.setSourceName(attrNode.getSymbol());
				edge.setTargetName(node.getSymbol());
				
				edge.setNetworkGroupName("Attributes");
				List<String> netNames = new LinkedList<String>();
				netNames.add(rAttr.getResultAttributeGroup().getAttributeGroup().getName());
				edge.setNetworkNames(netNames);
				
				edges.add(edge);
			}
			node.setAttrids(attrids);

			nodes.add(node);
		}

		Double highestScore = 0.0;
		Double lowestNonZeroScore = null;
		Double highestQueryScore = null;
		for (Node node : nodes) {
			if (node.isAttribute()) {
				// attributes don't have a score
			} else if (!node.isQueryGene()) {

				if (node.getRawScore() > highestScore) {
					highestScore = node.getRawScore();
				} else if (node.getRawScore() != 0.0) {
					if (lowestNonZeroScore == null
							|| node.getRawScore() < lowestNonZeroScore) {
						lowestNonZeroScore = node.getRawScore();
					}
				}

			} else {
				highestQueryScore = highestQueryScore == null ? node
						.getRawScore() : Math.max(highestQueryScore,
						node.getRawScore());
			}
		}
		
		// if we don't have query genes, we need to adjust the score bounds
		if( !haveNonQueryGenes ){
			highestScore = highestQueryScore;
			lowestNonZeroScore = highestQueryScore;
		}
		
		for (Node node : nodes) {
			
			if (node.isAttribute()) {
				node.setScore((highestScore - lowestNonZeroScore) * 0.35
						+ lowestNonZeroScore);
			} else if (node.isQueryGene()) {
				node.setScore(highestScore);
			} else {
				node.setScore(node.getRawScore() == 0.0 ? lowestNonZeroScore
						: node.getRawScore());
			}
		}
		Collections.sort((List) nodes);
		data.setNodes(nodes);

		Map<InteractionId, Edge> idToEdge = new HashMap<InteractionId, Edge>();

		for (ResultInteractionNetworkGroup rNetworkGroup : results
				.getResultNetworkGroups()) {

			for (ResultInteractionNetwork rNetwork : rNetworkGroup
					.getResultNetworks()) {

				for (Interaction interaction : rNetwork.getNetwork()
						.getInteractions()) {

					InteractionId id = new InteractionId(rNetworkGroup
							.getNetworkGroup().getCode(), interaction
							.getFromNode().getId(), interaction.getToNode()
							.getId());

					// create edge
					Edge edge;
					if (!idToEdge.containsKey(id)) {
						edge = new Edge();
						edge.setId(id.toString());
						edge.setCode(rNetworkGroup.getNetworkGroup().getCode());
						edge.setNetworkGroupId(rNetworkGroup.getNetworkGroup()
								.getId());
						edge.setSource("" + interaction.getFromNode().getId());
						edge.setTarget("" + interaction.getToNode().getId());
						edge.setWeight(interaction.getWeight());
						edge.setNetworkIdToWeight(new HashMap<Long, Double>());
						edge.setNetworkGroupName(rNetworkGroup
								.getNetworkGroup().getName());
						edge.setNetworkNames(new LinkedList<String>());
						edge.getNetworkNames().add(
								rNetwork.getNetwork().getName());

						ResultGene from = idToResultGene.get(interaction
								.getFromNode().getId());
						ResultGene to = idToResultGene.get(interaction
								.getToNode().getId());
						edge.setSourceName(from.getGene().getSymbol());
						edge.setTargetName(to.getGene().getSymbol());

						edges.add(edge);
						idToEdge.put(id, edge);
					} else {
						edge = idToEdge.get(id);

						double prevWeight = edge.getWeight();
						edge.setWeight(prevWeight + interaction.getWeight());

						edge.getNetworkNames().add(
								rNetwork.getNetwork().getName());
					}

					// add network weights for edge

					Long netId = rNetwork.getNetwork().getId();
					if (!edge.getNetworkIdToWeight().keySet()
							.contains(rNetwork.getNetwork().getId())) {
						edge.getNetworkIdToWeight().put(netId, 0.0);
					}

					Double weight = (double) interaction.getWeight();
					Double prevWeight = edge.getNetworkIdToWeight().get(netId);
					edge.getNetworkIdToWeight().put(netId, prevWeight + weight);
				}
			}
		}

		// sort network names in each edge
		for (Edge edge : edges) {
			if (edge != null && edge.getNetworkNames() != null) {
				Collections.sort(edge.getNetworkNames());
			}
		}

		Collections.sort((List) edges);
		data.setEdges(edges);

		vis.setData(data);
		return vis;
	}

	private static class InteractionId {
		private long smallId;
		private long largeId;
		private String groupName;

		public InteractionId(String groupName, long id1, long id2) {
			this.groupName = groupName;

			if (id1 > id2) {
				this.smallId = id2;
				this.largeId = id1;
			} else {
				this.smallId = id1;
				this.largeId = id2;
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((groupName == null) ? 0 : groupName.hashCode());
			result = prime * result + (int) (largeId ^ (largeId >>> 32));
			result = prime * result + (int) (smallId ^ (smallId >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			InteractionId other = (InteractionId) obj;
			if (groupName == null) {
				if (other.groupName != null)
					return false;
			} else if (!groupName.equals(other.groupName))
				return false;
			if (largeId != other.largeId)
				return false;
			if (smallId != other.smallId)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return smallId + "-" + largeId + ":" + groupName;
		}

	}
}
