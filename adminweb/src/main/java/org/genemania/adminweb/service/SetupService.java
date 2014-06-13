package org.genemania.adminweb.service;

import org.genemania.adminweb.exception.DatamartException;

public interface SetupService {

    /*
     * Create tables and populate with basic records
     * such as standard network groups and data file
     * formats. The configured database should already
     * exist, but be otherwise empty.
     */
    public void setup() throws DatamartException;

    /*
     * Ensure a database exists and is of a compatible version.
     *
     * returns silently if all is good, otherwise throws exception
     */
    public void check() throws DatamartException;

    /*
     * insert an initial set of data records into an existing,
     * empty database schema
     */
    void populate() throws DatamartException;

    /*
     * create an empty schema
     */
    void create() throws DatamartException;
}
