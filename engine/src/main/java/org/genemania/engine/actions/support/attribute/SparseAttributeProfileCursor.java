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
package org.genemania.engine.actions.support.attribute;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

import org.genemania.exception.ApplicationException;

import au.com.bytecode.opencsv.CSVReader;

/*
 * parse text containing attribute data formatted with
 * each line containing a node id followed by one or more
 * attribute ids.
 */
public class SparseAttributeProfileCursor implements AttributeCursor {
    
    CSVReader csvReader;
    protected String[] nextLine;
    int pos; // counter in line for current assoc
    
    public SparseAttributeProfileCursor(Reader reader, char delim) throws ApplicationException {
        csvReader = new CSVReader(new BufferedReader(reader), delim);
    }
    
    public SparseAttributeProfileCursor(File file, String fileEncoding, char delim) throws ApplicationException {
        try {
            InputStream stream = getStream(file);
            Reader source = new BufferedReader(new InputStreamReader(stream, fileEncoding));
            csvReader = new CSVReader(source, delim);
        } catch (Exception e) {
            throw new ApplicationException(e);
        }
    }
    
    private InputStream getStream(File file) throws FileNotFoundException {
        try {
            return new GZIPInputStream(new FileInputStream(file));
        } catch (IOException e) {
        }
        return new FileInputStream(file);
    }
    
    @Override
    public long getNodeId() {
        return Long.parseLong(nextLine[0]);
    }

    @Override
    public long getAttributeId() {
        return Long.parseLong(nextLine[pos]);
    }
    
    @Override
    public boolean next() throws ApplicationException {
        boolean next = nextAssociation();
        if (next) {
            return true;
        }
        else {
            return nextLine();
        }
    }
    
    private boolean nextLine() throws ApplicationException {
        try {
            nextLine = csvReader.readNext();
            pos = 1;
        } catch (Exception e) {
            throw new ApplicationException(e);
        }

        if (nextLine == null) {
            return false;
        } else {
            return true;
        }        
    }
    
    private boolean nextAssociation() throws ApplicationException {
        if (nextLine == null) { // first time through
            return false;
        }
        else if (pos+1 < nextLine.length) {
            pos++;
            return true;
        }
        return false;       
    }
    
    public void close() throws ApplicationException {
        if (csvReader != null) {
            try {
                csvReader.close();
                csvReader = null;
            } catch (Exception e) {
                throw new ApplicationException(e);
            }
        }
    }
}
