package org.genemania.adminweb.dao.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.genemania.adminweb.dao.*;
import org.genemania.adminweb.entity.*;
import org.springframework.stereotype.Component;

import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

@Component
public class DatamartDbImpl implements DatamartDb {

    String dbUrl;
    String dbUser;
    String dbPass;

    ConnectionSource connectionSource;
    private OrganismDao organismDao;
    private NetworkDao networkDao;
    private GroupDao groupDao;
    private FormatDao formatDao;
    private IdentifiersDao identifiersDao;
    private AttributeMetadataDao attributeMetadataDao;
    private DataFileDao dataFileDao;
    private FunctionsDao functionsDao;
    private DbVersionDao dbVersionDao;

    Class<?> [] domainClasses = {Organism.class, Network.class, Group.class, Format.class,
            Identifiers.class, AttributeMetadata.class, DataFile.class, Functions.class,
            DbVersion.class};

    public void setConnectionSource(ConnectionSource connectionSource) {
        this.connectionSource = connectionSource;
    }

    public DatamartDbImpl() throws SQLException {
    }

    public DatamartDbImpl(String dbUrl, String dbUser, String dbPass) throws SQLException {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPass = dbPass;
    }

    @Override
    public void init() throws SQLException {
        connectionSource = new JdbcPooledConnectionSource(dbUrl, dbUser, dbPass);

        organismDao = DaoManager.createDao(connectionSource, Organism.class);
        networkDao = DaoManager.createDao(connectionSource, Network.class);
        groupDao = DaoManager.createDao(connectionSource, Group.class);
        formatDao = DaoManager.createDao(connectionSource, Format.class);
        identifiersDao = DaoManager.createDao(connectionSource, Identifiers.class);
        attributeMetadataDao = DaoManager.createDao(connectionSource, AttributeMetadata.class);
        dataFileDao = DaoManager.createDao(connectionSource,  DataFile.class);
        functionsDao = DaoManager.createDao(connectionSource, Functions.class);
        dbVersionDao = DaoManager.createDao(connectionSource, DbVersion.class);
    }

    @Override
    public void destroy() {
        connectionSource.closeQuietly();
    }

    @Override
    public void createTables() throws SQLException {
        for (Class<?> domainClass: domainClasses) {
            TableUtils.createTable(connectionSource, domainClass);
        }
    }

    @Override
    public void dropTables() throws SQLException {
        for (Class<?>  domainClass: domainClasses) {
            TableUtils.dropTable(connectionSource, domainClass, true);
        }
    }

    @Override
    public void clearTables() throws SQLException {
        for (Class<?>  domainClass: domainClasses) {
            TableUtils.clearTable(connectionSource, domainClass);
        }
    }

    @Override
    public List<String> getTableCreateStatements() throws SQLException {
        List<String> stmts = new ArrayList<String>();
        for (Class<?>  domainClass: domainClasses) {
            stmts.addAll(TableUtils.getCreateTableStatements(connectionSource, domainClass));
        }
        return stmts;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPass() {
        return dbPass;
    }

    public void setDbPass(String dbPass) {
        this.dbPass = dbPass;
    }

    protected ConnectionSource getConnectionSource() {
        return connectionSource;
    }

    @Override
    public OrganismDao getOrganismDao() {
        return organismDao;
    }

    @Override
    public NetworkDao getNetworkDao() {
        return networkDao;
    }

    @Override
    public GroupDao getGroupDao() {
        return groupDao;
    }


    @Override
    public FormatDao getFormatDao() {
        return formatDao;
    }

    @Override
    public IdentifiersDao getIdentifiersDao() {
        return identifiersDao;
    }

    @Override
    public String info() {
        if (dbUrl != null && dbUrl.startsWith("jdbc:")) {
            return dbUrl.substring(5);
        }
        else {
            return dbUrl;
        }
    }

    @Override
    public AttributeMetadataDao getAttributeMetadataDao() {
        return attributeMetadataDao;
    }

    public DataFileDao getDataFileDao() {
        return dataFileDao;
    }

    public FunctionsDao getFunctionsDao() {
        return functionsDao;
    }

    @Override
    public DbVersionDao getDbVersionDao() {
        return dbVersionDao;
    }
}
