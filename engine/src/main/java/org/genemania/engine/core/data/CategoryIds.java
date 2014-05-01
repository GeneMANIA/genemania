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
 * mapping between category ids as in the
 * external genemania db and indices into the matrices representing
 * an anotations table.
 *
 */
public class CategoryIds extends Data {
    private static final long serialVersionUID = -6402995408418744458L;
    private long [] categoryIds;
    private long ontologyId;
    private Map<Long, Integer> reverseMap;

    public CategoryIds(long organismId, long ontologyId) {
        super(Data.CORE, organismId);
        this.ontologyId = ontologyId;
    }

    public long getOntologyId() {
        return ontologyId;
    }

    public void setOntologyId(long ontologyId) {
        this.ontologyId = ontologyId;
    }
    
    /**
     * @return the nodeIds
     */
    public long[] getCategoryIds() {
        return categoryIds;
    }

    /**
     * @param nodeIds the nodeIds to set
     */
    public void setCategoryIds(long[] nodeIds) {
        this.categoryIds = nodeIds;
    }

    @Override
    public String [] getKey() {
        return new String [] {getNamespace(), "" + getOrganismId(), "" + ontologyId + ".categoryIds"};
    }

    /*
     * return node id for matrix position index
     */
    public long getIdForIndex(int index) throws ApplicationException {
        try {
            return categoryIds[index];
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
            reverseMap = Data.makeReverseMap(categoryIds);
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
        ArrayList indices = new ArrayList<Integer>();

        for (Long id: ids) {
            indices.add(getIndexForId(id));
        }

        return indices;
    }
}
