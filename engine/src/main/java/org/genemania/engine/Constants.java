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

package org.genemania.engine;

import org.genemania.exception.ApplicationException;

/*
 * Enums and constants used in the
 * engine package.
 */
public class Constants {

    public static final String ENGINE_PROPERTY_FILE = "Engine.properties";
    public static final String UNKNOWN_VERSION = "UNKNOWN";

    public static final boolean DEFAULT_IS_REGULARIZATION_ENABLED = false;
    public static final double DEFAULT_REGULARIZATION_CONSTANT = 1d;

    public static final boolean DEFAULT_NORMALIZE_NETWORK_WEIGHTS_ENABLED = true;
    public static final boolean DEFAULT_COMBINED_NETWORK_NORMALIZATION_ENABLED = false;
    public static final int DEFAULT_ATTRIBUTE_ENRICHMENT_MAX_SIZE = 100;
    
    public static final double DISCRIMINANT_THRESHOLD = 0.0d;
    
    // TODO: lets make this next value property-loadable
    public static final int DEFAULT_AUTO_SELECT_MIN_GENE_THRESHOLD = 5;

    // we support two types of networks, either backed by the
    // usual sparse matrices, as well as by attribute vectors.
    public enum NetworkType {
        BIAS("BIAS"), 
        SPARSE_MATRIX("SPARSE_MATRIX"),
        ATTRIBUTE_VECTOR("ATTRIBUTE_VECTOR");
        
        private String code;
        private NetworkType(String code) {
            this.code = code;
        }

        public String getCode() {
            return this.code;
        }
    }
    
    // define available combining methods
    public enum CombiningMethod {
        AVERAGE(false),             // simple average of all networks
        AUTOMATIC(true),           // unregularized genemania
        AUTOMATIC_FAST(true),      // unregularized genemania with precomputed KtK including -/- interactions
        AVERAGE_CATEGORY(false),    // average equally between categories (eg network groups)
        BP(false),                  // simultaneous weighting with GO BP branch target
        CC(false),                  // as above, CC
        MF(false),                  // as above, MF
        AUTOMATIC_RELEVANCE(false); // simultaneous weigting, chosing best matching GO branch
        
        private final boolean querySpecific;
        
        CombiningMethod(boolean querySpecific) {
        	this.querySpecific = querySpecific;
        }
        
        public boolean isQuerySpecific() {
        	return querySpecific;
        }
    }

    // define available scoring methods
    public enum ScoringMethod {
        DISCRIMINANT, CONTEXT, ZSCORE
    }
    public enum GOCache {
    	Data_Matrix, GOIndexMap, combinedDataMatrix, childSiblingMap  
    }
    public enum FastWeightCache {
    	
    }
    //value in label vector that are treated as unknown
    public static final double EXCLUDED_ROW_VALUE = -2.0;


    /* the names of the different cache files are scattered around the code
     * in the form of strings. to be replaced with constants below
     */ 
    public enum DataFileNames {
        KtK("KtK"), // for fast weighting
        KtK_BASIC("KtK_BASIC"); // generic, without scaling, later replace specific versions with this
        
        private String code;
        private DataFileNames(String code) {
            this.code = code;
        }

        public String getCode() {
            return this.code;
        }
    }

    // TODO: replace this go branch array of string names by enum
    public static final String[] goBranches = {"BP", "CC", "MF"};
    public static final String ALL_ONTOLOGY = "ALL"; // for data structures gather BP, CC, MF into one
    public static final CombiningMethod[] combiningMethodForBranch = {CombiningMethod.BP, CombiningMethod.CC, CombiningMethod.MF}; // should be consistent with the goBranches array. rediculous no?
    public static int getIndexForGoBranch(String branch) throws ApplicationException {
        int index;
        if (branch.equalsIgnoreCase("BP")) {
            index = 0;
        }
        else if (branch.equalsIgnoreCase("CC")) {
            index = 1;
        }
        else if (branch.equals("MF")) {
            index = 2;
        }
        else {
            throw new ApplicationException("unexpected branch name: " + branch);
        }

        // quick consistency check
        if (!goBranches[index].equalsIgnoreCase(branch)) {
            throw new ApplicationException("inconsistent branch numbering");
        }

        return index;

    }
    
