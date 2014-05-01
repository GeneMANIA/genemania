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

package org.genemania.engine.summary;

import java.util.List;

import org.apache.log4j.Logger;
import org.genemania.domain.Organism;
import org.genemania.engine.apps.support.DataConnector;

/*
 * TODO: describe report format
 */
public class OrganismsSummarizer implements Summarizer {
    private static Logger logger = Logger.getLogger(OrganismsSummarizer.class);	
	
	DataConnector dataConnector;

	public OrganismsSummarizer(DataConnector dataConnector) {
		this.dataConnector = dataConnector;
	}

	public void summarize(ReporterFactory reporterFactory) throws Exception {
		logger.info("summarizing organisms");
		Reporter reporter = reporterFactory.getReporter("organisms");
        reporter.init(new String[] {"Id", "Name", "Taxonomy ID"});
        try {
        	List<Organism> organisms = dataConnector.getOrganismMediator().getAllOrganisms();
        	for (Organism organism: organisms) {
        		reporter.write("" + organism.getId(), organism.getName(), "" + organism.getTaxonomyId());
        	}
        }
        finally {
        	reporter.close();
        }
	}
	
	public void setUp() throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void tearDown() throws Exception {
		// TODO Auto-generated method stub
		
	}
}
