package org.genemania.adminweb.service;

/*
 * object to string and back. e.g. json
 */
public interface MappingService {
    String map(Object object);
    Object unmap(String string, Class clazz);
}
