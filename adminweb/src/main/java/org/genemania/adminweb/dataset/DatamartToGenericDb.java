
package org.genemania.adminweb.dataset;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.genemania.adminweb.dao.DatamartDb;
import org.genemania.adminweb.dataset.GenericDbSchema.Record;
import org.genemania.adminweb.entity.Identifiers;
import org.genemania.adminweb.entity.Organism;
import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

@Component
public class DatamartToGenericDb {
    final Logger logger = LoggerFactory.getLogger(DatamartToGenericDb.class);

    @Autowired
    DatamartDb dmdb;

    @Autowired
    FileStorageService fileStorageService;

    public static final String SCHEMA_FILETYPE = "SCHEMA";
    public static final String ORGANISMS_FILETYPE = "ORGANISMS";
    public static final String IDENTIFIERS_FILETYPE = "GENES";
    public static final String NODES_FILETYPE = "NODES";
    public static final String GENE_NAMING_SOURCES_FILETYPE = "GENE_NAMING_SOURCES";

    public void build(String dir, long organismId) throws DatamartException {
        try {
            Organism organism = dmdb.getOrganismDao().queryForId((int) organismId);

            String schemaFilename = makeFilename(dir, SCHEMA_FILETYPE);
            makeSchema(schemaFilename);

            GenericDbSchema schema = new GenericDbSchema(schemaFilename);
            schema.load();

            makeOrganisms(schema, dir, organism);
            makeNamingSourceIds(schema, dir);
            Set<Integer> nodes = makeIdentifiers(schema, dir, organism);
            makeNodes(schema, dir, organism, nodes);
            makeNetworks(dir, organism);
            makeStubGenericDbFiles(dir);
        }
        catch (IOException e) {
            throw new DatamartException("Failed to create generic db", e);
        }
        catch (SQLException e) {
            throw new DatamartException("Failed to create generic db", e);
        }

    };

    private void makeNodes(GenericDbSchema schema, String dir,
            Organism organism, Set<Integer> nodes) throws IOException {

        File file = new File(makeFilename(dir, NODES_FILETYPE));
        FileUtils.touch(file);

        for (Integer i: nodes) {
            Record record = schema.new Record(NODES_FILETYPE);
            record.set("ID", i);
            record.set("NAME", "node " + i);
            record.set("GENE_DATA_ID", i);
            record.set("ORGANISM_ID", organism.getId());

            FileUtils.write(file, record.toString(), true);
        }
    }

    private void makeNetworks(String dir, Organism organism) {

    }

    void makeOrganisms(GenericDbSchema schema, String dir, Organism organism) throws IOException {
        File file = new File(makeFilename(dir, ORGANISMS_FILETYPE));
        FileUtils.touch(file);
        appendOrganism(schema, organism, file);
    }

    private void appendOrganism(GenericDbSchema schema, Organism organism, File file) throws IOException {
        Record record = schema.new Record(ORGANISMS_FILETYPE);
        record.set("ID", organism.getId());
        record.set("NAME", makeShortName(organism.getName()));
        record.set("ALIAS",  organism.getName());
        record.set("ONTOLOGY_ID", 0); // TODO: temp
        record.set("TAXONOMY_ID", 0); // TODO: temp

        FileUtils.write(file, record.toString(), true);
    }

    // TODO: temp hack, need to store more variations of organism name in adminweb db
    public static String makeShortName(String name) {
      String [] parts = name.split(" ");
      String shortName;
      if (parts.length == 2) {
          shortName = parts[0].charAt(0) + ". " + parts[1];
      }
      else {
          shortName = name;
      }
      return shortName;
    }

    Set<Integer> makeIdentifiers(GenericDbSchema schema, String dir, Organism organism) throws IOException, DatamartException, SQLException {
        File file = new File(makeFilename(dir, IDENTIFIERS_FILETYPE));
        FileUtils.touch(file);

        Set<Integer> nodes = appendIdentifiers(schema, organism, file);
        return nodes;
    }

