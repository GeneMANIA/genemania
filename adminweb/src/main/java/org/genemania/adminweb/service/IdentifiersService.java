package org.genemania.adminweb.service;

import java.io.File;
import java.io.InputStream;

import org.genemania.adminweb.entity.Identifiers;
import org.genemania.adminweb.exception.DatamartException;

public interface IdentifiersService {

    public Identifiers addIdentifiers(int organismId,
            String originalFilename, InputStream inputStream);

    public Identifiers replaceIdentifiers(int organismId, int identifiersId,
            String originalFilename, InputStream inputStream);

    public void deleteIdentifiers(int organismId, int identifiersId) throws DatamartException;

    public File getIdentifiersFile(Identifiers identifiers) throws DatamartException;

}
