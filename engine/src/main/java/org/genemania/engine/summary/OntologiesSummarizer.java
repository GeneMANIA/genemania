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

import org.apache.log4j.Logger;
import org.genemania.domain.Ontology;
import org.genemania.domain.Organism;
import org.genemania.engine.apps.support.DataConnector;
import org.genemania.engine.core.data.GoAnnotations;
import org.genemania.engine.core.data.GoIds;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.engine.matricks.Matrix;
import org.genemania.engine.matricks.Vector;

/*
 * TODO: describe report format
 */
public class OntologiesSummarizer implements Summarizer {
    private static Logger logger = Logger.getLogger(OntologiesSummarizer.class);	
	
	Organism organism;
	DataConnector dataConnector;

	public OntologiesSummarizer(Organism organism, DataConnector dataConnector) {
		this.organism = organism;
		this.dataConnector = dataConnector;
	}

	public void summarize(ReporterFactory reporterFactory) throws Exception {
		logger.info(String.format("summarizing ontologies for organism %d - %s", organism.getId(), organism.getName()));

		Ontology ontology = organism.getOntology();
		GoAnnotations annos = dataConnector.getCache().getGoAnnotations(organism.getId(),
				"" + ontology.getId());
		GoIds goIds = dataConnector.getCache().getGoIds(organism.getId(), "" + ontology.getId());
		NodeIds nodeIds = dataConnector.getCache().getNodeIds(organism.getId());

		Matrix data = annos.getData();

		int numGenes = data.numRows();
		int numCategories = data.numCols();

		Vector annotationsPerGene = data.rowSums();
		Vector annotationsPerCategory = data.columnSums();
		
		reportAnnotationsPerGene(reporterFactory, nodeIds, annotationsPerGene);
		reportAnnotationsPerCategory(reporterFactory, goIds, annotationsPerCategory);
	}
	
	public void reportAnnotationsPerGene(ReporterFactory reporterFactory, NodeIds nodeIds, Vector annotationsPerGene) throws Exception {
		Reporter reporter = reporterFactory.getReporter("gene_annos");
		reporter.init(new String[] {"Node ID", "Annotations"});
		
		try {
			for (int i = 0; i<annotationsPerGene.getSize(); i++) {
				long nodeId = nodeIds.getIdForIndex(i);
				long count = Math.round(annotationsPerGene.get(i));
				if (count != 0) {
					reporter.write("" + nodeId, "" + count);
				}
			}
		}
		finally {
			reporter.close();
		}
		
	}
	
	public void reportAnnotationsPerCategory(ReporterFactory reporterFactory, GoIds goIds, Vector annotationsPerCategory) throws Exception {
		Reporter reporter = reporterFactory.getReporter("category_annos");
		reporter.init(new String[] {"Category ID", "Annotations"});
		
		try {
			for (int i = 0; i<annotationsPerCategory.getSize(); i++) {
				String categoryId = goIds.getIdForIndex(i);
				long count = Math.round(annotationsPerCategory.get(i));
				if (count != 0) {
					reporter.write(categoryId, "" + count);
				}
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
