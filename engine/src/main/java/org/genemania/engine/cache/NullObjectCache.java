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

import java.util.List;

import org.genemania.exception.ApplicationException;

/**
 * saves nothing, returns nothing. just for testing.
 *
 */
public class NullObjectCache implements IObjectCache {


    public String getCacheDir() {
        return null;
    }

    public void put(String [] key, Object value, boolean isVolatile){}
    public Object get(String [] key, boolean isVolatile) {return null;}
    public void remove(String [] key) {}

	public boolean exists(String[] key) throws ApplicationException {
		// TODO Auto-generated method stub
		return false;
	}

	public List<String[]> list(String[] key) throws ApplicationException {
		// TODO Auto-generated method stub
		return null;
	}

}
