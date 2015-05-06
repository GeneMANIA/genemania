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
 * Represents a constant gravitational force, like the pull of gravity
 * for an object on the Earth (F = mg). The force experienced by a
 * given item is calculated as the product of a GravitationalConstant 
 * parameter and the mass of the item.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class GravitationalForce extends AbstractForce {

    private static final String[] pnames
        = { "GravitationalConstant", "Direction" };
    
    public static final int GRAVITATIONAL_CONST = 0;
    public static final int DIRECTION = 1;
    
    public static final float DEFAULT_FORCE_CONSTANT = 1E-4f;
    public static final float DEFAULT_MIN_FORCE_CONSTANT = 1E-5f;
    public static final float DEFAULT_MAX_FORCE_CONSTANT = 1E-3f;
    
    public static final float DEFAULT_DIRECTION = (float)-Math.PI/2;
    public static final float DEFAULT_MIN_DIRECTION = (float)-Math.PI;
    public static final float DEFAULT_MAX_DIRECTION = (float)Math.PI;
    
    /**
     * Create a new GravitationForce.
     * @param forceConstant the gravitational constant to use
     * @param direction the direction in which gravity should act,
     * in radians.
     */
    public GravitationalForce(float forceConstant, float direction) {
        params = new float[] { forceConstant, direction };
        minValues = new float[] 
            { DEFAULT_MIN_FORCE_CONSTANT, DEFAULT_MIN_DIRECTION };
        maxValues = new float[] 
            { DEFAULT_MAX_FORCE_CONSTANT, DEFAULT_MAX_DIRECTION };
    }
    
    /**
     * Create a new GravitationalForce with default gravitational
     * constant and direction.
     */
    public GravitationalForce() {
        this(DEFAULT_FORCE_CONSTANT, DEFAULT_DIRECTION);
    }
    
    /**
     * Returns true.
     * @see org.genemania.plugin.cytoscape3.layout.prefuse.util.force.Force#isItemForce()
     */
    public boolean isItemForce() {
        return true;
    }

    /**
     * @see org.genemania.plugin.cytoscape3.layout.prefuse.util.force.AbstractForce#getParameterNames()
     */
    protected String[] getParameterNames() {
        return pnames;
    }
    
    /**
     * @see org.genemania.plugin.cytoscape3.layout.prefuse.util.force.Force#getForce(org.genemania.plugin.cytoscape3.layout.prefuse.util.force.ForceItem)
     */
    public void getForce(ForceItem item) {
        float theta = params[DIRECTION];
        float coeff = params[GRAVITATIONAL_CONST]*item.mass;
        
        item.force[0] += Math.cos(theta)*coeff;
        item.force[1] += Math.sin(theta)*coeff;
    }

} // end of class GravitationalForce
