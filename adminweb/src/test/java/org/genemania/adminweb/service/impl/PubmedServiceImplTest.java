package org.genemania.adminweb.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.genemania.adminweb.service.PubmedService;
import org.genemania.adminweb.service.impl.PubmedServiceImpl.PubmedInfo;
import org.genemania.adminweb.testutils.BaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class PubmedServiceImplTest extends BaseTest {

    @Autowired
    PubmedService pubmedService;

    @Override
    @Before
    public void setUp() throws Exception {
    }

    @Override
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() throws Exception {
        long pubmedId = 20576703;
        PubmedInfo pubmedInfo = pubmedService.getInfo(pubmedId);
        assertNotNull(pubmedInfo);
        assertEquals("The GeneMANIA prediction server: biological network integration for gene prioritization and predicting gene function.",
                pubmedInfo.title);
        assertEquals("2010", pubmedInfo.year);
        assertEquals("Warde-Farley", pubmedInfo.faln);
        assertEquals("Morris", pubmedInfo.laln);
    }
}
