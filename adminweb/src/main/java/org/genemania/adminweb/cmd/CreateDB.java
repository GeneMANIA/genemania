package org.genemania.adminweb.cmd;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.genemania.adminweb.dao.impl.DatamartDbImpl;
import org.genemania.adminweb.service.impl.SetupServiceImpl;

import java.util.List;

/**
 * command-line utility application to
 * create an empty database schema
 */
public class CreateDB {

    @Parameter(names = "-dburl", description = "dburl")
    private String dburl = "jdbc:mysql://localhost/adminwebtest";

    @Parameter(names = "-user", description = "user")
    private String user = "username";

    @Parameter(names = "-pass", description = "pass")
    private String pass = "password";

    public static void main(String args[]) throws Exception {
        CreateDB tool = new CreateDB();
        new JCommander(tool, args);
        System.out.println("dburl: " + tool.dburl);
        tool.create();
    }

    public void create() throws Exception {
        DatamartDbImpl dmdb = new DatamartDbImpl(dburl, user, pass);
        dmdb.init();

//        List<String> is = dmdb.getTableCreateStatements();
//        for (String s: is) {
//            System.out.println(s + ";");
//        }
//        if (true) return;

        dmdb.createTables();

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
