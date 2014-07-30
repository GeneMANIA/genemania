/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2010 University of Toronto.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.genemania.controller.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.service.GeneService;
import org.genemania.service.OrganismService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GeneValidationController {

	// ========[ PRIVATE PROPERTIES
	// ]===============================================================

	@Autowired
	private GeneService geneService;

	@Autowired
	private OrganismService organismService;

	@Autowired
	private MappingJacksonHttpMessageConverter httpConverter;

	public MappingJacksonHttpMessageConverter getHttpConverter() {
		return httpConverter;
	}

	public void setHttpConverter(
			MappingJacksonHttpMessageConverter httpConverter) {
		this.httpConverter = httpConverter;
	}

	public GeneService getGeneService() {
		return geneService;
	}

	public void setGeneService(GeneService geneValidationService) {
		this.geneService = geneValidationService;
	}

	public OrganismService getOrganismService() {
		return organismService;
	}

	public void setOrganismService(OrganismService organismService) {
		this.organismService = organismService;
	}

	protected final Logger logger = Logger.getLogger(getClass());

	// ========[ PUBLIC METHODS
	// ]===================================================================

	public static class ValidationRequest {
		private Integer organism;
		private String genes;

		public ValidationRequest() {
			super();
		}

		public Integer getOrganism() {
			return organism;
		}

		public void setOrganism(Integer organism) {
			this.organism = organism;
		}

		public String getGenes() {
			return genes;
		}

		public void setGenes(String genes) {
			this.genes = genes;
		}

		public boolean assertParamsSet() throws ApplicationException {
			if (this.genes == null) {
				throw new ApplicationException("`genes` not set");
			}

			if (this.organism == null) {
				throw new ApplicationException("`organism` not set");
			}

			return true;
		}

	}

	/**
	 * Validates a list of genes separated by newlines
	 * 
	 * @param organismId
	 *            the organism to validate gene names against
	 * @param geneLines
	 *            the newline separated list of genes in a string
	 * @param session
	 *            the session
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/gene_validation")
	@ResponseBody
	public GeneService.ValidationResult list(HttpSession session,
			HttpServletRequest req) throws ApplicationException {
		logger.debug("Return validation list...");

		ValidationRequest vReq = null;

		String contentType = req.getHeader("Content-Type");
		contentType = contentType != null ? contentType : "";

		if (contentType.toLowerCase().contains("application/json")) {
			try {
				vReq = httpConverter.getObjectMapper().readValue(
						req.getInputStream(), ValidationRequest.class);
			} catch (Exception e) {

			}
		} else { // assume form params
			vReq = new ValidationRequest();
			vReq.setGenes(req.getParameter("genes"));
			vReq.setOrganism(Integer.parseInt(req.getParameter("organism")));
		}

		vReq.assertParamsSet();

		return geneService.validateGeneLines(vReq.getOrganism(),
				vReq.getGenes());

	}
	// show (GET), list (GET), create (POST), update (POST), delete (DELETE ?)
}