    private Set<Integer> appendIdentifiers(GenericDbSchema schema, Organism organism, File file) throws IOException, DatamartException, SQLException {
        Set<Integer> nodes = new HashSet<Integer>();
        List<Identifiers> identifiersList = dmdb.getIdentifiersDao().getIdentifiers(organism);
        int i=1;
        for (Identifiers identifiers: identifiersList) {
            Writer writer = new BufferedWriter(new OutputStreamWriter(
                    FileUtils.openOutputStream(file, true), "UTF8"));

            try {
                dmdb.getDataFileDao().refresh(identifiers.getDataFile());
                File identifiersFile = fileStorageService.getFile(identifiers.getDataFile().getFilename());

                InputStream identifiersInputStream = new FileInputStream(identifiersFile);
                CSVReader reader = new CSVReader(new InputStreamReader(
                        new BufferedInputStream(identifiersInputStream), "UTF8"), '\t', CSVParser.NULL_CHARACTER);
                String[] line = reader.readNext();

                while (line != null) {
                    Record record = schema.record(IDENTIFIERS_FILETYPE);
                    Integer nodeId = parseNodeId(line[0]);
                    nodes.add(nodeId);

                    record.set("ID", i);
                    record.set("SYMBOL", line[1]);
                    record.set("SYMBOL_TYPE", line[2]);
                    record.set("ORGANISM_ID", organism.getId());
                    record.set("NODE_ID", nodeId);
                    record.set("DEFAULT_SELECTED", 0);
                    record.set("NAMING_SOURCE_ID", 0);


                    writer.write(record.toString());

                    i += 1;
                    line = reader.readNext();
                }
            }
            finally {
                writer.close();
            }
        }

        return nodes;
    }

    /*
     * historically, we prefix node ids with the organism short code, 'At:12345'.
     * probably shouldn't anymore, but strip it off if present.
     */
    private Integer parseNodeId(String nodeId) {
        int i = nodeId.indexOf(':');
        if (i >= 0) {
            return Integer.parseInt(nodeId.substring(i+1));
        }
        else {
            return Integer.parseInt(nodeId);
        }
    }

    // TODO: temp hardcoding, put in adminweb db
    void makeNamingSourceIds(GenericDbSchema schema, String dir) throws IOException {
        File file = new File(makeFilename(dir, GENE_NAMING_SOURCES_FILETYPE));
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("1\tEntrez Gene ID\t1\tEntrez");
        lines.add("2\tRefSeq Protein ID\t3\tEntrez");
        /*
1       Entrez Gene ID  1       Entrez
2       RefSeq Protein ID       3       Entrez
3       Entrez Gene Name        8       Entrez
4       RefSeq mRNA ID  6       Entrez
5       Uniprot ID      4       Uniprot
6       TAIR ID 5       TAIR
7       Synonym 0       Synonym
8       Ensembl Gene ID 7       Ensembl
9       Ensembl Gene Name       9       Ensembl
10      Ensembl Protein ID      2       Ensembl
         */
        FileUtils.writeLines(file, lines);
    }

    public void resourceToFile(String resourceName, String outputPath) throws IOException {
        InputStream input = getClass().getResourceAsStream(resourceName);
        try {
            OutputStream output = new FileOutputStream(outputPath);
            try {
                IOUtils.copy(input, output);
            }
            finally {
                output.close();
            }
        }
        finally {
            input.close();
        }
    }

    void makeSchema(String schemaFilename) throws IOException {
        resourceToFile("/SCHEMA.txt", schemaFilename);
    }

    private String makeFilename(String dir, String filetype) {
        return dir + File.separator + filetype + ".txt";
    }

    void makeStubGenericDbFiles(String dir) throws IOException {
        GenericDbSchema schema = new GenericDbSchema(makeFilename(dir, SCHEMA_FILETYPE));
        schema.load();

        for (String fileType: schema.keySet()) {
            File file = new File(makeFilename(dir, fileType));
            if (!file.exists()) {
                FileUtils.touch(file);
            }
        }
    }

    public DatamartDb getDatamartDb() {
        return dmdb;
    }

    public void setDatamartDb(DatamartDb datamartDb) {
        this.dmdb = datamartDb;
    }
}
