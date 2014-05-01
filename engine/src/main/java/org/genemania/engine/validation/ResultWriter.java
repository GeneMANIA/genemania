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

package org.genemania.engine.validation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

/**
 * factored out of our output programs like
 * VCV. TODO: need to update those progs to use this class.
 *
 * maybe should switch this out and use csv writer instead?
 */
public class ResultWriter {

    String outputFilename;
    char sepChar;
    private PrintWriter writer;

    
    public ResultWriter(String outputFilename, char sepChar, boolean append) throws IOException {
        this.outputFilename = outputFilename;
        this.sepChar = sepChar;
        this.writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFilename, append)));
    }

    /*
     * sepchar defaults to tab, append to false
     */
    public ResultWriter(String outputFilename) throws IOException {
        this(outputFilename, '\t', false);
    }

    public void close() {
        writer.close();
    }
    
    /*
     * this is unformatted, you probably don't want to use this
     * unless you have unusual formatting requirements
     */
    public void writeOutput(String msg) {
        writer.println(msg);
    }

    public void write(String [] fields) {
        StringBuilder msg = new StringBuilder(fields[0]);
        for (int i=1; i<fields.length; i++) {
            msg.append(sepChar + fields[i]);
        }

        writeOutput(msg.toString());
    }

    public void write(List<String> fields) {
        StringBuilder msg = new StringBuilder(fields.get(0));
        for (int i = 1; i < fields.size(); i++) {
            msg.append(sepChar + fields.get(i));
        }

        writeOutput(msg.toString());
    }

    public void writeComment(String comment) {
        writer.print("# " + comment);
    }

    public void flush() {
        writer.flush();
    }

    /*
     * for when you don't actually want to generate output
     */
    public static ResultWriter getNullWriter() {
        return new NullResultWriter();
    }

    private ResultWriter() {
    };

    private static class NullResultWriter extends ResultWriter {

        public NullResultWriter() {
        }

        public NullResultWriter(String outputFilename, char sepChar) {
        }

        @Override
        public void writeOutput(String msg) {
        }


        @Override
        public void close() {
        }
    }

}
