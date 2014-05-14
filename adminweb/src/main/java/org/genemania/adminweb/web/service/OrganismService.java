package org.genemania.adminweb.web.service;

import java.util.List;

import org.genemania.adminweb.web.model.OrganismForm;
import org.genemania.adminweb.web.model.OrganismTN;
import org.genemania.adminweb.web.model.ViewModel;

public interface OrganismService {

    public void updateModel(ViewModel model);
    public List<OrganismTN> getOrganismsTree();
    public void deleteOrganism(int organismId);
    OrganismTN addOrganism(OrganismForm organismForm);
}
