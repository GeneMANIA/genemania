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


package org.genemania.engine.core.data;

import org.genemania.engine.Constants;

/**
 * its appears useful to keep some
 * simple summary info in a separate,
 * persisted class, to avoid having to
 * load large data objects & recompute
 * each time needed.
 */
public class DatasetInfo extends Data {
    private static final long serialVersionUID = 7242877007927500445L;
    private int[] numCategories = new int[Constants.goBranches.length];
    private int numGenes;
    private int numInteractingGenes;

    public DatasetInfo(long organismId) {
        super(Data.CORE, organismId);
    }
    
    public int[] getNumCategories() {
        return numCategories;
    }

    public void setNumCategories(int[] numCategories) {
        this.numCategories = numCategories;
    }

    public int getNumGenes() {
        return numGenes;
    }

    public void setNumGenes(int numGenes) {
        this.numGenes = numGenes;
    }

    public int getNumInteractingGenes() {
        return numInteractingGenes;
    }

    public void setNumInteractingGenes(int numInteractingGenes) {
        this.numInteractingGenes = numInteractingGenes;
    }

    @Override
    public String [] getKey() {
        return new String [] {getNamespace(), "" + getOrganismId(), "DatasetInfo"};
    }
}
