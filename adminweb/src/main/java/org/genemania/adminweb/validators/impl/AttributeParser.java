package org.genemania.adminweb.validators.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.genemania.adminweb.dataset.DataSetContext;
import org.genemania.adminweb.dataset.LuceneDataSet;
import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.validators.stats.NetworkValidationStats;
import org.genemania.domain.Organism;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.GeneMediator;
import org.genemania.mediator.impl.CachingGeneMediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

public class AttributeParser {
    final Logger logger = LoggerFactory.getLogger(AttributeParser.class);

    private DataSetContext context;
    private int organismId;

    private String sampleAccession;
    private String sampleName;

    private Collection<Long> attributeIds;
    private Collection<? extends List<Long>> nodeAttributeAssociations;

    private NetworkValidationStats validationStats = new NetworkValidationStats();

    public AttributeParser(DataSetContext context, int organismId) {
        this.context = context;
        this.organismId = organismId;
    }
    /*
     * Parse the input attribute file into a list of node-id, attribute-id pairs, assigning
     * attribute-ids based on the input file.
     */
    public void parse(File file) throws IOException, DatamartException, DataStoreException {
        ArrayList<Long> nodeIds = new ArrayList<Long>();
        ArrayList<String> attributes = new ArrayList<String>();

        HashSet<String> unrecognizedGenes = new HashSet<String>();
        AttributeFormatSniffer sniffer = new AttributeFormatSniffer(file);

        int format = sniffer.sniff();
        if (format == AttributeFormatSniffer.FORMAT_ATTRIBUTE_LIST) {
            parseAttributeList(file, nodeIds, attributes, unrecognizedGenes);
        }
        else if (format == AttributeFormatSniffer.FORMAT_GMT) {
            parseGMT(file, nodeIds, attributes, unrecognizedGenes);
        }
        else {
            throw new DatamartException("failed to determine file format");
        }

        // assign ids to unique attributes, in sorted order
        logger.info("assigning unique attribute ids");
        ArrayList<String> sorted = new ArrayList<String>(attributes);
        Collections.sort(sorted);
        HashMap<String, Long> uniqueAttributes = new HashMap<String, Long>();
        attributeIds = new ArrayList<Long>();
        Long id = 1L;
        for (String a: sorted) {
            if (!uniqueAttributes.containsKey(a)) {
                uniqueAttributes.put(a, id);
                attributeIds.add(id);
                id = id.longValue()+1;
            }
        }

        // create associations between the node ids and the newly defined attribute ids
        logger.info("creating attribute id list");
        ArrayList<ArrayList<Long>> assocs= new ArrayList<ArrayList<Long>>(attributes.size());
        for (int i=0; i<attributes.size(); i++) {
            ArrayList<Long> assoc = new ArrayList<Long>(2);
            String a = attributes.get(i);
            Long attributeId = uniqueAttributes.get(a);
            if (attributeId == null) {
                throw new DatamartException("internal error"); // shouldn't happen
            }
            assoc.add(nodeIds.get(i));
            assoc.add(attributeId);
            assocs.add(assoc);
        }
        nodeAttributeAssociations = assocs;

        List<String> invalids = new ArrayList<String>(unrecognizedGenes);
        Collections.sort(invalids);
        validationStats.setInvalidCount(invalids.size());
        // only report first few
        if (invalids.size() > 0) {
            invalids = invalids.subList(0, Math.min(20, invalids.size())-1);
        }
        validationStats.setInvalidInteractions(invalids);
        validationStats.setNumAttributes(attributeIds.size());
        validationStats.setNumAssociations(assocs.size());

        // how many unique genes were there anyway?
        HashSet<Long> uniqueNodeIds = new HashSet<Long>(nodeIds);
        validationStats.setNumGenes(uniqueNodeIds.size());

        logger.info("# attributes: " + validationStats.getNumAttributes());
        logger.info("# assocations: " + validationStats.getNumAssociations());
        logger.info("# genes: " + validationStats.getNumGenes());

        // sample for linkouts
        validationStats.setSampleAccession(sampleAccession);
        validationStats.setSampleName(sampleName);

    }

