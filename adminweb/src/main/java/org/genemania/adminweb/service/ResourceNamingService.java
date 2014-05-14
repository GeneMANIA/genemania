package org.genemania.adminweb.service;

import org.genemania.adminweb.entity.AttributeMetadata;
import org.genemania.adminweb.entity.DataFile;
import org.genemania.adminweb.entity.Identifiers;
import org.genemania.adminweb.entity.Network;
import org.genemania.adminweb.entity.Organism;

public interface ResourceNamingService {
    String getName(Network network);
    String getName(Identifiers identifiers);
    String getName(Organism organism, AttributeMetadata attributeMetadata);
    String getName(Organism organism, DataFile dataFile);
}
