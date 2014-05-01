package org.genemania.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.genemania.domain.SearchResults;

/**
 * Provides a way to convert a search result to visualization data for cytoweb
 */
public interface VisualizationDataService {

	/**
	 * Gets the visualization data for cytoweb
	 * 
	 * @param results
	 *            Search results from the engine
	 * @return The data needed by cytoweb
	 */
	public Visualization getVisualizationData(SearchResults results);

	public static class Visualization {
		Schema dataSchema;
		Data data;

		public Visualization() {
			this.dataSchema = new Schema();
		}

		public Schema getDataSchema() {
			return dataSchema;
		}

		public void setDataSchema(Schema schema) {
			this.dataSchema = schema;
		}

		public Data getData() {
			return data;
		}

		public void setData(Data data) {
			this.data = data;
		}

	}

	public static class Data {
		Collection<Node> nodes;
		Collection<Edge> edges;

		public Collection<Node> getNodes() {
			return nodes;
		}

		public void setNodes(Collection<Node> nodes) {
			this.nodes = nodes;
		}

		public Collection<Edge> getEdges() {
			return edges;
		}

		public void setEdges(Collection<Edge> edges) {
			this.edges = edges;
		}

	}

	public static class Schema {
		Collection<SchemaEntry> nodes;
		Collection<SchemaEntry> edges;

		public Schema() {
			this.nodes = Arrays.asList(new SchemaEntry("score", "number"),
					new SchemaEntry("rawScore", "number"), new SchemaEntry(
							"symbol", "string"), new SchemaEntry("ocids",
							"object"), new SchemaEntry("attrids", "object"),
					new SchemaEntry("queryGene", "boolean"), new SchemaEntry(
							"attribute", "boolean"), new SchemaEntry(
							"attributeId", "number"), new SchemaEntry("id",
							"string"), new SchemaEntry("code", "string"));

			this.edges = Arrays.asList(new SchemaEntry("weight", "number"),
					new SchemaEntry("code", "string"), new SchemaEntry(
							"networkGroupId", "int"), new SchemaEntry(
							"networkIdToWeight", "object"), new SchemaEntry(
							"source", "string"), new SchemaEntry("target",
							"string"), new SchemaEntry("id", "string"),
					new SchemaEntry("networkNames", "object"), new SchemaEntry(
							"networkGroupName", "string"), new SchemaEntry(
							"targetName", "string"), new SchemaEntry(
							"sourceName", "string"), new SchemaEntry(
							"attributeGroupId", "number"), new SchemaEntry(
							"attributeId", "number"));
		}

		public Collection<SchemaEntry> getNodes() {
			return nodes;
		}

		public void setNodes(Collection<SchemaEntry> nodes) {
			this.nodes = nodes;
		}

		public Collection<SchemaEntry> getEdges() {
			return edges;
		}

		public void setEdges(Collection<SchemaEntry> edges) {
			this.edges = edges;
		}

	}

	public static class SchemaEntry {
		String name;
		String type;

		public SchemaEntry() {

		}

		public SchemaEntry(String name, String type) {
			this.name = name;
			this.type = type;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

	}

	public static class Node implements Comparable {
		Double score;
		Double rawScore;
		String symbol = "";
		Collection<Long> ocids = new LinkedList<Long>();
		boolean queryGene = false;
		String id = "";
		Collection<Long> attrids = new LinkedList<Long>();
		Long attributeId;
		boolean attribute;
		private String code = null;

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public boolean isAttribute() {
			return attribute;
		}

		public void setAttribute(boolean attribute) {
			this.attribute = attribute;
		}

		public void setScore(Double score) {
			this.score = score;
		}

		public void setRawScore(Double rawScore) {
			this.rawScore = rawScore;
		}

		public Long getAttributeId() {
			return attributeId;
		}

		public void setAttributeId(Long attributeId) {
			this.attributeId = attributeId;
		}

		public Double getScore() {
			return score;
		}

		public void setScore(double score) {
			this.score = score;
		}

