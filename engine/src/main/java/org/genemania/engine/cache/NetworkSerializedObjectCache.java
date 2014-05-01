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

package org.genemania.engine.cache;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.List;

import org.genemania.exception.ApplicationException;

/**
 * Very simple cache implementation using java file serialization. So obviously
 * the objects being cached must be Serializable.
 * 
 * TODO: exception handling
 */
public class NetworkSerializedObjectCache implements IObjectCache {

    private String cacheDir;

    public String getCacheDir() {
        return cacheDir;
    }

    /*
     * should be url, eg http://server.somewhere.com/networks/v2.2
     */
    public NetworkSerializedObjectCache(String cacheDir) {
        this.cacheDir = cacheDir;
    }

    private Object getRemoteFile(String filename) throws ApplicationException {
        Object value = null;
        try {
            URL url = new URL(filename);
            InputStream input = url.openStream();
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(input));

            value = in.readObject();

            in.close();

        } catch (Exception e) {
            throw new ApplicationException("failed to retrieve file " + filename + " from cache at " + getCacheDir(), e);
        }

        return value;
    }

    public void put(String [] key, Object value, boolean isVolatile) throws ApplicationException {
         throw new ApplicationException("not implemented");
    }

    public Object get(String [] key, boolean isVolatile) throws ApplicationException {
         throw new ApplicationException("not implemented");
    }
    
    public void remove(String [] key) throws ApplicationException {
         throw new ApplicationException("not implemented");
    }
    
	public boolean exists(String[] key) throws ApplicationException {
        throw new ApplicationException("not implemented");
	}

	public List<String[]> list(String[] key) throws ApplicationException {
        throw new ApplicationException("not implemented");
	} 
}
