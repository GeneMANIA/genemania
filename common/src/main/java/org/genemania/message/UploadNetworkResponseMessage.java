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
 * UploadNetworkResponseMessage: JMS object wrapper for Upload Network response data   
 * Created Oct 19, 2009
 * @author Ovi Comes
 */
package org.genemania.message;

public class UploadNetworkResponseMessage extends UploadNetworkMessageBase {

	// __[static]______________________________________________________________
	private static final long serialVersionUID = -602093599200762923L;

	// __[attributes]__________________________________________________________
	private long interactionCount;

	// __[constructors]________________________________________________________
	public UploadNetworkResponseMessage() {
	}

	// __[accessors]___________________________________________________________
	public long getInteractionCount() {
		return interactionCount;
	}

	public void setInteractionCount(long interactionCount) {
		this.interactionCount = interactionCount;
	}
	
	// __[public helpers]______________________________________________________
	public static UploadNetworkResponseMessage fromXml(String xml) {
		return (UploadNetworkResponseMessage)XS.fromXML(xml);
	}

}