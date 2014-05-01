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
import java.io.IOException;

import org.apache.log4j.Logger;
import org.genemania.domain.Ontology;
import org.genemania.domain.Organism;
import org.genemania.engine.core.integration.AnnoFilter;
import org.genemania.engine.core.integration.AssocLoader;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.kohsuke.args4j.Option;

/**
 * use the already existing annotation files to create a filtered
 * set of annotations given a list of id's.
 *
 */
public class EnrichmentCategoryBuilder extends AbstractEngineApp {
    private static Logger logger = Logger.getLogger(EnrichmentCategoryBuilder.class);

    @Option(name = "-annodir", usage = "optional directory containing annotations, will use cache GoAnnos structure if not given")
    String annoDir;

    @Option(name = "-orgId", usage = "optional organism id, otherwise will process all oganisms")
    private long orgId = -1;

    public String getAnnoDir() {
        return annoDir;
    }

    public void setAnnoDir(String annoDir) {
        this.annoDir = annoDir;
    }

    public long getOrgId() {
        return orgId;
    }

    public void setOrgId(long orgId) {
        this.orgId = orgId;
    }

    public void logParams() {
        logger.info("cache dir: " + getCacheDir());
        if (annoDir != null) {
            logger.info("annotations dir: " + annoDir);
        }
        else {
            logger.info("no annoDir specified, using GoAnnos from engine cache");
        }
    }

    @Override
    public void init() throws Exception {
    	super.init();
    	logParams();
    }

    @Override
    public void process() throws Exception {
        if (orgId != -1) {
            throw new ApplicationException("single organism processing not implemented");
        }

        for (Organism organism: organismMediator.getAllOrganisms()) {
            processOrganism(organism);
        }
    }

    public void processOrganism(Organism organism) throws Exception {
        if (annoDir == null) {
            AnnoFilter filter = new AnnoFilter(cache);
            filter.filter(organism);
        }
        else {
            loadFromAnnoFile(organism);
        }

    }

    private void loadFromAnnoFile(Organism organism)
            throws ApplicationException, IOException, DataStoreException {


        Ontology ontology = organism.getOntology();
        if (ontology == null) {
            logger.warn(String.format("No default ontology specified for %d (%s), skipping", organism.getId(), organism.getName()));
            return;
        }

        logger.info(String.format("processing ontology %d (%s) for organism %d (%s)", ontology.getId(), ontology.getName(), organism.getId(), organism.getName()));

        String filename = getAnnoFilename(organism);

        AssocLoader gen = new AssocLoader(organism, getGeneMediator(),
                getOntologyMediator(), getCache());
        logger.info("getting labels from file: " + filename);
        gen.loadEnrichmentAnnos(filename, ontology.getId());
    }

    /*
     * the processed ontology file should be in the
     * pondir with name ontologyname.txt
     */
    protected String getAnnoFilename(Organism organism) {
        return annoDir + File.separator + organism.getOntology() + ".txt";
    }

    public static void main(String[] args) throws Exception {

        EnrichmentCategoryBuilder builder = new EnrichmentCategoryBuilder();
        if (!builder.getCommandLineArgs(args)) {
            System.exit(1);
        }

        try {
            builder.init();
        	builder.process();
        	builder.cleanup();
        }
        catch (Exception e) {
            logger.error("Fatal error", e);
            System.exit(1);
        }
    }
}
