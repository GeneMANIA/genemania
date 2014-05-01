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

package org.genemania.engine.core.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.FlexCompColMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.genemania.engine.core.KHeap;
import org.genemania.engine.core.MatrixUtils;
import org.genemania.engine.core.evaluation.correlation.Correlation;
import org.genemania.engine.core.evaluation.correlation.CorrelationFactory;
import org.genemania.engine.core.evaluation.correlation.MutualInformationData;
import org.genemania.engine.core.evaluation.correlation.MutualInformationData.SizeType;
import org.genemania.engine.core.evaluation.correlation.CorrelationFactory.CorrelationType;
import org.genemania.exception.ApplicationException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import au.com.bytecode.opencsv.CSVReader;
import java.io.StringReader;
import java.util.HashSet;
import org.genemania.engine.exception.CancellationException;
import org.genemania.engine.utils.FileUtils;
import org.genemania.util.NullProgressReporter;
import org.genemania.util.ProgressReporter;

public class ProfileToNetworkDriver {

    private static Logger logger = Logger.getLogger(ProfileToNetworkDriver.class);
    // defined so that we can construct lists with a default size
    private static final int MIN_NUMBER_OF_GENES = 1000; 

    private ProfileData profile;
    private Matrix network;

    private ProgressReporter progress = NullProgressReporter.instance();

    private Map<String, String> synonyms = new LinkedHashMap<String, String>();
    private Map<String, List<Integer>> identifiers = new HashMap<String, List<Integer>>();

    public static final String CONTINUOUS = "cont";
    public static final String BINARY = "bin";
    public static final String NETWORK = "net";

    @Option(name="-in",usage="name of input file containing profile data")
    private String inFilename;

    @Option(name="-out",usage="name of output file to contain output network")
    private String outFilename;

    @Option(name="-k",usage="nearest k neighbours threshold")
    private int k = 50;

    @Option(name="-sep",usage="separator character for input file")
    private char sepChar = '\t';

    @Option(name="-maxmissing",usage="max % of feature values for a given gene that are allowed to be missing before the entire gene is discarded")
    private double maxMissingPercentage = 25.0;

    @Option(name="-log",usage="name of processing log file to create (will truncate old file)")
    private String logFilename;

    @Option(name="-cor",usage="type of correlation to be computed")	
    private CorrelationType correlationType = CorrelationType.PEARSON;

    @Option(name="-equalElementBin",usage="Do the bins have equal number of elements")	
    private boolean equalElementBin = false;

    @Option(name="-binSize",usage="upper bound, lower bounder, or average of the two bounds")	
    private SizeType sizeType = SizeType.MEDIAN;

    @Option(name="-proftype",usage="profile type: " + BINARY + "{ary}, " + CONTINUOUS + "{inuous} (default), " + NETWORK + "{work}")
    private String profileType = CONTINUOUS;

    @Option(name="-syn",usage="name of identifier naming files")
    private String synFilename;

    @Option(name="-synsep",usage="separator character for synonuym file")	
    private char synSepChar = '\t';

    @Option(name="-synuid",usage="column # in synonym file for unique ids, 0-indexed")
    private int synIdColumn = 0;

    @Option(name="-synname",usage="column # in synonym file for identifier names, 0-indexed")	
    private int synNameColumn = 1;

    @Option(name="-noHeader",usage="file does not have a header (for binary file only)")	
    private boolean noHeader = false;

    @Option(name="-keepAllTies",usage="store more than top k if there are ties for the weakest interaction")
    private boolean keepAllTies = false;

    public static final String THRESHOLD_AUTO = "auto";
    public static final String THRESHOLD_OFF = "off";
    public static final String THRESHOLD_DEFAULT = THRESHOLD_AUTO;
    
    @Option(name="-threshold", usage="only report correlations satisfying threshold, values=" 
        + THRESHOLD_AUTO + " (method and possibly dataset dependent), " 
        + THRESHOLD_OFF 
        + ", default is " + THRESHOLD_DEFAULT)
    private String threshold = THRESHOLD_DEFAULT;

    @Option(name="-limitTies") 
    private boolean limitTies = false;
    
    public static void main(String [] args) throws Exception {

        ProfileToNetworkDriver t = new ProfileToNetworkDriver();

        // load up command line arguments
        t.getCommandLineArgs(args);

        t.setupLogging();

        try {
            t.processProfile();
        }
        catch (Exception e) {
            logger.error("Fatal error", e);
        }
    }

