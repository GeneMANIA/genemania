package org.genemania.adminweb.validators.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.genemania.adminweb.dao.DatamartDb;
import org.genemania.adminweb.dataset.DataSetContext;
import org.genemania.adminweb.entity.Network;
import org.genemania.adminweb.entity.Organism;
import org.genemania.adminweb.service.BuildService;
import org.genemania.adminweb.service.FileStorageService;
import org.genemania.adminweb.service.MappingService;
import org.genemania.adminweb.testutils.BaseTest;
import org.genemania.adminweb.testutils.TestDataBuilder;
import org.genemania.adminweb.validators.Validator;
import org.genemania.adminweb.validators.stats.NetworkValidationStats;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class AttributeValidatorImplTest extends BaseTest {
    @Autowired
    TestDataBuilder testDataBuilder;

    @Autowired
    BuildService builder;

    @Autowired
    DatamartDb dmdb;

    @Autowired
    MappingService mappingService;

    @Autowired
    FileStorageService fileStorageService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        testDataBuilder.build();
    }

    @Override
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testValidateAttributes() throws Exception {

        Organism organism = getDmdb().getOrganismDao().queryForId(testDataBuilder.testOrganismId);
        assertNotNull(organism);

        List<Network> network = getDmdb().getNetworkDao().getNetworks(organism, "test_network");
        assertNotNull(network);
        assertEquals(1, network.size());
        Network testNetwork = network.get(0);

        DataSetContext context = getDataSetManagerService().getContext(organism.getId());
        builder.build(context, organism.getId());
        Validator attributeValidator = new AttributeValidatorImpl(dmdb, mappingService,
                fileStorageService, context, testNetwork);
        NetworkValidationStats validationStats = attributeValidator.validate();

        assertNotNull(validationStats);
    }
}
