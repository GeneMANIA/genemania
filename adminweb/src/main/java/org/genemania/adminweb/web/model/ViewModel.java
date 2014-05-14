package org.genemania.adminweb.web.model;

import java.util.Map;

/*
 * generic view object
 */
public interface ViewModel extends Map<String, Object> {

    // vocabulary of keys used in model
    public static final String USER = "user";
    public static final String ALL_ORGANISMS = "organisms";
    public static final String NETWORKS = "networks";
}
