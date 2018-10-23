/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2008-2017 University of Toronto.
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
package org.genemania.domain;

import org.genemania.exception.ApplicationException;
import org.genemania.type.CombiningMethod;

/**
 * Request object for web search (could be xml, json, etc).
 */
public class SearchRequest {
	
	private Long organism = 4L;
	private String genes;
	private CombiningMethod weighting = CombiningMethod.AUTOMATIC_SELECT;
	private Integer geneThreshold = 20;
	private Integer attrThreshold = 10;
	private Long[] networks;
	private Long[] attrGroups;
	private String sessionId;

	public SearchRequest() {
	}

	public SearchRequest(Long organism, String genes) {
		this.organism = organism;
		this.genes = genes;
	}

	public Long getOrganism() {
		return organism;
	}

	public void setOrganismFromLong(Long organism) {
		this.organism = organism;
	}

	public void setOrganism(Integer organism) {
		this.organism = organism.longValue();
	}

	public void setOrganismFromString(String organism) {
		this.organism = Long.parseLong(organism);
	}

	public String getGenes() {
		return genes;
	}

	public void setGenes(String genes) {
		this.genes = genes;
	}

	public CombiningMethod getWeighting() {
		return weighting;
	}

	public void setWeightingFromEnum(CombiningMethod weighting) {
		this.weighting = weighting;
	}

	public void setWeighting(String weighting) {
		CombiningMethod c = CombiningMethod.fromCode(weighting);

		if (c != CombiningMethod.UNKNOWN)
			this.weighting = c;
	}

	public Integer getGeneThreshold() {
		return geneThreshold;
	}

	public void setGeneThreshold(Integer geneThreshold) {
		this.geneThreshold = geneThreshold;
	}

	public void setGeneThresholdFromString(String t) {
		this.geneThreshold = Integer.parseInt(t);
	}

	public Integer getAttrThreshold() {
		return attrThreshold;
	}

	public void setAttrThreshold(Integer attrThreshold) {
		this.attrThreshold = attrThreshold;
	}

	public void setAttrThresholdFromString(String t) {
		this.attrThreshold = Integer.parseInt(t);
	}

	public Long[] getNetworks() {
		return networks;
	}

	public void setNetworks(Long[] networks) {
		this.networks = networks;
	}

	public void setNetworksFromString(String s) {
		String[] idStrs = s.split("\\s*,\\s*");
		Long[] ids = new Long[idStrs.length];

		for (int i = 0; i < idStrs.length; i++)
			ids[i] = Long.parseLong(idStrs[i]);

		this.networks = ids;
	}

	public Long[] getAttrGroups() {
		return attrGroups;
	}

	public void setAttrGroups(Long[] attrGroups) {
		this.attrGroups = attrGroups;
	}

	public void setAttrGroupsFromString(String s) {
		String[] idStrs = s.split("\\s*,\\s*");
		Long[] ids = new Long[idStrs.length];

		for (int i = 0; i < idStrs.length; i++)
			ids[i] = Long.parseLong(idStrs[i]);

		this.attrGroups = ids;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public boolean assertParamsSet() throws ApplicationException {
		if (genes == null)
			throw new ApplicationException("`genes` not set");

		return true;
	}
}