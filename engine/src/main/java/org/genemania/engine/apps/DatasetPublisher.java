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
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.genemania.domain.Organism;
import org.genemania.engine.summary.AttributesDumper;
import org.genemania.engine.summary.GMTReporterFactory;
import org.genemania.engine.summary.IdentifiersDumper;
import org.genemania.engine.summary.NetworksDumper;
import org.genemania.engine.summary.PrecombinedDumper;
import org.genemania.engine.summary.PreferredNames;
import org.genemania.engine.summary.ReporterFactory;
import org.genemania.engine.summary.Summarizer;
import org.genemania.engine.summary.TabularReporterFactory;
import org.kohsuke.args4j.Option;

/*
 * generate summary information describing a
 * genemania dataset, such as counts of identifiers,
 * networks, interactions etc.
 */
public class DatasetPublisher extends AbstractEngineApp {
    private static Logger logger = Logger.getLogger(DatasetPublisher.class);	

    @Option(name = "-reportDir", usage = "location of report directory")	
    private String reportDir;
    
    @Option(name = "-combined", usage = "what combined networks do we want, defaults to 'BP.DEFAULT', other values 'ALL', 'NONE'")
    private String combinedNetworksFilter = "BP.DEFAULT";

    private String reportSubdir; // we'll autogen a name based on date inside index under reportDir

    public DatasetPublisher() {
        super();
    }

    void summarize(Summarizer summarizer, ReporterFactory reporterFactory) throws Exception {
        summarizer.setUp();
        try {
            summarizer.summarize(reporterFactory);
        }
        finally {
            summarizer.tearDown();
        }
    }

    public void init() throws Exception {
        super.init();
    }

    void summarizeOrganism(Organism organism) throws Exception {
        String orgname = organism.getAlias(); // genes species
        orgname = orgname.replace(' ', '_');

        String reportLocation = reportSubdir + File.separator + orgname;
        ReporterFactory reporterFactory = new TabularReporterFactory(reportLocation);
        Summarizer summarizer = null;

        PreferredNames preferredNames = new PreferredNames(organism.getId(), dataConnector, 
                "Ensembl Gene ID", "Entrez Gene ID", "TAIR ID", "Gene Name", "Entrez Gene Name", "Ensembl Gene Name", "Ensembl Protein ID", "Uniprot ID"); // TODO: don't hardcode this list!

        // networks
        summarizer = new NetworksDumper(organism, dataConnector, preferredNames);
        summarize(summarizer, reporterFactory);	

        // identifiers
        summarizer = new IdentifiersDumper(organism, dataConnector, preferredNames);
        summarize(summarizer, reporterFactory);
      
        // attributes
        summarizer = new AttributesDumper(organism, dataConnector, preferredNames);
        summarize(summarizer, new GMTReporterFactory(reportLocation));

        // combined, if needed
        if (!"NONE".equalsIgnoreCase(combinedNetworksFilter)) {
            reporterFactory = new TabularReporterFactory(reportSubdir + File.separator + orgname + ".COMBINED");
            summarizer = new PrecombinedDumper(organism, dataConnector, combinedNetworksFilter, preferredNames);
            summarize(summarizer, reporterFactory);
        }
    }

    @Override
    public void process() throws Exception {

        // dataset level report
        //		Summarizer summarizer = new OrganismsSummarizer(dataConnector);
        //		summarize(summarizer, new TabularReporterFactory(reportDir));

        // get dataset date, used for top level report dir
        Date datasetDate = getStatsMediator().getLatestStatistics().getDate();
        System.out.println(datasetDate);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String dirName = df.format(datasetDate);
        reportSubdir = reportDir + File.separator + dirName;
        logger.info("writing report to " + reportSubdir);


        // organism level reports
        for (Organism organism : organismMediator.getAllOrganisms()) {
            logger.info(String.format("Organism %d: %s", organism.getId(),
                    organism.getName()));
            summarizeOrganism(organism);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        try {		
            DatasetPublisher instance = new DatasetPublisher();

            // load up command line arguments
            instance.getCommandLineArgs(args);
            instance.setupLogging();

            instance.init();
            instance.process();
            instance.cleanup();
            logger.info("summary report completed.");
        }
        catch (Exception e) {
            logger.error("Fatal error", e);
            System.exit(1);
        }
    }

    public String getReportDir() {
        return reportDir;
    }

    public void setReportDir(String reportDir) {
        this.reportDir = reportDir;
    }

    public String getCombinedNetworksFilter() {
        return combinedNetworksFilter;
    }

    public void setCombinedNetworksFilter(String combinedNetworksFilter) {
        this.combinedNetworksFilter = combinedNetworksFilter;
    }
}
