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

package org.genemania.engine.core.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.genemania.exception.ApplicationException;

/**
 * mapping between node ids (that is, genes), as in the
 * external genemania db and indices into the matrices representing
 * interaction networks.
 *
 * So if a node id 'n' is referenced in our networks at row/col
 * index i, then nodeIds[i] = n
 */
public class NodeIds extends Data {
    private static final long serialVersionUID = 7748900129975191354L;
    private long [] nodeIds;
    private Map<Long, Integer> reverseMap;

    public NodeIds(long organismId) {
        super(Data.CORE, organismId);
    }
    
    /**
     * @return the nodeIds
     */
    public long[] getNodeIds() {
        return nodeIds;
    }

    /**
     * @param nodeIds the nodeIds to set
     */
    public void setNodeIds(long[] nodeIds) {
        this.nodeIds = nodeIds;
    }

    @Override
    public String [] getKey() {
        return new String [] {getNamespace(), "" + getOrganismId(), "nodeIds"};
    }

    /*
     * return node id for matrix position index
     */
    public long getIdForIndex(int index) throws ApplicationException {
        try {
            return nodeIds[index];
        }
        catch (IndexOutOfBoundsException e) {
            throw new ApplicationException("there is no id at index postion: " + index);
        }
    }

    /*
     * return matrix position index for node id
     */
    public int getIndexForId(long id) throws ApplicationException {
        if (reverseMap == null) {
            reverseMap = Data.makeReverseMap(nodeIds);
        }

        Integer index = reverseMap.get(id);
        if (index == null) {
            throw new ApplicationException("there is no index position for node id: " + id);
        }

        return index;
    }

    /*
     * conversion utility, list of ids -> corresponding list of position indices
     */
    public List<Integer> getIndicesForIds(Collection<Long> ids) throws ApplicationException {
        ArrayList<Integer> indices = new ArrayList<Integer>();

        for (Long id: ids) {
            indices.add(getIndexForId(id));
        }

        return indices;
    }
}
