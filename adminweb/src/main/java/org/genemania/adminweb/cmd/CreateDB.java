package org.genemania.adminweb.cmd;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.genemania.adminweb.dao.impl.DatamartDbImpl;
import org.genemania.adminweb.service.impl.SetupServiceImpl;

import java.util.List;

/**
 * command-line utility to setup a
 * new admin sql database.
 */
public class CreateDB {

    @Parameter(names = "-dburl", description = "dburl")
    private String dburl = "jdbc:h2:file:./adminwebtest";

    @Parameter(names = "-user", description = "")
    private String user = "";

    @Parameter(names = "-pass", description = "")
    private String pass = "";

    @Parameter(names = "-dump", description = "print create database sql")
    private boolean dump = false;

    public static void main(String args[]) throws Exception {
        CreateDB tool = new CreateDB();
        new JCommander(tool, args);
        System.out.println("dburl: " + tool.dburl);

        tool.run();
   }

    public void run() throws Exception {
        if (dump) {
            dumpCreateTableSql();
        }
        else {
            createDb();
        }
    }

    public void createDb() throws Exception {
        DatamartDbImpl dmdb = new DatamartDbImpl(dburl, user, pass);
        dmdb.init();

        SetupServiceImpl setupService = new SetupServiceImpl();
        setupService.setDatamartDb(dmdb);
        setupService.setup();
    }

    public void dumpCreateTableSql()throws Exception {
        DatamartDbImpl dmdb = new DatamartDbImpl(dburl, user, pass);
        dmdb.init();

        List<String> stmts = dmdb.getTableCreateStatements();
        for (String s: stmts) {
            System.out.println(s + ";");
        }
    }
}
