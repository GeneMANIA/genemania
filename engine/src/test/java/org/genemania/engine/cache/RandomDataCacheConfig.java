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
package org.genemania.engine.cache;

/*
 * parameters controlling the construction of engine
 * test data used in unit tests.
 */
public class RandomDataCacheConfig {
    private long seed;

    private int org1numGenes;
    private int org1numNetworks;
    private double org1networkSparsity;
    private int numCategories;
    private int numAttributeGroups;
    private int numAttributesPerGroup;
    private double org1AnnotationSparsity;
    private double org1AttributeSparsity;
   
    // TODO: the following should be set by RandomDataCacheBuilder, 
    // tests should make no assumptions. a couple of these are
    // hardcoded here for now
    private long org1Id;
    private long [] org1NetworkIds;
    private long [] attributeGroupId;

    private static RandomDataCacheConfig standardConfig;

    public static RandomDataCacheConfig getStandardConfig() {
        if (standardConfig == null) {
            standardConfig = new RandomDataCacheConfig();
            standardConfig.seed = 7132;
            standardConfig.org1Id = 1;
            standardConfig.org1numGenes = 20;
            standardConfig.org1numNetworks = 10;
            standardConfig.org1networkSparsity = .5;
            standardConfig.numCategories = 50;
            standardConfig.numAttributeGroups = 2;
            standardConfig.numAttributesPerGroup = 10;
            standardConfig.org1AnnotationSparsity = .5;         
            standardConfig.attributeGroupId = new long[]{1};    
        }
        
        return standardConfig;
    }

    private static RandomDataCacheConfig standardConfig2;

    public static RandomDataCacheConfig getStandardConfig2() {
        if (standardConfig2 == null) {
            standardConfig2 = new RandomDataCacheConfig();
            standardConfig2.seed = 7132;
            standardConfig2.org1Id = 1;
            standardConfig2.org1numGenes = 50;
            standardConfig2.org1numNetworks = 10;
            standardConfig2.org1networkSparsity = .5;
            standardConfig2.numCategories = 20;
            standardConfig2.numAttributeGroups = 2;
            standardConfig2.numAttributesPerGroup = 10;
            standardConfig2.org1AnnotationSparsity = .5; 
            standardConfig2.org1AttributeSparsity = .5;
            standardConfig2.attributeGroupId = new long[]{1};           
        }
        
        return standardConfig2;
    }

    private static RandomDataCacheConfig standardConfig3;

    public static RandomDataCacheConfig getStandardConfig3() {
        if (standardConfig2 == null) {
            standardConfig2 = new RandomDataCacheConfig();
            standardConfig2.seed = 2112;
            standardConfig2.org1Id = 1;
            standardConfig2.org1numGenes = 10;
            standardConfig2.org1numNetworks = 3;
            standardConfig2.org1networkSparsity = .5;
            standardConfig2.numCategories = 5;
            standardConfig2.numAttributeGroups = 2;
            standardConfig2.numAttributesPerGroup = 10;
            standardConfig2.org1AnnotationSparsity = .5; 
            standardConfig2.org1AttributeSparsity = .5;
            standardConfig2.attributeGroupId = new long[]{1};           
        }
        
        return standardConfig3;
    }

    public long getSeed() {
        return seed;
    }

    public long getOrg1Id() {
        return org1Id;
    }

    public int getOrg1numGenes() {
        return org1numGenes;
    }

    public int getOrg1numNetworks() {
        return org1numNetworks;
    }

    public double getOrg1networkSparsity() {
        return org1networkSparsity;
    }

    public int getNumCategories() {
        return numCategories;
    }

    public int getNumAttributeGroups() {
        return numAttributeGroups;
    }

    public int getNumAttributesPerGroup() {
        return numAttributesPerGroup;
    }

    public double getOrg1AnnotationSparsity() {
        return org1AnnotationSparsity;
    }
    
    public double getOrg1AttributeSparsity() {
        return org1AttributeSparsity;
    }

    public void setOrg1NetworkIds(long [] org1NetworkIds) {
        this.org1NetworkIds = org1NetworkIds;
    }

    public long [] getOrg1NetworkIds() {
        return org1NetworkIds;
    }

    public long [] getAttributeGroupId() {
        return attributeGroupId;
    }
}
