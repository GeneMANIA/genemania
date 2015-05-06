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
 * Interface for force functions in a force simulation.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public interface Force {

    /**
     * Initialize this force function.
     * @param fsim the encompassing ForceSimulator
     */
    public void init(ForceSimulator fsim);

    /**
     * Returns the number of parameters (e.g., gravitational constant or
     * spring force coefficient) affecting this force function. 
     * @return the number of parameters
     */
    public int getParameterCount();

    /**
     * Returns the specified, numbered parameter.
     * @param i the index of the parameter to return
     * @return the parameter value
     */
    public float getParameter(int i);
    
    /**
     * Get the suggested minimum value for a parameter. This value is not
     * strictly enforced, but is used by interface components that allow force
     * parameters to be varied.
     * @param param the parameter index
     * @return the suggested minimum value.
     */
    public float getMinValue(int param);
    
    /**
     * Get the suggested maximum value for a parameter. This value is not
     * strictly enforced, but is used by interface components that allow force
     * parameters to be varied.
     * @param param the parameter index
     * @return the suggested maximum value.
     */
    public float getMaxValue(int param);
    
    /**
     * Gets the text name of the requested parameter.
     * @param i the index of the parameter
     * @return a String containing the name of this parameter
     */
    public String getParameterName(int i);

    /**
     * Sets the specified parameter value.
     * @param i the index of the parameter
     * @param val the new value of the parameter
     */
    public void setParameter(int i, float val);
    
    /**
     * Set the suggested minimum value for a parameter. This value is not
     * strictly enforced, but is used by interface components that allow force
     * parameters to be varied.
     * @param i the parameter index
     * @param val the suggested minimum value to use
     */
    public void setMinValue(int i, float val);
    
    /**
     * Set the suggested maximum value for a parameter. This value is not
     * strictly enforced, but is used by interface components that allow force
     * parameters to be varied.
     * @param i the parameter index
     * @return the suggested maximum value to use
     */
    public void setMaxValue(int i, float val);
    
    /**
     * Indicates if this force function will compute forces
     * on Spring instances.
     * @return true if this force function processes Spring instances 
     */
    public boolean isSpringForce();
    
    /**
     * Indicates if this force function will compute forces
     * on ForceItem instances
     * @return true if this force function processes Force instances 
     */
    public boolean isItemForce();
    
    /**
     * Updates the force calculation on the given ForceItem
     * @param item the ForceItem on which to compute updated forces
     */
    public void getForce(ForceItem item);
    
    /**
     * Updates the force calculation on the given Spring. The ForceItems
     * attached to Spring will have their force values updated appropriately.
     * @param spring the Spring on which to compute updated forces
     */
    public void getForce(Spring spring);
    
} // end of interface Force
