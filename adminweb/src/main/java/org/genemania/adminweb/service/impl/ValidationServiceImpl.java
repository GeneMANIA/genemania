package org.genemania.adminweb.service.impl;

import org.genemania.adminweb.dataset.DataSetContext;
import org.genemania.adminweb.entity.Functions;
import org.genemania.adminweb.entity.Identifiers;
import org.genemania.adminweb.entity.Network;
import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.service.BuildService;
import org.genemania.adminweb.service.DataSetManagerService;
import org.genemania.adminweb.service.ValidationService;
import org.genemania.adminweb.validators.Validator;
import org.genemania.adminweb.validators.ValidatorFactory;
import org.genemania.adminweb.validators.impl.AttributeMetadataValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidationServiceImpl implements ValidationService {
    final Logger logger = LoggerFactory.getLogger(ValidationServiceImpl.class);

    @Autowired
    private BuildService buildService;

    @Autowired
    private ValidatorFactory validatorFactory;

    @Autowired
    private DataSetManagerService dataSetManagerService;

    @Override
    public void validateNetwork(Network network) throws DatamartException {

        // ensure we have a dataset to validate against
        DataSetContext context = dataSetManagerService.getContext(network.getOrganism().getId());
        buildService.refresh(context, network.getOrganism().getId());

        // validate the network
        if (Network.TYPE_NETWORK.equalsIgnoreCase(network.getType())) {

            Validator networkValidator = validatorFactory.networkValidator(context, network);
            networkValidator.validate();
        }
        else if (Network.TYPE_ATTRIBUTE.equalsIgnoreCase(network.getType())) {
            Validator attributeValidator = validatorFactory.attributeValidator(context, network);
            attributeValidator.validate();
        }
        else {
            throw new DatamartException("unknown network type: " + network.getType());
        }

        logger.debug("completed network validation");
    }

    @Override
    public void validateIdentifiers(Identifiers identifiers) {
        logger.info("not implemented");
    }

    boolean isAttributeData(Network network) {
        // switch in group name. TODO: make explicit
        // db field, either in dataset, or in group.
        if ("Attribute".equalsIgnoreCase(network.getGroup().getName())) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void validateAttributeMetadata(Network network)
            throws DatamartException {
        AttributeMetadataValidator validator = validatorFactory.attributeMetadataValidator(network);
        validator.validate();
        logger.debug("attribute metadata validation finished");
    }

    @Override
    public void validateFunctions(Functions functions) throws DatamartException {
        // functions have the same format as attributes

        // ensure we have a dataset to validate against
        int organismId = functions.getDataFile().getOrganism().getId();
        DataSetContext context = dataSetManagerService.getContext(organismId);
        buildService.refresh(context, organismId);

        Validator validator = validatorFactory.functionsValidator(context, functions);
        validator.validate();

    }
}
