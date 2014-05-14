package org.genemania.adminweb.service.impl;

import static org.genemania.adminweb.service.impl.ResourceNamingServiceImpl.simplifyName;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ResourceNamingServiceImplTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSimplifyName() {
        assertEquals("xyz.txt", simplifyName("xyz.txt"));
        assertEquals("_ab_CD0-5.txt", simplifyName(" ab_CD0-5().txt"));
    }

}