    private void processProfile() throws Exception {
        Reader in = new BufferedReader(new FileReader(inFilename));
        Writer out = new BufferedWriter(new FileWriter(outFilename));
        process(in, out);
        out.close();
        in.close();
    }

    /**
     * process the specified profile, using the given identifier mapping. filtering for
     * missing data, and averaging of repeated identifiers is applied.
     * 
     * @param in
     * @param out
     * @param synonymSource
     * @throws Exception
     */
    public void process(Reader in, Writer out) throws IOException, ApplicationException {
        if ( synonyms.size() == 0 ) {
            if ( synFilename == null ){
                throw new ApplicationException("Please pass in the location of the identifier" +
                " mapping file with -syn");
            }
            Reader synonymSource = new BufferedReader(new FileReader(synFilename));
            synonyms = FileUtils.loadSynonyms(synonymSource, synSepChar, synIdColumn, synNameColumn, true);
        }

        if (profileType.toLowerCase().startsWith(CONTINUOUS)) {
            load(in, sepChar);
        }
        else if (profileType.toLowerCase().startsWith(BINARY)) {
            loadSparse(in, sepChar);
        }
        else if (profileType.toLowerCase().startsWith(NETWORK)) {
            loadNetwork(in, sepChar);
        }
        else {
            throw new ApplicationException("Unknown profile type: " + profileType);
        }

        if (progress.isCanceled()) {
            throw new CancellationException();
        }

        logger.info("computing network");
        long t1 = (new Date()).getTime();
        network = convertProfileToNetwork();
        long t2 = (new Date()).getTime();
        logger.info("Total genes in resulting network (after filtering/averaging): " + network.numRows());
        logger.info("Time to compute network from profile (ms): " + (t2-t1));

        if (progress.isCanceled()) {
            throw new CancellationException();
        }

        PrintWriter writer = new PrintWriter(out);
        int totalInteractions = dump(writer);
        logger.info("Total #interactions in network (including symmetric interactions): " + totalInteractions);
        logger.info(String.format("network sparsity: %.2f%%", (totalInteractions*100d)/(network.numRows()*network.numColumns())));
        logger.info("done");
    }

    /**
     * Creates a ProfileData from source with delim used as delimiters
     * Filters out gene if it's missing too many feature values
     *
     * @throws Exception IOException
     */
    public void load(Reader source, char delim) throws IOException {
        CSVReader reader = new CSVReader(source, delim);
        String[] nextLine;

        List<Vector> geneExpressions = new ArrayList<Vector>(MIN_NUMBER_OF_GENES);
        List<String> geneList = new ArrayList<String>(MIN_NUMBER_OF_GENES);

        String[] header = null;
        int numFields = -1;
        int rowNum; // TODO: tidy this header skipping ...
        if (!noHeader) {
            rowNum = 0;
        }else{
            rowNum = 1;
        }

        while ((nextLine = reader.readNext()) != null) {
            if (rowNum == 0) {
                header = nextLine;
                numFields = header.length;
                rowNum++;
                logLine("skipping header", header);
            }
            else {
                logLine("record " + rowNum, nextLine);
                if (numFields < 0) {
                    numFields = nextLine.length;
                }
                String geneName = nextLine[0].trim().toUpperCase();

                if (geneName == null) {
                    logger.info("skipping null key");
                }				
                else if (!synonyms.containsKey(geneName)) {
                    logger.info("name not found in identifer table (skipping): " + geneName);					
                }
                else {
                    double [] values = new double[nextLine.length-1];
                    int zeroCounts = 0;

                    // load up values for the row, keep track of
                    // the number of missing samples encountered
                    for (int i=1; i<nextLine.length; i++) {
                        double val;
                        try {
                            val = Double.parseDouble(nextLine[i]);
                            // note that NaN parses as a double without
                            // exception so check here
                            if (Double.isNaN(val)) {
                                zeroCounts++;
                            }
                            // this one is odd, we don't have special
                            // handling elsewhere for inf, if this comes
                            // up lets force it to NaN so its treated the
                            // same way (ie not counted in the normalization,
                            // and set to zero after so as not to figure into
                            // the correlation measure)
                            else if (Double.isInfinite(val)) {
                                val = Double.NaN;
                                zeroCounts++;
                            }
                        }
                        catch (NumberFormatException e) {
                            val = Double.NaN;
                            zeroCounts++;
                        }
                        values[i-1] = val;
                    }

                    // line lengths should be constant: TODO: check

                    // only create a row for this gene if it has enough feature values
                    if ( ((double) zeroCounts / values.length * 100 ) < this.maxMissingPercentage ){
                        String identifier = synonyms.get(geneName);

                        List<Integer> index = identifiers.get(identifier);
                        if ( index == null ){
                            index = new ArrayList<Integer>();
                            identifiers.put(identifier, index);
                        }
                        index.add( rowNum - 1 );

                        geneList.add( geneName );

                        Vector expressionLevels = new DenseVector(values);
                        geneExpressions.add(rowNum - 1, expressionLevels);
                        rowNum++;
                    } else {
                        logger.info( geneName + " filtered out");
                    }
                }
            }
        }
        int rows = geneExpressions.size(); // # genes
        int cols = numFields - 1; // # features

        logger.info("total gene records: " + rows);
        logger.info("total features: " + cols);

        this.profile = new ProfileData( geneExpressions, geneList );
    }

