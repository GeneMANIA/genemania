package org.genemania.adminweb.dataset;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.genemania.adminweb.service.DataSetManagerService;
import org.genemania.adminweb.testutils.BaseTest;
import org.genemania.adminweb.testutils.TestDataBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class DatamartToGenericDbTest extends BaseTest {

    @Autowired
    DataSetManagerService dataSetManagerService;

    @Autowired
    DatamartToGenericDb datamartToGenericDb;

    @Autowired
    TestDataBuilder testDataBuilder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        testDataBuilder.build();
    }

    @Test
    public void test() throws Exception {
        assertNotNull(datamartToGenericDb);

        DataSetContext context = dataSetManagerService.getContext(testDataBuilder.testOrganismId);
        datamartToGenericDb.build(context.getGenericDbPath(), testDataBuilder.testOrganismId);

        exists(context, "SCHEMA.txt");
        exists(context, "ORGANISMS.txt");
        exists(context, "NETWORKS.txt");
        exists(context, "NODES.txt");
        exists(context, "GENES.txt");

        int n = countLines(context, "NODES.txt");
        System.out.println("NODES.txt length: " + n);

        n = countLines(context, "GENES.txt");
        System.out.println("GENES.txt length: " + n);
    }

    void exists(DataSetContext context, String filename) {
        File file = new File(context.getGenericDbPath() + File.separator + filename);
        assertTrue(filename + " must exist", file.exists());
    }

    int countLines(DataSetContext context, String filename) throws IOException {
        File file = new File(context.getGenericDbPath() + File.separator + filename);
        LineIterator iter = FileUtils.lineIterator(file);

        int lineCount = 0;
        try {
            while (iter.hasNext()) {
                lineCount += 1;
                iter.next();
            }
        }
        finally {
            iter.close();
        }

        return lineCount;
    }
}
