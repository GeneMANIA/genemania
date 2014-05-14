package org.genemania.adminweb.service.impl;

import java.io.File;

import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.service.FileStorageService;
import org.springframework.stereotype.Component;

@Component
public class FileStorageServiceImpl implements FileStorageService {

    private String basePath;

    public FileStorageServiceImpl() {}

    public FileStorageServiceImpl(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public void init() throws DatamartException {
        File file = new File(basePath);
        if (!file.isDirectory()) {
            boolean result = file.mkdirs();
            if (!result) {
                throw new DatamartException("failed to create basePath: '" + basePath + "'");
            }
        }
    }

    @Override
    public void destroy() throws DatamartException {
    }

    @Override
    public void remove(String path) throws DatamartException {
        path = fullPath(path);
        File file = new File(path);
        boolean result = file.delete();
        if (!result) {
            throw new DatamartException("failed to delete resource: '" + basePath + "'");
        }
    }

    private String fullPath(String path) {
        return basePath + File.separator + path;
    }

    /*
     * given /a/b/c.txt create folders /a/b
     */
    private void mkdirs(String path) throws DatamartException {

        File file = new File(path);
        String directory = file.getParent();
        File dir = new File(directory);
        if (!dir.isDirectory()) {
            boolean result = dir.mkdirs();
            if (!result) {
                throw new DatamartException("failed to create path: '" + dir + "'");
            }
        }
    }

    @Override
    public boolean exists(String path) throws DatamartException {
        path = fullPath(path);
        File file = new File(path);
        return file.exists();
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    /*
     * doesn't have to exist, return file so can be created
     *
     * (non-Javadoc)
     * @see org.genemania.adminweb.service.FileStorageService#getFile(java.lang.String)
     */
    @Override
    public File getFile(String path) throws DatamartException {
        path = fullPath(path);
        mkdirs(path);
        return new File(path);
    }

    @Override
    public void trash(String path) throws DatamartException {
        // TODO Auto-generated method stub

    }
}