    public void loadSparse(Reader source, char delim) throws IOException {
        CSVReader reader = new CSVReader(source, delim);
        String[] nextLine;

        List<Vector> geneExpressions = new ArrayList<Vector>(MIN_NUMBER_OF_GENES);
        List<String> geneList = new ArrayList<String>(MIN_NUMBER_OF_GENES);

        int rowNum;
        int nextFeatureId = 0;
        HashMap<String, Integer> featureNameToIdMap = new HashMap<String, Integer>();
        List<Integer[]> allRows = new ArrayList<Integer[]>(MIN_NUMBER_OF_GENES);
        if (!noHeader) {
            rowNum = 0;
        }else{
            rowNum = 1;
        }

        while ((nextLine = reader.readNext()) != null) {
            if (rowNum == 0) {
                //header
            }
            else {
                String geneName = nextLine[0].toUpperCase().trim();
                String identifier = synonyms.get(geneName);

                List<Integer> index = identifiers.get(identifier);
                if ( index == null ){
                    index = new ArrayList<Integer>();
                    identifiers.put(identifier, index);
                }
                index.add( rowNum - 1 );

                geneList.add(geneName);
                ArrayList<Integer> indices = new ArrayList<Integer>( (nextLine.length - 1) / 2 );

                // load up values for the row
                for (int i = 1; i < nextLine.length; i++) {
                    String featureName = nextLine[i];
                    if (featureNameToIdMap.containsKey(featureName)) {
                        indices.add( featureNameToIdMap.get(featureName) );
                    }
                    else {								
                        featureNameToIdMap.put(featureName, nextFeatureId);
                        indices.add(nextFeatureId);
                        nextFeatureId += 1;
                    }
                }
                Collections.sort(indices);
                allRows.add( indices.toArray(new Integer[0]) );
            }
            rowNum++;
        }

        for ( Integer[] a: allRows ){
            int[] index = new int[a.length];
            double[] data = new double[a.length];
            for (int i = 0; i < data.length; i++){
                data[i] = 1;
                index[i] = a[i];
            }

            Vector expressionLevels = new SparseVector(nextFeatureId, index, data);
            geneExpressions.add(expressionLevels);
        }

        int rows = geneExpressions.size(); // # genes
        int cols = nextFeatureId - 1; // # features

        logger.info("total gene records: " + rows);
        logger.info("total features: " + cols);

        this.profile = new ProfileData( geneExpressions, geneList );
    }

    /*
     * too lazy to convert the network to our profile data structure,
     * instead, convert it to a sparse feature profile in memory,
     * then pass it on through to loadSparse
     */
    public void loadNetwork(Reader source, char delim) throws IOException {
        CSVReader reader = new CSVReader(source, delim);
        String[] nextLine;

        // each gene maps to a set of genes that are its neighbors
        HashMap<String, HashSet<String>> features = new HashMap<String, HashSet<String>>();

        int nextFeatureId = 0;
        if (!noHeader) {
            reader.readNext();
        }

        while ((nextLine = reader.readNext()) != null) {

            String gene1 = nextLine[0].toUpperCase().trim();
            String gene2 = nextLine[1].toUpperCase().trim();

            // add gene2 as gene1's partner
            if (features.containsKey(gene1)) {
                HashSet set = features.get(gene1);
                set.add(gene2);
            }
            else {
                HashSet<String> set = new HashSet<String>();
                set.add(gene2);
                features.put(gene1, set);
            }

            if (features.containsKey(gene2)) {
                HashSet set = features.get(gene2);
                set.add(gene1);
            }
            else {
                HashSet<String> set = new HashSet<String>();
                set.add(gene1);
                features.put(gene2, set);
            }
        }
        // build out a sparse profile, and use the existing
        // sparse loader to pull it in
        StringBuilder builder = new StringBuilder();
        if (!noHeader) {
            builder.append("header\n");
        }
        for (String gene: features.keySet()) {
            HashSet<String> set = features.get(gene);
            builder.append(gene);
            for (String feature: set) {
                builder.append(delim);
                builder.append(feature);
            }
            builder.append("\n");
        }
        StringReader stringReader = new StringReader(builder.toString());

        loadSparse(stringReader, delim);
    }

