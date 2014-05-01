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

package org.genemania.engine.apps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.FlexCompColMatrix;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.genemania.engine.converter.Mapping;
import org.genemania.engine.core.MatrixUtils;
import org.genemania.engine.utils.FileUtils;
import org.genemania.exception.ApplicationException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import au.com.bytecode.opencsv.CSVReader;

/**
 * take a network file and synonym file, remove any interactions in the network
 * that don't correspond to an entry in the synonym file, ensure the network
 * is symmetric, normalize the network, and write it back out. The network
 * is written with the unique id's as the names.
 */
public class NetworkNormalizer {

    private static Logger logger = Logger.getLogger(NetworkNormalizer.class);
    // TODO: make encodings configurable
    private static String MAPPING_FILE_ENCODING = "utf8";
    private static String NETWORK_FILE_ENCODING = "utf8";
    @Option(name = "-in", usage = "name of input file containing network data")
    private String inFilename;
    @Option(name = "-out", usage = "name of output file to contain output network")
    private String outFilename;
    @Option(name = "-log", usage = "name of processing log file to create (will truncate old file)")
    private String logFilename;
    @Option(name = "-syn", usage = "name of identifier naming files")
    private String synFilename;
    @Option(name = "-norm", usage = "normalize the interactions, -norm {true|false}, defaults to true")
    private String normalize = "true";
    @Option(name = "-outtype", usage = "output network using names or unique ids, -outtype {name,uid,uidstripped}, defaults to uid")
    private String outType = "uid"; // TODO: add constants for the possible values, and validation
    @Option(name = "-k", usage = "nearest k neighbours threshold, no sparsification if not given")
    private int k = 0; // k=0 means don't sparsify
    // TODO: add command option for case sensitivity
    boolean forceUpper = true;

    public String getOutType() {
        return outType;
    }

    public void setOutType(String outType) {
        this.outType = outType;
    }
    boolean isNormalizationEnabled;
    Mapping<String, String> mapping;
    Matrix network;

    Set<String> unrecognizedIdentifiers = new HashSet<String>();

    public static void main(String[] args) throws Exception {

        NetworkNormalizer normalizer = new NetworkNormalizer();

        // load up command line arguments
        if (!normalizer.getCommandLineArgs(args)) {
        	System.exit(1);
        }

        normalizer.setupLogging();

        try {
            normalizer.process();
        }
        catch (Exception e) {
            logger.error("Fatal error", e);
        }
    }

    public String getInFilename() {
        return inFilename;
    }

    public void setInFilename(String inFilename) {
        this.inFilename = inFilename;
    }

    public String getOutFilename() {
        return outFilename;
    }

    public void setOutFilename(String outFilename) {
        this.outFilename = outFilename;
    }

    public String getLogFilename() {
        return logFilename;
    }

    public void setLogFilename(String logFilename) {
        this.logFilename = logFilename;
    }

    public String getSynFilename() {
        return synFilename;
    }

    public void setSynFilename(String synFilename) {
        this.synFilename = synFilename;
    }

