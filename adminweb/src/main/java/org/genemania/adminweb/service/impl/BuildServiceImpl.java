
package org.genemania.adminweb.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.genemania.configobj.ConfigObj;
import org.genemania.adminweb.dao.DatamartDb;
import org.genemania.adminweb.dataset.DataSetContext;
import org.genemania.adminweb.dataset.DatamartToGenericDb;
import org.genemania.adminweb.dataset.DbConfigGenerator;
import org.genemania.adminweb.dataset.LuceneDataSet;
import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.service.BuildService;
import org.genemania.domain.Organism;
import org.genemania.dto.AddOrganismEngineRequestDto;
import org.genemania.dto.AddOrganismEngineResponseDto;
import org.genemania.engine.IMania;
import org.genemania.engine.Mania2;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.cache.FileSerializedObjectCache;
import org.genemania.engine.cache.IObjectCache;
import org.genemania.engine.cache.MemObjectCache;
import org.genemania.engine.cache.SynchronizedObjectCache;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.NodeCursor;
import org.genemania.mediator.lucene.exporter.Generic2LuceneExporter;
import org.genemania.util.NullProgressReporter;
import org.genemania.util.ProgressReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BuildServiceImpl implements BuildService {
    final Logger logger = LoggerFactory.getLogger(BuildServiceImpl.class);

    @Autowired
    DatamartToGenericDb dm2gdb;

    @Autowired
    DatamartDb dmdb;

    @Autowired
    DbConfigGenerator dbConfigGenerator;

    @Override
    public void delete(DataSetContext context) {
        try {
            File file = new File(context.getBasePath());
            FileUtils.deleteDirectory(file);
        }
        catch (IOException e) {
            logger.error("failed to delete dataset", e);
        }
    }

    @Override
    public void refresh(DataSetContext context, long organismId) throws DatamartException {
        // for now, just check if status file exists and assume all is well.
        // later, determine if we need to rebuild identifiers, networks etc
        // for the given organism, depending on adminweb state. may
        // need some dependency management system
        // TODO: fix

        if (!isOk(context)) {
            build(context, organismId);
        }
    }

    @Override
    public void build(DataSetContext context, long organismId) throws DatamartException {

        try {
            setNotOk(context);
            buildGenericDb(context, organismId);
            buildLuceneIndex(context, organismId);
            buildEngineData(context, organismId);
            setOk(context);
        }
        catch (Exception e) {
            throw new DatamartException("failed to build dataset", e);
        }
    }

    void buildGenericDb(DataSetContext context, long organismId) throws DatamartException {
        dm2gdb.build(context.getGenericDbPath(), organismId);
    }

    public DatamartToGenericDb getDm2gdb() {
        return dm2gdb;
    }

    public void setDm2gdb(DatamartToGenericDb dm2gdb) {
        this.dm2gdb = dm2gdb;
    }

    public DatamartDb getDmdb() {
        return dmdb;
    }

    public void setDmdb(DatamartDb dmdb) {
        this.dmdb = dmdb;
    }

    void buildLuceneIndex(DataSetContext context, long organismId) throws DatamartException {

        try {
            String configurationPath = buildConfig(context, organismId);

            String colourConfigPath = context.getBasePath() + File.separator + "colours.txt";
            resourceToFile("/colours.txt", colourConfigPath);
            final Map<String, String> colours = Generic2LuceneExporter.loadColours(colourConfigPath);

            ConfigObj config = new ConfigObj(new FileReader(configurationPath));

            Generic2LuceneExporter exporter = new Generic2LuceneExporter();
            exporter.setNetworkGroupColours(colours);
            exporter.setBasePath(context.getIndexPath());
            exporter.setGenericDbPath(context.getGenericDbPath());
            exporter.setProfileName(null); // default
            exporter.setConfig(config);
            exporter.setIndexPath(context.getIndexPath());

            exporter.export();
        }
        catch (Exception e) {
            throw new DatamartException("failed to build lucene index", e);
        }
    }

    /*
     * was hard to resist calling this buildBuildConfig().
     */
    String buildConfig(DataSetContext context, long organismId) throws IOException, DatamartException {
        String config = dbConfigGenerator.makeConfig(organismId);
        File file = new File(context.basePath + File.separator + "db.cfg");
        FileUtils.write(file, config, "UTF8");
        return file.getPath();
    }

    void buildEngineData(DataSetContext context, long organismId) throws IOException, ApplicationException, DataStoreException {
        IMania mania = getMania(context, true);

        LuceneDataSet luceneDataSet = LuceneDataSet.instance(context.getIndexPath());
        Organism organism = luceneDataSet.getOrganismMediator().getOrganism(organismId);

        AddOrganismEngineRequestDto request = new AddOrganismEngineRequestDto();
        request.setOrganismId(organismId);
        List<Long> nodeIds = loadNodeIds(luceneDataSet.getOrganismMediator().createNodeCursor(organismId), NullProgressReporter.instance());
        logger.info("nodeIds length: " + nodeIds.size());
        request.setNodeIds(nodeIds);
        request.setProgressReporter(NullProgressReporter.instance());

        AddOrganismEngineResponseDto response = mania.addOrganism(request);

    }

    public List<Long> loadNodeIds(NodeCursor cursor, ProgressReporter progress) throws ApplicationException, DataStoreException {

        ArrayList<Long> allNodeIds = new ArrayList<Long>();

        while (cursor.next()) {
            if (progress.isCanceled()) {
                logger.info("cancelled");
                return null;
            }

            allNodeIds.add(cursor.getId());
        }

        return allNodeIds;
    }

    // TODO: move
    public static IMania getMania(DataSetContext context, boolean memCacheEnabled) throws IOException {

        // TODO: wrap in serializer
        IObjectCache objectCache = new FileSerializedObjectCache(context.getCachePath());
        if (memCacheEnabled) {
            objectCache = new MemObjectCache(objectCache);
        }

        objectCache = new SynchronizedObjectCache(objectCache);
        DataCache cache = new DataCache(objectCache);

        IMania mania = new Mania2(cache);
        return mania;

    }

    String getResourceFile(String resourceName) {
        return getClass().getResource(resourceName).getFile();
    }

    public void resourceToFile(String resourceName, String outputPath) throws IOException {
        InputStream input = getClass().getResourceAsStream(resourceName);
        try {
            OutputStream output = new FileOutputStream(outputPath);
            try {
                IOUtils.copy(input, output);
            }
            finally {
                output.close();
            }
        }
        finally {
            input.close();
        }
    }

    public boolean isOk(DataSetContext context) {
        return getOkFile(context).exists();
    }

    public void setOk(DataSetContext context) throws IOException {
        FileUtils.touch(getOkFile(context));
    }

    public void setNotOk(DataSetContext context) throws IOException {
        getOkFile(context).delete();
    }

    File getOkFile(DataSetContext context) {
        return new File(context.getBasePath() + File.separator + "OK.txt");
    }
}
