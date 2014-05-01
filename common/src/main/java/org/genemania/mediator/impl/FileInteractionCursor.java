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

package org.genemania.mediator.impl;

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
import org.genemania.mediator.InteractionCursor;

import au.com.bytecode.opencsv.CSVReader;

/* 
 * Cursor over interactions stored in a tabular text file
 *
 * - interaction id is not supported (will throw an exception)
 * - getTotalInteractions(), if called, does a loop over the
 * file to count the records, using a second, independent reader
 * from the one which is called by next().
 * - uses opencsv for the record parsing, so we should get support
 * for quoted fields etc for free.
 *
 * the short constructor reads a file in a standard format of
 * 3 tab-delimited columns, id1<tab>id2<tab>weight, but
 * the long format constructor can be used for other files.
 *
 * if weightcol < 0, then the input file is assumed to be binary (no
 * weight col), and the weight is always returned as 1
 * 
 * TODO: 
 * - add an option to long-format constructor to skip N header lines.
 * - maybe store the interaction count in case someone calls it often?
 */
public class FileInteractionCursor implements InteractionCursor {

    private long networkId;
	private File file;
    String fileEncoding;
    protected int idCol1;
    protected int idCol2;
    protected int weightCol;
    char delim;

    CSVReader reader;
    protected String[] nextLine;

    public FileInteractionCursor(long networkId, File file, String fileEncoding, int idCol1, int idCol2, int weightCol, char delim) throws ApplicationException {
        this.networkId = networkId;
        this.file = file;
        this.idCol1 = idCol1;
        this.idCol2 = idCol2;
        this.weightCol = weightCol;
        this.fileEncoding = fileEncoding;
        this.delim = delim;

        reader = open();
    }

    public FileInteractionCursor(long networkId, File file) throws ApplicationException {
        this(networkId, file, "UTF8", 0, 1, 2, '\t');
    }

    private CSVReader open() throws ApplicationException {
        try {
        	InputStream stream = getStream(file);
            Reader source = new BufferedReader(new InputStreamReader(stream, fileEncoding));
            CSVReader csvReader = new CSVReader(source, delim);
            return csvReader;
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

	public long getId() throws ApplicationException {
        throw new ApplicationException("'Id' not supported for interaction files");
    }

    public long getFromNodeId() throws ApplicationException {
        return Long.parseLong(nextLine[idCol1]);
    }

    public long getToNodeId() throws ApplicationException {
        return Long.parseLong(nextLine[idCol2]);
    }

    public float getWeight() throws ApplicationException {
        if (weightCol < 0) {
            return 1;
        }
        else {
            return Float.parseFloat(nextLine[weightCol]);
        }
    }

    public long getTotalInteractions() throws ApplicationException {
        long lineCount = 0;

        CSVReader countReader = null;
        try {
            countReader = open();
            while (countReader.readNext() != null) {
                lineCount += 1;
            }
        }
        catch (Exception e) {
            throw new ApplicationException(e);
        }
        finally {
            try {
                countReader.close();
            }
            catch (Exception e) {
                // we'll let an exception on close pass silently ...
            }
        }

        return lineCount;
    }

    public void close() throws ApplicationException {
        if (reader != null) {
            try {
                reader.close();
                reader = null;
            } catch (Exception e) {
                throw new ApplicationException(e);
            }
        }
    }

    public boolean next() throws ApplicationException {
        try {
            nextLine = reader.readNext();
        } catch (Exception e) {
            throw new ApplicationException(e);
        }

        if (nextLine == null) {
            return false;
        } else {
            return true;
        }
    }

    public long getNetworkId() {
        return this.networkId;
    }
}