    public static CombiningMethod getMethodForBranch(String branch) throws ApplicationException {
        int index = Constants.getIndexForGoBranch(branch);
        return Constants.combiningMethodForBranch[index];
    }

    // progress reporting states for find related genes
    public static final int PROGRESS_START = 0;
    public static final String PROGRESS_START_MESSAGE = "starting";

    public static final int PROGRESS_WEIGHTING = 1;
    public static final String PROGRESS_WEIGHTING_MESSAGE = "computing network weights";

    public static final int PROGRESS_COMBINING = 2;
    public static final String PROGRESS_COMBINING_MESSAGE = "building combined network";

    public static final int PROGRESS_SCORING = 3;
    public static final String PROGRESS_SCORING_MESSAGE = "computing gene scores";

    public static final int PROGRESS_OUTPUTS = 4;
    public static final String PROGRESS_OUTPUTS_MESSAGE = "preparing outputs";

    public static final int PROGRESS_COMPLETE = 5;
    public static final String PROGRESS_COMPLETE_MESSAGE = "done";

    // progress reportering for upload network
    public static final int PROGRESS_UPLOAD_START = 0;
    public static final String PROGRESS_UPLOAD_START_MESSAGE = "starting";

    public static final int PROGRESS_UPLOAD_PROCESSING = 1;
    public static final String PROGRESS_UPLOAD_PROCESSING_MESSAGE = "processing network";

    public static final int PROGRESS_UPLOAD_PRECOMPUTING = 2;
    public static final String PROGRESS_UPLOAD_PRECOMPUTING_MESSAGE = "updating data structures";

    public static final int PROGRESS_UPLOAD_COMPLETE = 3;
    public static final String PROGRESS_UPLOAD_COMPLETE_MESSAGE = "done";

    /**
     * little helper to determine combining method based on common
     * terms. returns null if not found.
     *
     * @param methodName
     * @return
     */
    public static CombiningMethod getCombiningMethod(String methodName) {
        if (methodName == null) {
            return null;
        }
        else if (methodName.equalsIgnoreCase("average") || methodName.equalsIgnoreCase("equal")) {
            return CombiningMethod.AVERAGE;
        }
        else if (methodName.equalsIgnoreCase("automatic") || methodName.equalsIgnoreCase("auto") ||
                methodName.equalsIgnoreCase("smart")) {
            return CombiningMethod.AUTOMATIC;
        }
        else if (methodName.equalsIgnoreCase("automatic_relevance") || methodName.equalsIgnoreCase("Simutaneous_Weighting") ||
                methodName.equalsIgnoreCase("automatic_relevant")) {
            return CombiningMethod.AUTOMATIC_RELEVANCE;
        }
        else if (methodName.equalsIgnoreCase("average by categories") || methodName.equalsIgnoreCase("categories") || methodName.equalsIgnoreCase("average category") || methodName.equalsIgnoreCase("average_category")) {
            return CombiningMethod.AVERAGE_CATEGORY;
        }
        else if (methodName.equalsIgnoreCase("BP") || methodName.equalsIgnoreCase("Biological_Processes")) {
            return CombiningMethod.BP;
        }
        else if (methodName.equalsIgnoreCase("CC") || methodName.equalsIgnoreCase("Cellular_Components")) {
            return CombiningMethod.CC;
        }
        else if (methodName.equalsIgnoreCase("MF") || methodName.equalsIgnoreCase("Molecular_Functions")) {
            return CombiningMethod.MF;
        }
        else if (methodName.equalsIgnoreCase("automatic_fast")) {
            return CombiningMethod.AUTOMATIC_FAST;
        }
        else {
            return null;
        }
    }

