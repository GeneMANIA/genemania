package org.genemania.adminweb.web.service;

import java.io.InputStream;

import org.genemania.adminweb.entity.DataFile;
import org.genemania.adminweb.exception.DatamartException;

public interface UploadService {

    DataFile addDataFile(int organismId, String originalFilename,
            InputStream inputStream) throws DatamartException;
}
