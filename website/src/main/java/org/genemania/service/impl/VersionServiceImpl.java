package org.genemania.service.impl;

import org.genemania.exception.DataStoreException;
import org.genemania.service.StatsService;
import org.genemania.service.VersionService;
import org.springframework.beans.factory.annotation.Autowired;

public class VersionServiceImpl implements VersionService {

	@Autowired
	private StatsService statsService;

	@Autowired
	private String appVersion;

	public VersionInfo getVersion() throws DataStoreException {
		VersionInfo version = new VersionInfo();

		version.setWebappVersion(this.appVersion);
		version.setDbVersion(
				new java.text.SimpleDateFormat("d MMMMM yyyy HH:mm:ss").format(statsService.getStats().getDate()));

		return version;
	}

	public StatsService getStatsService() {
		return statsService;
	}

	public void setStatsService(StatsService statsService) {
		this.statsService = statsService;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

}
