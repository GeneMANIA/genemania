package org.genemania.adminweb.dao;

import java.sql.SQLException;
import java.util.List;

public interface DatamartDb {
    String info();
    void init() throws SQLException;
    void createTables() throws SQLException;
    void createTablesIfNotExist() throws SQLException;
    void dropTables() throws SQLException;
    void clearTables() throws SQLException;
    List<String> getTableCreateStatements() throws SQLException;
    OrganismDao getOrganismDao();
    NetworkDao getNetworkDao();
    GroupDao getGroupDao();
    FormatDao getFormatDao();
    IdentifiersDao getIdentifiersDao();
    AttributeMetadataDao getAttributeMetadataDao();
    void destroy();
    DataFileDao getDataFileDao();
    FunctionsDao getFunctionsDao();
    DbVersionDao getDbVersionDao();
}