    /**
     * Computes the correlation for the profile, storing top k interactions for each gene by using KHeap.
     * The network is then created from the top k interactions for each gene. 
     * If there are synonyms/aliases for the genes, then a smaller network is created by 
     * averaging the identifiers that represent the same gene.
     * 
     * @return a Matrix of top k interactions for each gene
     */
    private Matrix convertProfileToNetwork() throws CancellationException {
        int numGene = profile.getGeneExpression().size();
        Matrix network = new FlexCompColMatrix( numGene, numGene );

        // initialize the KHeap
        KHeap[] topInteractions = new KHeap[numGene];
        for ( int i = 0; i < numGene; i++ ) {
            topInteractions[i] = new KHeap(k, keepAllTies);
        }

        MutualInformationData MIData = null;
        if ( correlationType == CorrelationType.MUTUAL_INFORMATION ){
            if ( profileType.toLowerCase().startsWith(CONTINUOUS) ){
                MIData = new MutualInformationData(true);
                MIData.setBinningInfo(equalElementBin, sizeType);
            } else {
                // no binning needed for binary profiles
                MIData = new MutualInformationData(false);
            }
        }

        // Get the correlation we're interested in using, i.e. Pearson, Spearman
        Correlation cor = CorrelationFactory.getCorrelation( correlationType, MIData );
        logger.info("Initializing correlation with profile");
        long t1 = System.currentTimeMillis();
        cor.init( profile );
        long t2 = System.currentTimeMillis();
        logger.info("Correlation initialized. Total time: " + (t2-t1));
        
        
        final double thresholdValue = cor.getThresholdValue();
        final boolean isThresholdEnabled = THRESHOLD_AUTO.equalsIgnoreCase(getThreshold());
        if (isThresholdEnabled) {
            logger.info("thresholding enabled at " + thresholdValue);
        }
        
        t1 = System.currentTimeMillis();

        Collection<List<Integer>> indices = identifiers.values();
        List<Integer>[] indices_arr = indices.toArray(new ArrayList[0]);
        for ( int i = 0; i < indices_arr.length; i++ ){
            List<Integer> i_indices = indices_arr[i];
            for ( int j = i + 1; j < indices_arr.length; j++ ){
                double totalCorrelation = 0;
                List<Integer> j_indices = indices_arr[j];

                for ( int i1 : i_indices ){

                    if (progress.isCanceled()) {
                        throw new CancellationException();
                    }

                    for ( int j1 : j_indices ){
                        double correlation = cor.computeCorrelations(i1, j1);
                        totalCorrelation += correlation;
                    }
                }

                // average it
                totalCorrelation = totalCorrelation / (i_indices.size() * j_indices.size());

                // record only if correlation is greater than threshold
                if (isThresholdEnabled && totalCorrelation <= thresholdValue) {
                    // don't need these, log?
                }
                else {
                    topInteractions[i_indices.get(0)].offer(j_indices.get(0), totalCorrelation);
                    topInteractions[j_indices.get(0)].offer(i_indices.get(0), totalCorrelation);
                }
            }
        }

        // control sparsity when keeping ties by dropping the
        // last level if things get too big
        if (keepAllTies && limitTies) {
            levelControl(topInteractions);
        }
        
        //builds the network from the KHeap
        for ( int i = 0; i < numGene; i++ ) {
            int n = topInteractions[i].size();

            for (int j = 0; j < n; j++) {
                int i2 = (int) topInteractions[i].getId(j);
                double weight = topInteractions[i].getWeight(j);
                network.set(i, i2, weight);
            }
        }

        // ensure symmetric
        network = MatrixUtils.computeMaxTranspose(network);

        t2 = System.currentTimeMillis();

        logger.info("Total time for calculating: " + (t2-t1) );
        return network;
    }

