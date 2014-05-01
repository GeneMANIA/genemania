package org.genemania.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.genemania.util.ProgressReporter;

/* 
 * Load a new attribute group identified by the given attribute
 * group id, for the given namespace and existing organism.
 * 
 * An attribute group can be thought of as a group of networks,
 * one for each attribute, where all the genes having that attribute
 * are all connected to each other.
 * 
 * The given collection of attribute group ids identifies each network,
 * and the nodeAttributeAssocation is a list of gene-attribute-attribute 
 * lists, that is each of the sublists starts with a node-id identifying 
 * the gene (the node id should already exist for the organism) followed 
 * by one or more attributes that the gene has. A gene can appear in multiple 
 * sublists, so when convenient the associations can be loaded in flattened 
 * form with each of the sublists being a single gene-attribute pair.
 */
public class AddAttributeGroupEngineRequestDto implements Serializable {
	private static final long serialVersionUID = 3908501083079679065L;

	private String namespace;
	private long organismId;
	private long attributeGroupId;
	private Collection<Long> attributeIds;
	private Collection<? extends List<Long>> nodeAttributeAssociations;

	private ProgressReporter progressReporter;

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public long getOrganismId() {
		return organismId;
	}

	public void setOrganismId(long organismId) {
		this.organismId = organismId;
	}

	public long getAttributeGroupId() {
		return attributeGroupId;
	}

	public void setAttributeGroupId(long attributeGroupId) {
		this.attributeGroupId = attributeGroupId;
	}

	public ProgressReporter getProgressReporter() {
		return progressReporter;
	}

	public void setProgressReporter(ProgressReporter progressReporter) {
		this.progressReporter = progressReporter;
	}

	public void setNodeAttributeAssociations(
			Collection<? extends List<Long>> nodeAttributeAssociations) {
		this.nodeAttributeAssociations = nodeAttributeAssociations;
	}

	public Collection<Long> getAttributeIds() {
		return attributeIds;
	}

	public void setAttributeIds(Collection<Long> attributeIds) {
		this.attributeIds = attributeIds;
	}

	public Collection<? extends List<Long>> getNodeAttributeAssociations() {
		return nodeAttributeAssociations;
	}
}
