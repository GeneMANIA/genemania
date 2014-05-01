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
 * UploadNetworkWebRequestDto: Website-specific Upload Network request data transfer object   
 * Created Oct 19, 2009
 * @author Ovi Comes
 */
package org.genemania.dto;

import java.io.Serializable;

import org.genemania.type.DataLayout;
import org.genemania.type.NetworkProcessingMethod;

public class UploadNetworkWebRequestDto implements Serializable {

	// __[static]______________________________________________________________
	private static final long serialVersionUID = 5603968481654715595L;

	// __[attributes]__________________________________________________________
	private long networkId;
	private long organismId;
	private String data;
	private String namespace;
	private int sparsification;
	private DataLayout dataLayout = DataLayout.UNKNOWN; 
	private NetworkProcessingMethod processingMethod = NetworkProcessingMethod.UNKNOWN; 

	// __[constructors]________________________________________________________
	public UploadNetworkWebRequestDto() {
	}

	// __[accessors]___________________________________________________________
	public String getData() {
		return data;
	}

	public long getNetworkId() {
		return networkId;
	}

	public void setNetworkId(long networkId) {
		this.networkId = networkId;
	}

	public long getOrganismId() {
		return organismId;
	}

	public void setOrganismId(long organismId) {
		this.organismId = organismId;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public int getSparsification() {
		return sparsification;
	}

	public void setSparsification(int sparsification) {
		this.sparsification = sparsification;
	}

	public DataLayout getDataLayout() {
		return dataLayout;
	}

	public void setDataLayout(DataLayout dataLayout) {
		this.dataLayout = dataLayout;
	}

	public NetworkProcessingMethod getProcessingMethod() {
		return processingMethod;
	}

	public void setProcessingMethod(NetworkProcessingMethod processingMethod) {
		this.processingMethod = processingMethod;
	}

}
