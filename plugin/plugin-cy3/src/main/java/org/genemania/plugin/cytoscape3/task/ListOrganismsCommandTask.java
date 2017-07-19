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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;
import org.genemania.domain.Organism;
import org.genemania.plugin.cytoscape3.model.OrganismManager;

import com.google.gson.Gson;

public class ListOrganismsCommandTask extends AbstractTask implements ObservableTask {

	@Tunable(description = "Offline search:", context = "nogui")
	public boolean offline;
	
	private Set<Organism> organisms;
	
	private final OrganismManager organismManager;

	public ListOrganismsCommandTask(OrganismManager organismManager) {
		this.organismManager = organismManager;
	}
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("GeneMANIA");
		tm.setStatusMessage("Retrieving organisms "
				+ (offline ? "from installed data set" : "supported by the GeneMANIA server") + "...");
		tm.setProgress(-1);
		
		organisms = offline ? organismManager.getLocalOrganisms() : organismManager.getRemoteOrganisms();
		
		if (organisms == null)
			organisms = Collections.emptySet();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getResults(Class type) {
		if (Collection.class.isAssignableFrom(type))
			return organisms;
		
		if (type == String.class) {
			StringBuilder sb = new StringBuilder(String.format(
					"<html><body><table style='font-family: monospace; color: %s;'>"
					+ "<tr style='font-weight: bold; border-width: 0px 0px 1px 0px; border-style: dotted;'>"
					+ "<th style='text-align: left;'>Name</th>"
					+ "<th style='text-align: left;'>Alias</th>"
					+ "<th style='text-align: left;'>Description</th>"
					+ "<th style='text-align: left;'>Taxonomy ID</th>"
					+ "</tr>",
					("#" + Integer.toHexString(LookAndFeelUtil.getSuccessColor().getRGB()).substring(2))
			));
			
			for (Organism org : organisms)
				sb.append(String.format(
						"<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
						org.getName(),
						org.getAlias(),
						org.getDescription(),
						org.getTaxonomyId()
				));
			
			sb.append("</table></body></html>");
			
			return sb.toString();
		}
		
		if (type == JSONResult.class) {
			Gson gson = new Gson();
			JSONResult res = () -> { return gson.toJson(organisms); };
			return res;
		}
			
		return null;
	}
}
