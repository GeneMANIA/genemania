package org.genemania.adminweb.service;

public interface SetupService {

    /*
     * Create tables and populate with basic records
     * such as standard network groups and data file
     * formats. The configured database should already
     * exist, but be otherwise empty.
     */
    public void setup();
}
