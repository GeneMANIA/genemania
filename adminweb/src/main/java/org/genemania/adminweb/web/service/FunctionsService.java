package org.genemania.adminweb.web.service;

import java.io.InputStream;

import org.genemania.adminweb.entity.Functions;


public interface FunctionsService {

    Functions addFunctions(int organismId, String originalFilename,
            InputStream inputStream);

    Functions replaceFunctions(int organismId, int functionsId,
            String originalFilename, InputStream inputStream);

    Functions addFunctionDescriptions(int organismId, int functionsId,
            String originalFilename, InputStream inputStream);

}
