package org.genemania.adminweb.validators.impl;

import java.sql.SQLException;
import java.util.List;

/**
 * Vacuum up some messy identifiers
 */
public class IdentVac extends H2Vac {

    int numRecordsRead;
    int numMissing;
    int numDups;
    int numIds;
    int numSymbols;
    int numSources;
    List<Object[]> sourceCounts;

    public IdentVac(String dbName) throws ClassNotFoundException, SQLException {
        super(dbName);
    }

    public void countRecordsRead() throws SQLException {
        numRecordsRead = selectOneInt("SELECT COUNT(*) FROM DATA;");
    }

    public void cleanMissing() throws SQLException {

        numMissing = update("DELETE FROM DATA " +
                "WHERE ID IS NULL " +
                "OR TRIM(ID) = '' " +
                "OR SYMBOL IS NULL " +
                "OR TRIM(SYMBOL) = '' " +
                "OR SOURCE IS NULL " +
                "OR TRIM(SOURCE) = '';");
    }

    public void findDups() throws SQLException {

        exec("CREATE TABLE DUPLICATE_SYMBOLS AS " +
             "SELECT SYMBOL FROM DATA " +
             "GROUP BY SYMBOL " +
             "HAVING COUNT(SYMBOL) > 1;");

        numDups = selectOneInt("SELECT COUNT(*) FROM DUPLICATE_SYMBOLS;");
    }

    public void dropDups() throws SQLException {
        exec("DELETE FROM DATA " +
             "WHERE SYMBOL IN " +
             "(SELECT SYMBOL FROM DUPLICATE_SYMBOLS);");
    }

    public void countEmUp() throws SQLException {
        numIds = selectOneInt("SELECT COUNT (DISTINCT ID) FROM DATA;");
        numSymbols = selectOneInt("SELECT COUNT (DISTINCT SYMBOL) FROM DATA;");
        numSources = selectOneInt("SELECT COUNT (DISTINCT SOURCE) FROM DATA;");

        sourceCounts = select("SELECT SOURCE, COUNT(SOURCE) AS COUNT FROM DATA " +
                              "GROUP BY SOURCE;");

    }

    public void process(String filename, String sep) throws SQLException {
        readCSV(filename, a("ID", "SYMBOL", "SOURCE"), sep);
        countRecordsRead();
        cleanMissing();
        findDups();
        dropDups();
        countEmUp();
    }
}
