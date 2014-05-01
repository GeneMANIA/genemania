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

import com.thoughtworks.xstream.XStream;

public class UploadNetworkMessageBase implements Serializable {

	// __[static]______________________________________________________________
	private static final long serialVersionUID = 7901591389061112723L;
	protected static XStream XS = new XStream();
	static {
		XS.alias("UpNetReq", UploadNetworkRequestMessage.class);
		XS.alias("UpNetRes", UploadNetworkResponseMessage.class);
	}

	// __[attributes]__________________________________________________________
	private int errorCode = 0;
	private String errorMessage = "";

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

	// __[public helpers]______________________________________________________
	public String toXml() {
		return XS.toXML(this);
	}

}