		public String getSymbol() {
			return symbol;
		}

		public void setSymbol(String symbol) {
			this.symbol = symbol;
		}

		public Collection<Long> getOcids() {
			return ocids;
		}

		public void setOcids(Collection<Long> ocids) {
			this.ocids = ocids;
		}

		public Collection<Long> getAttrids() {
			return attrids;
		}

		public void setAttrids(Collection<Long> attrids) {
			this.attrids = attrids;
		}

		public boolean isQueryGene() {
			return queryGene;
		}

		public void setQueryGene(boolean queryGene) {
			this.queryGene = queryGene;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public Double getRawScore() {
			return rawScore;
		}

		public void setRawScore(double rawScore) {
			this.rawScore = rawScore;
		}

		@Override
		public int compareTo(Object o) {
			if (o instanceof Node) {
				Node other = (Node) o;

				if (this.isAttribute() && !other.isAttribute()) {
					return -1;
				} else if (!this.isAttribute() && other.isAttribute()) {
					return 1;
				} else if (this.isAttribute() && other.isAttribute()) {
					return 0;
				} else if (this.getRawScore() > other.getRawScore()) {
					return 1;
				} else if (this.getRawScore() < other.getRawScore()) {
					return -1;
				} else {
					return 0;
				}
			}
			return 0;
		}

	}

	public static class Edge implements Comparable {
		double weight = 0;
		String id;
		String code = "";
		long networkGroupId;
		Map<Long, Double> networkIdToWeight = new HashMap<Long, Double>();
		String source;
		String target;
		List<String> networkNames = new LinkedList<String>();
		String networkGroupName = "";
		String targetName = "";
		String sourceName = "";
		Long attributeGroupId = null;
		Long attributeId = null;

		public Long getAttributeGroupId() {
			return attributeGroupId;
		}

		public void setAttributeGroupId(Long attributeGroupId) {
			this.attributeGroupId = attributeGroupId;
		}

		public Long getAttributeId() {
			return attributeId;
		}

		public void setAttributeId(Long attributeId) {
			this.attributeId = attributeId;
		}

		public double getWeight() {
			return weight;
		}

		public void setWeight(double weight) {
			this.weight = weight;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String c) {
			this.code = c;
		}

		public long getNetworkGroupId() {
			return networkGroupId;
		}

		public void setNetworkGroupId(long networkGroupId) {
			this.networkGroupId = networkGroupId;
		}

		public String getSource() {
			return source;
		}

		public void setSource(String source) {
			this.source = source;
		}

		public String getTarget() {
			return target;
		}

		public void setTarget(String target) {
			this.target = target;
		}

		public Map<Long, Double> getNetworkIdToWeight() {
			return networkIdToWeight;
		}

		public void setNetworkIdToWeight(Map<Long, Double> networkIdToWeight) {
			this.networkIdToWeight = networkIdToWeight;
		}

		public String getNetworkGroupName() {
			return networkGroupName;
		}

		public void setNetworkGroupName(String networkGroupName) {
			this.networkGroupName = networkGroupName;
		}

		public String getTargetName() {
			return targetName;
		}

		public void setTargetName(String targetName) {
			this.targetName = targetName;
		}

		public String getSourceName() {
			return sourceName;
		}

		public void setSourceName(String sourceName) {
			this.sourceName = sourceName;
		}

		public List<String> getNetworkNames() {
			return networkNames;
		}

		public void setNetworkNames(List<String> networkNames) {
			this.networkNames = networkNames;
		}

		@Override
		public int compareTo(Object o) {

			if (o instanceof Edge) {
				Edge other = (Edge) o;

				if (!this.getNetworkGroupName().equals(
						other.getNetworkGroupName())) {
					return this.getNetworkGroupName().compareToIgnoreCase(
							other.getNetworkGroupName());
				} else if (this.getWeight() > other.getWeight()) {
					return -1;
				} else if (this.getWeight() < other.getWeight()) {
					return 1;
				} else {
					return 0;
				}
			}

			return 0;
		}

	}

}
