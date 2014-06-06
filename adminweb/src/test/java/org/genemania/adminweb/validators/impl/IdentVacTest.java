package org.genemania.adminweb.validators.impl;

import org.junit.Test;

import static org.junit.Assert.*;

public class IdentVacTest {

    static final String IDENTIFIERS_FILENAME = "messy_identifier_data.txt";

    @Test
    public void testFileName() throws Exception {

        String filename = getResourceFile("/" + IDENTIFIERS_FILENAME);

        IdentVac vac = new IdentVac("test1");
        vac.process(filename, "\t");

        assertEquals(15, vac.numRecordsRead);
        assertEquals(2, vac.numMissing);
        assertEquals(1, vac.numDups);
        assertEquals(5, vac.numIds);
        assertEquals(11, vac.numSymbols);
        assertEquals(3, vac.numSources);

        assertNotNull(vac.sourceCounts);
        for (Object[] i: vac.sourceCounts) {
            for (Object j: i) {
                System.out.print(j.toString());
                System.out.print(" ");
            }
            System.out.println("");
        }

    }

    String getResourceFile(String resourceName) {
        return getClass().getResource(resourceName).getFile();
    }
}