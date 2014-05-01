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
 * UploadNetworkRequestMessage: JMS object wrapper for Upload Network request data   
 * Created Oct 19, 2009
 * @author Ovi Comes
 */
package org.genemania.message;

public class UploadNetworkRequestMessage extends UploadNetworkMessageBase {

	// __[static]______________________________________________________________
	private static final long serialVersionUID = 7406472225061579700L;
	
	// __[attributes]__________________________________________________________
	private long networkId;
	private long organismId;
	private String layout;
	private String method;
	private String namespace;
	private String data;
	private int sparsification;

	// __[constructors]________________________________________________________
	public UploadNetworkRequestMessage() {
	}

	// __[accessors]___________________________________________________________
	public static UploadNetworkRequestMessage fromXml(String xml) {
		return (UploadNetworkRequestMessage)XS.fromXML(xml);
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

	public String getLayout() {
		return layout;
	}

	public void setLayout(String layout) {
		this.layout = layout;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public int getSparsification() {
		return sparsification;
	}

	public void setSparsification(int sparsification) {
		this.sparsification = sparsification;
	}

	// __[public helpers]______________________________________________________
	
}
