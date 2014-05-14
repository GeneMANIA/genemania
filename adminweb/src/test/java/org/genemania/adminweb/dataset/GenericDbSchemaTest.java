package org.genemania.adminweb.dataset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;

import org.genemania.adminweb.dataset.GenericDbSchema.Parser;
import org.genemania.adminweb.dataset.GenericDbSchema.Record;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class GenericDbSchemaTest {

	@Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

	@Test
	public void testLoad() throws Exception {
		String schemaFilename = getClass().getResource("/SCHEMA.txt").getFile();
		GenericDbSchema schema = new GenericDbSchema(schemaFilename);
		schema.load();

		assertEquals(16, schema.size());
		assertEquals(2, schema.get("TAGS").size());
		assertEquals("ID", schema.get("TAGS").get(0));
	}

	@Test
	public void testRecord() throws Exception {
        String schemaFilename = getClass().getResource("/SCHEMA.txt").getFile();
        GenericDbSchema schema = new GenericDbSchema(schemaFilename);
        schema.load();

        Record record = schema.record("ORGANISMS");
        record.set("ID", 1);
        record.set("NAME", "test");
        record.set("TAXONOMY_ID", "2112");

        try {
            record.set("This is not a valid field", "so this shouldn't work");
            fail("assigned to invalid field");
        }
        catch (IllegalArgumentException e) {}

        assertEquals("test", record.get("NAME"));
        assertEquals("", record.get("DESCRIPTION"));
        assertEquals("1\ttest\t\t\t\t2112\n", record.toString());

        try {
            record.set("INVALID", "THIS BETTER FAIL");
            fail("expected exception");
        }
        catch (Exception e) {}
	}

	@Test
	public void testParser() throws Exception {
        String schemaFilename = getClass().getResource("/SCHEMA.txt").getFile();
        GenericDbSchema schema = new GenericDbSchema(schemaFilename);
        schema.load();

        String organismsFilename = getClass().getResource("/ORGANISMS.txt").getFile();
        Parser parser = schema.parser("ORGANISMS", new File(organismsFilename), "UTF8", '\t');

        int i=0;
        for (Record record: parser) {
            i+=1;
            if (i == 4) {
                assertEquals(record.get("NAME"), "H. sapiens");
            }
        }

        parser.close();

        assertEquals(7, i);
	}
}
