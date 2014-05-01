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
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.VectorEntry;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.genemania.domain.AttributeGroup;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.NetworkMetadata;
import org.genemania.domain.Organism;
import org.genemania.engine.Constants;
import org.genemania.engine.apps.support.LabelWriter;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.cache.FileSerializedObjectCache;
import org.genemania.engine.cache.MemObjectCache;
import org.genemania.engine.cache.SynchronizedObjectCache;
import org.genemania.engine.config.Config;
import org.genemania.engine.core.MatrixUtils;
import org.genemania.engine.core.data.GoAnnotations;
import org.genemania.engine.core.data.GoIds;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.engine.core.mania.CoreMania;
import org.genemania.engine.matricks.Matrix;
import org.genemania.engine.utils.FileUtils;
import org.genemania.engine.validation.AucPr;
import org.genemania.engine.validation.AucRoc;
import org.genemania.engine.validation.EvaluationMeasure;
import org.genemania.engine.validation.PrecisionFixedRecall;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Port of VectorCrossValidator to updated datacache.
 * 
 * Performs cross-validation. Wikipedia gives a good background on 
 * cross-validation. Right now, measures from folds with 0 positives 
 * are still factored into the average measures.
 * 
 * TODO: verify that reading positives and negatives from query 
 * files (i.e. -all_neg_cross_validation is NOT provided) works.
 * 
 * TODO: NovelTester, VectorCrossValidator and VectorPredictor share many similar methods 
 * which can probably be moved to a separate like ValidationUtils or something. Note that 
 * some methods have the same name and are very similar but are also customized for that 
 * particular class.
 */
public class VectorCrossValidator extends AbstractEngineApp {

    public static final double[] SKIPPED = new double[0];
    
    private static Logger logger = Logger.getLogger(VectorCrossValidator.class);


    @Option(name = "-method", usage = "network combination method, should be one of 'equal' or 'smart'")
    private String combiningMethodName;

    @Option(name = "-organism", usage = "organism name, eg 'Home Sapiens', if -orgid not given")
    private String organismName;

    @Option(name = "-orgid", usage = "organism id")
    private long organismId = -1; // -1 means unspecified

    @Option(name = "-qfile", usage = "file containing gene queries")
    private String queryFileName;

    @Option(name = "-out", usage = "name of output file to contain validation results")
    private String outFilename;

    @Option(name = "-numfolds", usage = "number of folds to use for each query, defaults to 5")
    private int numFolds = 5;

    @Option(name = "-recallpoint", usage = "computes precision at the given recall. E.g. -recallPoint 10 computes precision at 10% recall")
    private String recallPoint;

    @Option(name = "-biasing_method", usage = "biasing method, defaults to average")
    private String biasingMethod = "average";
    
    @Option(name = "-all_neg_cross_validation", usage = "use all genes less positives as negatives")
    private boolean allNegCrossVal;

    @Option(name = "-useCachedGoAnnos", usage = "use only a go category id 'GO:001234' from each line, and lookup the +ve annotations from engine cache files. this forces all other genes to be negatives (allnegcrossval)")
    private boolean useCachedGoAnnos;
    
    @Option(name = "-netids", usage = "comma delim list of network ids to use eg '3,4,19', or 'all', or 'default', or 'preferred' for our selection heuristic.")
    private String networkIdsList;

    @Option(name = "-attrIds", usage = "comma delim list of attribute group ids.")
    private String attrIds;
    
    @Option(name = "-dump", usage = "optional, one of 'organisms' or 'networks'. causes program to dump ids & names and exit without executing any queries")
    private String dumpType;

    @Option(name = "-seed", usage = "optional, random seed to use when generating cross-validation folds, 0 (default) will select a seed based on system time")
    private long seed;

    @Option(name = "-threads", usage = "optional, total threads to use for parallel prediction, defaults to 1")
	private int totalThreads = 1;
    
    // TODO
    //@Option(name = "-strat", usage = "optional, compute stratified cross-validation folds. defaults fo false")
    //private boolean stratifiedFolds = false;

    @Option(name = "-label", usage = "optional, output labels to file. defaults to false")
    private boolean writeLabels = false;

    // when doing preferred network selection, the min # of interactions
    // the network must contain
    private static final long INTERACTION_COUNT_THRESHOLD = 1000;

    // when doing preferred network selection, only networks with the following
    // codes will be used
    private static String [] preferredGroupCodes = {"coexp", // co-expression
                                                    "pi",    // physical interaction
                                                    "gi"     // genetic interaction
                                                    };
    private Organism organism;
    private List<EvaluationMeasure> measures;
    private PrintWriter writer = null;
    private Collection<String[]> queries;
    private NodeIds nodeIds;
    private Collection<Collection<Long>> idList;
    private Collection<Long> attributeGroupIds;
    private DataCache cache;
    private Map<String, Integer> symbolToIndexCache = Collections.synchronizedMap(new HashMap<String, Integer>());

