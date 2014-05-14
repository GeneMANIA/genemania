package org.genemania.adminweb.web.service;

import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.web.model.AttributeMetadataForm;
import org.genemania.adminweb.web.model.FunctionsForm;
import org.genemania.adminweb.web.model.NetworkForm;

public interface FormProcessorService {
    void updateNetwork(NetworkForm networkForm) throws DatamartException;
    void updateAdttributeMetadata(AttributeMetadataForm attributeMetadataForm) throws DatamartException;
    void updateFunctions(FunctionsForm functionsForm) throws DatamartException;
}
