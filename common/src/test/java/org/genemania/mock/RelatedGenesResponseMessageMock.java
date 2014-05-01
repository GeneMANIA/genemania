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
 * RelatedGenesResponseMessageMock: RelatedGenesResponseMessage mock object
 * Created Aug 06, 2009
 * @author Ovi Comes
 */
package org.genemania.mock;

import java.util.ArrayList;
import java.util.Collection;

import org.genemania.dto.NetworkDto;
import org.genemania.message.RelatedGenesResponseMessage;

public class RelatedGenesResponseMessageMock {
	
	public static RelatedGenesResponseMessage getMockObject(long id) {
		RelatedGenesResponseMessage ret = new RelatedGenesResponseMessage();
		Collection<NetworkDto> networks = new ArrayList<NetworkDto>();
		NetworkDto nvo1 = NetworkVOMock.getMockObject(id);
		networks.add(nvo1);
		ret.setNetworks(networks);
		return ret;
	}

}
