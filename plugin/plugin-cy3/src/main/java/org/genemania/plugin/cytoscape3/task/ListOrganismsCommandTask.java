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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.plugin.cytoscape3.model.OrganismDto;
import org.genemania.plugin.cytoscape3.model.OrganismManager;

import com.google.gson.Gson;

public class ListOrganismsCommandTask extends AbstractTask implements ObservableTask {

	@Tunable(
			description = "Offline search:",
			longDescription = 
					"If ```true```, it lists only organisms that have been installed locally and are available for offline searches. "
					+ "If ```false```, it lists all the organisms that are supported by the GeneMANIA server on online searches.",
			exampleStringValue = "false",
			context = "nogui"
	)
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
				+ (offline ? "from installed data set (offline)" : "accepted in online searches") + "...");
		tm.setProgress(-1);
		
		organisms = offline ? organismManager.getLocalOrganisms() : organismManager.getRemoteOrganisms();
		
		if (organisms == null)
			organisms = Collections.emptySet();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getResults(Class type) {
		if (Set.class.isAssignableFrom(type))
			return new LinkedHashSet<>(organisms);
		else if (List.class.isAssignableFrom(type))
			return new ArrayList<>(organisms);
		else if (Collection.class.isAssignableFrom(type))
			return new LinkedHashSet<>(organisms);
		
		if (type == String.class) {
			Color color = LookAndFeelUtil.getSuccessColor();
			
			if (color == null)
				color = Color.DARK_GRAY;
			
			String rgb = "#" + Integer.toHexString(color.getRGB()).substring(2);
			
			StringBuilder sb = new StringBuilder(String.format(
					"<html><body>"
					+ "<table style='font-family: monospace; color: %s;'>"
					+ "<tr style='font-weight: bold; border-width: 0px 0px 1px 0px; border-style: dotted;'>"
					+ "<th style='text-align: left; padding: 0px 24px 0px 0px;'>Taxonomy ID</th>"
					+ "<th style='text-align: left; padding: 0px 24px 0px 0px;'>Scientific Name</th>"
					+ "<th style='text-align: left; padding: 0px 24px 0px 0px;'>Abbreviated Name</th>"
					+ "<th style='text-align: left; padding: 0px 24px 0px 0px;'>Common Name</th>"
					+ "</tr>",
					rgb
			));
			
			for (Organism org : organisms) {
				sb.append(String.format(
						"<tr>"
						+ "<td style='padding: 4px 24px 4px 0px; text-align: right;'>%s</td>"
						+ "<td style='padding: 4px 24px 4px 0px;'>%s</td>"
						+ "<td style='padding: 4px 24px 4px 0px;'>%s</td>"
						+ "<td style='padding: 4px 24px 4px 0px;'>%s</td></tr>",
						org.getTaxonomyId(),
						org.getAlias(),
						org.getName(),
						org.getDescription()
				));
				
				// Also list the interaction networks
				if (org.getInteractionNetworkGroups() == null)
					continue;
				
				sb.append(
						"<tr style='font-weight: bold;'>"
						+ "<td style='padding: 2px 24px 2px 0px;'> </td>"
						+ "<td style='padding: 2px 24px 2px 0px;' colspan='3'>Interaction Networks:</td></tr>"
				);
				sb.append(
						"<tr>"
						+ "<td style='padding: 2px 24px 2px 0px;'> </td>"
						+ "<td style='padding: 2px 24px 2px 0px; text-align: center;' colspan='3'>"
				);
				sb.append(String.format(
						"<table style='font-family: monospace; color: %s;>"
						+ "<tr style='font-weight: bold;'>"
						+ "<th style='text-align: left; padding: 0px 24px 0px 0px;'>Default</th>"
						+ "<th style='text-align: left; padding: 0px 24px 0px 0px;'>ID</th>"
						+ "<th style='text-align: left; padding: 0px 24px 0px 0px;'>Name</th></tr>",
						rgb
				));
				
				for (InteractionNetworkGroup ng : org.getInteractionNetworkGroups()) {
					if (ng.getInteractionNetworks() == null)
						continue;
					
					sb.append(String.format(
							"<tr style='font-style: italic; border-width: 1px 0px 1px 0px; border-style: dotted;'>"
							+ "<td style='padding: 2px 24px 2px 0px;'> </td>"
							+ "<td style='padding: 2px 24px 2px 0px; text-align: left;'>%s</td>"
							+ "<td style='padding: 2px 24px 2px 0px; text-align: left;' colspan='2'>%s</td>"
							+ "</tr>",
							ng.getCode(),
							ng.getName()
					));
					
					for (InteractionNetwork n : ng.getInteractionNetworks())
						sb.append(String.format(
								"<tr>"
								+ "<td style='padding: 2px 24px 2px 0px; text-align: center;'>%s</td>"
								+ "<td style='padding: 2px 24px 2px 0px; text-align: left;'>%s</td>"
								+ "<td style='padding: 2px 24px 2px 0px; text-align: left;' colspan='2'>%s</td>"
								+ "</tr>",
								n.isDefaultSelected() ? "X" : " ",
								n.getId(),
								n.getName()
						));
				}
				
				sb.append("</table></td></tr>");
			}
			
			sb.append("</table>");
			sb.append("</body></html>");
			
			return sb.toString();
		}
		
		if (type == JSONResult.class) {
			List<OrganismDto> dtoList = new ArrayList<>();
			
			for (Organism org : organisms)
				dtoList.add(new OrganismDto(org));
			
			Gson gson = new Gson();
			String json = gson.toJson(dtoList);
			JSONResult res = () -> {
				return "{ \"organisms\": " + json + " }";
			};
			
			return res;
		}
			
		return null;
	}
	
	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(Set.class, List.class, Collection.class, String.class, JSONResult.class);
	}
}