    /*
     * parse a file where each record is in the format:
     *
     *   gene attribute attribute attribute ...
     *
     * into a pair of tall skinny vectors: nodeIds corresponding to the gene, and
     * the attribute string.
     *
     * records with an unrecognized gene are dropped.
     */
    public void parseAttributeList(File file,
            ArrayList<Long> nodeIds, ArrayList<String> attributes, HashSet<String> unrecognizedGenes)
            throws DatamartException, IOException, DataStoreException {

        logger.info("parsing attribute list user data");

        LuceneDataSet luceneDataSet = getLuceneDataSet(getContext());
        Organism organism = luceneDataSet.getOrganismMediator().getOrganism(organismId);

        CSVReader reader = new CSVReader(new InputStreamReader(
                new BufferedInputStream(new FileInputStream(file)), "UTF8"),
                '\t', CSVParser.NULL_CHARACTER);

        final GeneMediator geneMediator = luceneDataSet.getGeneMediator();

        // load data into a pair of lists, element i corresponds to nodeId, attribute name
        logger.info("loading attribute data");
        try {
            String[] line = reader.readNext();
            while (line != null) {
                if (line.length < 2) {
                    line = reader.readNext();
                    continue;
                }
                String geneSymbol = line[0];
                Long nodeId = geneMediator.getNodeId(organism.getId(), geneSymbol);
                if (nodeId == null) {
                    unrecognizedGenes.add(geneSymbol);
                    line = reader.readNext();
                    continue;
                }
                for (int i=1; i<line.length; i++) {
                    String attribute = line[i];
                    nodeIds.add(nodeId);
                    attributes.add(attribute);

                    if (sampleAccession == null) {
                        sampleAccession = attribute;
                    }

                }
                line = reader.readNext();
            }
        }
        finally {
            reader.close();
        }
    }

    /*
     * parse a file where each record is in gmt format:
     *
     *   attribute description gene gene gene ...
     *
     * into a pair of tall skinny vectors: nodeIds corresponding to the gene, and
     * the attribute string.
     *
     * unrecognized genes are dropped.
     */
    public void parseGMT(File file,
            ArrayList<Long> nodeIds, ArrayList<String> attributes, HashSet<String> unrecognizedGenes)
                    throws DatamartException, IOException, DataStoreException {

        logger.info("parsing GMT format user data");

        LuceneDataSet luceneDataSet = getLuceneDataSet(getContext());
        Organism organism = luceneDataSet.getOrganismMediator().getOrganism(organismId);

        CSVReader reader = new CSVReader(new InputStreamReader(
                new BufferedInputStream(new FileInputStream(file)), "UTF8"),
                '\t', CSVParser.NULL_CHARACTER);

        final GeneMediator geneMediator = new CachingGeneMediator(luceneDataSet.getGeneMediator());

        // load data into a pair of lists, element i corresponds to nodeId, attribute name
        logger.info("loading attribute data");
        try {
            String[] line = reader.readNext();
            while (line != null) {
                if (line.length < 3) {
                    line = reader.readNext();
                    continue;
                }

                String attribute = line[0];
                String description = line[1];

                for (int i=2; i<line.length; i++) {
                    String geneSymbol = line[i];

                    Long nodeId = geneMediator.getNodeId(organism.getId(), geneSymbol);
                    if (nodeId == null) {
                        unrecognizedGenes.add(geneSymbol);
                    }
                    else {
                        nodeIds.add(nodeId);
                        attributes.add(attribute);
                    }

                    if (sampleAccession == null) {
                        sampleAccession = attribute;
                        sampleName = description;
                    }
                }
                line = reader.readNext();
            }
        }
        finally {
            reader.close();
        }
    }

    protected LuceneDataSet getLuceneDataSet(DataSetContext context) throws IOException {
        LuceneDataSet luceneDataSet = LuceneDataSet.instance(context.getIndexPath());
        return luceneDataSet;
    }

    public DataSetContext getContext() {
        return this.context;
    }

    public NetworkValidationStats getValidationStats() {
        return validationStats;
    }

    public Collection<Long> getAttributeIds() {
        return attributeIds;
    }
    public Collection<? extends List<Long>> getNodeAttributeAssociations() {
        return nodeAttributeAssociations;
    }
}
