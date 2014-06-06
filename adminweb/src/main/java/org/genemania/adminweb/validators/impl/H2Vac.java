package org.genemania.adminweb.validators.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
 * Vacuum up the data mess! uses an in-memory
 * H2 SQL db
 *
 * make sure to call close() to release resources
 */
public class H2Vac {
    final Logger logger = LoggerFactory.getLogger(H2Vac.class);

    static final String DBURL_BASE = "jdbc:h2:mem:";
    String dburl;

    Connection conn = null;

    public H2Vac(String dbName) throws ClassNotFoundException, SQLException {
        this.dburl = DBURL_BASE + dbName;
        init();
    }

    public void init() throws ClassNotFoundException, SQLException {
        if (conn == null) {
            Class.forName("org.h2.Driver");
            conn = DriverManager.getConnection(dburl, "", "");
        }
    }

    public void close() throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }

    public void readCSV(String filename, String [] colNames, String sep) throws SQLException {
        String tableName = "DATA";
        String sql = String.format("CREATE TABLE %s as SELECT * FROM CSVREAD('%s', '%s', 'charset=UTF-8 fieldSeparator=%s');",
                     tableName, filename, joinArray(colNames, sep), sep);

        exec(sql);
    }

    public boolean exec(String sql) throws SQLException {
        Statement s = conn.createStatement();
        logger.debug(sql);
        boolean n = s.execute(sql);
        conn.commit();
        s.close();
        return n;
    }

    public int update(String sql) throws SQLException {
        Statement s = conn.createStatement();
        try {
            logger.debug(sql);
            int n = s.executeUpdate(sql);
            conn.commit();
            return n;
        }
        finally {
            s.close();
        }
    }

    public List<Object[]> select(String sql) throws SQLException {
        List<Object[]> result = new ArrayList<Object[]>();
        Statement s = conn.createStatement();
        ResultSet rs = null;

        try {
            logger.debug(sql);
            rs = s.executeQuery(sql);
            ResultSetMetaData rsMeta = rs.getMetaData();
            int n = rsMeta.getColumnCount();
            while (rs.next()) {
                Object[] rec = new Object[n];
                for (int field = 0; field < n; field++) {
                    rec[field] = rs.getObject(field + 1);
                }
                result.add(rec);
            }
        }
        finally {
            if (rs != null) rs.close();
            s.close();
        }

        return result;
    }

    public int selectOneInt(String sql) throws SQLException {
        Statement s = conn.createStatement();
        ResultSet rs = null;

        try {
            logger.debug(sql);
            rs = s.executeQuery(sql);
            rs.next();
            return rs.getInt(1);
        }
        finally {
            rs.close();
            s.close();
        }

    }

    /*
     * a small wheel, reinvented a thousand times. and again.
     */
    public static String joinArray(String [] colNames, String sep) {
        String s = "";
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<colNames.length; i++) {
            sb.append(s);
            sb.append(colNames[i]);
            s = sep;
        }
        return sb.toString();
    }

    // convenience: create an array of strings
    public static String [] a(String... args) {
        return args;
    }

}
