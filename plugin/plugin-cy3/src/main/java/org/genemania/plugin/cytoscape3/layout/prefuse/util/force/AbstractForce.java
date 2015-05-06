package org.genemania.plugin.cytoscape3.layout.prefuse.util.force;

/*
 * #%L
 * Cytoscape Prefuse Layout Impl (layout-prefuse-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


/**
 * Abstract base class for force functions in a force simulation. This
 * skeletal version provides support for storing and retrieving float-valued
 * parameters of the force function. Subclasses should use the protected
 * field <code>params</code> to store parameter values.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public abstract class AbstractForce implements Force {

    protected float[] params;
    protected float[] minValues;
    protected float[] maxValues;

    /**
     * Initialize this force function. This default implementation does nothing.
     * Subclasses should override this method with any needed initialization.
     * @param fsim the encompassing ForceSimulator
     */
    public void init(ForceSimulator fsim) {
        // do nothing.
    }

    /**
     * @see org.genemania.plugin.cytoscape3.layout.prefuse.util.force.Force#getParameterCount()
     */
    public int getParameterCount() {
        return ( params == null ? 0 : params.length );
    }

    /**
     * @see org.genemania.plugin.cytoscape3.layout.prefuse.util.force.Force#getParameter(int)
     */
    public float getParameter(int i) {
        if ( i < 0 || params == null || i >= params.length ) {
            throw new IndexOutOfBoundsException();
        } else {
            return params[i];
        }
    }
    
    /**
     * @see org.genemania.plugin.cytoscape3.layout.prefuse.util.force.Force#getMinValue(int)
     */
    public float getMinValue(int i) {
        if ( i < 0 || params == null || i >= params.length ) {
            throw new IndexOutOfBoundsException();
        } else {
            return minValues[i];
        }
    }
    
    /**
     * @see org.genemania.plugin.cytoscape3.layout.prefuse.util.force.Force#getMaxValue(int)
     */
    public float getMaxValue(int i) {
        if ( i < 0 || params == null || i >= params.length ) {
            throw new IndexOutOfBoundsException();
        } else {
            return maxValues[i];
        }
    }
    
    /**
     * @see org.genemania.plugin.cytoscape3.layout.prefuse.util.force.Force#getParameterName(int)
     */
    public String getParameterName(int i) {
        String[] pnames = getParameterNames();
        if ( i < 0 || pnames == null || i >= pnames.length ) {
            throw new IndexOutOfBoundsException();
        } else {
            return pnames[i];
        }
    }

    /**
     * @see org.genemania.plugin.cytoscape3.layout.prefuse.util.force.Force#setParameter(int, float)
     */
    public void setParameter(int i, float val) {
        if ( i < 0 || params == null || i >= params.length ) {
            throw new IndexOutOfBoundsException();
        } else {
            params[i] = val;
        }
    }
    
    /**
     * @see org.genemania.plugin.cytoscape3.layout.prefuse.util.force.Force#setMinValue(int, float)
     */
    public void setMinValue(int i, float val) {
        if ( i < 0 || params == null || i >= params.length ) {
            throw new IndexOutOfBoundsException();
        } else {
            minValues[i] = val;
        }
    }
    
    /**
     * @see org.genemania.plugin.cytoscape3.layout.prefuse.util.force.Force#setMaxValue(int, float)
     */
    public void setMaxValue(int i, float val) {
        if ( i < 0 || params == null || i >= params.length ) {
            throw new IndexOutOfBoundsException();
        } else {
            maxValues[i] = val;
        }
    }
    
    protected abstract String[] getParameterNames();
    
    /**
     * Returns false.
     * @see org.genemania.plugin.cytoscape3.layout.prefuse.util.force.Force#isItemForce()
     */
    public boolean isItemForce() {
        return false;
    }
    
    /**
     * Returns false.
     * @see org.genemania.plugin.cytoscape3.layout.prefuse.util.force.Force#isSpringForce()
     */
    public boolean isSpringForce() {
        return false;
    }
    
    /**
     * Throws an UnsupportedOperationException.
     * @see org.genemania.plugin.cytoscape3.layout.prefuse.util.force.Force#getForce(org.genemania.plugin.cytoscape3.layout.prefuse.util.force.ForceItem)
     */
    public void getForce(ForceItem item) {
        throw new UnsupportedOperationException(
            "This class does not support this operation");
    }
    
    /**
     * Throws an UnsupportedOperationException.
     * @see org.genemania.plugin.cytoscape3.layout.prefuse.util.force.Force#getForce(org.genemania.plugin.cytoscape3.layout.prefuse.util.force.Spring)
     */
    public void getForce(Spring spring) {
        throw new UnsupportedOperationException(
            "This class does not support this operation");
    }
    
} // end of abstract class AbstractForce