    // keep track of some totals, not for reporting but
    // just to facilitate regression testing
    private int queryCounter = 0;

    private final Object outputMutex = new Object();
	private String namespace;
    
    public long getOrganismId() {
        return organismId;
    }

    public void setOrganismId(long organismId) {
        this.organismId = organismId;
    }

    public String getNetworkIdsList() {
        return networkIdsList;
    }

    public void setNetworkIdsList(String networkIdsList) {
        this.networkIdsList = networkIdsList;
    }

	public String getAttrIdsList() {
        return attrIds;
    }

    public void setAttrIdsList(String attrIds) {
        this.attrIds = attrIds;
    }

    public Collection<Long> getAttrIds() {
    	return attributeGroupIds;
    }
    
    public void setAttrIds(Collection<Long> ids) {
    	attributeGroupIds = ids;
    }
    
    public void setNetworkIds(Collection<Collection<Long>> ids) {
		this.idList = ids;
	}
	
	public Collection<Collection<Long>> getNetworkIds() {
		return idList;
	}
	
    public String getBiasingMethod() {
        return biasingMethod;
    }

    public void setBiasingMethod(String biasingMethod) {
        this.biasingMethod = biasingMethod;
    }

    public String getCombiningMethodName() {
        return combiningMethodName;
    }

    public void setCombiningMethodName(String combiningMethodName) {
        this.combiningMethodName = combiningMethodName;
    }

    public String getLogFilename() {
        return logFilename;
    }

    public void setLogFilename(String logFilename) {
        this.logFilename = logFilename;
    }

    public String getOutFilename() {
        return outFilename;
    }

    public void setOutFilename(String outFilename) {
        this.outFilename = outFilename;
    }

    public String getQueryFileName() {
        return queryFileName;
    }

    public void setQueryFileName(String queryFileName) {
        this.queryFileName = queryFileName;
    }

    public String getRecallPoint() {
        return recallPoint;
    }

    public void setRecallPoint(String recallPoint) {
        this.recallPoint = recallPoint;
    }

    public DataCache getCache() {
        return cache;
    }

    public void setCache(DataCache cache) {
        this.cache = cache;
    }

    public int getQueryCounter() {
        return queryCounter;
    }

    public void setQueryCounter(int queryCounter) {
        this.queryCounter = queryCounter;
    }

    public int getNumFolds() {
        return numFolds;
    }

    public void setNumFolds(int numFolds) {
        this.numFolds = numFolds;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }
    
    public void setAllNegCrossVal(boolean value) {
    	allNegCrossVal = value;
    }
    
	public void setThreads(int threads) {
		totalThreads = threads;
	}
	
	public void setUseCachedGoAnnotations(boolean value) {
		useCachedGoAnnos = value;
	}
	
    public void initValidation() throws Exception {

        openOutput();

        logger.info("initializing...");

        cache = new DataCache(new SynchronizedObjectCache(new MemObjectCache(new FileSerializedObjectCache(getCacheDir()))));

        organism = getOrganism();

        if (idList == null) {
        	idList = getNetworkIdList(networkIdsList);
        }

        if (attributeGroupIds == null) {
            attributeGroupIds = getAttributeGroupIdsList(attrIds);
            logger.debug("intialized attributeGroupIds to " + attributeGroupIds);
        }

        logger.debug(String.format("regularization enabled: %s, constant: %f", Config.instance().isRegularizationEnabled(), Config.instance().getRegularizationConstant()));
        logger.debug(String.format("attribute pre-selection limit: %d", Config.instance().getAttributeEnrichmentMaxSize()));
        
        //initialize measures
        measures = new ArrayList<EvaluationMeasure>();
        measures.add(new AucRoc("AUC-ROC"));
        measures.add(new AucPr("AUC-PR"));

        double recall = 10;
        if (recallPoint != null) {
            try {
                recall = Double.parseDouble(recallPoint);
            }
            catch (NumberFormatException e) {
                logger.warn(e.getMessage());
                logger.info("setting recall point to 10%");
                recall = 10;
            }
        } else {
            recallPoint = "10";
        }
        measures.add(new PrecisionFixedRecall("PR-" + recallPoint, recall));

        writeHeader();
        loadQueries();

        // load node ids
        nodeIds = cache.getNodeIds(organism.getId());

        // seed random number generator
        if (seed == 0) {
            seed = System.currentTimeMillis();
        }
        logger.info("setting random seed to: " + seed);
    }

