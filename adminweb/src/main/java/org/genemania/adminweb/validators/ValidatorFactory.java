package org.genemania.adminweb.validators;

import org.genemania.adminweb.dao.DatamartDb;
import org.genemania.adminweb.dataset.DataSetContext;
import org.genemania.adminweb.entity.Functions;
import org.genemania.adminweb.entity.Identifiers;
import org.genemania.adminweb.entity.Network;
import org.genemania.adminweb.service.FileStorageService;
import org.genemania.adminweb.service.MappingService;
import org.genemania.adminweb.validators.impl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*
 * want objects initialized with instance data but also wired up with
 * spring dependencies. not sure how to accomplish this most cleanly.
 * use factory wiring up and returning impl classes that are not themselves
 * spring components.
 *
 * also, spring hurts my brain.
 */
@Component
public class ValidatorFactory {

    @Autowired
    DatamartDb dmdb;

    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    MappingService mappingService;

    public Validator networkValidator(DataSetContext context, Network network) {
        return new NetworkValidatorImpl(dmdb, mappingService,
                fileStorageService, context, network);
    }

    public Validator attributeValidator(DataSetContext context, Network network) {
        return new AttributeValidatorImpl(dmdb, mappingService,
                fileStorageService, context, network);
    }

    public AttributeMetadataValidator attributeMetadataValidator(Network network) {
        return new AttributeMetadataValidator(dmdb, fileStorageService, network);
    }

    public FunctionsValidator functionsValidator(DataSetContext context, Functions functions) {
        return new FunctionsValidator(dmdb, mappingService,
                fileStorageService, context, functions);
    }

    public Validator identifiersValidator(DataSetContext context, Identifiers identifiers) {
        return new IdentifiersValidatorImpl(dmdb, mappingService, fileStorageService, context, identifiers);

    }
}
