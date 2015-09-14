package org.genemania.controller.rest;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.genemania.exception.DataStoreException;
import org.genemania.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class VersionController {

	private class VersionInfo {
		private String webappVersion;
		private String dbVersion;

		public String getWebappVersion() {
			return webappVersion;
		}

		public void setWebappVersion(String webappVersion) {
			this.webappVersion = webappVersion;
		}

		public String getDbVersion() {
			return dbVersion;
		}

		public void setDbVersion(String dbVersion) {
			this.dbVersion = dbVersion;
		}

	}

	@Autowired
	private StatsService statsService;

	@Autowired
	private String appVersion;

	@RequestMapping(method = RequestMethod.GET, value = "/version")
	@ResponseBody
	public VersionInfo list(HttpServletRequest request, HttpSession session) {
		VersionInfo version = new VersionInfo();

		version.setWebappVersion(this.appVersion);
		try {
			version.setDbVersion(new java.text.SimpleDateFormat(
					"d MMMMM yyyy HH:mm:ss").format(statsService.getStats()
					.getDate()));
		} catch (DataStoreException e) {
			// can't do much here
		}

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