    private void writeHeader() {
        StringBuilder header = new StringBuilder("queryIdentifier\tfold #");
        String[] names = getMeasureNames();
        for (String name: names) {
            header.append("\t" + name);
        }
        header.append("\t#t(+)\t#t(-)\t#v(+)\t#v(-)");

        logger.info(header.toString());
        writeOutput(header.toString());
    }

    public void openOutput() throws IOException {
        if (outFilename != null) {
            logger.info("writing network to " + outFilename);
            writer = new PrintWriter(new File(outFilename));
        }
    }

    private void writeOutput(String msg) {
        if (writer != null) {
            writer.println(msg);
            writer.flush();
        }
    }

    public void writeResult(String queryIdentifier, int fold, int numPosT,
            int numNegT, int numPosV, int numNegV, double[] measures) {

        StringBuilder msg = new StringBuilder(String.format("%s\t%s",
                queryIdentifier, fold + 1));

        if (measures == SKIPPED) {
        	msg.append("\tskipped");
        } else {
	        for (double e: measures) {
	            msg.append("\t" + e);
	        }
	        msg.append(String.format("\t%s\t%s\t%s\t%s", numPosT, numNegT,
	                numPosV, numNegV));
        }
        
        logger.info(msg.toString());
        writeOutput(msg.toString());
    }

    public void writeResult(String queryIdentifier, double[] measures) {

        StringBuilder msg = new StringBuilder(
                String.format("%s\t-", queryIdentifier));

        if (measures == SKIPPED) {
        	msg.append("\tskipped");
        } else {
	        for (double e: measures) {
	            msg.append("\t" + e);
	        }
        }
        
        logger.info(msg.toString());
        writeOutput(msg.toString());
    }

    public String[] getMeasureNames() {
        String[] names = new String[measures.size()];
        for (int i = 0; i < measures.size(); i++) {
            names[i] = measures.get(i).getName();
        }

        return names;
    }

    /*
     * select the set of networks to be used for validation
     */
    private Collection<Collection<Long>> getNetworkIdList(String ids) throws ApplicationException, DataStoreException {

        if (ids.equalsIgnoreCase("all")) {
            return getAllNetworks();
        }
        else if (ids.equalsIgnoreCase("preferred")) {
            return getPreferredNetworks();
        }
        else if (ids.equalsIgnoreCase("default")) {
            return getDefaultNetworks();
        }
        else {
            return getNetworksById(ids);
        }

    }
    
    private Collection<Long> getAttributeGroupIdsList(String ids) throws ApplicationException {
        return getAttributeGroupsById(ids);
    }

    /*
     * return list of all networks associated with the organism in the db,
     * organized by group
     */
	private Collection<Collection<Long>> getAllNetworks() {
        Collection<InteractionNetworkGroup> groups = organism.getInteractionNetworkGroups();
        int numFound = 0;
        
        Collection<Collection<Long>> ids = new ArrayList<Collection<Long>>();
        for (InteractionNetworkGroup group: groups) {

            Collection<InteractionNetwork> networks = group.getInteractionNetworks();
            List<Long> list = new ArrayList<Long>();

            for (InteractionNetwork n: networks) {
                list.add(n.getId());
                numFound += 1;
            }

            if (list.size() > 0) {
                ids.add(list);
            }
        }

        logger.info(String.format("total %d networks selected", numFound));
        return ids;
    }

    /*
     * given string of comma delimited ids (no space), validate against networks in the db,
     * and return grouped by the corresponding network groups.
     *
     * you would think we could just do networkMediator.getNetwork(id) to do the lookups, but
     * i don't see how you can get to the network groups this way. so instead we actually iterate
     * through all the network groups and search each (argh!)
     */
	private Collection<Collection<Long>> getNetworksById(String idsArg) throws ApplicationException, DataStoreException {
        String [] parts = idsArg.split(",");

        HashSet<String> wantedIds = new HashSet<String>();
        wantedIds.addAll(Arrays.asList(parts));
        int numFound = 0;

        Collection<InteractionNetworkGroup> groups = organism.getInteractionNetworkGroups();

        Collection<Collection<Long>> ids = new ArrayList<Collection<Long>>();
        for (InteractionNetworkGroup group: groups) {

            Collection<InteractionNetwork> networks = group.getInteractionNetworks();
            List<Long> list = new ArrayList<Long>();

            for (InteractionNetwork n: networks) {
                NetworkMetadata metadata = n.getMetadata();
                String key = "" + n.getId();

                if (wantedIds.contains(key)) {

                    logger.info(String.format("using network %d containing %d interactions from group %s: %s",
                            n.getId(), metadata.getInteractionCount(), group.getName(), n.getName()));
                    
                    list.add(n.getId());
                    numFound += 1;
                }
            }

            if (list.size() > 0) {
                ids.add(list);
            }
        }

        if (numFound != parts.length) {
            throw new ApplicationException("some of the specified networks could not be found");
        }
        
        logger.info(String.format("total %d networks selected", numFound));
        return ids;
    }

