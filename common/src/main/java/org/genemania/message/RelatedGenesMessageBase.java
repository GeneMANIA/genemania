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
 * RelatedGenesRequestMessageBase: base JMS object wrapper for Related Genes request data   
 * Created Jul 17, 2009
 * @author Ovi Comes
 */
package org.genemania.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.genemania.dto.InteractionDto;
import org.genemania.dto.NetworkDto;
import org.genemania.dto.NetworkGroupDto;
import org.genemania.dto.NodeDto;
import org.genemania.dto.OntologyCategoryDto;

import com.thoughtworks.xstream.XStream;

public class RelatedGenesMessageBase implements Serializable {

	// __[static]______________________________________________________________
	private static final long serialVersionUID = 1930790196821490296L;
	protected static XStream XS = new XStream();
	static {
		XS.alias("RelGenReq", RelatedGenesRequestMessage.class);
		XS.aliasField("oid", RelatedGenesRequestMessage.class, "organismId");
		XS.aliasField("networks", RelatedGenesRequestMessage.class, "networks");
		XS.aliasField("nodes", RelatedGenesRequestMessage.class, "positiveNodes");
		XS.aliasField("method", RelatedGenesRequestMessage.class, "combiningMethod");
		XS.alias("network", NetworkDto.class);
		XS.alias("RelGenRes", RelatedGenesResponseMessage.class);
		XS.alias("group", NetworkGroupDto.class);
		XS.alias("network", NetworkDto.class);
		XS.alias("interaction", InteractionDto.class);
		XS.alias("node", NodeDto.class);
		XS.alias("category", OntologyCategoryDto.class);
		XS.aliasField("ontid", RelatedGenesRequestMessage.class, "ontologyId");
		XS.aliasField("attributeGroups", RelatedGenesRequestMessage.class, "attributeGroups");
	}

	// __[attributes]__________________________________________________________
	private int errorCode = 0;
	private String errorMessage = "";
	private long organismId;
	private long ontologyId;
	private Collection<NetworkDto> networks = new ArrayList<NetworkDto>();
	private Collection<Long> positiveNodes = new ArrayList<Long>();
	private String combiningMethod;
	private Collection<Long> attributeGroups = new ArrayList<Long>();

    // __[accessors]___________________________________________________________
	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public long getOrganismId() {
		return organismId;
	}

	public void setOrganismId(long organismId) {
		this.organismId = organismId;
	}

	public long getOntologyId() {
		return ontologyId;
	}

	public void setOntologyId(long ontolgyId) {
		this.ontologyId = ontolgyId;
	}

	public Collection<Long> getPositiveNodes() {
		return positiveNodes;
	}

	public void setPositiveNodes(Collection<Long> positiveNodes) {
		this.positiveNodes = positiveNodes;
	}

	public String getCombiningMethod() {
		return combiningMethod;
	}

	public void setCombiningMethod(String combiningMethod) {
		this.combiningMethod = combiningMethod;
	}

	public Collection<NetworkDto> getNetworks() {
		return networks;
	}

	public void setNetworks(Collection<NetworkDto> networks) {
		this.networks = networks;
	}

    public Collection<Long> getAttributeGroups() {
        return attributeGroups;
    }

    public void setAttributeGroups(Collection<Long> attributeGroups) {
        this.attributeGroups = attributeGroups;
    }

	// __[public helpers]______________________________________________________
	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer();
		if (errorCode > 0) {
			ret.append("errorCode=" + errorCode);
		}
		if (StringUtils.isNotEmpty(errorMessage)) {
			ret.append("errorMessage=" + errorMessage);
		}
		if (organismId > 0) {
			ret.append("organimsId=" + organismId);
		}
		ret.append(", combiningMethod=" + combiningMethod);
		if (networks != null) {
			ret.append(", networks=" + networks.size());
		} else {
			ret.append(", networks=null");
		}
		if (positiveNodes != null) {
			ret.append(", positiveNodes=" + positiveNodes.size());
		} else {
			ret.append(", positiveNodes=null");
		}
		if (attributeGroups != null) {
		    ret.append(", attributeGroups=" + attributeGroups.size());
		}
		else {
		    ret.append(", attributeGroups=null");
		}
		return ret.toString();
	}

	public String toXml() {
		return XS.toXML(this);
	}
	
}
