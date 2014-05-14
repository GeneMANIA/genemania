package org.genemania.adminweb.dataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.genemania.adminweb.exception.DatamartException;
import org.genemania.exception.ApplicationException;

import au.com.bytecode.opencsv.CSVReader;

/*
 * key: genericDb filename
 * value: array of field names
 */
@SuppressWarnings("serial")
public class GenericDbSchema extends HashMap<String, ArrayList<String>>{
    private String filename;

    public GenericDbSchema(String filename) {
        this.filename = filename;
    }

    public void load() throws IOException {

        CSVReader reader = new CSVReader(new FileReader(filename), '\t');
        List<String[]> records = reader.readAll();

        for (String[] record: records) {
            String fileType = record[0];
            ArrayList<String> fields = new ArrayList<String>(Arrays.asList(record));
            fields.remove(0); // don't need the filename
            put(fileType, fields);
        }
    }

    public Record record(String fileType) {
        return this.new Record(fileType);
    }

    public Parser parser(String fileType, File file, String encoding, char sep) throws DatamartException {
        return this.new Parser(fileType, file, encoding, sep);
    }

    /*
     * help generating records formatted for a given profile
     *
     *  Record record = schema.new Record("ORGANISMS.txt");
     *  record.set("ID", organism.getId());
     *  record.set("NAME", organism.getName());
     *
     *  doSomethingWith(record.toString());  // fields ordered correctly,
     *                                       // unspecified values blank
     */
    public class Record {
        static final String SEP = "\t";
        ArrayList<String> names;
        String [] values;

        Record(String fileType) {
            names = GenericDbSchema.this.get(fileType);
            values = new String[names.size()];
        }

        Record(String fileType, String [] values) {
            names = GenericDbSchema.this.get(fileType);
            this.values = values;
        }

        public void set(String name, int value) {
            set(name, "" + value);
        }

        public void set(String name, String value) {
            int i = names.indexOf(name);
            if (i < 0) {
                throw new IllegalArgumentException("invalid field name");
            }
            values[i] = value;
        }

        public String get(String name) {
            int i = names.indexOf(name);
            if (values[i] == null) {
                return "";
            }
            else {
                return values[i];
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(values[0]);
            for (int i=1; i<values.length; i++) {
                builder.append(SEP);
                if (values[i] != null) {
                    builder.append(values[i]);
                }
                else {
                    builder.append("");
                }
            }

            builder.append('\n');
            return builder.toString();
        }
    }

    /*
     * help parse a given file into records using the
     * specified schema
     */
    public class Parser implements Iterable<Record> {
        String fileType;
        CSVReader csvReader;
        protected String[] nextLine;

        public Parser(String fileType, File file, String fileEncoding, char delim) throws DatamartException {
            try {
                this.fileType = fileType;
                InputStream stream = getStream(file);
                Reader source = new BufferedReader(new InputStreamReader(stream, fileEncoding));
                csvReader = new CSVReader(source, delim);
            } catch (Exception e) {
                throw new DatamartException("error reading file", e);
            }
        }

        private InputStream getStream(File file) throws FileNotFoundException {
            try {
                return new GZIPInputStream(new FileInputStream(file));
            } catch (IOException e) {}
            return new FileInputStream(file);
        }

        private void nextLine() {
            try {
                nextLine = csvReader.readNext();
            } catch (Exception e) {
                nextLine = null;
            }
        }

        public void close() throws ApplicationException {
            if (csvReader != null) {
                try {
                    csvReader.close();
                    csvReader = null;
                } catch (Exception e) {
                    throw new ApplicationException(e);
                }
            }
        }

        @Override
        public Iterator<Record> iterator() {
            Iterator<Record> iter = new Iterator<Record>() {

                @Override
                public boolean hasNext() {
                    return nextLine != null;
                }

                @Override
                public Record next() {
                    Record record = new Record(fileType, nextLine);
                    nextLine();
                    return record;
                }

                @Override
                public void remove() {
                    throw new RuntimeException("Not implemented");
                }
            };

            nextLine();
            return iter;
        }
    }
}
