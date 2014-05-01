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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;


import org.apache.log4j.Logger;
import org.genemania.domain.Gene;
import org.genemania.domain.Node;
import org.genemania.domain.Organism;
import org.genemania.dto.RelatedGenesEngineRequestDto;
import org.genemania.dto.RelatedGenesEngineResponseDto;
import org.genemania.engine.IMania;
import org.genemania.engine.Mania2;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.cache.FileSerializedObjectCache;
import org.genemania.engine.cache.IObjectCache;
import org.genemania.engine.cache.MemObjectCache;
import org.genemania.engine.validation.ResultWriter;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.type.CombiningMethod;
import org.genemania.type.ScoringMethod;
import org.genemania.util.NullProgressReporter;
import org.kohsuke.args4j.Option;

/**
 * benchmark harness for measuring standard queries on a production dataset,
 * tests default gene lists against default or all networks for given organism
 * and combining method.
 */
public class QueryBench extends AbstractEngineApp {

    private static Logger logger = Logger.getLogger(QueryBench.class);

    @Option(name = "-method", usage = "network combination method, should be one of 'equal' or 'smart'")
    private String combiningMethodName;

    @Option(name = "-orgid", usage = "organism id")
    private long organismId = -1; // -1 means unspecified

    @Option(name = "-netids", usage = "comma delim list of network ids to use eg '3,4,19', or 'all', or 'default'.")
    private String networkIdsList;

    @Option(name = "-nodeids", usage = "comma delim list of node ids eg '230,231,555', or 'default' for organism defaults")
    private String nodeIds;

    @Option(name = "-out", usage = "name of output file to contain validation results.")
    private String outFilename;

    @Option(name = "-appendOut", usage = "append to existing output file, if present. defaults to false, out file will be overriden")
    private boolean appendOut;
    
    @Option(name = "-warmupIters", usage = "# of warmup iterations to perform")
    private int warmupIters;

    @Option(name = "-timingIters", usage = "# of timing iterations to perform")
    private int timingIters;

    @Option(name = "-clearMemCache", usage = "clear memory cache between algorithm invocations, defaults to false")
    private boolean clearMemCache;

    @Option(name = "-compCache", usage = "use compressed cache, defaults to false")
    private boolean compressedCache;

    private Organism organism;
    private ResultWriter writer;
    private Collection<Collection<Long>> benchmarkNetworkIds;
    private Collection<Long> benchmarkNodeIds;

    private int numNodesUsed;
    private int numNetworksUsed;
    private long minTime = Long.MAX_VALUE;
    private long maxTime = Long.MIN_VALUE;
    private long totalTime = 0;

    public void initBench() throws Exception {

        logger.info("initializing benchmark");
//        dumpSystemProperties();
        
        organism = getOrganism();
        benchmarkNetworkIds = getNetworkIdList();
        benchmarkNodeIds = getGeneIdList();

        if (outFilename != null) {
            File f = new File(outFilename);
            // append to file, skip writing another header
            if (appendOut && f.exists()) { 
                writer = new ResultWriter(outFilename, '\t', true);
            }
            // overwrite file if it exists, write out header
            else { 
                writer = new ResultWriter(outFilename, '\t', false);
                writer.write(formatHeader());
                writer.flush();
            }
        }
        else {
            writer = ResultWriter.getNullWriter();
        }
    }

