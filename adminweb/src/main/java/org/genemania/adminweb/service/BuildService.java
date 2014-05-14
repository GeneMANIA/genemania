package org.genemania.adminweb.service;

import org.genemania.adminweb.dataset.DataSetContext;
import org.genemania.adminweb.exception.DatamartException;

/*
 * construct & update a genemania dataset
 * (index & cache) from adminweb metadata &
 * source files.
 */
public interface BuildService {

    void build(DataSetContext context, long organismId) throws DatamartException;
    void refresh(DataSetContext context, long organismId) throws DatamartException;
    void delete(DataSetContext context);

}
