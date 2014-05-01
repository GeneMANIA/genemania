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
import java.util.Collection;
import org.genemania.util.ProgressReporter;

/**
 * add a new organism to the engine's dataset. Two pieces of
 * information needed:
 * 
 *   * organism id, engine doesn't assign id's on its own
 *   
 *   * set of node ids (ie genes). These currently can not
 *     be augmented with new genes later, you need the entire
 *     set up front, or else you'll need to rebuild the organism
 *     for additional genes. 
 *
 */
public class AddOrganismEngineRequestDto implements Serializable {
	private static final long serialVersionUID = -8882550690435051306L;

	long organismId;
	Collection<Long> nodeIds;
        ProgressReporter progressReporter;

    public Collection<Long> getNodeIds() {
        return nodeIds;
    }

    public void setNodeIds(Collection<Long> nodeIds) {
        this.nodeIds = nodeIds;
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

}
