package org.genemania.adminweb.testutils;

import java.io.File;

import org.genemania.adminweb.dao.impl.DatamartDbImpl;
import org.genemania.adminweb.service.impl.DataSetManagerServiceImpl;
import org.genemania.adminweb.service.impl.FileStorageServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/*
 * customize some of the spring wiring for unit tests, to
 * use temporary storage.
 *
 * here we autowire the concrete implementations setup by the
 * test-context, and complete their setup and initialization.
 */
@ContextConfiguration(locations={"/test-context.xml"})
public abstract class BaseTest {
    // having trouble getting @Rule to work with spring, so create() &
    // delete manually
    TemporaryFolder testFolder;

    @Autowired
    DatamartDbImpl dmdb;

    @Autowired
    FileStorageServiceImpl fileStorageService;

    @Autowired
    DataSetManagerServiceImpl dataSetManagerService;

    @Before
    public void setUp() throws Exception {

        testFolder = new TemporaryFolder();
        testFolder.create();
        File dbfile = testFolder.newFile("testdatamart");
        String dburl = "jdbc:h2:" + dbfile;

        System.out.println("initializing: " + dburl);
        dmdb.setDbUrl(dburl);
        dmdb.setDbUser(null);
        dmdb.setDbPass(null);
        dmdb.init();
        dmdb.createTables();

        fileStorageService.setBasePath(testFolder.newFolder("filestorage").getPath());
        fileStorageService.init();

        dataSetManagerService.setBasePath(testFolder.newFolder("builds").getPath());

    }

    @After
    public void tearDown() throws Exception {
        testFolder.delete();
    }

    public DatamartDbImpl getDmdb() {
        return dmdb;
    }

    public void setDmdb(DatamartDbImpl dmdb) {
        this.dmdb = dmdb;
    }

    public FileStorageServiceImpl getFileStorageService() {
        return fileStorageService;
    }

    public void setFileStorageService(FileStorageServiceImpl fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public DataSetManagerServiceImpl getDataSetManagerService() {
        return dataSetManagerService;
    }

    public void setDataSetManagerService(
            DataSetManagerServiceImpl dataSetManagerService) {
        this.dataSetManagerService = dataSetManagerService;
    }
}
