package org.genemania.adminweb.validators.impl;

import org.genemania.adminweb.dao.DatamartDb;
import org.genemania.adminweb.dataset.DataSetContext;
import org.genemania.adminweb.entity.Identifiers;
import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.service.FileStorageService;
import org.genemania.adminweb.service.MappingService;
import org.genemania.adminweb.validators.Validator;
import org.genemania.adminweb.validators.stats.NetworkValidationStats;

/**
 * validation on a single identifiers file
 */
public class IdentifiersValidatorImpl implements Validator {
    public IdentifiersValidatorImpl(DatamartDb dmdb, MappingService mappingService,
                                    FileStorageService fileStorageService,
                                    DataSetContext context, Identifiers identifiers) {

    }

    @Override
    public NetworkValidationStats validate() throws DatamartException {
        return null;
    }
}
