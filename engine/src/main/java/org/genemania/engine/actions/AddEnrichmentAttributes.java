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
package org.genemania.engine.actions;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.genemania.dto.AddEnrichmentAttributesEngineRequestDto;
import org.genemania.dto.AddEnrichmentAttributesEngineResponseDto;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.config.Config;
import org.genemania.engine.core.data.CategoryIds;
import org.genemania.engine.core.data.GoAnnotations;
import org.genemania.engine.core.data.GoIds;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.engine.matricks.Matrix;
import org.genemania.exception.ApplicationException;

public class AddEnrichmentAttributes {
    private static Logger logger = Logger.getLogger(AddOrganism.class);
    
    DataCache cache;
    AddEnrichmentAttributesEngineRequestDto request;

    GoAnnotations annos;
    GoIds goIds;
    CategoryIds catIds;
    Set<Long> UniqueCategoryIds = new HashSet<Long>();
    
    public AddEnrichmentAttributes(DataCache cache, AddEnrichmentAttributesEngineRequestDto request) {
        this.cache = cache;
        this.request = request;
    }
    
    public AddEnrichmentAttributesEngineResponseDto process() throws ApplicationException {
        
        logger.info(String.format("adding ontology %s for organism %s", request.getOntologyId(), request.getOrganismId()));
        
        check();
        load();
        write();
        
        AddEnrichmentAttributesEngineResponseDto response = new AddEnrichmentAttributesEngineResponseDto();
        return response;
        
    }
    
    protected void load() throws ApplicationException {

        // annotation matrix
        annos = new GoAnnotations(request.getOrganismId(), "" + request.getOntologyId());

        int numCategories = UniqueCategoryIds.size();
        NodeIds nodeIds = cache.getNodeIds(request.getOrganismId());
        int numGenes = nodeIds.getNodeIds().length;

        Matrix data = Config.instance().getMatrixFactory().sparseMatrix(numGenes, numCategories);

        annos.setData(data);

        // id lists, one by names and the other by database ids.
        goIds = new GoIds(request.getOrganismId(), "" + request.getOntologyId());
        catIds = new CategoryIds(request.getOrganismId(), request.getOntologyId());

        String[] catNames = new String[numCategories];
        long [] catIdList = new long[numCategories];

        int ix = 0;        
        for (long categoryId: UniqueCategoryIds) {
            catNames[ix] ="tmp" + categoryId; // TODO
            catIdList[ix] = categoryId;
            ix += 1;
        }

        goIds.setGoIds(catNames);
        catIds.setCategoryIds(catIdList);
        
        int n = request.getCategoryIds().size();
        for (int i=0; i<n; i++) {
            int nodeIx = nodeIds.getIndexForId(request.getNodeIds().get(i));
            int catIx = catIds.getIndexForId(request.getCategoryIds().get(i));
            data.set(nodeIx, catIx, 1);
        }
    }

    /* 
     * make sure all the node ids referenced exist for the
     * given organism. build up list of unique category ids
     * as we go along
     */
    protected void check() throws ApplicationException {
        
        // make sure the ontology doesn't already exist
        boolean ok = false;
        try {
            cache.getGoAnnotations(request.getOrganismId(), "" + request.getOntologyId());
        }
        catch (ApplicationException e) {
            ok = true;
        }
                
        if (!ok) {
            throw new ApplicationException(String.format("the ontology %s already exists", request.getOntologyId()));
        }
        
        ok = false;
        try {
            cache.getGoIds(request.getOrganismId(), "" + request.getOntologyId());
        }
        catch (ApplicationException e) {
            ok = true;
        }
                
        if (!ok) {
            throw new ApplicationException(String.format("the ontology %s already exists", request.getOntologyId()));
        }
        
        ok = false;
        try {
            cache.getCategoryIds(request.getOrganismId(), request.getOntologyId());
        }
        catch (ApplicationException e) {
            ok = true;
        }
                
        if (!ok) {
            throw new ApplicationException(String.format("the ontology %s already exists", request.getOntologyId()));
        }
        
        int n = request.getCategoryIds().size();
        if (request.getNodeIds().size() != n) {
            throw new ApplicationException("inconsistent length of annotation mappings");
        }
        
        if (hasNames()) {
            if (request.getCategoryNames().size() != n) {
                throw new ApplicationException("inconsistent length of annotation mappings");
            }
        }

        // check node ids
        NodeIds nodeIds = cache.getNodeIds(request.getOrganismId());
        
        for (int i=0; i<n; i++) {
            if (request.getNodeIds().get(i) == null) {
                throw new ApplicationException("null node id");
            }
            
            try {
                int ix = nodeIds.getIndexForId(request.getNodeIds().get(i));
            }
            catch (ApplicationException e) {
                throw new ApplicationException(String.format("The given node id %s does not belong to organism %s", request.getNodeIds().get(i), request.getOrganismId()));
            }
        }
        
        // check category ids
        for (int i=0; i<n; i++) {
            Long categoryId = request.getCategoryIds().get(i);
            if (categoryId == null) {
                throw new ApplicationException("null category id");
            }
            
            UniqueCategoryIds.add(categoryId);
        }
        
    }
    
    /*
     * so, make the names optional, then later take them away?
     */
    protected boolean hasNames() {
        return request.getCategoryNames() != null && request.getCategoryNames().size() != 0;
    }    

    protected void write() throws ApplicationException {
        cache.putGoAnnotations(annos);
        cache.putGoIds(goIds);
        cache.putCategoryIds(catIds);
    }
}
