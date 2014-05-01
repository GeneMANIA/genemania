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

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.genemania.exception.DataStoreException;
import org.genemania.service.GeneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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

	public GeneService getGeneService() {
		return geneService;
	}

	public void setGeneService(
			GeneService geneValidationService) {
		this.geneService = geneValidationService;
	}

	protected final Log logger = LogFactory.getLog(getClass());

	// ========[ PUBLIC METHODS
	// ]===================================================================

	/**
	 * Validates a list of genes separated by newlines
	 * 
	 * @param organismId
	 *            the organism to validate gene names against
	 * @param geneLines
	 *            the newline separated list of genes in a string
	 * @param session
	 *            the session
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/gene_validation")
	@ResponseBody
	public GeneService.ValidationResult list(
			@RequestParam("organism") Integer organismId,
			@RequestParam("genes") String geneLines, HttpSession session)
			throws DataStoreException {
		logger.debug("Return validation list...");

		return geneService.validateGeneLines(organismId, geneLines);
	}

	// show (GET), list (GET), create (POST), update (POST), delete (DELETE ?)
}
