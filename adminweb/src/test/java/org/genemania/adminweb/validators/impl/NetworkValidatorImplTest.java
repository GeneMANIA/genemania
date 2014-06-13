package org.genemania.adminweb.validators.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.genemania.adminweb.dataset.DataSetContext;
import org.genemania.adminweb.entity.Network;
import org.genemania.adminweb.entity.Organism;
import org.genemania.adminweb.service.BuildService;
import org.genemania.adminweb.testutils.BaseTest;
import org.genemania.adminweb.testutils.TestDataBuilder;
import org.genemania.adminweb.validators.Validator;
import org.genemania.adminweb.validators.ValidatorFactory;
import org.genemania.adminweb.validators.stats.NetworkValidationStats;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class NetworkValidatorImplTest extends BaseTest {
    @Autowired
    TestDataBuilder testDataBuilder;

    @Autowired
    BuildService builder;

    @Autowired
    ValidatorFactory validatorFactory;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        testDataBuilder.build();
    }

    @Test
    public void testValidateNetwork() throws Exception {

        Organism organism = getDmdb().getOrganismDao().queryForId(testDataBuilder.testOrganismId);
        assertNotNull(organism);

        List<Network> network = getDmdb().getNetworkDao().getNetworks(organism, "test_network");
        assertNotNull(network);
        assertEquals(1, network.size());
        Network testNetwork = network.get(0);

        DataSetContext context = getDataSetManagerService().getContext(organism.getId());
        Validator networkValidator = validatorFactory.networkValidator(context, testNetwork);
        NetworkValidationStats validationStats = networkValidator.validate();

        assertNotNull(validationStats);
    }
}