	private Collection<Long> getAttributeGroupsById(String idsArg) throws ApplicationException {

	    Collection<Long> ids = new ArrayList<Long>();

	    if (idsArg != null) {
	        String [] parts = idsArg.split(",");
	        for (String part: parts) {
	            long attributeGroupId = Long.parseLong(part);
	            AttributeGroup attributeGroup = getAttributeMediator().findAttributeGroup(organismId, attributeGroupId);
	            if (attributeGroup == null) {
	                throw new ApplicationException("unrecognized attribute group id: " + attributeGroupId);
	            }
	            ids.add(attributeGroupId);
	        }
	    }

	    return ids;
	}

    /*
     * selection of preferred networks for validation:
     *
     *   * network must contain at least INTERACTION_COUNT_THRESHOLD interactions (eg 1000)
     *     to prevent inclusion of small-scale studies networks that apparently have circularity issues
     *     with our GO annotions
     *   * network must belong to one of our preferred groups (checked by group code defined in
     *     the master db.cfg)
     */
	private Collection<Collection<Long>> getPreferredNetworks() throws ApplicationException, DataStoreException {

        Set<String> preferredGroupSet = new HashSet<String>();
        preferredGroupSet.addAll(Arrays.asList(preferredGroupCodes));
        int numFound = 0;

        Collection<InteractionNetworkGroup> groups = organism.getInteractionNetworkGroups();

        Collection<Collection<Long>> ids = new ArrayList<Collection<Long>>();
        for (InteractionNetworkGroup group: groups) {

            if (!preferredGroupSet.contains(group.getCode())) {
                logger.debug("skipping all networks in group since not preferred: " + group.getName() + " " + group.getCode());
                continue;
            }
            
            Collection<InteractionNetwork> networks = group.getInteractionNetworks();
            List<Long> list = new ArrayList<Long>();

            for (InteractionNetwork n: networks) {
                NetworkMetadata metadata = n.getMetadata();
                if (metadata.getInteractionCount() > INTERACTION_COUNT_THRESHOLD) {

                    logger.info(String.format("using network %d containing %d interactions from group %s: %s",
                            n.getId(), metadata.getInteractionCount(), group.getName(), n.getName()));

                    list.add(n.getId());
                    numFound += 1;
                }
            }

            if (list.size() > 0) {
                ids.add(list);
            }
        }

        if (ids.size() == 0) {
            throw new ApplicationException("no preferred networks found!");
        }

        logger.info(String.format("total %d networks selected", numFound));
        return ids;        
    }

    /*
     * select networks marked as default for the organism in the db. walk the
     * entire network tree for the org to get the grouping right ...
     *
     */
	private Collection<Collection<Long>> getDefaultNetworks() throws ApplicationException, DataStoreException {

        int numFound = 0;

        Collection<InteractionNetworkGroup> groups = organism.getInteractionNetworkGroups();

        Collection<Collection<Long>> ids = new ArrayList<Collection<Long>>();
        for (InteractionNetworkGroup group: groups) {


            Collection<InteractionNetwork> networks = group.getInteractionNetworks();
            List<Long> list = new ArrayList<Long>();

            for (InteractionNetwork n: networks) {
                if (n.isDefaultSelected()) {
                NetworkMetadata metadata = n.getMetadata();
                
                    logger.info(String.format("using default network %d containing %d interactions from group %s: %s",
                            n.getId(), metadata.getInteractionCount(), group.getName(), n.getName()));

                    list.add(n.getId());
                    numFound += 1;
                }
            }

            if (list.size() > 0) {
                ids.add(list);
            }
        }

        if (ids.size() == 0) {
            throw new ApplicationException("no default networks found!");
        }

        logger.info(String.format("total %d networks selected", numFound));
        return ids;
    }

    private void dump(String option) throws ApplicationException, DataStoreException {
        if (option.equalsIgnoreCase("organisms")) {
            dumpOrganisms();
        }
        else if (option.equalsIgnoreCase("networks")) {
            dumpNetworks();
        }
        else {
            throw new ApplicationException("unknown dump option: " + option);
        }
    }
    
	private void dumpNetworks() throws ApplicationException, DataStoreException {

        Organism organism = getOrganism();

        Collection<InteractionNetworkGroup> groups = organism.getInteractionNetworkGroups();

        for (InteractionNetworkGroup group: groups) {

            Collection<InteractionNetwork> networks = group.getInteractionNetworks();
            for (InteractionNetwork n: networks) {
                NetworkMetadata metadata = n.getMetadata();

                System.out.println(String.format("network %d contains %d interactions from group %s: %s",
                        n.getId(), metadata.getInteractionCount(), group.getName(), n.getName()));

            }
        }
    }

