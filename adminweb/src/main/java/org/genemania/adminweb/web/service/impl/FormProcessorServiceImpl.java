package org.genemania.adminweb.web.service.impl;

import java.sql.SQLException;

import org.genemania.adminweb.dao.DatamartDb;
import org.genemania.adminweb.entity.AttributeMetadata;
import org.genemania.adminweb.entity.Functions;
import org.genemania.adminweb.entity.Network;
import org.genemania.adminweb.entity.Organism;
import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.service.FileStorageService;
import org.genemania.adminweb.service.NetworkService;
import org.genemania.adminweb.web.model.AttributeMetadataForm;
import org.genemania.adminweb.web.model.FunctionsForm;
import org.genemania.adminweb.web.model.NetworkForm;
import org.genemania.adminweb.web.service.FormProcessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FormProcessorServiceImpl implements FormProcessorService {

    @Autowired
    DatamartDb dmdb;

    @Autowired
    NetworkService networkService;

    @Autowired
    FileStorageService fileStorageService;

    @Override
    public void updateNetwork(NetworkForm networkForm) throws DatamartException {
        try {
            Organism organism = dmdb.getOrganismDao().queryForId(networkForm.getOrganismId());
            Network network = dmdb.getNetworkDao().queryForId(networkForm.getNetworkId());

            if (organism == null || network.getOrganism().getId() != organism.getId()) {
                throw new DatamartException("inconsistent data");
            }

            network.setName(networkForm.getName());
            network.setDescription(networkForm.getDescription());
            network.setComment(networkForm.getComment());

            networkService.refreshPubmed(network, networkForm.getPubmedId());

            network.setDefault(networkForm.isDefault());
            network.setRestrictedLicense(networkForm.isRestrictedLicense());
            network.setEnabled(networkForm.isEnabled());

            dmdb.getNetworkDao().update(network);
        }
        catch (SQLException e) {
            throw new DatamartException("failed to update network", e);
        }
    }

    @Override
    public void updateFunctions(FunctionsForm functionsForm) throws DatamartException {
        try {
            Organism organism = dmdb.getOrganismDao().queryForId(functionsForm.getOrganismId());
            Functions functions = dmdb.getFunctionsDao().queryForId(functionsForm.getFunctionsId());
            dmdb.getDataFileDao().refresh(functions.getDataFile());

            check(organism, functions);

            functions.setComment(functionsForm.getComment());
            functions.setFunctionType(functionsForm.getUsage());

            dmdb.getFunctionsDao().update(functions);
        }
        catch (SQLException e) {
            throw new DatamartException("failed to update network", e);
        }
    }

    /*
     * the organism id specified in the input should match that of
     * the associated with the function record in the db
     */
    private void check(Organism organism, Functions functions) throws DatamartException {
        if (organism == null) {
            throw new DatamartException("failed to find organism");
        }
        if (functions == null) {
            throw new DatamartException("failed to find functions");
        }

        if (functions.getDataFile().getOrganism().getId() != organism.getId()) {
            throw new DatamartException("inconsistent data");
        }
    }

    @Override
    public void updateAdttributeMetadata(
            AttributeMetadataForm form)
                    throws DatamartException {
        try {
            Organism organism = dmdb.getOrganismDao().queryForId(form.getOrganismId());
            Network network = dmdb.getNetworkDao().queryForId(form.getNetworkId());

            if (organism == null || network.getOrganism().getId() != organism.getId()) {
                throw new DatamartException("inconsistent data");
            }

            AttributeMetadata md = network.getAttributeMetadata();
            if (md == null) {
                md = new AttributeMetadata();
            }
            else {
                dmdb.getAttributeMetadataDao().refresh(md);
            }

            md.setLinkoutLabel(form.getLinkoutLabel());
            md.setLinkoutUrl(form.getLinkoutUrl());

            dmdb.getAttributeMetadataDao().createOrUpdate(md);

            if (network.getAttributeMetadata() == null) {
                network.setAttributeMetadata(md);
                dmdb.getNetworkDao().update(network);
            }
        }
        catch (SQLException e) {
            throw new DatamartException("failed to update network", e);
        }

    }

}

