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

import no.uib.cipr.matrix.DenseVector;
import org.genemania.engine.matricks.SymMatrix;

/**
 * wrapper class for CoAnnotation Matrix, vector y, and the constant
 * used by CalculateFastWeight
 * see geneMANIASW_V2.pdf for generation
 */

public class CoAnnotationSet extends Data {
    private static final long serialVersionUID = -7510342610607641682L;
    private String goBranch;
    private SymMatrix coAnnotationMatrix;
    private DenseVector BHalf;
    private double constant;

    public CoAnnotationSet(long organismId, String goBranch) {
        this(organismId, goBranch, null, null, 0d);
    }
    
    public CoAnnotationSet(long organismId, String goBranch, SymMatrix coAnnotation, DenseVector bh, double consta) {
        super(Data.CORE, organismId);
        this.goBranch = goBranch;
        coAnnotationMatrix = coAnnotation;
        BHalf = bh;
        constant = consta;
    }

    public SymMatrix GetCoAnnotationMatrix() {
        return coAnnotationMatrix;
    }

    public DenseVector GetBHalf() {
        return BHalf;
    }

    public Double GetConstant() {
        return constant;
    }

    public void putCoAnnotationMatrix(SymMatrix CoAnn) {
        coAnnotationMatrix = CoAnn;
    }

    public void putBHalf(DenseVector BelowHalf) {
        BHalf = BelowHalf;
    }

    public void putConstant(Double C) {
        constant = C;
    }

    @Override
    public String [] getKey() {
        return new String [] {getNamespace(), "" + getOrganismId(), "" + getGoBranch() + ".CoAnnotationSet"};
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
}
