package org.genemania.adminweb.web.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.genemania.adminweb.dao.DatamartDb;
import org.genemania.adminweb.entity.Organism;
import org.genemania.adminweb.web.model.OrganismForm;
import org.genemania.adminweb.web.model.OrganismTN;
import org.genemania.adminweb.web.model.ViewModel;
import org.genemania.adminweb.web.service.OrganismService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrganismServiceImpl implements OrganismService {
    final Logger logger = LoggerFactory.getLogger(OrganismServiceImpl.class);

    @Autowired
    private DatamartDb dmdb;

    @Override
    public void updateModel(ViewModel model) {
        try {
            List<Organism> allOrganisms = dmdb.getOrganismDao().queryForAll();
            model.put(ViewModel.ALL_ORGANISMS, allOrganisms);

        }
        catch (SQLException e) {
            logger.error("failed to update model", e);
        }
    }

    @Override
    public List<OrganismTN> getOrganismsTree() {

        ArrayList<OrganismTN> organismsTree = new ArrayList<OrganismTN>();
        try {
            List<Organism> organisms = dmdb.getOrganismDao().queryForAll();
            for (Organism organism: organisms) {
                organismsTree.add(fromOrganism(organism));
            }

        }
        catch (SQLException e) {
            logger.error("failed to retrieve organisms", e);
        }

        return organismsTree;
    }

    @Override
    public OrganismTN addOrganism(OrganismForm organismForm) {
        logger.info("adding new organism");
        Organism organism = null;
        boolean insert = true;
        try {
            if (organismForm.getOrganismId() != null && !organismForm.getOrganismId().equalsIgnoreCase("")) {
                int id = Integer.parseInt(organismForm.getOrganismId());
                organism = dmdb.getOrganismDao().queryForId(id);
                insert = false;
            }
            else {
                organism = new Organism();
            }

            String name = organismForm.getName();
            if (name == null || name.trim().equals("")) {
                name = "User added organism";
            }
            else {
                name = name.trim();
            }

            organism.setName(name);

            String code = organismForm.getCode();
            if (code == null) {
                code = "";
            }
            else {
                code = code.trim();
            }
            organism.setCode(code);

            if (insert) {
                dmdb.getOrganismDao().create(organism);
            }
            else {
                dmdb.getOrganismDao().update(organism);
            }
        }
        catch (SQLException e) {
            logger.error("failed to add organism", e);
        }

        return fromOrganism(organism);
    }

    @Override
    public void deleteOrganism(int organismId) {
        logger.info("deleting organism " + organismId);

        try {
            Organism organism = dmdb.getOrganismDao().queryForId(organismId);

            // TODO: logging, checking, mark as trashed instead of really deleting?
            dmdb.getOrganismDao().delete(organism);
        }
        catch (SQLException e) {
            logger.error("failed to add organism", e);
        }

    }

    private OrganismTN fromOrganism(Organism organism) {

        OrganismTN node = new OrganismTN(organism.getName());
        node.setId(organism.getId());
        node.setCode(organism.getCode());

        return node;
    }

}
