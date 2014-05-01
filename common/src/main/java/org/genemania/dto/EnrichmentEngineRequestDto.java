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

package org.genemania.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import org.genemania.util.ProgressReporter;

/*
 * specify an enrichment request (ie for GO annotations) for
 * a given set of nodes (genes).
 */
public class EnrichmentEngineRequestDto implements Serializable {
	
	// __[static]______________________________________________________________
	private static final long serialVersionUID = -223528430781009868L;
	
	// __[attributes]__________________________________________________________
	private long organismId;
	private long ontologyId;
	private Collection<Long> nodes = new ArrayList<Long>();
	private double qValueThreshold;
	private int minCategories;
	private ProgressReporter progressReporter;

	// __[constructors]________________________________________________________
	public EnrichmentEngineRequestDto() {
	}
	
	// __[accessors]____________________________________________________________
    public Collection<Long> getNodes() {
        return nodes;
    }

    public void setNodes(Collection<Long> nodes) {
        this.nodes = nodes;
    }

    public long getOntologyId() {
		return ontologyId;
	}

	public void setOntologyId(long ontologyId) {
		this.ontologyId = ontologyId;
	}

	public long getOrganismId() {
        return organismId;
    }

    public void setOrganismId(long organismId) {
        this.organismId = organismId;
    }

    public ProgressReporter getProgressReporter() {
        return progressReporter;
    }

    public void setProgressReporter(ProgressReporter progressReporter) {
        this.progressReporter = progressReporter;
    }

    public int getMinCategories() {
        return minCategories;
    }

    public void setMinCategories(int minCategories) {
        this.minCategories = minCategories;
    }

    public double getqValueThreshold() {
        return qValueThreshold;
    }

    public void setqValueThreshold(double qValueThreshold) {
        this.qValueThreshold = qValueThreshold;
    }
    
}