    private void dumpOrganisms() throws DataStoreException {
        List<Organism> organisms = organismMediator.getAllOrganisms();
        for (Organism o: organisms) {
            System.out.println(String.format("%d: %s", o.getId(), o.getName()));
        }
    }
    
    public void setupLogging() throws Exception {
        if (logFilename == null) {
            return;
        }

        PatternLayout layout = new PatternLayout("%d{HH:mm:ss} %-5p: %m%n");
        Appender appender = new FileAppender(layout, logFilename, false);

        Logger.getLogger("org.genemania").setLevel((Level) Level.DEBUG);
        Logger.getRootLogger().addAppender(appender);
    }

    public boolean getCommandLineArgs(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        }
        catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java -jar myprogram.jar [options...] arguments...");
            parser.printUsage(System.err);
            return false;
        }
        return true;
    }

    private Organism getOrganism() throws ApplicationException, DataStoreException {

        Organism organism;
        if (organismId != -1) {
            organism = getOrganismById(organismId);
        }
        else if (organismName != null) {
            organism = getOrganismByName(organismName);
        }
        else {
            throw new ApplicationException("organism not specified");
        }

        logger.info("quering organism: " + organism.getName());
        return organism;

    }

    private Organism getOrganismByName(String name) throws ApplicationException, DataStoreException {
        List<Organism> organisms = organismMediator.getAllOrganisms();
        Organism organism = null;
        for (Organism o: organisms) {
            if (o.getName().equalsIgnoreCase(organismName)) {
                organism = o;
            }
        }

        if (organism == null) {
            throw new ApplicationException("Failed to find organism " + organismName);
        }

        return organism;        
    }

    private Organism getOrganismById(long organismId) throws ApplicationException, DataStoreException {
        Organism organism = organismMediator.getOrganism(organismId);
        return organism;
    }

    public void loadQueries() throws IOException {
        Reader reader = new BufferedReader(new FileReader(queryFileName));
        queries = FileUtils.loadRecords(reader, '\t', '\t');
        reader.close();
    }

    private Constants.CombiningMethod getCombiningMethod(String[] queryRecord) throws ApplicationException {

        Constants.CombiningMethod combiningMethod = null;
        if ("auto_detect_category".equalsIgnoreCase(combiningMethodName)) {
            throw new ApplicationException("auto_detect_category not implemented");
//            //retrieve information from cache
//            Map<String, List<String>> goBranchMap = cache.getGOBranchMap((int) organism.getId());
//
//            for (String key: goBranchMap.keySet()) {
//                if (goBranchMap.get(key).contains(queryRecord[0])) {
//                    combiningMethod = Mania.getCombiningMethod(key);
//                    logger.info("using combining method " + key);
//                    break;
//                }
//            }
//
//            if (combiningMethod == null) {
//                logger.info("could not detect appropriate combining category, using smart");
//                combiningMethod = Mania.getCombiningMethod("smart");
//            }

        }
        else {
            combiningMethod = Constants.getCombiningMethod(combiningMethodName);
        }
        return combiningMethod;
    }

    private double[] getMeasureResults(no.uib.cipr.matrix.Vector initialLabel,
            no.uib.cipr.matrix.Vector discriminant, Collection<Integer> excludedRowIndices) {

        int n = excludedRowIndices.size();
        double[] scores = new double[n];
        boolean[] classes = new boolean[n];

        int i = 0;
        for (Integer index: excludedRowIndices) {
            scores[i] = discriminant.get(index);
            classes[i] = initialLabel.get(index) == 1;
            i++;
        }

        return calculateMeasureResults(measures, scores, classes);
    }

    private double[] calculateMeasureResults(Collection<EvaluationMeasure> measures, double[] scores, boolean[] classes) {
        double[] results = new double[measures.size()];
        int i = 0;
        // pass the data into each evaluation measure to compute the result and get the result back
        for (EvaluationMeasure measure: measures) {
            results[i] = measure.computeResult(classes, scores);
            i++;
        }

        return results;
    }

    /*
     * this assumes that the combining method and network list is constant
     * for all invocations, and checks for average combining being used,
     * in which case the average is only computed once and saved for
     * future calls! Otherwise the weights are computed for each call.
     */
    private void crossValidateVector(CoreMania coreMania,
            no.uib.cipr.matrix.Vector initialLabel,
            Constants.CombiningMethod method, String goCategory,
            double[] averageMeasures, int k, int [] allPerm) throws ApplicationException {

        //copy initial vector
        Vector label = new DenseVector(initialLabel);

        Collection<Integer> excludedRowIndices = new ArrayList<Integer>();
        Collection<Integer> includedRowIndices = new ArrayList<Integer>();

        //TODO: need to make different fold size for different negative set
        double foldSize = allPerm.length * 1.0 / numFolds;
        int firstIndex = (int) Math.ceil(k * foldSize);
        int lastIndex = (int) Math.ceil((k + 1) * foldSize) - 1;

        // included portion
        for (int i = 0; i < firstIndex; i++) {
            includedRowIndices.add(allPerm[i]);
        }
        for (int i = lastIndex + 1; i < allPerm.length; i++) {
            includedRowIndices.add(allPerm[i]);
        }

        int numPosIncluded = 0;
        int numNegIncluded = 0;
        for (int index: includedRowIndices) {
        	double value = initialLabel.get(index);
            if (value == 1) {
                numPosIncluded++;
            } else if (value == -1) {
                numNegIncluded++;
            }
        }

        // excluded portion
        int numPos = 0;
        int numNeg = 0;
        for (int j = firstIndex; j <= lastIndex; j++) {
            excludedRowIndices.add(allPerm[j]);
            label.set(allPerm[j], Constants.EXCLUDED_ROW_VALUE);
            double value = initialLabel.get(allPerm[j]);
            if (value == 1) {
                numPos++;
            }
            else if (value == -1) {
                numNeg++;
            }
        }

        logger.info(MatrixUtils.countMatches(label, Constants.EXCLUDED_ROW_VALUE) + " unknowns in label");

        checkLabels(label);

        // If a weighting method isn't specific to a particular gene list, don't
        // recompute the weights for each fold.
        if (!method.isQuerySpecific()) {
        	synchronized(coreMania) {
	            if (coreMania.getCombinedKernel(organism.getId(), namespace) == null) {
	                logger.info("computing weights since none saved");
	                coreMania.computeWeights(namespace, organism.getId(), label, method, idList, attributeGroupIds, Config.instance().getAttributeEnrichmentMaxSize()); // TODO: make attribute limit configurable by user input
	            }
	            else {
	                logger.info("reusing weights");
	            }
        	}
        }
        else {
            coreMania = new CoreMania(cache);
            coreMania.computeWeights(namespace, organism.getId(), label, method, idList, attributeGroupIds, Config.instance().getAttributeEnrichmentMaxSize()); // TODO: make attribute limit configurable by user input);
        }

        // label propagation
        coreMania.computeDiscriminant(namespace, organism.getId(), label, goCategory, biasingMethod);

        //coreMania.compute(organism.getId(), label, method, idList, mapping, goCategory, biasingMethod);

        double[] measures = getMeasureResults(initialLabel, coreMania.getDiscriminant(), excludedRowIndices);
        for (int i = 0; i < measures.length; i++) {
            averageMeasures[i] += measures[i];
        }

        writeResult(goCategory, k, numPosIncluded, numNegIncluded, numPos, numNeg, measures);
        
        if (writeLabels) {
            LabelWriter writer = new LabelWriter(outFilename, nodeMediator, organismId);
            writer.write(goCategory, k, initialLabel, coreMania.getDiscriminant(), excludedRowIndices, nodeIds);
        }
    	queryCounter += 1;
    }

    /*
     * reloading those cache files everytime is a waste! TODO fix.
     */
    private Vector loadAnnosFromCache(String goCategory) throws ApplicationException {
        logger.info("loading annotations for " + goCategory + " from cache");
    	long organismId = organism.getId();
    	GoIds goIds = cache.getGoIds(organismId, Constants.ALL_ONTOLOGY);
    	int goIndex = goIds.getIndexForId(goCategory);
    	GoAnnotations annotations = cache.getGoAnnotations(organismId, Constants.ALL_ONTOLOGY);
    	Matrix data = annotations.getData();
    	
    	Vector label = new DenseVector(data.numRows());
    	for (int i = 0; i < label.size(); i++) {
    		if (data.get(i, goIndex) == 1) {
    			label.set(i, 1);
    		} else {
    			label.set(i, -1);
    		}
    	}
    	return label;
    }

    public Map<String,double[]> crossValidate() throws Exception {
    	// Set up task queue; one task per query
        final List<ValidationTask> tasks = new ArrayList<ValidationTask>();
        for (String[] queryRecord : queries) {
        	tasks.add(new ValidationTask(queryRecord, seed));
        }
        
        final Iterator<ValidationTask> jobQueue = tasks.iterator();
        final Object jobMutex = new Object();

        // Fork workers
        final int[] jobCount = new int[1]; 
        List<Thread> threads = new ArrayList<Thread>();
        for (int threadIndex = 0; threadIndex < totalThreads; threadIndex++) {
        	final int threadId = threadIndex + 1;
	        Thread thread = new Thread(new Runnable() {
				public void run() {
					while (true) {
						try {
							ValidationTask task;
							synchronized (jobMutex) {
								if (!jobQueue.hasNext()) {
									return;
								}
								task = jobQueue.next();
							}
							jobCount[0] += 1;
							logger.info(String.format("[Thread %d] %d/%d %s", threadId, jobCount[0], queries.size(), task.queryRecord[0]));
							task.run();
						} catch (Throwable t) {
							logger.error("Unexpected error", t);
						}
					}
				}
			});
	        threads.add(thread);
	        thread.start();
        }
        
        // Wait for them all to finish
        for (Thread thread : threads) {
        	thread.join();
        }
        
        
        Map<String, double[]> result = new HashMap<String, double[]>();
        // Assemble results
        for (ValidationTask task : tasks) {
        	double[] results = task.getMeasures();
        	result.put(task.getQueryId(), results);
        }
		return result;
    }

    enum ProcessMode {
    	Scan,
    	Positive,
    	Negative,
    }
    
    private int populateLabel(Vector initialLabel, String[] queryRecord, boolean autoComputeNegatives) {
		ProcessMode mode = ProcessMode.Scan;
		int totalPositive = 0;
		int totalNegative = 0;
		
	process:
		for (String item : queryRecord) {
			switch (mode) {
			case Scan:
				if ("+".equals(item)) {
					mode = ProcessMode.Positive;
				} else if ("-".equals(item)) {
					mode = ProcessMode.Negative;
				}
				break;
			case Positive:
				if ("-".equals(item)) {
					mode = ProcessMode.Negative;
				} else {
			        Integer index = lookupSymbol(organism, item);
			        if (index != null && initialLabel.get(index) == 0) {
		        		totalPositive++;
			            initialLabel.set(index, 1);
			        }
				}
				break;
			case Negative:
				if ("%".equals(item)) {
					break process;
				} else {
			        Integer index = lookupSymbol(organism, item);
			        if (index != null && initialLabel.get(index) == 0) {
			            totalNegative++;
			            initialLabel.set(index, -1);
			        }
				}
				break;
			}
		}
		
		if (!autoComputeNegatives && totalNegative == 0) {
			autoComputeNegatives = true;
			logger.warn(String.format("Query %s has no negative examples.  Forcing automatic computation of negatives.", queryRecord[0]));
		}
		
		if (autoComputeNegatives) {
			for (int i = 0; i < initialLabel.size(); i++) {
				if (initialLabel.get(i) == 0) {
					initialLabel.set(i, -1);
					totalNegative++;
				}
			}
		}

		return totalPositive + totalNegative;
	}

	private void checkLabels(no.uib.cipr.matrix.Vector labels) {
        // what's going on here?

        int size = labels.size();
        int ones = MatrixUtils.countMatches(labels, 1d);
        int minus_ones = MatrixUtils.countMatches(labels, -1d);
        int zeros = MatrixUtils.countMatches(labels, 0d);
        int excluded = MatrixUtils.countMatches(labels, Constants.EXCLUDED_ROW_VALUE);
        int unaccounted = size - ones - minus_ones - zeros - excluded;

        logger.info(String.format("label vector: size %d, +1 %d, -1 %d, 0: %d, excl: %d, unaccounted %d", size, ones, minus_ones, zeros, excluded, unaccounted));
    }

    /*
     * So symbol strings are used to lookup genes, from genes we
     * lookup nodes, and then use the node id to lookup the corresponding
     * index for that gene in our network matrices. This function
     * does the lookup, and as an optimization stores the
     * symbol string -> matrix index map because the db queries aren't
     * the fastest thing in the world, and the mapping table ought to
     * be small enough to comfortably fit in memory.
     *
     * TODO: hey, what about case sensitivity here? There's a split-out version
     * of this in engine.converter.SymbolCache, use that instead.
     *
     * this function returns null for symbols that could not be found.
     */
    private static final int SYMBOL_NOT_FOUND = -1;

	private Integer minimumGeneSetSize;
	private Integer maximumGeneSetSize;

    private Integer lookupSymbol(Organism organism, String symbol) {
        // check cache first
        Integer index = symbolToIndexCache.get(symbol);

        // look in db if not found
        if (index == null) {
            Long nodeId = geneMediator.getNodeId(organism.getId(), symbol);
            if (nodeId == null) {
                // not in db either, put a marker in the cache
                logger.info("symbol not in db: " + symbol);
                symbolToIndexCache.put(symbol, SYMBOL_NOT_FOUND);
            }
            else {
                // we got the gene, look up index, and again mark the
                // cache if not found. otherwise, cache it
                try {
                    index = nodeIds.getIndexForId(nodeId);
                    symbolToIndexCache.put(symbol, index);
                }
                catch (ApplicationException e) {
                    logger.warn("gene not in mappings for " + symbol);
                    symbolToIndexCache.put(symbol, SYMBOL_NOT_FOUND);
                    index = null;
                }
            }
        }
        else if (index == SYMBOL_NOT_FOUND) {
            index = null;
        }

        return index;
    }

    private static void logEngineVersion() {
        String version = Config.instance().getVersion();
        logger.info("Version: " + version);
    }

    @Override
    public void process() throws Exception {
        // either just dump, or execute queries
        if (dumpType != null) {
            dump(dumpType);
        }
        else {
        	initValidation();
        	crossValidate();
        }   	
    }
    
    @Override
    public void init() throws Exception {
    	super.init();
    	logEngineVersion();
    }
    
    public static void main(String args[]) throws Exception {
    	VectorCrossValidator validator = new VectorCrossValidator();
        if (!validator.getCommandLineArgs(args)) {
            System.exit(1);
        }

        try {
        	validator.init();
        	validator.process();
        	validator.cleanup();
        }
        catch (Exception e) {
            logger.error("Fatal error", e);
            System.exit(1);
        }
    }

    private class ValidationTask {
		private String[] queryRecord;
		private long randomSeed;
		private double[] averageMeasures;

		ValidationTask(String[] queryRecord, long seed) {
    		this.queryRecord = queryRecord;
    		this.randomSeed = seed;
    	}
    	
		public String getQueryId() {
			return queryRecord[0];
		}

		public double[] getMeasures() {
			return averageMeasures;
		}
		
    	void run() throws ApplicationException {
            String goCategory = queryRecord[0];
            Constants.CombiningMethod combiningMethod = getCombiningMethod(queryRecord);
            
            Vector initialLabel;
          	int totalNodes;
          	if (useCachedGoAnnos) {
                totalNodes = nodeIds.getNodeIds().length;
                initialLabel = loadAnnosFromCache(goCategory); 
            } else {
                initialLabel = new DenseVector(nodeIds.getNodeIds().length);
				totalNodes = populateLabel(initialLabel, queryRecord, allNegCrossVal);
           }

      		// Enforce any query size restrictions
          	if (minimumGeneSetSize != null || maximumGeneSetSize != null) {
          		int totalPositive = countPositive(initialLabel);
          		if (minimumGeneSetSize != null && totalPositive < minimumGeneSetSize) {
          			averageMeasures = SKIPPED;
          			return;
          		}
          		if (maximumGeneSetSize != null && totalPositive > maximumGeneSetSize) {
          			averageMeasures = SKIPPED;
          			return;
          		}
          	}
          	
            //random permutation of indices from which to construct folds
            int[] allPerm = computePermutation(totalNodes, initialLabel);
            
            averageMeasures = new double[measures.size()];

            CoreMania coreMania = new CoreMania(cache);
            for (int k = 0; k < numFolds; k++) {
                logger.debug(String.format("executing fold %d of %d", k + 1, numFolds));
                crossValidateVector(coreMania, initialLabel, combiningMethod, goCategory, averageMeasures, k, allPerm);
            }
            
            for (int i = 0; i < averageMeasures.length; i++) {
                averageMeasures[i] /= numFolds;
            }

            synchronized (outputMutex) {
            	writeResult(goCategory, averageMeasures);
            }
    	}

		private int[] computePermutation(int totalNodes, Vector initialLabel) {
			int labelSize = nodeIds.getNodeIds().length;
			int[] permutations = MatrixUtils.permutation(labelSize, new Random(randomSeed));
			if (totalNodes == labelSize) {
				return permutations;
			}
			
			// We're using a subset of the total nodes so we need to ensure
			// our resulting map only references the nodes we're interested
			// in.
			int[] nodeMap = new int[totalNodes];
			int index = 0;
			for (int i = 0; i < permutations.length; i++) {
				int permutatedIndex = permutations[i];
				if (initialLabel.get(permutatedIndex) == 0) {
					continue;
				}
				nodeMap[index] = permutatedIndex;
				index++;
			}
			return nodeMap;
		}
    }

	private int countPositive(Vector label) {
		int totalPositive = 0;
		for (VectorEntry entry : label) {
			if (entry.get() == 1) {
				totalPositive++;
			}
		}
		return totalPositive;
	}

	public void setMinimumGeneSetSize(Integer minimumGeneSetSize) {
		this.minimumGeneSetSize = minimumGeneSetSize;
	}

	public void setMaxmimumGeneSetSize(Integer maximumGeneSetSize) {
		this.maximumGeneSetSize = maximumGeneSetSize;
	}
	
	public void setCacheNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	public void setWriteLabels(boolean writeLabels) {
		this.writeLabels = writeLabels;
	}
}