    /*
     * when the top-interactions heaps are in extensible mode for
     * the last (lowest = weakest) interaction in the heap, we still
     * want to be careful not to impact the desired sparsity too much.
     * if the total # of interactions is large, we drop the last level
     * in the heap.
     * 
     * for too large we use 2% of the # of genes in the organism, up
     * to a maximum of 600 (tunable).
     * 
     */
    static final double MAX_PERCENTAGE = .02;
    static final int MAX_ALLOWED_INTERACTIONS_INCLUDING_LEVEL_MATCH = 600;
    void levelControl(KHeap[] topInteractions) {
        
        int numGenes = getNumGenes();
        int sizeLimit = Math.max(k, Math.min((int)(MAX_PERCENTAGE*numGenes), MAX_ALLOWED_INTERACTIONS_INCLUDING_LEVEL_MATCH));
        logger.debug("level control at size limit of " + sizeLimit);
        for (KHeap heap: topInteractions) {
            if (heap.size() > sizeLimit) {
                double lastLevel = heap.getWeight(0);
                int sizeBefore = heap.size();
                heap.popLE(lastLevel);
                logger.debug("trimmed heap from " + sizeBefore + " to " + heap.size());
            }            
        }        
    }
    
    /*
     * oh, we aren't keeping track of the mapping in a way
     * where its convenient to get the total number of unique 'nodes'. 
     * should really revise the data structure. but just compute here
     * for now.
     */
    int getNumGenes() {
        Set<String> uniqueIds = new HashSet<String>();
        uniqueIds.addAll(synonyms.values());        
        return uniqueIds.size();        
    }
    
    /**
     * Write out a network in the format
     * 
     *   GeneSymbol GeneSymbol Weight
     *   GeneSymbol GeneSymbol Weight
     *   ...
     *   
     * @param network
     * @param genes
     * @param writer
     */
    public int dump(PrintWriter writer) {
        DecimalFormat df = new DecimalFormat("#.#####");
        int n = 0;
        for (MatrixEntry e: network) {
            if (e.get() != 0d && e.row() != e.column()) {
                String line = profile.getGeneName().get(e.row()) + "\t" + profile.getGeneName().get(e.column()) + "\t" + df.format( e.get() );
                writer.println(line);
                n++;
            }
        }

        return n;
    }

    private boolean getCommandLineArgs(String [] args) {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            System.err.println("java -jar myprogram.jar [options...] arguments...");
            parser.printUsage(System.err);
            return false;
        }

        return true;	    
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
        logger.setLevel((Level) Level.DEBUG);		
    }

    public void setK(int k){
        this.k = k;
    }

    public void setSepChar(char sepChar){
        this.sepChar = sepChar;
    }

    public void setCorrelationType(CorrelationType corType){	
        this.correlationType = corType;
    }

    public void setEqualElementBin(boolean equalElementBin){
        this.equalElementBin = equalElementBin;
    }

    public void setSizeType(SizeType sizeType){
        this.sizeType = sizeType;
    }

    public void setProfileType(String profileType){
        this.profileType = profileType;
    }

    public void setMaxMissingPercentage(double missingPercentage){
        this.maxMissingPercentage = missingPercentage;
    }

    public void setSynSepChar( char synSepChar ){
        this.synSepChar = synSepChar;
    }

    public void setSynIdColumn( int col ){
        this.synIdColumn = col;
    }

    public void setSynNameColumn( int col ){
        this.synNameColumn = col;
    }

    public void setSynReader(Reader synReader) throws IOException {
        this.synonyms = FileUtils.loadSynonyms(synReader, synSepChar, synIdColumn, synNameColumn, true);
    }

    public boolean isNoHeader() {
        return this.noHeader;
    }

    public void setNoHeader(boolean val) {
        this.noHeader = val;
    }

    public void setProgressReporter(ProgressReporter progress) {
        this.progress = progress;
    }

    public ProgressReporter getProgressReporter() {
        return this.progress;
    }

    public String getThreshold() {
        return threshold;
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }

    public boolean isKeepAllTies() {
        return keepAllTies;
    }

    public void setKeepAllTies(boolean keepAllTies) {
        this.keepAllTies = keepAllTies;
    }
    
    public boolean isLimitTies() {
        return limitTies;
    }

    public void setLimitTies(boolean limitTies) {
        this.limitTies = limitTies;
    }

    /*
     * debugging
     */
    private void logLine(String msg, String [] line) {
        return;
        //            logger.debug(msg);
        //            logger.debug("# fields: " + line.length);
        //            for (int i=0; i<line.length; i++) {
        //                logger.debug(String.format("   field %d: >%s<", i, line[i]));
        //            }
    }
}