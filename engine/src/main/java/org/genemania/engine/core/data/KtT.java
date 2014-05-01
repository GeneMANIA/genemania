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

import no.uib.cipr.matrix.DenseMatrix;

/**
 * Store's KtT, for a given go Branch.
 *
 * While the data is stored as a matrix, its just one-column.
 *
 * The indexing of KtT should be consistent with the NetworkIds data structure.
 */
public class KtT extends Data {
    private static final long serialVersionUID = -2709140842827973884L;

    private String goBranch;
    private DenseMatrix data;

    public KtT(String namespace, long organismId, String goBranch) {
        super(namespace, organismId);
        this.goBranch = goBranch;
    }

    public KtT copy(String newNamespace) {
        KtT copy = new KtT(newNamespace, getOrganismId(), getGoBranch());
        DenseMatrix newData = data.copy();
        copy.setData(newData);

        return copy;
    }

    /**
     * @return the data
     */
    public DenseMatrix getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(DenseMatrix data) {
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
        return new String [] {getNamespace(), "" + getOrganismId(), getGoBranch() + ".KtT"};
    }

    public void removeNetworkAtIndex(int index) {
        DenseMatrix newData = new DenseMatrix(data.numRows()-1, 1);

        int j=0;
        for (int i=0; i<data.numRows(); i++) {
            if (i != index) {
                newData.set(j, 0, data.get(i, 0));
                j++;
            }
        }

        data = newData;
    }
}