    protected boolean getCommandLineArgs(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
            isNormalizationEnabled = Boolean.parseBoolean(normalize);
        }
        catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java -jar myprogram.jar [options...] arguments...");
            parser.printUsage(System.err);
            return false;
        }

        return true;
    }

    public String getNormalize() {
        return normalize;
    }

    public void setNormalize(String normalize) {
        this.normalize = normalize;
    }

    public boolean isNormalizationEnabled() {
        return isNormalizationEnabled;
    }

    public void setNormalizationEnabled(boolean isNormalizationEnabled) {
        this.isNormalizationEnabled = isNormalizationEnabled;
    }

    /**
     * For command-line usage, user can specify a log file with an optional argument.
     * log4j is used as the actual logging mechanism, and is configured here.
     *
     */
    private void setupLogging() throws Exception {
        // setup logging
        if (logFilename == null) {
            return;
        }

        SimpleLayout layout = new SimpleLayout();
        FileAppender appender = new FileAppender(layout, logFilename, false);

        logger.addAppender(appender);
        logger.setLevel(Level.DEBUG);
    }

    public void process() throws Exception {
        loadMapping();
        loadNetwork(this.inFilename, '\t', 0, 1, 2);

        MatrixUtils.setDiagonalZero(network);

        if (k > 0) {
            logger.info("sparsifying to top " + k + " interactions");
            sparsifyRowsTopKPositives(network, k);
        }

        MatrixUtils.setToMaxTranspose(network);

        if (isNormalizationEnabled()) {
            MatrixUtils.normalizeNetwork(network);
        }

        writeNetwork();
    }

    void loadMapping() throws Exception {
        Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(synFilename), MAPPING_FILE_ENCODING));
        mapping = FileUtils.loadMapping(reader, '\t', 0, 1, forceUpper);
        reader.close();
    }

    void loadNetwork(String fileName, char delim, int sourceColNum, int targetColNum, int weightColNum) throws Exception {
        Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFilename), NETWORK_FILE_ENCODING));
        try {
            network = loadNetwork(reader, delim, sourceColNum, targetColNum, weightColNum);
        }
        finally {
            reader.close();
        }
    }

    public Matrix loadNetwork(Reader source, char delim, int sourceColNum, int targetColNum, int weightColNum) throws Exception {
        CSVReader reader = new CSVReader(source, delim);

        String[] nextLine;

        // TODO: we could create a smaller matrix if we could filter the mapping based on the identifiers that appear in the file,
        // would it make a difference?
        Matrix m = new FlexCompColMatrix(mapping.size(), mapping.size());
        Matrix counts = new FlexCompColMatrix(mapping.size(), mapping.size());

        while ((nextLine = reader.readNext()) != null) {
            String fromSymbol;
            String toSymbol;
            double weight;

            try {
                fromSymbol = nextLine[sourceColNum].trim();
                toSymbol = nextLine[targetColNum].trim();

                if (forceUpper) {
                    fromSymbol = fromSymbol.toUpperCase();
                    toSymbol = toSymbol.toUpperCase();
                }

                weight = Double.parseDouble(nextLine[weightColNum]);
            }
            catch (NumberFormatException e) {
                // skip bad data, eg  header lines
                System.out.println("ignoring: " + e);
                continue;
            }
            catch (ArrayIndexOutOfBoundsException e) {
                // not enough fields?
                System.out.println("ignoring: " + e);
                continue;
            }

            int fromIndex = mapping.getIndexForUniqueId(mapping.getUniqueIdForAlias(fromSymbol));
            int toIndex = mapping.getIndexForUniqueId(mapping.getUniqueIdForAlias(toSymbol));

            if (fromIndex == -1) {
            	if (!unrecognizedIdentifiers.contains(fromSymbol)) {
            		logger.warn(String.format("failed to find symbol: \"%s\"", fromSymbol));
            		unrecognizedIdentifiers.add(fromSymbol);
            	}
            }
            else if (toIndex == -1) {
            	if (!unrecognizedIdentifiers.contains(toSymbol)) {
            		logger.warn(String.format("failed to find symbol: \"%s\"", toSymbol));
            		unrecognizedIdentifiers.add(toSymbol);
            	}
            }
            else {
                double count = counts.get(fromIndex, toIndex);
                /**
                 *  adding to previous entry when
                 *  1. the count has already been incremented (3rd, 4th, ... th time adding to
                 *     entry at (fromIndex, toIndex)
                 *  2. the entry at (fromIndex, toIndex) isn't zero (2nd time adding to entry
                 *     at (fromIndex, toIndex) )
                 */
                if (count > 0 || m.get(fromIndex, toIndex) != 0) {
                    count++;
                    m.set(fromIndex, toIndex, m.get(fromIndex, toIndex) + weight);
                    counts.set(fromIndex, toIndex, count);
                }
                else {
                    // first time setting entry at (fromIndex, toIndex)
                    m.set(fromIndex, toIndex, weight);
                }
            }
        }

        // average the identifiers
        for (MatrixEntry e: counts) {
            // no need to check if e.get() > 0 because we're using a sparse matrix

            //entries on the diagonal are going to be set to 0 anyways
            if (e.row() != e.column()) {
                double newValue = m.get(e.row(), e.column()) / (e.get() + 1);
                m.set(e.row(), e.column(), newValue);
            }
        }

        reader.close();
        return m;
    }

    /*
     * zero-out any elements of each row that are not among the top-k values
     *
     * So this doesn't assume symmetry, but does assume that all the elements
     * are non-negative!
     */
    public static void sparsifyRowsTopKPositives(Matrix m, int k) {

        for (int i = 0; i < m.numRows(); i++) {
            Vector v = MatrixUtils.extractRowToVector(m, i);
            int[] indices = MatrixUtils.getIndicesForSortedValues(v);

            for (int j = k; j < indices.length; j++) {
                if (v.get(indices[j]) > 0) {
                    m.set(i, indices[j], 0);
                }
                else {
                    // since v is sorted in desc order, the rest of the elements
                    // must already be zero so we don't have to zero them
                    break;
                }
            }
        }
    }

    public void writeNetwork() throws Exception {
        PrintWriter writer = new PrintWriter(new File(outFilename));
        if (outType.equalsIgnoreCase("uid")) {
            FileUtils.dump(writer, network, mapping, '\t', true, false);
        }
        else if (outType.equalsIgnoreCase("uidstripped")) {
            FileUtils.dump(writer, network, mapping, '\t', true, true);
        }
        else if (outType.equalsIgnoreCase("name")) {
            FileUtils.dump(writer, network, mapping, '\t', false, false);
        }
        else {
            throw new ApplicationException("Unexpected output type: " + outType);
        }

        writer.close();
    }
}
