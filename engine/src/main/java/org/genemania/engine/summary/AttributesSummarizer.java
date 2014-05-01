package org.genemania.engine.summary;

import java.util.List;

import org.apache.log4j.Logger;
import org.genemania.domain.Attribute;
import org.genemania.domain.AttributeGroup;
import org.genemania.domain.Organism;
import org.genemania.engine.apps.DatasetSummarizer;
import org.genemania.engine.apps.support.DataConnector;
import org.genemania.engine.core.data.AttributeData;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.matricks.Matrix;
import org.genemania.engine.matricks.Vector;
import org.genemania.exception.ApplicationException;

public class AttributesSummarizer implements Summarizer {
    private static Logger logger = Logger.getLogger(AttributesSummarizer.class);

    Organism organism;
    DataConnector dataConnector;
    PreferredNames preferredNames;

    Reporter attributesReporter;

    public AttributesSummarizer(Organism organism, DataConnector dataConnector) throws Exception {
        super();
        this.organism = organism;
        this.dataConnector = dataConnector;

        this.preferredNames = new PreferredNames(organism.getId(), dataConnector, DatasetSummarizer.preferredNamesList);
    }

    @Override
    public void setUp() throws Exception {
    }

    @Override
    public void summarize(ReporterFactory reporterFactory) throws Exception {
        attributesReporter = reporterFactory.getReporter("attributes");

        try {
            attributesReporter.init("Attribute Group ID", "Attribute Group Name", "Attribute ID", "Attribute Name", "Num Nodes");

            List<AttributeGroup> groups = dataConnector.getAttributeMediator().findAttributeGroupsByOrganism(organism.getId());

            for (AttributeGroup group: groups) {
                summarizeGroup(group);
            }
        }
        finally {
            attributesReporter.close();
        }
    }

    private void summarizeGroup(AttributeGroup group) throws ApplicationException {

        List<Attribute> attributes = dataConnector.getAttributeMediator().findAttributesByGroup(organism.getId(), group.getId());
        AttributeData attributeData = dataConnector.getCache().getAttributeData(Data.CORE, organism.getId(), group.getId());
        Matrix data = attributeData.getData();

        Vector sums = data.columnSums();

        for (int attributeIndex=0; attributeIndex < attributes.size(); attributeIndex++) {

            Attribute attribute = attributes.get(attributeIndex);
            long count = Math.round(sums.get(attributeIndex));
            attributesReporter.write("" + group.getId(), group.getName(), "" + attribute.getId(), attribute.getName(), "" + count);
        }
    }

    @Override
    public void tearDown() throws Exception {
    }

}
