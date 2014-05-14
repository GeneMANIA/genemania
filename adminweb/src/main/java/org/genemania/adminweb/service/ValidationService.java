package org.genemania.adminweb.service;

import org.genemania.adminweb.entity.Functions;
import org.genemania.adminweb.entity.Identifiers;
import org.genemania.adminweb.entity.Network;
import org.genemania.adminweb.exception.DatamartException;

public interface ValidationService {
    void validateNetwork(Network network) throws DatamartException;
    void validateIdentifiers(Identifiers identifiers) throws DatamartException;
    void validateAttributeMetadata(Network network) throws DatamartException;
    void validateFunctions(Functions functions) throws DatamartException;
}
