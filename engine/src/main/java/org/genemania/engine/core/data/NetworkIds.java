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
 * mapping between nework ids, as in the
 * external genemania db and indices into the matrices
 * KtK and KtT.
 *
 * Note there is an ugly offset of 1 due to us storing
 * a bias term in the first row/col of KtK and the first element
 * of KtT that isn't represented in the network id table.
 *
 * So if a network id 'n' is referenced in KtK at position 'i',
 * then networkIds[i-1] = n
 */
public class NetworkIds extends Data {
    private static final long serialVersionUID = 8740819123941019128L;
    private long [] networkIds;
    private Map<Long, Integer> reverseMap;

    public NetworkIds(String namespace, long organismId) {
        super(namespace, organismId);
    }

    @Override
    public NetworkIds copy(String newNamespace) {
        NetworkIds copy = new NetworkIds(newNamespace, getOrganismId());
        long [] ids = new long[networkIds.length];
        System.arraycopy(networkIds, 0, ids, 0, networkIds.length);
        copy.setNetworkIds(ids);
        
        return copy;
    }
    
    /**
     * @return the networkIds
     */
    public long[] getNetworkIds() {
        return networkIds;
    }

    /**
     * @param networkIds the networkIds to set
     */
    public void setNetworkIds(long[] networkIds) {
        this.networkIds = networkIds;
        reverseMap = null;
    }

    @Override
    public String [] getKey() {
        return new String [] {getNamespace(), "" + getOrganismId(), "networkIds"};
    }

    /*
     * return network id for matrix position index
     */
    public long getIdForIndex(int index) throws ApplicationException {
        try {
            return networkIds[index];
        }
        catch (IndexOutOfBoundsException e) {
            throw new ApplicationException("there is no id at index postion: " + index);
        }
    }

    /*
     * return matrix position index for network id
     */
    public int getIndexForId(long id) throws ApplicationException {
        checkReverseMap();

        Integer index = reverseMap.get(id);
        if (index == null) {
            throw new ApplicationException("there is no index position for network id: " + id);
        }

        return index;        
    }

    /*
     * an id we know about?
     */
    public boolean containsId(long id) throws ApplicationException {
        checkReverseMap();

        return reverseMap.containsKey(id);
    }

    /*
     * lazy creation of reverse lookup
     */
    private void checkReverseMap() throws ApplicationException {
        if (reverseMap == null) {
            reverseMap = Data.makeReverseMap(networkIds);
        }
    }

    /*
     * used to support user networks, where we update
     * one network at a time instead of building the entire
     * network list at once as for core networks. so this isn't
     * the most efficient thing to do but not expected to be
     * a bottleneck. we reallocate the entire array 1 larger (!)
     * and clear the lookup map so it will get recreated on next use.
     * 
     */
    public int addNetwork(long id) {
       int nextIndex = networkIds.length;

       long [] newIds = new long[networkIds.length+1];
       System.arraycopy(networkIds, 0, newIds, 0, networkIds.length);
       newIds[nextIndex] = id;

       networkIds = newIds;
       reverseMap = null; // force rebuild next time
       return nextIndex;
    }

    /*
     * remove the given network id from the list (not the network from the disk)
     * awkward data structure, have to shift elements up. should really switch
     * to a List. or use lib of data structures for primitive types.
     */
    public void removeNetwork(long id) throws ApplicationException {
  
        if (!containsId(id)) {
            return;
        }

        long [] newIds = new long[networkIds.length-1];

        int j=0;
        for (int i=0; i<networkIds.length; i++) {
            if (networkIds[i] != id) {
                newIds[j] = networkIds[i];
                j++;
            }
        }
        
        networkIds = newIds;
        reverseMap = null;
    }
}
