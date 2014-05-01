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
 * RelatedGenesResponsetMessage: JMS object wrapper for Related Genes response data   
 * Created Jul 21, 2009
 * @author Ovi Comes
 */
package org.genemania.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.genemania.dto.AttributeDto;
import org.genemania.dto.NodeDto;
import org.genemania.dto.OntologyCategoryDto;

public class RelatedGenesResponseMessage extends RelatedGenesMessageBase {

	// __[static]______________________________________________________________
	private static final long serialVersionUID = -1518300840222370253L;

	// __[attributes]__________________________________________________________
	private Map<Long, Collection<OntologyCategoryDto>> annotations = new Hashtable<Long, Collection<OntologyCategoryDto>>();
	private Map<Long, Collection<AttributeDto>> attributes = new Hashtable<Long, Collection<AttributeDto>>();
	private List<NodeDto> nodes = new ArrayList<NodeDto>();

	// __[constructors]________________________________________________________
	public RelatedGenesResponseMessage() {
	}

	// __[accessors]___________________________________________________________
	public Map<Long, Collection<OntologyCategoryDto>> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(Map<Long, Collection<OntologyCategoryDto>> annotations) {
		this.annotations = annotations;
	}

	public Map<Long, Collection<AttributeDto>> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<Long, Collection<AttributeDto>> attributes) {
        this.attributes = attributes;
    }
    
    public void setNodes(List<NodeDto> nodes) {
        this.nodes = nodes;
    }

    public List<NodeDto> getNodes() {
        return nodes;
    }    

    // __[public helpers]______________________________________________________
	public static RelatedGenesResponseMessage fromXml(String xml) {
		return (RelatedGenesResponseMessage)XS.fromXML(xml);
	}

}