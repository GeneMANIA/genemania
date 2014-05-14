
package org.genemania.adminweb.testutils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import org.genemania.adminweb.dao.DatamartDb;
import org.genemania.adminweb.entity.Format;
import org.genemania.adminweb.entity.Group;
import org.genemania.adminweb.entity.Identifiers;
import org.genemania.adminweb.entity.Network;
import org.genemania.adminweb.entity.Organism;
import org.genemania.adminweb.service.IdentifiersService;
import org.genemania.adminweb.service.NetworkService;
import org.genemania.adminweb.service.SetupService;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Ignore
@Component
public class TestDataBuilder {
    @Autowired
    public DatamartDb dmdb;

    @Autowired
    public SetupService setupService;

    @Autowired
    public IdentifiersService identifiersService;

    @Autowired
    public NetworkService networkService;

    public int testOrganismId;

    // Bigfoot
    static final String organismName = "Magnum peditum";
    static final String organismCode = "Mp";
    static final String IDENTIFIERS_FILENAME = "Mp_names.txt";
    static final String NETWORK_FILENAME = "Mp_network.txt";
    static final String ATTRIBUTES_FILENAME = "Mp_domains.txt";

    public void build() throws Exception {
        Organism organism = populate();
        testOrganismId = organism.getId();
    }

    // put some test data into the database
    public Organism populate() throws Exception {

        setupService.setup();

        // add an organism
        Organism organism = new Organism();
        organism.setName(organismName);
        organism.setCode(organismCode);
        dmdb.getOrganismDao().create(organism);

        // add identifiers
        String filename = getResourceFile("/" + IDENTIFIERS_FILENAME);
        InputStream inputStream = new BufferedInputStream(new FileInputStream(filename));
        Identifiers identifiers = identifiersService.addIdentifiers(organism.getId(), IDENTIFIERS_FILENAME, inputStream);

        // add network
        Group group = dmdb.getGroupDao().getGroupByCode("coexp");
        Format format = dmdb.getFormatDao().queryForId(1); // TODO
        filename = getResourceFile("/" + NETWORK_FILENAME);
        inputStream = new BufferedInputStream(new FileInputStream(filename));

        Network network = networkService.addNetwork(organism.getId(), group.getId(), NETWORK_FILENAME, inputStream);
        network.setName("test_network");
        network.setFormat(format);
        dmdb.getNetworkDao().update(network);

        // add attributes
        group = dmdb.getGroupDao().getGroupByCode("attrib");
        filename = getResourceFile("/" + NETWORK_FILENAME);
        inputStream = new BufferedInputStream(new FileInputStream(filename));
        network = networkService.addNetwork(organism.getId(), group.getId(), ATTRIBUTES_FILENAME, inputStream);
        network.setName("test_attributes");
        dmdb.getNetworkDao().update(network);

        return organism;
    }

    String getResourceFile(String resourceName) {
        return getClass().getResource(resourceName).getFile();
    }
}
