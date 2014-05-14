package org.genemania.adminweb.service;

public interface MappingService {
    String map(Object object);
    Object unmap(String string, Class clazz);
}
