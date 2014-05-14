package org.genemania.adminweb.dataset;

import org.genemania.adminweb.exception.DatamartException;

/*
 * parts of the data build process require a config file
 * 'db.cfg'. for integration, generate a subset of this
 * file.
 */
public interface DbConfigGenerator {
	public String makeConfig() throws DatamartException;
	public String makeConfig(long organismId) throws DatamartException;
}