    /*
     * this method suggests our design has a, umm, layering problem. TODO: get rid of the enum duplication
     */
    public static org.genemania.engine.Constants.CombiningMethod convertCombiningMethod(org.genemania.type.CombiningMethod combiningMethod, int size)
            throws ApplicationException {
        if (combiningMethod == org.genemania.type.CombiningMethod.AUTOMATIC) {
            return org.genemania.engine.Constants.CombiningMethod.AUTOMATIC;
        }
        else if (combiningMethod == org.genemania.type.CombiningMethod.AUTOMATIC_RELEVANCE) {
            return org.genemania.engine.Constants.CombiningMethod.AUTOMATIC_RELEVANCE;
        }
        else if (combiningMethod == org.genemania.type.CombiningMethod.AVERAGE) {
            return org.genemania.engine.Constants.CombiningMethod.AVERAGE;
        }
        else if (combiningMethod == org.genemania.type.CombiningMethod.AVERAGE_CATEGORY) {
            return org.genemania.engine.Constants.CombiningMethod.AVERAGE_CATEGORY;
        }
        else if (combiningMethod == org.genemania.type.CombiningMethod.BP) {
            return org.genemania.engine.Constants.CombiningMethod.BP;
        }
        else if (combiningMethod == org.genemania.type.CombiningMethod.CC) {
            return org.genemania.engine.Constants.CombiningMethod.CC;
        }
        else if (combiningMethod == org.genemania.type.CombiningMethod.MF) {
            return org.genemania.engine.Constants.CombiningMethod.MF;
        }
        else if (combiningMethod == org.genemania.type.CombiningMethod.AUTOMATIC_FAST) {
            return org.genemania.engine.Constants.CombiningMethod.AUTOMATIC_FAST;
        }
        /* 
         * heuristic for combining method selection
         */
        else if (combiningMethod == org.genemania.type.CombiningMethod.AUTOMATIC_SELECT) {
        	if (size <= DEFAULT_AUTO_SELECT_MIN_GENE_THRESHOLD) {
        		return org.genemania.engine.Constants.CombiningMethod.BP;        		
        	}
        	else {
        		return org.genemania.engine.Constants.CombiningMethod.AUTOMATIC;
        	}
        }
        else if (combiningMethod == org.genemania.type.CombiningMethod.UNKNOWN) {
            throw new ApplicationException("unkown combining method");
        }
        else {
            throw new ApplicationException("unexpected combining method");
        }
    }

    /*
     * more enum duplication ugliness, TODO: consolidate, probably just use the version of the constants
     * in common and give up on the engine core being independent.
     */
    public static org.genemania.type.CombiningMethod convertCombiningMethod(org.genemania.engine.Constants.CombiningMethod combiningMethod)
            throws ApplicationException {
        if (combiningMethod == org.genemania.engine.Constants.CombiningMethod.AUTOMATIC) {
            return org.genemania.type.CombiningMethod.AUTOMATIC;
        }
        else if (combiningMethod == org.genemania.engine.Constants.CombiningMethod.AUTOMATIC_RELEVANCE) {
            return org.genemania.type.CombiningMethod.AUTOMATIC_RELEVANCE;
        }
        else if (combiningMethod == org.genemania.engine.Constants.CombiningMethod.AVERAGE) {
            return org.genemania.type.CombiningMethod.AVERAGE;
        }
        else if (combiningMethod == org.genemania.engine.Constants.CombiningMethod.AVERAGE_CATEGORY) {
            return org.genemania.type.CombiningMethod.AVERAGE_CATEGORY;
        }
        else if (combiningMethod == org.genemania.engine.Constants.CombiningMethod.BP) {
            return org.genemania.type.CombiningMethod.BP;
        }
        else if (combiningMethod == org.genemania.engine.Constants.CombiningMethod.CC) {
            return org.genemania.type.CombiningMethod.CC;
        }
        else if (combiningMethod == org.genemania.engine.Constants.CombiningMethod.MF) {
            return org.genemania.type.CombiningMethod.MF;
        }
        else if (combiningMethod == org.genemania.engine.Constants.CombiningMethod.AUTOMATIC_FAST) {
            return org.genemania.type.CombiningMethod.AUTOMATIC_FAST;
        }
        else {
            throw new ApplicationException("unexpected combining method");
        }
    }
    
    public static org.genemania.engine.Constants.ScoringMethod convertScoringMethod(org.genemania.type.ScoringMethod scoringMethod)
            throws ApplicationException {
        if (scoringMethod == org.genemania.type.ScoringMethod.DISCRIMINANT) {
            return org.genemania.engine.Constants.ScoringMethod.DISCRIMINANT;
        }
        else if (scoringMethod == org.genemania.type.ScoringMethod.CONTEXT) {
            return org.genemania.engine.Constants.ScoringMethod.CONTEXT;
        }
        else if (scoringMethod == org.genemania.type.ScoringMethod.ZSCORE) {
            return org.genemania.engine.Constants.ScoringMethod.ZSCORE;
        }
        else {
            throw new ApplicationException("unexpected combining method");
        }
    }
} 