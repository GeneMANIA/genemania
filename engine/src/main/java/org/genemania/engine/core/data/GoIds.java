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

import java.util.Map;
import org.genemania.exception.ApplicationException;

/**
 * String go identifiers eg "GO:01234". position
 * in array indicates position in other data
 * structures for the corresponding go term
 */
public class GoIds extends Data {
    private static final long serialVersionUID = 8553816706315210693L;

    private String goBranch;
    private String[] goIds;
    private Map<String, Integer> reverseMap;

    public GoIds(long organismId, String goBranch) {
        super(Data.CORE, organismId);
        this.goBranch = goBranch;
    }
    
    /**
     * @return the goIds
     */
    public String[] getGoIds() {
        return goIds;
    }

    /**
     * @param goIds the goIds to set
     */
    public void setGoIds(String[] goIds) {
        this.goIds = goIds;
    }

    /**
     * @return the goBranch
     */
    public String getGoBranch() {
        return goBranch;
    }

    /**
     * @param goBranch the goBranch to set
     */
    public void setGoBranch(String goBranch) {
        this.goBranch = goBranch;
    }

    @Override
    public String [] getKey() {
        return new String [] {getNamespace(), "" + getOrganismId(), goBranch + ".GoIds"};
    }

    /*
     * return go id for matrix position index
     */
    public String getIdForIndex(int index) throws ApplicationException {
        try {
            return goIds[index];
        }
        catch (IndexOutOfBoundsException e) {
            throw new ApplicationException("there is no id at index postion: " + index);
        }
    }

    /*
     * return matrix position index for go id
     */
    public int getIndexForId(String id) throws ApplicationException {
        if (reverseMap == null) {
            reverseMap = Data.makeReverseMap(goIds);
        }

        Integer index = reverseMap.get(id);
        if (index == null) {
            throw new ApplicationException("there is no index position for node id: " + id);
        }

        return index;
    }
}
