package org.genemania.adminweb.testutils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.genemania.adminweb.entity.Organism;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class ConfigTest extends BaseTest {

    @Test
    public void test() throws Exception {
        getSetupService().setup();
        List<Organism> organisms = getDmdb().getOrganismDao().queryForAll();
        assertNotNull(organisms);
        assertEquals(0, organisms.size());
    }

    @Test
    public void test2() throws Exception {
        getSetupService().setup();
        List<Organism> organisms = getDmdb().getOrganismDao().queryForAll();
        assertNotNull(organisms);
        assertEquals(0, organisms.size());
    }
}
