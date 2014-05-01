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
package org.genemania.engine.core.integration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.genemania.engine.Constants.NetworkType;
import org.genemania.exception.ApplicationException;

/* 
 * ordered list of features
 */
public class FeatureList extends ArrayList<Feature> {
    private static final long serialVersionUID = 5333968524718172348L;
    
    public FeatureList() {
        super();
    }
    
    public FeatureList(int size) {
        super(size);
    }
    
    public FeatureList(FeatureList featureList, boolean addBias) {
        this.addAll(featureList);
        this.addBias();
    }
    
    /*
     * check for duplicates. not polite enough to tell
     * you what they are though
     */
    public void validate() throws ApplicationException {
        
        HashSet<Feature> uniq = new HashSet<Feature>();
        uniq.addAll(this);
        
        if (uniq.size() != this.size()) {
            throw new ApplicationException("the feature list contains duplicates");
        }        
    }
        
    /*
     * return a collection of featureLists, grouped by network type
     * and group id
     */
    public Collection<FeatureList> group() {
        HashMap<String, FeatureList> groups = new HashMap<String, FeatureList>();

        for (Feature feature: this) {
            String key = String.format("%s-%d", feature.getType().getCode(), feature.getGroupId());
            
            FeatureList list = null;
            if (groups.containsKey(key)) {
                list = groups.get(key);
            }
            else {
                list = new FeatureList();
                groups.put(key, list);
            }
            
            list.add(feature);
        }
        
        return groups.values();
    }


    /* 
     * ignores group id for sparse networks
     */
    public int indexOf(Feature feature) {
    	synchronized (this) {
	        if (reverseMap == null) {
	            buildReverseMap();
	        }
    	}
        
        String key = makeReverseMapKey(feature);
        Integer result = reverseMap.get(key);
        if (result == null) {
            return -1;
        }
        else {
            return result;
        }
    }
    
    private String makeReverseMapKey(Feature feature) {
        return feature.key(true); // ignore group id for sparse networks
    }

    HashMap<String, Integer> reverseMap = null; // soo not safe, if we mutate featureList!
    void buildReverseMap() {

        reverseMap = new HashMap<String, Integer>();

        for (int i=0; i<this.size(); i++) {
            Feature feature = this.get(i);
            String key = makeReverseMapKey(feature);
            
            if (reverseMap.containsKey(key)) {
                throw new RuntimeException("consistency error");
            }
            else {
                reverseMap.put(key, i);
            }            
        }
    }
    
    /*
     * include a bias in the first position
     */
    public void addBias() {
        this.add(0, new Feature(NetworkType.BIAS, 0, 0));
    }
    
    /*
     * remove bias from first position
     */
    public void removeBias() {
        if (this.size() > 0 && this.get(0).getType() == NetworkType.BIAS) {
            this.remove(0);
        }
    }
    
    public boolean hasBias() {
        if (this.size() > 0 && this.get(0).getType() == NetworkType.BIAS) {
            return true;
        }
        return false;
    }
}
