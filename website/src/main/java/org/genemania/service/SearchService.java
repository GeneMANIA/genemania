package org.genemania.service;

import org.genemania.domain.SearchParameters;
import org.genemania.domain.SearchResults;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.exception.NoUserNetworkException;

public interface SearchService {

	/**
	 * Gets search results from the engine
	 * 
	 * @param params
	 *            The search parameters
	 * @return The search results from the engine
	 */
	public SearchResults search(SearchParameters params)
			throws ApplicationException, DataStoreException,
			NoUserNetworkException;

}
