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


package org.genemania.engine.mediators;

import java.util.List;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.engine.cache.DataCache;
import org.genemania.mediator.InteractionCursor;
import org.genemania.mediator.NetworkMediator;

/**
 *
 */
public class DataCacheNetworkMediator implements NetworkMediator {
    private DataCache cache;

    public DataCacheNetworkMediator(DataCache cache) {
        this.cache = cache;
    }
    
    public InteractionCursor createInteractionCursor(long networkId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<InteractionNetwork> getAllNetworks() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InteractionNetwork getNetwork(long networkId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InteractionNetworkGroup getNetworkGroupByName(String groupName, long organismId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void saveNetwork(InteractionNetwork network) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void saveNetworkGroup(InteractionNetworkGroup group) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

	public List<InteractionNetworkGroup> getNetworkGroupsByOrganism(
			long organismId) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public InteractionNetworkGroup getNetworkGroupForNetwork(long networkId) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public boolean isValidNetwork(long organismId, long networkId) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
