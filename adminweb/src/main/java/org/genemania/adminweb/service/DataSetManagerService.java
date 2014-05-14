package org.genemania.adminweb.service;

import org.genemania.adminweb.dataset.DataSetContext;
import org.genemania.adminweb.exception.DatamartException;

/*
 * There can be multiple data builds, each
 * consisting of a lucene index and corresponding
 * engine cache, in various states of production.
 */
public interface DataSetManagerService {

    public DataSetContext getContext(int organismId);
    public void delete(DataSetContext context) throws DatamartException;

}
