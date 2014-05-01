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

package org.genemania.engine.apps.support;

import org.genemania.engine.cache.DataCache;
import org.genemania.mediator.AttributeMediator;
import org.genemania.mediator.GeneMediator;
import org.genemania.mediator.NetworkMediator;
import org.genemania.mediator.NodeMediator;
import org.genemania.mediator.OntologyMediator;
import org.genemania.mediator.OrganismMediator;
import org.genemania.mediator.StatsMediator;

public class DataConnector {
	String DbVersion;
    OrganismMediator organismMediator;
    NetworkMediator networkMediator;
    GeneMediator geneMediator;
    NodeMediator nodeMediator;
    OntologyMediator ontologyMediator;
    StatsMediator statsMediator;
    AttributeMediator attributeMediator;
    DataCache cache;
    
	public String getDbVersion() {
		return DbVersion;
	}
	public void setDbVersion(String dbVersion) {
		DbVersion = dbVersion;
	}
	public OrganismMediator getOrganismMediator() {
		return organismMediator;
	}
	public void setOrganismMediator(OrganismMediator organismMediator) {
		this.organismMediator = organismMediator;
	}
	public NetworkMediator getNetworkMediator() {
		return networkMediator;
	}
	public void setNetworkMediator(NetworkMediator networkMediator) {
		this.networkMediator = networkMediator;
	}
	public GeneMediator getGeneMediator() {
		return geneMediator;
	}
	public void setGeneMediator(GeneMediator geneMediator) {
		this.geneMediator = geneMediator;
	}
	public NodeMediator getNodeMediator() {
		return nodeMediator;
	}
	public void setNodeMediator(NodeMediator nodeMediator) {
		this.nodeMediator = nodeMediator;
	}
	public OntologyMediator getOntologyMediator() {
		return ontologyMediator;
	}
	public void setOntologyMediator(OntologyMediator ontologyMediator) {
		this.ontologyMediator = ontologyMediator;
	}
	public StatsMediator getStatsMediator() {
		return statsMediator;
	}
	public void setStatsMediator(StatsMediator statsMediator) {
		this.statsMediator = statsMediator;
	}
	public AttributeMediator getAttributeMediator() {
        return attributeMediator;
    }
    public void setAttributeMediator(AttributeMediator attributeMediator) {
        this.attributeMediator = attributeMediator;
    }
    public DataCache getCache() {
		return cache;
	}
	public void setCache(DataCache cache) {
		this.cache = cache;
	}
}
