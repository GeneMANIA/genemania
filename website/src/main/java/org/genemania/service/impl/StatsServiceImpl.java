package org.genemania.service.impl;

import org.genemania.dao.StatsDao;
import org.genemania.domain.Statistics;
import org.genemania.exception.DataStoreException;
import org.genemania.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;

public class StatsServiceImpl implements StatsService {

	@Autowired
	StatsDao statsDao;

	@Override
	public Statistics getStats() throws DataStoreException {
		return this.statsDao.getLatestStatistics();
	}

	public StatsDao getStatsDao() {
		return statsDao;
	}

	public void setStatsDao(StatsDao statsDao) {
		this.statsDao = statsDao;
	}
	
	

}
