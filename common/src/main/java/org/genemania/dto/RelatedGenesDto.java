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
 * RelatedGenesDto: Data Transfer Object used in Related Genes use case
 * Created Sep 17, 2008
 * @author Ovi Comes
 */
package org.genemania.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.genemania.domain.Interaction;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.Node;
import org.genemania.type.CombiningMethod;

public class RelatedGenesDto implements Serializable {

	// __[static]______________________________________________________________
	private static final long serialVersionUID = -303180192634583267L;

	// __[attributes]__________________________________________________________
	//in
	private long organismId;
	private List<Long> networkIds = new ArrayList<>();
	private List<Long> postiveNodeIds = new ArrayList<>();
	private CombiningMethod method = CombiningMethod.AUTOMATIC;
	private int threshold;
	// out
	private Map<InteractionNetwork, Double> networkWeights;
	private Map<Node, Double> scoreMap;
	private Collection<Interaction> topInteractions;

	// __[constructors]________________________________________________________
	public RelatedGenesDto() {
	}

	// __[accessors]___________________________________________________________
	public long getOrganismId() {
		return organismId;
	}

	public void setOrganismId(long organismId) {
		this.organismId = organismId;
	}

	public List<Long> getNetworkIds() {
		return networkIds;
	}

	public void setNetworkIds(List<Long> networkIds) {
		this.networkIds = networkIds;
	}

	public List<Long> getPostiveNodeIds() {
		return postiveNodeIds;
	}

	public void setPostiveNodeIds(List<Long> postiveNodeIds) {
		this.postiveNodeIds = postiveNodeIds;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public void setMethod(CombiningMethod method) {
		this.method = method;
	}

	public CombiningMethod getMethod() {
		return method;
	}

}