    private static void dumpSystemProperties() {
        Properties p = System.getProperties();
        Enumeration keys = p.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = (String) p.get(key);
            System.out.println(key + ": " + value);
        }
        
    }
    
    public void cleanupBench() {
        writer.close();
    }
    
    /*
     * select the set of networks to be used for validation
     */
    private Collection<Collection<Long>> getNetworkIdList() throws ApplicationException, DataStoreException {

        Collection<Collection<Long>> idList = null;

        if (networkIdsList.equalsIgnoreCase("all")) {
            idList = getAllNetworks(organism);
        }
        else if (networkIdsList.equalsIgnoreCase("default")) {
            idList = getDefaultNetworks(organism);
        }
        else {
            idList = getNetworksById(organism, networkIdsList);
        }

        numNetworksUsed = count(idList);
        logger.info(String.format("total %d networks selected", numNetworksUsed));
        return idList;
    }
    
    public List<Long> getGeneIdList() throws ApplicationException, DataStoreException {
        logger.info("loading gene list");
        if (nodeIds == null || nodeIds.equalsIgnoreCase("default")) {
            return getDefaultNodes();
        }
        else {
            return getNodesById();
        }
    }

    /*
     * check given node ids against db
     */
    private List<Long> getNodesById() throws ApplicationException {
        String [] idlist = nodeIds.split(",");

        List<Long> result = new ArrayList<Long>();

        for (int i=0; i<idlist.length; i++) {            
            Node node = nodeMediator.getNode(Long.parseLong(idlist[i]), organism.getId());
            if (node == null) {
                throw new ApplicationException("node id not found in db: " + idlist[i]);
            }
            else {
                result.add(node.getId());
            }
        }

        numNodesUsed = result.size();
        return result;
    }


    /*
     * pull in default genes
     */
    private List<Long> getDefaultNodes() throws DataStoreException {
        List<Gene> genes = organismMediator.getDefaultGenes(organism.getId());

        List<Long> ids = new ArrayList<Long>();
        for (Gene gene: genes) {
            ids.add(gene.getNode().getId());
        }

        numNodesUsed = ids.size();
        return ids;        
    }

    
    private Organism getOrganism() throws ApplicationException, DataStoreException {

        Organism organism;

        organism = getOrganismById(organismId);

        logger.info("quering organism: " + organism.getName());
        return organism;

    }

    private Organism getOrganismById(long organismId) throws ApplicationException, DataStoreException {
        Organism organism = organismMediator.getOrganism(organismId);
        return organism;
    }

    public void bench() throws Exception {

        logger.info("performaing benchmark");

        IMania mania = getMania();
        
        logger.info(String.format("executing %d warmup iterations", warmupIters));
        for (int i=0; i<warmupIters; i++) {
            runQuery(mania, false);
        }

        logger.info(String.format("executing %d timing iterations", timingIters));
        for (int i=0; i<timingIters; i++) {
            runQuery(mania, true);
        }

    }

    /*
     * which implementation?
     */
    private IMania getMania() {

        IObjectCache cache = new FileSerializedObjectCache(getCacheDir(), compressedCache);
        cache = new MemObjectCache(cache);
        return new Mania2(new DataCache(cache));

    }

    private void runQuery(IMania mania, boolean shouldReport) throws ApplicationException {

            RelatedGenesEngineRequestDto request = new RelatedGenesEngineRequestDto();
            request.setOrganismId(organism.getId());
            request.setCombiningMethod(CombiningMethod.AUTOMATIC);
            request.setScoringMethod(ScoringMethod.DISCRIMINANT);

            request.setPositiveNodes(benchmarkNodeIds);

            request.setInteractionNetworks(benchmarkNetworkIds);
            request.setLimitResults(10);
            request.setProgressReporter(NullProgressReporter.instance());

            // compute result

            if (clearMemCache) {
                mania.clearMemCache();
            }
            
            long t0 = System.nanoTime();
            RelatedGenesEngineResponseDto response = mania.findRelated(request);
            long t1 = System.nanoTime();

            long duration = t1-t0;

            if (shouldReport) {

                if (duration < minTime) {
                    minTime = duration;
                }
                if (duration > maxTime) {
                    maxTime = duration;
                }

                totalTime += duration;
                
            }
    }
    
    public static List<String> formatHeader() {
        ArrayList<String> fields = new ArrayList<String>();
        fields.add("organism id");
        fields.add("organism name");
        fields.add("combining method");
        fields.add("networks");
        fields.add("num networks");
        fields.add("num genes");
        fields.add("warmup iters");
        fields.add("timing iters");
        fields.add("clear mem cache");

        fields.add("status");   // ok or error
        fields.add("ave time");
        fields.add("min time");
        fields.add("max time");

        fields.add("java vm name");
        fields.add("java version");
        fields.add("max mem");

        return fields;
    }

    /*
     * write out a record summarizing the results of the benchmark run
     */
    public List<String> formatResult(boolean success, String msg) {
        ArrayList<String> fields = new ArrayList<String>();

        // params
        fields.add("" + organism.getId());
        fields.add("" + organism.getName());
        fields.add(combiningMethodName);
        fields.add(networkIdsList);
        fields.add("" + numNetworksUsed);
        fields.add("" + numNodesUsed);
        fields.add("" + warmupIters);
        fields.add("" + timingIters);
        fields.add("" + Boolean.toString(clearMemCache));

        // timing
        if (success) {
            fields.add(msg);

            double secs = totalTime * (1e-9);
            double aveSecs = secs / timingIters;
            fields.add("" + aveSecs);

            secs = minTime * (1e-9);
            fields.add("" + secs);

            secs = maxTime * (1e-9);
            fields.add("" + secs);
        }
        else {
            fields.add(msg);
            fields.add("-"); // ave time
            fields.add("-"); // min time
            fields.add("-"); // max time
        }

        // system params
        fields.add(java.lang.System.getProperty("java.vm.name"));
        fields.add(java.lang.System.getProperty("java.version"));
        fields.add("" + Runtime.getRuntime().maxMemory());

        return fields;
    }

    /*
     * reporting: we just write out the summary record. maybe later
     * we should write out something about the results eg # weighted networks etc?
     */
    public void reportBench(boolean success, String msg) {
        writer.write(formatResult(success, msg));
        writer.flush();
    }
    
    @Override
    public void process() throws Exception {
        try {
        	bench();
        	reportBench(true, "ok");
        }
        catch (OutOfMemoryError e) {
            logger.warn("out of memory", e);
            reportBench(false, "out of memory");
        }
        catch (Exception e) {
            logger.warn("unexpected error", e);
            reportBench(false, "unexpected error");
        }
    }
    
    @Override
    public void init() throws Exception {
    	super.init(); // TODO:check about datacache creation ... we need to override here?
    	initBench();
    }

    @Override
    public void cleanup() throws Exception {
    	cleanupBench();
    	super.cleanup();
    }
    
    public static void main(String args[]) throws Exception {
    	QueryBench bench = new QueryBench();
        if (!bench.getCommandLineArgs(args)) {
            System.exit(1);
        }

        try {
            bench.init();
            bench.process();
            bench.cleanup();
        }
        catch (Exception e) {
            logger.error("Fatal exception", e);
            System.exit(1);
        }
    }
}
