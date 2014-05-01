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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.genemania.domain.Attribute;
import org.genemania.domain.AttributeGroup;
import org.genemania.domain.Organism;
import org.genemania.dto.AddAttributeGroupEngineRequestDto;
import org.genemania.engine.Mania2;
import org.genemania.engine.actions.support.attribute.AttributeCursor;
import org.genemania.engine.actions.support.attribute.SparseAttributeProfileCursor;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.utils.FileUtils;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.util.NullProgressReporter;
import org.genemania.util.ProgressReporter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import au.com.bytecode.opencsv.CSVReader;

/**
 * build attribute related data structures. attribute
 * data is read from generic_db. We temporarily also read
 * metadata directly, instead of via lucene since those
 * api's don't yet exist.
 *
 */
public class AttributeBuilder extends AbstractEngineApp {

    private static Logger logger = Logger.getLogger(AttributeBuilder.class);
    static char SEP = '\t';
    
    @Option(name = "-genericDbDir", usage = "folder containing data for loading")
    private static String genericDbDir;
    
    Mania2 mania;
        
    public void processAllOrganisms(ProgressReporter progress) throws ApplicationException, DataStoreException {
        try {
            for (Organism organism: organismMediator.getAllOrganisms()) {
                processOrganism(organism, progress);
            }
        }
        finally {
        }
    }

    public void processOrganism(int orgId, ProgressReporter progress) throws ApplicationException, DataStoreException {
        Organism organism = organismMediator.getOrganism(orgId);
        processOrganism(organism, progress);
    }

    @SuppressWarnings("unchecked")
    public void processOrganism(Organism organism, ProgressReporter progress) throws ApplicationException, DataStoreException {
        logger.info("processing organism " + organism.getId() + " " + organism.getName());

        buildAttributes(organism, progress);
    }

    /*
     * attribute group ids and attribute ids, 
     * read from generic_db, write to engine cache
     */
    public void buildAttributes(Organism organism, ProgressReporter progress) throws ApplicationException {
        try {
            List<AttributeGroup> grps = getAttributeGroups(organism.getId());
        
            for (AttributeGroup group: grps) {
                logger.info("processing group " + group.getId());

                AddAttributeGroupEngineRequestDto request = new AddAttributeGroupEngineRequestDto();
                request.setAttributeGroupId(group.getId());
                request.setNamespace(Data.CORE);
                request.setOrganismId(organism.getId());
                request.setProgressReporter(progress);
                                              
                ArrayList<Long> attributeIds = new ArrayList<Long>();        
                List<Attribute> attrs = getAttributes(group.getId());
                for (Attribute attr: attrs) {
                    attributeIds.add(attr.getId());
                }
                
                request.setAttributeIds(attributeIds);
                                
                String filename = genericDbDir + File.separator + "ATTRIBUTES" + File.separator + group.getId() + ".txt";
                ArrayList<ArrayList<Long>> nodeAttributeAssociations = loadAssocs(filename);
                
                request.setNodeAttributeAssociations(nodeAttributeAssociations);
                mania.addAttributeGroup(request);

            }
        }
        catch (IOException e) {
            throw new ApplicationException(e);
        }
    }
    
    ArrayList<ArrayList<Long>> loadAssocs(String filename) throws FileNotFoundException, ApplicationException {
        ArrayList<ArrayList<Long>> nodeAttributeAssociations = new ArrayList<ArrayList<Long>>();
        
        AttributeCursor cursor = new SparseAttributeProfileCursor(new FileReader(filename), SEP);
        while (cursor.next()) {
            ArrayList<Long> assoc = new ArrayList<Long>();
            assoc.add(cursor.getNodeId());
            assoc.add(cursor.getAttributeId());
            
            nodeAttributeAssociations.add(assoc);
        }
        
        return nodeAttributeAssociations;
    }
    
