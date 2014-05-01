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

import org.apache.log4j.Logger;
import org.genemania.domain.Organism;
import org.genemania.engine.apps.support.DataConnector;
import org.genemania.engine.summary.IdentifiersSummarizer;
import org.genemania.engine.summary.NetworksSummarizer;
import org.genemania.engine.summary.NullReporter;
import org.genemania.engine.summary.OntologiesSummarizer;
import org.genemania.engine.summary.OrganismsSummarizer;
import org.genemania.engine.summary.Reporter;
import org.genemania.engine.summary.ReporterFactory;
import org.genemania.engine.summary.Summarizer;
import org.genemania.engine.summary.TabularReporterFactory;
import org.kohsuke.args4j.Option;

/*
 * generate summary information describing a
 * genemania dataset, such as counts of identifiers,
 * networks, interactions etc.
 */
public class DatasetSummarizer extends AbstractEngineApp {
    private static Logger logger = Logger.getLogger(DatasetSummarizer.class);	
    
    @Option(name = "-reportDir", usage = "location of report directory")	
    private String reportDir;
    
    public DatasetSummarizer() {
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
    	ReporterFactory reporterFactory = new TabularReporterFactory(reportDir + File.separator + organism.getId());
    	Summarizer summarizer = null;

    	// ontologies
    	summarizer = new OntologiesSummarizer(organism, dataConnector);
    	summarize(summarizer, reporterFactory);
    	
    	// identifiers
    	summarizer = new IdentifiersSummarizer(organism, dataConnector);
    	summarize(summarizer, reporterFactory);
    	
    	// networks
    	summarizer = new NetworksSummarizer(organism, dataConnector);
    	summarize(summarizer, reporterFactory);    	    	
    }
    
	@Override
	public void process() throws Exception {
		
		// dataset level report
		Summarizer summarizer = new OrganismsSummarizer(dataConnector);
		summarize(summarizer, new TabularReporterFactory(reportDir));
		
		// organism level reports
		for (Organism organism : organismMediator.getAllOrganisms()) {
			logger.info(String.format("Organism %d: %s", organism.getId(),
					organism.getName()));
			summarizeOrganism(organism);
		}
	}
	
	// thank-you stackoverflow
	// argh, this is a java6 thing, and we need to support java5 for the plugin!
//	public static String decompose(String s) {
//	    return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+","");
//	}
        
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
        try {		
        	DatasetSummarizer instance = new DatasetSummarizer();

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
}
