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


import java.awt.geom.Line2D;

/**
 * Uses a gravitational force model to act as a "wall". Can be used to
 * construct line segments which either attract or repel items.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class WallForce extends AbstractForce {

    private static String[] pnames = new String[] { "GravitationalConstant" };
    
    public static final float DEFAULT_GRAV_CONSTANT = -0.1f;
    public static final float DEFAULT_MIN_GRAV_CONSTANT = -1.0f;
    public static final float DEFAULT_MAX_GRAV_CONSTANT = 1.0f;
    public static final int GRAVITATIONAL_CONST = 0;
    
    private float x1, y1, x2, y2;
    private float dx, dy;
    
    /**
     * Create a new WallForce.
     * @param gravConst the gravitational constant of the wall
     * @param x1 the first x-coordinate of the wall
     * @param y1 the first y-coordinate of the wall
     * @param x2 the second x-coordinate of the wall
     * @param y2 the second y-coordinate of the wall
     */
    public WallForce(float gravConst, 
        float x1, float y1, float x2, float y2) 
    {
        params = new float[] { gravConst };
        minValues = new float[] { DEFAULT_MIN_GRAV_CONSTANT };
        maxValues = new float[] { DEFAULT_MAX_GRAV_CONSTANT };
        
        this.x1 = x1; this.y1 = y1;
        this.x2 = x2; this.y2 = y2;
        dx = x2-x1;
        dy = y2-y1;
        float r = (float)Math.sqrt(dx*dx+dy*dy);
        if ( dx != 0.0 ) dx /= r;
        if ( dy != 0.0 ) dy /= r;
    }
    
    /**
     * Create a new WallForce with default gravitational constant.
     * @param x1 the first x-coordinate of the wall
     * @param y1 the first y-coordinate of the wall
     * @param x2 the second x-coordinate of the wall
     * @param y2 the second y-coordinate of the wall
     */
    public WallForce(float x1, float y1, float x2, float y2) {
        this(DEFAULT_GRAV_CONSTANT,x1,y1,x2,y2);
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
        float[] n = item.location;
        int ccw = Line2D.relativeCCW(x1,y1,x2,y2,n[0],n[1]);
        float r = (float)Line2D.ptSegDist(x1,y1,x2,y2,n[0],n[1]);
        if ( r == 0.0 ) r = (float)Math.random() / 100.0f;
        float v = params[GRAVITATIONAL_CONST]*item.mass / (r*r*r);
        if ( n[0] >= Math.min(x1,x2) && n[0] <= Math.max(x1,x2) )
            item.force[1] += ccw*v*dx;
        if ( n[1] >= Math.min(y1,y2) && n[1] <= Math.max(y1,y2) )
            item.force[0] += -1*ccw*v*dy;
    }

} // end of class WallForce
