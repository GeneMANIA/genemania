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
 * RelatedGenesWebRequestDto: Website-specific Related Genes request data transfer object   
 * Created Jul 22, 2009
 * @author Ovi Comes
 */
package org.genemania.dto;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.genemania.domain.Gene;
import org.genemania.domain.InteractionNetwork;
import org.genemania.type.CombiningMethod;

public class RelatedGenesWebRequestDto implements Serializable {

	// __[static]______________________________________________________________
	private static final long serialVersionUID = -5062149471128161811L;

	// __[attributes]__________________________________________________________
	private long organismId;
	private Collection<Gene> inputGenes;
	private Collection<InteractionNetwork> inputNetworks;
	Collection<Long> attributeGroups;
	private int resultSize;
	private CombiningMethod combiningMethod;
	private String userDefinedNetworkNamespace;
	private long ontologyId; // for enrichment analysis
	private int attributesLimit;

	// __[constructors]________________________________________________________
	public RelatedGenesWebRequestDto() {
	}

	// __[accessors]___________________________________________________________

	// NOTE: this must be updated with new fields added to this object if
	// caching in the website is to work properly
	public String toString() {
		int i;

		String organismToken = "organism=" + getOrganismId() + ";";

		long[] genes = new long[this.getInputGenes().size()];
		String geneToken = "genes=";
		i = 0;
		for (Gene gene : this.getInputGenes()) {
			genes[i++] = gene.getNode().getId();
		}
		Arrays.sort(genes);
		for (i = 0; i < genes.length; i++) {
			geneToken += genes[i] + (i < genes.length - 1 ? "," : "");
		}
		geneToken += ";";

		String sizeToken = "size=" + this.getResultSize() + ";";
		
		String attrSizeToken = "attrSize=" + this.getAttributesLimit() + ";";

		String attributesGroupToken = "attributeGroups=";
		long[] attrGroups = new long[this.attributeGroups.size()];
		i = 0;
		for (Long group : this.attributeGroups) {
			attrGroups[i++] = group;
		}
		Arrays.sort(attrGroups);
		for (i = 0; i < attrGroups.length; i++) {
			attributesGroupToken += attrGroups[i]
					+ (i < attrGroups.length - 1 ? "," : "");
		}
		attributesGroupToken += ";";

		String networkToken = "networks=";
		long[] networks = new long[this.getInputNetworks().size()];
		i = 0;
		for (InteractionNetwork network : this.getInputNetworks()) {
			networks[i++] = network.getId();
		}
		Arrays.sort(networks);
		for (i = 0; i < networks.length; i++) {
			networkToken += networks[i] + (i < networks.length - 1 ? "," : "");
		}
		networkToken += ";";

		String weightingToken = "weighting=" + this.getCombiningMethod() + ";";

		return organismToken + geneToken + networkToken + attributesGroupToken
				+ weightingToken + sizeToken + attrSizeToken;
	}

	public long getOrganismId() {
		return organismId;
	}

	public void setOrganismId(long organismId) {
		this.organismId = organismId;
	}

	public Collection<Gene> getInputGenes() {
		return inputGenes;
	}

	public void setInputGenes(Collection<Gene> inputGenes) {
		this.inputGenes = inputGenes;
	}

	public Collection<InteractionNetwork> getInputNetworks() {
		return inputNetworks;
	}

	public void setInputNetworks(Collection<InteractionNetwork> inputNetworks) {
		this.inputNetworks = inputNetworks;
	}

	public CombiningMethod getCombiningMethod() {
		return combiningMethod;
	}

	public void setCombiningMethod(CombiningMethod combiningMethod) {
		this.combiningMethod = combiningMethod;
	}

	public String getUserDefinedNetworkNamespace() {
		return userDefinedNetworkNamespace;
	}

	public void setUserDefinedNetworkNamespace(
			String userDefinedNetworkNamespace) {
		this.userDefinedNetworkNamespace = userDefinedNetworkNamespace;
	}

	public long getOntologyId() {
		return ontologyId;
	}

	public void setOntologyId(long ontolgyId) {
		this.ontologyId = ontolgyId;
	}

	public Collection<Long> getAttributeGroups() {
		return attributeGroups;
	}

	public void setAttributeGroups(Collection<Long> attributeGroups) {
		this.attributeGroups = attributeGroups;
	}

    public int getResultSize() {
        return resultSize;
    }

    public void setResultSize(int resultSize) {
        this.resultSize = resultSize;
    }

	public int getAttributesLimit() {
		return attributesLimit;
	}

	public void setAttributesLimit(int attributesLimit) {
		this.attributesLimit = attributesLimit;
	}

}
