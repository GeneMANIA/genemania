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
 * NetworkConversionDto: Data Transfer Object used in the network conversion process    
 * Created Oct 10, 2008
 * @author Ovi Comes
 */
package org.genemania.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class NetworkConversionDto extends ConversionDto implements Serializable {

	// __[static]______________________________________________________________
	private static final long serialVersionUID = -3296810211295174977L;
	
	// __[attributes]__________________________________________________________
	private NetworkDto network = new NetworkDto();
	private List<InteractionDto> duplicatedInteractions = new ArrayList<InteractionDto>();

	// __[constructors]________________________________________________________
	public NetworkConversionDto() {
	}

	// __[accessors]___________________________________________________________
	public NetworkDto getNetwork() {
		return network;
	}

	public void setNetwork(NetworkDto network) {
		this.network = network;
	}

	public List<InteractionDto> getDuplicatedInteractions() {
		return duplicatedInteractions;
	}

	public void setDuplicatedInteractions(List<InteractionDto> duplicatedInteractions) {
		this.duplicatedInteractions = duplicatedInteractions;
	}

}
