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
 * Store's KtK.
 *
 * The goBranch field is no longer used, we store a single KtK.
 *
 * The indexing of KtK should be consistent with the NetworkIds data structure.
 *
 * KtK is constructed with a dense network of 1's in the first column of K, so
 * KtK(0,0) = n*(n-1) and KtK(0,i) = KtK(i,0) = sum(sum(network_i)) for each network i
 * from 1 to d=# of networks, and each network is n-by-n with no self-interactions.
 */
public class KtK extends Data {
    private static final long serialVersionUID = -513356220806789800L;
    private String goBranch;
    private DenseMatrix data;

    public KtK(String namespace, long organismId, String goBranch) {
        super(namespace, organismId);
        this.goBranch = goBranch;
    }

    @Override
    public KtK copy(String newNamespace) {
        KtK copy = new KtK(newNamespace, getOrganismId(), getGoBranch());
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
     * @return the branch
     */
    public String getGoBranch() {
        return goBranch;
    }

    /**
     * @param branch the branch to set
     */
    public void setGoBranch(String goBranch) {
        this.goBranch = goBranch;
    }

    @Override
    public String [] getKey() {
        return new String [] {getNamespace(), "" + getOrganismId(), getGoBranch() + ".KtK"};
    }

    public void removeNetworkAtIndex(int index) {
        DenseMatrix newData = new DenseMatrix(data.numRows()-1, data.numColumns()-1);

        int k=0;

        for (int i=0; i<data.numRows(); i++) {
            int l=0;
            if (i != index) {
                for (int j=0; j<data.numColumns(); j++) {
                    if (j != index) {
                        newData.set(k, l, data.get(i, j));
                        l += 1;
                    }
                }
                k += 1;
            }
        }

        data = newData;
    }
}
