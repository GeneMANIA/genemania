package org.genemania.adminweb.web.service;

import javax.servlet.http.HttpServletRequest;

import org.genemania.adminweb.web.model.ViewModel;
import org.genemania.adminweb.web.model.impl.ModelImpl;

public class ServiceSupport {
    public static ViewModel generateModel(HttpServletRequest request) {
        ViewModel model = new ModelImpl();        
        processParameters(request, model);
        // processHeaders, processCookies, etc
        return model;
    }

    private static void processParameters(HttpServletRequest request, ViewModel model) {
    }
}
