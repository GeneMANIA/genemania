package org.genemania.adminweb.service;

import org.genemania.adminweb.entity.Network;
import org.genemania.adminweb.exception.DatamartException;

public interface MetadataService {

    public void updateMetadata(Network network) throws DatamartException;

}
