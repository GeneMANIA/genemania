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
 * Interface to object cache implementations.
 * Probably should add a clear(key) method.
 */
public interface IObjectCache {
        public String getCacheDir() throws ApplicationException;
        public void put(String [] key, Object value, boolean isVolatile) throws ApplicationException;
        public Object get(String [] key, boolean isVolatile) throws ApplicationException;
        public void remove(String [] key) throws ApplicationException;
        public boolean exists(String [] key) throws ApplicationException;
        public List<String []> list(String [] key) throws ApplicationException;
}
