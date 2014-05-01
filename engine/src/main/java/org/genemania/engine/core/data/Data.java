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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.genemania.exception.ApplicationException;

/**
 * base class.
 *
 * Note: the public constructors for the Data objects that subclass
 * this should require enough parameters to specify the object key
 */
public class Data implements Serializable {
    private static final long serialVersionUID = -593697227777035420L;
    
    // core namespace, TODO: move to common?
    public static final String CORE = "CORE";

    private String namespace;
    private long organismId;

    private Data() {}

    public Data(String namespace, long organismId) {
        this.namespace = namespace;
        this.organismId = organismId;
    }
    
    /*
     * key is a string tuple that uniquely identifies
     * a data object in the cache. these are normally
     * used to build up a path string if storing in eg a file
     */
    public String [] getKey() {
        throw new RuntimeException("not implemented");
    }

    /*
     * for some of our mapping types, create a map to do reverse lookups. Would probably
     * be faster to have maps over primitive types, maybe investigate trove and colt's
     * collections over primitive types later if a bottleneck
     * 
     */
    protected static Map<Long, Integer> makeReverseMap(long [] data) throws ApplicationException {
        HashMap<Long, Integer> map = new HashMap<Long, Integer>();
        for (int i=0; i<data.length; i++) {
            Long key = data[i];

            if (map.containsKey(key)) {
                throw new ApplicationException("key already exists (must be unique!): " + key);
            }

            map.put(key, i);
        }

        return map;
    }

    protected static Map<String, Integer> makeReverseMap(String [] data) throws ApplicationException {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        for (int i=0; i<data.length; i++) {
            String key = data[i];

            if (map.containsKey(key)) {
                throw new ApplicationException("key already exists (must be unique!): " + key);
            }

            map.put(key, i);
        }

        return map;
    }

    /**
     * @return the namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @param namespace the namespace to set
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * @return the organismId
     */
    public long getOrganismId() {
        return organismId;
    }

    /**
     * @param organismId the organismId to set
     */
    public void setOrganismId(long organismId) {
        this.organismId = organismId;
    }

    public Data copy(String newNamespace) {
        throw new RuntimeException("not implemented");
    }

    /*
     * our namspaces are organized hierarchically by namespace and organism.
     * probably we should introduce Organism and Namespace objects with the below
     * getKey() methods as instance methods, instead of these static methods here ... TODO
     */
    public static String [] getOrganismKey(String namespace, long organismId) {
        return new String [] {namespace, "" + organismId};
    }

    public static String [] getNamespaceKey(String namespace) {
        return new String [] {namespace}; // there, that was easy
    }
}
