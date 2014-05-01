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
 * NetworkMediator: TODO add description
 * Created Jul 02, 2008
 * @author Ovi Comes
 */
package org.genemania.mediator;

import java.util.List;

import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;

public interface NetworkMediator {

	public List<InteractionNetwork> getAllNetworks();
	public InteractionNetwork getNetwork(long networkId);
	public void saveNetwork(InteractionNetwork network);
	public void saveNetworkGroup(InteractionNetworkGroup group);
	public InteractionNetworkGroup getNetworkGroupByName(String groupName, long organismId);
	public InteractionCursor createInteractionCursor(long networkId);
	public List<InteractionNetworkGroup> getNetworkGroupsByOrganism(long organismId);
	public InteractionNetworkGroup getNetworkGroupForNetwork(long networkId);
	public boolean isValidNetwork(long organismId, long networkId);
	
}
