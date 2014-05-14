package org.genemania.adminweb.validators.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.genemania.adminweb.dao.DatamartDb;
import org.genemania.adminweb.dataset.DataSetContext;
import org.genemania.adminweb.entity.Network;
import org.genemania.adminweb.service.FileStorageService;
import org.genemania.adminweb.service.MappingService;
import org.genemania.adminweb.validators.stats.NetworkValidationStats;
import org.genemania.dto.AddAttributeGroupEngineRequestDto;
import org.genemania.dto.AddAttributeGroupEngineResponseDto;
import org.genemania.engine.IMania;
import org.genemania.exception.ApplicationException;
import org.genemania.util.NullProgressReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttributeValidatorImpl extends BaseValidator {
    final Logger logger = LoggerFactory.getLogger(AttributeValidatorImpl.class);

    private int organismId;
    private int networkId;

    public AttributeValidatorImpl(DatamartDb dmdb, MappingService mappingService,
            FileStorageService fileStorageService,
            DataSetContext context, Network network) {
        super(dmdb, mappingService, fileStorageService, context, network);
        // for convenience
        this.organismId = network.getOrganism().getId();
        this.networkId = network.getId();
    }

    public void load(Collection<Long> attributeIds, Collection<? extends List<Long>> nodeAttributeAssociations) throws IOException, ApplicationException {

        AddAttributeGroupEngineRequestDto request = new AddAttributeGroupEngineRequestDto();
        request.setOrganismId(organismId);
        request.setAttributeGroupId(-networkId);
        request.setAttributeIds(attributeIds);
        request.setNodeAttributeAssociations(nodeAttributeAssociations);
        request.setNamespace(VALIDATION_NAMESPACE);
        request.setProgressReporter(NullProgressReporter.instance());

        // TODO: reorganize getting mania
        IMania mania = getMania(getContext());

        AddAttributeGroupEngineResponseDto response = mania.addAttributeGroup(request);
    }

    @Override
    public NetworkValidationStats process() throws Exception {
        String path = getNetwork().getDataFile().getFilename();
        File file = getFileStorageService().getFile(path);
        AttributeParser parser = new AttributeParser(getContext(), organismId);
        parser.parse(file);
        load(parser.getAttributeIds(), parser.getNodeAttributeAssociations());

        NetworkValidationStats validationStats = parser.getValidationStats();
        validationStats.setProcessingDescription("Attributes");
        validationStats.setStatus("OK");
        return validationStats;
    }
}
