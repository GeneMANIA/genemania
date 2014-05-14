package org.genemania.adminweb.service.impl;

import java.io.File;

import org.genemania.adminweb.dataset.DataSetContext;
import org.genemania.adminweb.service.BuildService;
import org.genemania.adminweb.service.DataSetManagerService;
import org.genemania.adminweb.testutils.BaseTest;
import org.genemania.adminweb.testutils.TestDataBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class BuildServiceImplTest extends BaseTest {

    @Autowired
	TestDataBuilder testDataBuilder;

    @Autowired
    BuildService builder;

    @Autowired
    DataSetManagerService dataSetManagerService;

	File dbfile;
	String dburl;

	@Override
    @Before
	public void setUp() throws Exception {
	    super.setUp();
	    testDataBuilder.build();
 	}

	@Test
	public void testBuild() throws Exception {
	    DataSetContext context = dataSetManagerService.getContext(testDataBuilder.testOrganismId);
		builder.build(context, testDataBuilder.testOrganismId);
	}
}
