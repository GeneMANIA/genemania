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

package org.genemania.engine.TestHelpers;

import java.util.Collection;
import java.util.Iterator;

import org.genemania.domain.Interaction;
import org.genemania.domain.InteractionNetwork;
import org.genemania.exception.ApplicationException;
import org.genemania.mediator.InteractionCursor;

/**
 * interaction cursor for unit test usage
 */
public class SimpleInteractionCursor implements InteractionCursor {

    private Iterator<Interaction> interactions;
    private Interaction next;
    private long networkId;
    private long count;

    public SimpleInteractionCursor(InteractionNetwork network) {
        networkId = network.getId();
        Collection<Interaction> allInteractions = network.getInteractions();
        count = allInteractions.size();
        this.interactions = allInteractions.iterator();
        if (this.interactions.hasNext()) {
            next = this.interactions.next();
        }
    }

    public boolean next() {
        if (next == null) {
            return false;
        }
        if (interactions.hasNext()) {
            next = interactions.next();
        } else {
            next = null;
        }
        return next != null;
    }

    public long getId() {
        return next.getId();
    }

    public long getFromNodeId() {
        return next.getFromNode().getId();
    }

    public long getToNodeId() {
        return next.getToNode().getId();
    }

    public float getWeight() {
        return next.getWeight();
    }

    public long getNetworkId() {
        return networkId;
    }

    public void close() {
    }

    public long getTotalInteractions() throws ApplicationException {
        return count;
    }
}
