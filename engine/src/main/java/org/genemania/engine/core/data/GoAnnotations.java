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

import org.genemania.engine.matricks.Matrix;

/**
 * rows of the data matrix represent genes (as indexed
 * by NodeIds) and columns represent Go categories
 * (as indexed by GoIds). value is 1 if that
 * go id is annotated to that go category, else 0.
 *
 * specific to a particular go branch
 */
public class GoAnnotations extends Data {
    private static final long serialVersionUID = 1829582112370893911L;

    private String goBranch;
    private Matrix data;

    public GoAnnotations(long organismId, String goBranch) {
        super(Data.CORE, organismId);
        this.goBranch = goBranch;
    }
    /**
     * @return the data
     */
    public Matrix getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(Matrix data) {
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
        return new String [] {getNamespace(), "" + getOrganismId(),  goBranch + ".GoAnnos"};
    }
}
