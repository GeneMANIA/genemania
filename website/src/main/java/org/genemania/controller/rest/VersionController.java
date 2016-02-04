package org.genemania.controller.rest;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.genemania.exception.DataStoreException;
import org.genemania.service.StatsService;
import org.genemania.service.VersionService;
import org.genemania.service.VersionService.VersionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class VersionController {

	@Autowired
	private VersionService versionService;

	@RequestMapping(method = RequestMethod.GET, value = "/version")
	@ResponseBody
	public VersionInfo list(HttpServletRequest request, HttpSession session) throws DataStoreException {
		return versionService.getVersion();
	}

	public VersionService getVersionService() {
		return versionService;
	}

	public void setVersionService(VersionService versionService) {
		this.versionService = versionService;
	}

}
