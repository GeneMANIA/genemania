package org.genemania.adminweb.service.impl;

import java.io.File;

import org.genemania.adminweb.entity.AttributeMetadata;
import org.genemania.adminweb.entity.DataFile;
import org.genemania.adminweb.entity.Identifiers;
import org.genemania.adminweb.entity.Network;
import org.genemania.adminweb.entity.Organism;
import org.genemania.adminweb.service.ResourceNamingService;
import org.springframework.stereotype.Component;

/*
 * no reason why the filenames provided can't collide, so we store
 * the files as:
 *
 *     ORGID/FT/ID_SIMPLENAME
 *
 * ORGID: organism id
 * FT: either 'networks' for dataset files, or 'identifiers' for identifier files
 * ID: the database id for the dataset or identifier record
 * SIMPLENAME: the provided filename with everything but ascii alphanumeric chars
 *   and '+-_.' removed, and whitespace replaced with underscores. because in
 *   real life someone will want to look inside a file on disk from outside the system
 *   and it will be painful to find otherwise
 */
@Component
public class ResourceNamingServiceImpl implements ResourceNamingService {

    public static final String IDENTS = "identifiers";
    public static final String NETWORKS = "networks";
    public static final String ATTR_META = "attr_meta";

    @Override
    public String getName(Network network) {
//        return network.getOrganism().getId()
//                + File.separator + NETWORKS + File.separator
//                + network.getId() + "_" + simplifyName(network.getOriginalFilename());
        throw new RuntimeException("don't use this anymore!");
    }

    @Override
    public String getName(Identifiers identifiers) {
//        return identifiers.getOrganism().getId()
//                + File.separator + IDENTS + File.separator
//                + identifiers.getId() + "_" + simplifyName(identifiers.getOriginalFilename());
        throw new RuntimeException("don't use this anymore!");
    }

    @Override
    public String getName(Organism organism, AttributeMetadata attributeMetadata) {
//        return organism.getId() + File.separator + ATTR_META + File.separator
//                + attributeMetadata.getId() + "_" + simplifyName(attributeMetadata.getOriginalFilename());
        throw new RuntimeException("don't use this anymore!");
    }

    @Override
    public String getName(Organism organism, DataFile dataFile) {
        return organism.getId() + File.separator + dataFile.getId()
                + "_" + simplifyName(dataFile.getOriginalFilename());
    }
    /*
     * strip non-alphanumeric chars, replace whitespace with underscores
     */
    public static String simplifyName(String name) {
        String fixed = name.replaceAll(" ", "_");
        fixed = fixed.replaceAll("[^0-9a-zA-Z_\\-\\.]", "");
        return fixed;
    }


}
