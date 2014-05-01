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

import java.util.Collection;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.exception.DataStoreException;
import org.genemania.service.NetworkGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class NetworkGroupController {
	
	// ========[ PRIVATE PROPERTIES
	// ]===============================================================

	@Autowired
	private NetworkGroupService networkGroupService;

	protected final Log logger = LogFactory.getLog(getClass());

	// ========[ PUBLIC METHODS
	// ]===================================================================
	
	/**
	 * Return query network groups as XML.
	 * 
	 * @throws DataStoreException
	 *             when the data can not be read from the database
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/network_groups/{organismId}")
	@ResponseBody
	public Collection<InteractionNetworkGroup> list(@PathVariable Long organismId, HttpSession session)
			throws DataStoreException {
		logger.debug("Return Network Groups list...");

		Collection<InteractionNetworkGroup> groups = networkGroupService
				.findNetworkGroupsByOrganism(organismId, session.getId());
		
		return groups;
	}

	public NetworkGroupService getNetworkGroupService() {
		return networkGroupService;
	}

	public void setNetworkGroupService(NetworkGroupService networkGroupService) {
		this.networkGroupService = networkGroupService;
	}

	// show (GET), list (GET), create (POST), update (POST), delete (DELETE ?)
}