    public boolean getCommandLineArgs(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        }
        catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java -jar myprogram.jar [options...] arguments...");
            parser.printUsage(System.err);
            return false;
        }

        return true;
    }

    public void createCacheDir() {
        File dir = new File(getCacheDir());
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    public void logParams() {
        logger.info("cache dir: " + getCacheDir());

    }
    
    @Override 
    public void process() throws DataStoreException, ApplicationException, IOException {
        String schemaFile = genericDbDir + File.separator + "SCHEMA.txt";
        loadSchemas(schemaFile);
        processAllOrganisms(NullProgressReporter.instance());
    }
    
    @Override 
    public void init() throws Exception {    	
    	super.init();
        createCacheDir();
        mania = new Mania2(cache);
        logParams();
    }

    public static void main(String[] args) throws Exception {

        AttributeBuilder cacheBuilder = new AttributeBuilder();
        if (!cacheBuilder.getCommandLineArgs(args)) {
            System.exit(1);
        }      

        try {
        	cacheBuilder.init();
            cacheBuilder.process();
            cacheBuilder.cleanup();
        }
        catch (Exception e) {
            logger.error("Fatal error", e);
            System.exit(1);
        }
    }
    
    static class TextCursor {
        Map<String, Integer> schema = new HashMap<String, Integer>();
        CSVReader reader;
        String [] line;
        String filterKey;
        String filterVal;
        TextCursor(String [] schema, String filename, String filterKey, String filterVal) throws FileNotFoundException {
            this.filterKey = filterKey;
            this.filterVal = filterVal;
            loadSchema(schema);
            reader = new CSVReader(new BufferedReader(new FileReader(filename)), SEP);
        }
        
        void loadSchema(String [] schema) {
            for (int i=0; i<schema.length; i++) {
                this.schema.put(schema[i], i);
            }
        }
        
        boolean next() throws IOException {
            
            while (true) {
                line = reader.readNext();
                if (line == null) {
                    return false;                
                }

                // check filter if we have one
                if (filterKey != null && line[schema.get(filterKey)].equals(filterVal)) {
                    return true;
                }
            }
        }
        
        String get(String field) {
            int index = schema.get(field); // exception unboxing if field is wrong
            return line[index];
        }
        
        void close() throws IOException {
            reader.close();
        }
    }
    
    Map<String, String[]> schemas;
    
    void loadSchemas(String schemaFile) throws IOException {
        schemas = new HashMap<String, String[]>();
        Reader reader = new BufferedReader(new FileReader(schemaFile));
        Vector<String[]> lines = FileUtils.loadRecords(reader, SEP, '#');
        for (String [] line: lines) {
            String name = line[0];
            String [] fields = new String[line.length-1];
            System.arraycopy(line, 1, fields, 0, fields.length);
            schemas.put(name, fields);
        }
    }
    
    private List<AttributeGroup> getAttributeGroups(long organismId) throws NumberFormatException, IOException {
        
        ArrayList<AttributeGroup> result = new ArrayList<AttributeGroup>();
        
        String filename = genericDbDir + File.separator + "ATTRIBUTE_GROUPS.txt";
        String [] schema = schemas.get("ATTRIBUTE_GROUPS");
        TextCursor cursor = new TextCursor(schema, filename, "ORGANISM_ID", "" + organismId);
        try {
            while (cursor.next()) {
                AttributeGroup grp = new AttributeGroup();
                grp.setId(Long.parseLong(cursor.get("ID")));
                result.add(grp);
            }
        }
        finally {
            cursor.close();
        }
        
        return result;
    }
    
    private List<Attribute> getAttributes(long groupId) throws IOException {
        ArrayList<Attribute> result = new ArrayList<Attribute>();
        
        String filename = genericDbDir + File.separator + "ATTRIBUTES.txt";
        String [] schema = schemas.get("ATTRIBUTES");
        TextCursor cursor = new TextCursor(schema, filename, "ATTRIBUTE_GROUP_ID", "" + groupId);
        try {
            while (cursor.next()) {
                Attribute attr = new Attribute();
                attr.setId(Long.parseLong(cursor.get("ID")));
                result.add(attr);
            }
        }
        finally {
            cursor.close();
        }
        
        return result;
    }
}
