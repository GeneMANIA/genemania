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
import java.util.List;

import org.apache.log4j.Logger;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.dto.AddOrganismEngineRequestDto;
import org.genemania.engine.IMania;
import org.genemania.engine.Mania2;
import org.genemania.engine.actions.AddOrganism;
import org.genemania.engine.converter.sym.FileNetworkSymMatrixProvider;
import org.genemania.engine.converter.sym.INetworkSymMatrixProvider;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.Network;
import org.genemania.engine.core.data.NetworkIds;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.NodeCursor;
import org.genemania.util.NullProgressReporter;
import org.genemania.util.ProgressReporter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * create an interaction cache the engine can use, by extracting all the
 * interactions in the database.
 *
 * The cache files are written to the directory specified by -cachedir.
 * Pre-existing files in this directory are not removed, to avoid confusion the
 * directory should be clean before invoking the program. Database connection
 * info is extracted from the application context xml file, which is loaded from
 * the file specified by -appcontext, or otherwise a file named
 * SpringApplicationContext.xml will be searched for on the classpath.
 *
 */
public class CacheBuilder extends AbstractEngineApp {

    private static Logger logger = Logger.getLogger(CacheBuilder.class);

    @Option(name = "-networkDir", usage = "if provided, networks are read as text files from the specified dir, otherwise networks are read from the INTERACTIONS table of the db")
    private String networkDir = null;
    @Option(name = "-orgId", usage = "optional organism id, otherwise will process all oganisms")
    private static int orgId = -1;

    /**
     * @return the networkDir
     */
    public String getNetworkDir() {
        return networkDir;
    }

    /**
     * @param networkDir the networkDir to set
     */
    public void setNetworkDir(String networkDir) {
        this.networkDir = networkDir;
    }

    public void processAllOrganisms(ProgressReporter progress) throws ApplicationException, DataStoreException {
        try {
            for (Organism organism: organismMediator.getAllOrganisms()) {
                processOrganism(organism, progress);
            }
        }
        finally {
        }
    }

    public void processOrganism(int orgId, ProgressReporter progress) throws ApplicationException, DataStoreException {
        Organism organism = organismMediator.getOrganism(orgId);
        processOrganism(organism, progress);
    }

    /**
     * produce all the cache files for a given organism. first the mapping from
     * matrix index's to GMID's is produced by scanning over the networks, next
     * each network is converted to matrix form.
     *
     * @param organism
     * @throws ApplicationException
     */
    @SuppressWarnings("unchecked")
    public void processOrganism(Organism organism, ProgressReporter progress) throws ApplicationException, DataStoreException {
        logger.info("processing organism " + organism.getId() + " " + organism.getName());

        addOrganism(organism, progress);
        buildNetworkIds(organism, progress);
        buildNetworks(organism, progress);
    }

    public void addOrganism(Organism organism, ProgressReporter progress) throws ApplicationException, DataStoreException {
        List<Long> nodeIds = loadNodeIds(organism, progress);

        AddOrganismEngineRequestDto request = new AddOrganismEngineRequestDto();
        request.setOrganismId(organism.getId());
        request.setNodeIds(nodeIds);
        IMania mania = new Mania2(cache);
        mania.addOrganism(request);
    	
    }

    public List<Long> loadNodeIds(Organism organism, ProgressReporter progress) throws ApplicationException, DataStoreException {
        NodeCursor cursor = organismMediator.createNodeCursor(organism.getId());

        ArrayList<Long> allNodeIds = new ArrayList<Long>();

        while (cursor.next()) {
            if (progress.isCanceled()) {
                logger.info("cancelled");
                return null;
            }

            allNodeIds.add(cursor.getId());
        }
        
        return allNodeIds;
    }
    /*
     * create the networkIds object in the datacache, for the given organism
     */
    public void buildNetworkIds(Organism organism, ProgressReporter progress) throws ApplicationException {
        logger.info("building network/index mapping for organism " + organism.getId());
        ArrayList<Long> allNetworkIds = new ArrayList<Long>();

        // go through each network
        Collection<InteractionNetworkGroup> groups = organism.getInteractionNetworkGroups();

        for (InteractionNetworkGroup group: groups) {
            if (progress.isCanceled()) {
                return;
            }
            Collection<InteractionNetwork> networks = group.getInteractionNetworks();

            for (InteractionNetwork network: networks) {
                allNetworkIds.add(network.getId());
            }
        }

        long[] table = AddOrganism.buildPrimitiveTable(allNetworkIds);

        NetworkIds networkIds = new NetworkIds(Data.CORE, organism.getId());
        networkIds.setNetworkIds(table);
        cache.putNetworkIds(networkIds);
    }

    /*
     * create individual network objects in the datacache, for the given organism.
     * depends on the node ids table already having been build in the cache.
     */
    public void buildNetworks(Organism organism, ProgressReporter progress) throws ApplicationException {
        // go through each network to create the matrix

        INetworkSymMatrixProvider provider = new FileNetworkSymMatrixProvider(organism.getId(), getNetworkDir(), cache.getNodeIds(organism.getId()), false);

        Collection<InteractionNetworkGroup> groups = organism.getInteractionNetworkGroups();
        for (InteractionNetworkGroup group: groups) {
            if (progress.isCanceled()) {
                return;
            }
            Collection<InteractionNetwork> networks = group.getInteractionNetworks();

            for (InteractionNetwork network: networks) {
                processNetwork(provider, organism, network, progress);
            }
        }
    }

    public void processNetwork(INetworkSymMatrixProvider provider, Organism organism, InteractionNetwork network, ProgressReporter progress) throws ApplicationException {
        logger.info("building matrix for network " + network.getId() + " " + network.getName() + " organism " + organism.getId());
        SymMatrix matrix = provider.getNetworkMatrix(network.getId(), progress);

        matrix.compact();

        Network networkObj = new Network(Data.CORE, organism.getId(), network.getId());
        networkObj.setData(matrix);
        cache.putNetwork(networkObj);
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

    public void createCacheDir() {
        File dir = new File(getCacheDir());
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    public void logParams() {
        logger.info("network dir: " + networkDir);
        logger.info("cache dir: " + getCacheDir());

    }
    
    @Override 
    public void process() throws DataStoreException, ApplicationException {
        // default, do all organisms
        if (orgId == -1) {
            processAllOrganisms(NullProgressReporter.instance());
        }
        else {
            processOrganism(orgId, NullProgressReporter.instance());
        }  	
    }
    
    @Override 
    public void init() throws Exception {    	
    	super.init();
        createCacheDir();
        logParams();
    }

    public static void main(String[] args) throws Exception {

        CacheBuilder cacheBuilder = new CacheBuilder();
        if (!cacheBuilder.getCommandLineArgs(args)) {
            System.exit(1);
        }      

        try {
        	cacheBuilder.init();
            cacheBuilder.process();
            cacheBuilder.cleanup();
        }
        catch (Exception e) {
            logger.error("Fatal error", e);
            System.exit(1);
        }
    }
}
