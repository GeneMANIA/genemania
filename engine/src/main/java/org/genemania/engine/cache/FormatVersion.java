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

import org.genemania.exception.ApplicationException;

/*
 * engine updates may introduce new
 * data objects, or revisions to the
 * storage representation of old ones.
 * 
 * a simple property file records format
 * data to help make decisions about I/O
 * backends to use.
 * 
 * 
 */
public class FormatVersion {
    
    private String type;
    private int major;
    private int minor;
    private int patch;

    /*
     * return a format version, or throw an exception if fail. 
     * handle should be some general resource spec, eg url like
     * thing.
     * 
     * 
     * should never return null
     */
    public static FormatVersion load(String handle) throws ApplicationException {
        // TODO: actually implement this
        throw new ApplicationException("Not implemented");
    }
    
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public int getMajor() {
        return major;
    }
    public void setMajor(int major) {
        this.major = major;
    }
    public int getMinor() {
        return minor;
    }
    public void setMinor(int minor) {
        this.minor = minor;
    }
    public int getPatch() {
        return patch;
    }
    public void setPatch(int patch) {
        this.patch = patch;
    }
    

}
