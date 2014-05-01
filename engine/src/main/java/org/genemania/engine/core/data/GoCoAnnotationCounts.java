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

import org.genemania.engine.matricks.SymMatrix;

/**
 * rows and cols of the data matrix represent genes (as indexed
 * by NodeIds) and the value indicates # of categories to which
 * the pair of genes share annotations. so symmetric. diagonal value
 * contains total # of annotations for the given gene.
 *
 * specific to a particular go branch
 */
public class GoCoAnnotationCounts extends Data {
    private static final long serialVersionUID = -6896690551182822989L;
    private String goBranch;
    private SymMatrix data;

    public GoCoAnnotationCounts(long organismId, String goBranch) {
        super(Data.CORE, organismId);
        this.goBranch = goBranch;
    }
    
    /**
     * @return the data
     */
    public SymMatrix getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(SymMatrix data) {
        this.data = data;
    }

    /**
     * @return the goBranch
     */
    public String getGoBranch() {
        return goBranch;
    }

    /**
     * @param goBranch the goBranch to set
     */
    public void setGoBranch(String goBranch) {
        this.goBranch = goBranch;
    }

    @Override
    public String [] getKey() {
        return new String [] {getNamespace(), "" + getOrganismId(), goBranch + ".GoCoAnnoCounts"};
    }
}
