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

import org.genemania.dto.AddAttributeGroupEngineRequestDto;
import org.genemania.dto.AddAttributeGroupEngineResponseDto;
import org.genemania.dto.AddEnrichmentAttributesEngineRequestDto;
import org.genemania.dto.AddEnrichmentAttributesEngineResponseDto;
import org.genemania.dto.AddOrganismEngineRequestDto;
import org.genemania.dto.AddOrganismEngineResponseDto;
import org.genemania.dto.EnrichmentEngineRequestDto;
import org.genemania.dto.EnrichmentEngineResponseDto;
import org.genemania.dto.ListAttributeGroupsEngineRequestDto;
import org.genemania.dto.ListAttributeGroupsEngineResponseDto;
import org.genemania.dto.ListNetworksEngineRequestDto;
import org.genemania.dto.ListNetworksEngineResponseDto;
import org.genemania.dto.NetworkCombinationRequestDto;
import org.genemania.dto.NetworkCombinationResponseDto;
import org.genemania.dto.RelatedGenesEngineRequestDto;
import org.genemania.dto.RelatedGenesEngineResponseDto;
import org.genemania.dto.RemoveAttributeGroupEngineRequestDto;
import org.genemania.dto.RemoveAttributeGroupEngineResponseDto;
import org.genemania.dto.RemoveNetworkEngineRequestDto;
import org.genemania.dto.RemoveNetworkEngineResponseDto;
import org.genemania.dto.UploadNetworkEngineRequestDto;
import org.genemania.dto.UploadNetworkEngineResponseDto;
import org.genemania.exception.ApplicationException;

/**
 * external interface to mania algorithms
 * 
 */
public interface IMania {

	/*
	 * find genes related to the given set, in the specified networks
	 */
    public RelatedGenesEngineResponseDto findRelated(RelatedGenesEngineRequestDto request) throws ApplicationException;

    /*
     * list networks currently stored in back-end data representation
     */
    public ListNetworksEngineResponseDto listNetworks(ListNetworksEngineRequestDto request) throws ApplicationException;

    /*
     * add a user uploaded network
     */
    public UploadNetworkEngineResponseDto uploadNetwork(UploadNetworkEngineRequestDto request) throws ApplicationException;

    /*
     * remove a user uploaded network
     */
    public RemoveNetworkEngineResponseDto removeUserNetworks(RemoveNetworkEngineRequestDto request) throws ApplicationException;

    /*
     * perform an enrichment analysis
     */
    public EnrichmentEngineResponseDto computeEnrichment(EnrichmentEngineRequestDto request) throws ApplicationException;

    /*
     * add a new organism
     */
    public AddOrganismEngineResponseDto addOrganism(AddOrganismEngineRequestDto request) throws ApplicationException;

    /*
     * add an attribute group
     */
    public AddAttributeGroupEngineResponseDto addAttributeGroup(AddAttributeGroupEngineRequestDto request) throws ApplicationException;

    /*
     * list attributes
     */
    public ListAttributeGroupsEngineResponseDto listAttributes(ListAttributeGroupsEngineRequestDto request) throws ApplicationException;
    
    /*
     * remove attribute group
     */
    public RemoveAttributeGroupEngineResponseDto removeAttributeGroup(RemoveAttributeGroupEngineRequestDto request) throws ApplicationException;
    
    /*
     * add attributes for enrichment analysis
     */
    public AddEnrichmentAttributesEngineResponseDto addOntology(AddEnrichmentAttributesEngineRequestDto request) throws ApplicationException;
   
    /*
     * 
     */
    public NetworkCombinationResponseDto getCombinedNetwork(NetworkCombinationRequestDto request) throws ApplicationException;
     
    /*
     * clear any cached data the engine is holding to improve performance accross multiple requests
     */
    public void clearMemCache();

    /*
     * return version info
     */
    public String getVersion();
}
