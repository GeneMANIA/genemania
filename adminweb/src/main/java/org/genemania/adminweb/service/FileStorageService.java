package org.genemania.adminweb.service;

import java.io.File;

import org.genemania.adminweb.exception.DatamartException;

/*
 * low level storage of data without additional semantics
 */
public interface FileStorageService {
    public void init() throws DatamartException;
    public void destroy() throws DatamartException;
    public void remove(String path) throws DatamartException;
    public void trash(String path) throws DatamartException;
    public boolean exists(String path) throws DatamartException;
    public File getFile(String path) throws DatamartException;
}
