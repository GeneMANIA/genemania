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
 * InteractionNetworkMock: InteractionNetwork mock object
 * Created Jul 28, 2009
 * @author Ovi Comes
 */
package org.genemania.mock;

import java.util.ArrayList;
import java.util.Collection;

import org.genemania.domain.Interaction;
import org.genemania.domain.InteractionNetwork;

public class InteractionNetworkMock {
	
	public static InteractionNetwork getMockObject(long id) {
		InteractionNetwork ret = new InteractionNetwork();
		ret.setDefaultSelected(true);
		ret.setDescription("just an interaction network mock");
		ret.setId(id);
		Collection<Interaction> interactions = new ArrayList<Interaction>();
		interactions.add(InteractionMock.getMockObject(id));
		ret.setInteractions(interactions );
		ret.setName("interaction network mock " + id);
		return ret;
	}
	
}
