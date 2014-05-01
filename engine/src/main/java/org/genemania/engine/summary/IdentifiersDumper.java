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

import java.util.Collection;
import org.apache.log4j.Logger;
import org.genemania.domain.Gene;
import org.genemania.domain.GeneNamingSource;
import org.genemania.domain.Node;
import org.genemania.domain.Organism;
import org.genemania.engine.apps.support.DataConnector;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.NodeCursor;

/*
 * for given organism, produce a table
 * of identifier mappings used by the system
 * for external publication (so without internal ids etc).
 * 
 * See the spec in:
 * 
 *   https://docs.google.com/document/d/1OLM9ynMInkHDi3AseVRgAvZaMmBFmE1MKIZ82XjVvgI/edit?hl=en#
 * 
 * TODO: describe report format
 */
public class IdentifiersDumper implements Summarizer {
    private static Logger logger = Logger.getLogger(IdentifiersDumper.class);	
	
	Organism organism;
	DataConnector dataConnector;
	PreferredNames preferredNames;
		
	public IdentifiersDumper(Organism organism, DataConnector dataConnector, PreferredNames preferredNames) {
		this.organism = organism;
		this.dataConnector = dataConnector;
		this.preferredNames = preferredNames;
	}

	public void summarize(ReporterFactory reporterFactory) throws Exception {
		logger.info(String.format("summarizing identifiers for organism %d - %s", organism.getId(), organism.getName()));
		
		Reporter reporter = reporterFactory.getReporter("identifier_mappings");
		try {
			count(reporter);
		}
		finally {
			reporter.close();
		}				
	}
	
	/*
	 * dump identifiers 
	 */
	void count(Reporter reporter) throws ApplicationException, DataStoreException {
        NodeCursor cursor = dataConnector.getOrganismMediator().createNodeCursor(organism.getId());
        
        reporter.init(new String[] {"Preferred_Name", "Name", "Source"});
                
        while (cursor.next()) {
        	long id = cursor.getId();
        	Node node = dataConnector.getNodeMediator().getNode(id, organism.getId());
        	Collection<Gene> genes = node.getGenes();
        	
        	for (Gene gene: genes) {
        		GeneNamingSource namingSource = gene.getNamingSource();
        		String sourceName = namingSource.getName();				
				String preferred = preferredNames.getName(id);
        		reporter.write(preferred, gene.getSymbol(), sourceName);
        	}
        }        
	}

	public void setUp() throws Exception {
	}

	public void tearDown() throws Exception {
	}	
}
