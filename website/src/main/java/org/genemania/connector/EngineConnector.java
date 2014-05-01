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

/**
 * EngineConnector: contract interface to the engine 
 * Created Jul 15, 2009
 * @author Ovi Comes
 */
package org.genemania.connector;

import org.genemania.dto.RelatedGenesWebRequestDto;
import org.genemania.dto.RelatedGenesWebResponseDto;
import org.genemania.dto.UploadNetworkWebRequestDto;
import org.genemania.dto.UploadNetworkWebResponseDto;
import org.genemania.exception.ApplicationException;

public interface EngineConnector {
	
	RelatedGenesWebResponseDto getRelatedGenes(RelatedGenesWebRequestDto dto) throws ApplicationException;
	UploadNetworkWebResponseDto uploadNetwork(UploadNetworkWebRequestDto dto) throws ApplicationException;
	
}
