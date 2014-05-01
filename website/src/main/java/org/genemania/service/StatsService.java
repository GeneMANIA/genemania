package org.genemania.service;

import org.genemania.domain.Statistics;
import org.genemania.exception.DataStoreException;

/**
 * Provides access to stats domain data
 */
public interface StatsService {
	public Statistics getStats() throws DataStoreException;
}
