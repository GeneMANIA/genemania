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
 * RelatedGenesEngineResponseDtoMock: RelatedGenesEngineResponseDtoMock mock object
 * Created Jul 28, 2009
 * @author Ovi Comes
 */
package org.genemania.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.genemania.dto.InteractionDto;
import org.genemania.dto.NetworkDto;
import org.genemania.dto.NodeDto;
import org.genemania.dto.RelatedGenesEngineResponseDto;

public class RelatedGenesEngineResponseDtoMock {

	private RelatedGenesEngineResponseDto mock;

	public RelatedGenesEngineResponseDtoMock() {
		mock = create();
	}
	
	public RelatedGenesEngineResponseDto getMockObject() {
		return mock;
	}
	
	private RelatedGenesEngineResponseDto create() {
		RelatedGenesEngineResponseDto ret = new RelatedGenesEngineResponseDto();
		List<NetworkDto> networks = new ArrayList<NetworkDto>();
		NetworkDto n1 = new NetworkDto();
		n1.setId(1);
		n1.setWeight(.5);
		Collection<InteractionDto> interactions = new ArrayList<InteractionDto>();
		interactions.add(new InteractionDto(new NodeDto(1, .1), new NodeDto(2, .2), .3));
		n1.setInteractions(interactions );
		networks.add(n1 );
		ret.setNetworks(networks);
		return ret;
	}
	
}
