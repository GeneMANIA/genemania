/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2017 University of Toronto.
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
package org.genemania.plugin.cytoscape3.task;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.genemania.domain.Organism;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Fetch all organisms supported by the GeneMANIA server.
 */
public class LoadRemoteOrganismsTask extends AbstractTask {

	// TODO Make it a CyProperty?
	protected static final String URL = "http://genemania.org/json/organisms";
	
	private Set<Organism> organisms = new LinkedHashSet<>();
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(URL);
		
		String json = target.request().get(String.class);
		Gson gson = new Gson();
		List<Organism> orgList = gson.fromJson(json, new TypeToken<List<Organism>>() {}.getType());
		
		if (orgList != null)
			organisms.addAll(orgList);
	}
	
	public Set<Organism> getOrganisms() {
		return organisms;
	}
}
