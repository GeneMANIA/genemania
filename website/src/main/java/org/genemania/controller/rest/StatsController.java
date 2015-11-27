package org.genemania.controller.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.genemania.domain.Statistics;
import org.genemania.exception.DataStoreException;
import org.genemania.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class StatsController {
	@Autowired
	private StatsService statsService;

	@RequestMapping(method = RequestMethod.GET, value = "/stats")
	@ResponseBody
	public Statistics list(HttpServletRequest request, HttpSession session) throws DataStoreException {
		return this.statsService.getStats();
	}

	public StatsService getStatsService() {
		return statsService;
	}

	public void setStatsService(StatsService statsService) {
		this.statsService = statsService;
	}
}
