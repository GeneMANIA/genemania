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

import java.util.Collection;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.VectorEntry;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.genemania.domain.Organism;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.DatasetInfo;
import org.genemania.engine.core.data.Network;
import org.genemania.engine.core.data.NodeDegrees;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.exception.ApplicationException;
import org.kohsuke.args4j.Option;

/** 
 * compute the degree of all the nodes for each organism
 */
public class NodeDegreeComputer extends AbstractEngineApp {

    private static Logger logger = Logger.getLogger(CacheBuilder.class);
    @Option(name = "-orgId", usage = "optional organism id, otherwise will process all oganisms")
    private long orgId = -1;

    int numGenes;
    DenseVector degreesInOrganism;
    int numConnectedGenes;
    
    public long getOrgId() {
        return orgId;
    }

    public void setOrgId(long orgId) {
        this.orgId = orgId;
    }

    public int getNumConnectedGenes() {
        return numConnectedGenes;
    }

    /**
     * For command-line usage, user can specify a log file with an optional argument.
     * log4j is used as the actual logging mechanism, and is configured here.
     *
     */
    public void setupLogging() throws Exception {
        // setup logging
        if (logFilename == null) {
            return;
        }

        SimpleLayout layout = new SimpleLayout();
        FileAppender appender = new FileAppender(layout, logFilename, false);

        logger.addAppender(appender);
        logger.setLevel((Level) Level.DEBUG);
    }

    public void logParams() {
        logger.info("cache dir: " + getCacheDir());
    }

    public void processAllOrganisms() throws Exception {
        for (Organism organism: organismMediator.getAllOrganisms()) {
            processOrganism(organism);
        }
    }

    public void processOrganism(Organism organism) throws Exception {

        logger.info(String.format("computing degrees for organism %d (%s)", organism.getId(), organism.getName()));

        allocDataStructures(organism);
        computeDegrees(organism);

        countNodesWithPositiveDegree();
        write(organism);
        log();
    }

    protected void allocDataStructures(Organism organism) throws ApplicationException {

        // annotation matrix
        NodeIds nodeIds = cache.getNodeIds(organism.getId());
        numGenes = nodeIds.getNodeIds().length;

        degreesInOrganism = new DenseVector(numGenes);
    }

    protected void computeDegrees(Organism organism) throws ApplicationException {
        Collection<Collection<Long>> groupedNetworks = getAllNetworks(organism);

        degreesInOrganism.zero();

        DenseVector degreesInNetwork = new DenseVector(numGenes);
        for (Collection<Long> networks: groupedNetworks) {
            for (long networkId: networks) {
                degreesInNetwork.zero();
                
                Network network = cache.getNetwork(Data.CORE, organism.getId(), networkId);
                SymMatrix networkData = network.getData();

                networkData.columnSums(degreesInNetwork.getData());
                degreesInOrganism.add(degreesInNetwork);
            }
        }
    }

    protected  int countNodesWithPositiveDegree() {
        numConnectedGenes = 0;

        for (VectorEntry e: degreesInOrganism) {
            if (e.get() > 0) {
                numConnectedGenes += 1;
            }
        }

        return numConnectedGenes;
    }

    protected void write(Organism organism) throws ApplicationException {

        // write out nodeDegree objects
        NodeDegrees nodeDegrees = new NodeDegrees(Data.CORE, organism.getId());
        nodeDegrees.setDegrees(degreesInOrganism);
        cache.putNodeDegrees(nodeDegrees);

        // update datasetInfo
        DatasetInfo datasetInfo = cache.getDatasetInfo(organism.getId());
        datasetInfo.setNumInteractingGenes(numConnectedGenes);
        cache.putDatasetInfo(datasetInfo);
    }

    protected void log() throws ApplicationException {
        logger.info(String.format("Total genes: %d, num interacting genes: %d", numGenes, numConnectedGenes));
    }
   
    @Override
    public void init() throws Exception {
    	super.init();
        logParams();    	
    }
    
    @Override
    public void process() throws Exception {
        if (orgId != -1) {
            Organism organism = getOrganismMediator().getOrganism(orgId);
            if (organism == null) {
                throw new ApplicationException("failed to find organims: " + orgId);
            }

            processOrganism(organism);
        }
        else {
            processAllOrganisms();
        }    	
    }
    
    public static void main(String[] args) throws Exception {

        NodeDegreeComputer ndc = new NodeDegreeComputer();
        if (!ndc.getCommandLineArgs(args)) {
            System.exit(1);
        }

        try {
        	ndc.init();
            ndc.process();
            ndc.cleanup();
        }
        catch (Exception e) {
            logger.error("Fatal error", e);
            System.exit(1);
        }
    }
}
