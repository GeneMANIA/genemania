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
 * RelatedGenesEngineResponseDto: Engine-specific Related Genes response data transfer object   
 * Created Jul 22, 2009
 * @author Khalid Zuberi
 */
package org.genemania.dto;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.genemania.type.CombiningMethod;


public class RelatedGenesEngineResponseDto implements Serializable {

	private static final long serialVersionUID = -5219421807631571720L;

	List<NetworkDto> networks;
	CombiningMethod combiningMethodApplied;
	Collection<AttributeDto> attributes; // redundant
	Map<Long, Collection<AttributeDto>> nodeToAttributes;  // maps Node ID -> Collection<AttributeDto>
	List<NodeDto> nodes;

    public RelatedGenesEngineResponseDto() {
	}

	public List<NetworkDto> getNetworks() {
		return networks;
	}

	public void setNetworks(List<NetworkDto> networks) {
		this.networks = networks;
	}
	
	public CombiningMethod getCombiningMethodApplied() {
		return combiningMethodApplied;
	}

	public void setCombiningMethodApplied(CombiningMethod combiningMethodApplied) {
		this.combiningMethodApplied = combiningMethodApplied;
	}

    public Collection<AttributeDto> getAttributes() {
        return attributes;
    }

    public void setAttributes(Collection<AttributeDto> attributes) {
        this.attributes = attributes;
    }

    public Map<Long, Collection<AttributeDto>> getNodeToAttributes() {
        return nodeToAttributes;
    }

    public void setNodeToAttributes(
            Map<Long, Collection<AttributeDto>> nodeToAttributes) {
        this.nodeToAttributes = nodeToAttributes;
    }

    public List<NodeDto> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeDto> nodes) {
        this.nodes = nodes;
    }    
}
