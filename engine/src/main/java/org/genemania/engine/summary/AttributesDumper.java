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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.genemania.domain.AttributeGroup;
import org.genemania.domain.Organism;
import org.genemania.engine.apps.support.DataConnector;
import org.genemania.engine.core.data.AttributeData;
import org.genemania.engine.core.data.AttributeGroups;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.engine.matricks.Matrix;
import org.genemania.exception.ApplicationException;

public class AttributesDumper implements Summarizer {
    private static Logger logger = Logger.getLogger(AttributesDumper.class);  

    Organism organism;
    DataConnector dataConnector;
    PreferredNames preferredNames;

    // summarization info
    Map<String, Integer> countsByGroup;
    int uniqueNetworks;

    ReporterFactory reporterFactory;
    Reporter interactionReporter;

    public AttributesDumper(Organism organism, DataConnector dataConnector, PreferredNames preferredNames) throws Exception {
        this.organism = organism;
        this.dataConnector = dataConnector;
        this.preferredNames = preferredNames;
    }    

    @Override
    public void setUp() throws Exception {
        countsByGroup = new HashMap<String, Integer>();
        uniqueNetworks = 0; 
    }

    @Override
    public void summarize(ReporterFactory reporterFactory) throws Exception {
        this.reporterFactory = reporterFactory;
        
        List<AttributeGroup> groups = dataConnector.getAttributeMediator().findAttributeGroupsByOrganism(organism.getId());
        for (AttributeGroup group: groups) {
            summarizeGroup(group);
        }
            
    }
    
    void summarizeGroup(AttributeGroup group) throws ApplicationException {

        String name = makeFileName(group);
        logger.info("Summarizing " + name);
        
        Reporter reporter = reporterFactory.getReporter(name);
        try {
            reporter.init();

            NodeIds nodeIds = dataConnector.getCache().getNodeIds(organism.getId());
            AttributeGroups groupData = dataConnector.getCache().getAttributeGroups(Data.CORE, organism.getId());
            AttributeData attributes = dataConnector.getCache().getAttributeData(Data.CORE, organism.getId(), group.getId());
            Matrix data = attributes.getData();

            // report all attributes (columns) for each gene (row)
            int numGenes = data.numRows();
            int numAttributes = data.numCols();

            for (int i=0; i<numGenes; i++) {
                long nodeId = nodeIds.getIdForIndex(i);
                String geneSymbol = preferredNames.getName(nodeId);
                ArrayList<String> record = new ArrayList<String>();

                record.add(geneSymbol);
                record.add("n/a"); // description optional
                for (int j=0; j<numAttributes; j++) {
                    if (data.get(i, j) == 1d) {
                        long attributeId = groupData.getAttributeIdForIndex(group.getId(), j);
                        String attributeName = dataConnector.getAttributeMediator().findAttribute(organism.getId(), attributeId).getExternalId();
                        record.add(attributeName);
                    }
                }

                if (record.size() > 2) { // must have at least 1 attribute
                    reporter.write(record);
                }
            }
        }
        finally {
            reporter.close();
        }
    }
    
    public void dumpGroup(AttributeGroup group, AttributeData attributes, String name) throws ApplicationException {

    }
 
    String makeFileName(AttributeGroup group) {
        return "Attributes." + group.getName();
    }
    
    @Override
    public void tearDown() throws Exception {
    }
}
