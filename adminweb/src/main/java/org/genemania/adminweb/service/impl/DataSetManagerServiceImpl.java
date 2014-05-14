package org.genemania.adminweb.service.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.genemania.adminweb.dataset.DataSetContext;
import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.service.DataSetManagerService;
import org.springframework.stereotype.Component;

@Component
public class DataSetManagerServiceImpl implements DataSetManagerService {
    String basePath;

    public DataSetManagerServiceImpl() {}

    public DataSetManagerServiceImpl(String basePath) {
        this.basePath = basePath;
    }

    /*
     * create parent folder containing all the individual data build contexts
     */
    public void init() throws IOException {
        File file = new File(basePath);
        if (!file.exists()) {
            FileUtils.forceMkdir(file);
        }
    }

    /*
     * service cleanup ... nothing here yet
     */
    public void destroy() {
    }

    private void initContext(DataSetContext context) {
        File file = new File(context.getBasePath());
        file.mkdirs();
        file = new File(context.getGenericDbPath());
        file.mkdirs();
        file = new File(context.getIndexPath());
        file.mkdirs();
        file = new File(context.getCachePath());
        file.mkdirs();
    }

    @Override
    public DataSetContext getContext(int organismId) {
        DataSetContext context = new DataSetContext();
        String nextBuild = getNextBuild();

        context.setBuildIdentifier(nextBuild);
        context.setBasePath(basePath + File.separator + organismId + getNextBuild());
        context.setIndexPath(context.getBasePath() + File.separator + "index");
        context.setCachePath(context.getBasePath() + File.separator + "cache");
        context.setGenericDbPath(context.getBasePath() + File.separator + "generic_db");

        initContext(context);
        return context;
    }

    @Override
    public void delete(DataSetContext context) throws DatamartException {
        try {
            FileUtils.deleteDirectory(new File(context.getBasePath()));
        }
        catch (IOException e) {
            throw new DatamartException("Failed to delete context", e);
        }
    }

    // should maybe create build numbers etc, no? remember to include
    // file.separator as prefix when implementing.
    String getNextBuild() {
        return "";
    }


    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

}
