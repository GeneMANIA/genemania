package org.genemania.adminweb.service;

import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.service.impl.PubmedServiceImpl.PubmedInfo;

public interface PubmedService {

    public PubmedInfo getInfo(long pubmedId) throws DatamartException;

}
