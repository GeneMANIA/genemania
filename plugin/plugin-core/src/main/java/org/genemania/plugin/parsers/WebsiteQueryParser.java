/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2008-2011 University of Toronto.
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
package org.genemania.plugin.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.exception.DataStoreException;
import org.genemania.plugin.Strings;
import org.genemania.plugin.apps.IQueryErrorHandler;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.Network;
import org.genemania.plugin.model.impl.InteractionNetworkGroupImpl;
import org.genemania.plugin.model.impl.InteractionNetworkImpl;
import org.genemania.type.CombiningMethod;

public class WebsiteQueryParser extends AbstractQueryParser {
	static Pattern pattern = Pattern.compile("(.+?)(\\s+\\((.+)\\))?"); //$NON-NLS-1$
	
	enum State {
		read_parameters,
		read_networks,
	}
	
	@Override
	public Query parse(DataSet data, Reader reader, IQueryErrorHandler handler) throws IOException {
		BufferedReader input = new BufferedReader(reader);
		Query query = new Query();
		query.setNetworks(new HashSet<Group<?, ?>>());
		State state = State.read_parameters;
		String line = input.readLine();
		try {
			while (line != null) {
				String[] values = line.split("\t"); //$NON-NLS-1$
				switch (state) {
				case read_parameters:
					if ("Organism".equals(values[0])) { //$NON-NLS-1$
						handleOrganism(data, values, query);
					} else if ("Genes".equals(values[0])) { //$NON-NLS-1$
						handleGenes(data, values, query, handler);
					} else if ("Networks".equals(values[0])) { //$NON-NLS-1$
						state = State.read_networks;
					} else if ("Network weighting code".equals(values[0])) { //$NON-NLS-1$
						handleWeightingMethod(data, values, query, handler);
					} else if ("Number of gene results".equals(values[0])) { //$NON-NLS-1$
						handleGeneLimit(data, values, query);
					}
					break;
				case read_networks:
					if ("".equals(values[0])) { //$NON-NLS-1$
						state = State.read_parameters;
					} else {
						handleNetworkGroup(data, values, query, handler);
					}
					break;
				}
				line = input.readLine();
			}
		} catch (DataStoreException e) {
			throw new IOException(e);
		}
		if (query.getOrganism() == null) {
			throw new IOException(Strings.websiteQueryParser_error);
		}
		return query;
	}

	private void handleNetworkGroup(DataSet data, String[] values, Query query, IQueryErrorHandler handler) throws IOException {
		String groupName = values[0];
		Organism organism = query.getOrganism();
		InteractionNetworkGroup targetGroup = null;
		for (InteractionNetworkGroup group : organism.getInteractionNetworkGroups()) {
			if (group.getCode().equals(groupName) || group.getName().equals(groupName)) {
				targetGroup = group;
			}
		}
		if (targetGroup == null) {
			throw new IOException(String.format("Unrecognized group: %s", groupName)); //$NON-NLS-1$
		}
		
		List<Network<InteractionNetwork>> networks = new ArrayList<Network<InteractionNetwork>>();
		
		for (int i = 1; i < values.length; i++) {
			String name = values[i];
			boolean found = false;
			for (InteractionNetwork network : targetGroup.getInteractionNetworks()) {
				if (network.getName().equals(name)) {
					networks.add(new InteractionNetworkImpl(network, 0));
					found = true;
					break;
				}
			}
			if (!found) {
				handler.handleUnrecognizedNetwork(name);
			}
		}
		if (networks.size() == 0) {
			throw new IOException("No networks were recognized"); //$NON-NLS-1$
		}
		
		query.getNetworks().add(new InteractionNetworkGroupImpl(targetGroup, networks));
	}

	private void handleGeneLimit(DataSet data, String[] values, Query query) {
		int limit = Integer.parseInt(values[1]);
		query.setGeneLimit(limit);
	}

	private void handleWeightingMethod(DataSet data, String[] values, Query query, IQueryErrorHandler handler) {
		Matcher matcher = pattern.matcher(values[1]);
		CombiningMethod method = null;
		if (matcher.matches()) {
			method = CombiningMethod.fromCode(matcher.group(1));
		}
		if (method == null || method == CombiningMethod.UNKNOWN) {
			method = CombiningMethod.AUTOMATIC_SELECT;
			handler.warn(String.format("Unrecognized combining method \"%s\".  Defaulting to: %s", values[1], method)); //$NON-NLS-1$
		}
		query.setCombiningMethod(method);
	}

	private void handleGenes(DataSet data, String[] values, Query query, IQueryErrorHandler handler) {
		List<String> genes = new ArrayList<String>();
		for (int i = 1; i < values.length; i++) {
			genes.add(values[i]);
		}
		query.setGenes(genes);
	}

	private void handleOrganism(DataSet data, String[] values, Query query) throws DataStoreException, IOException {
		Organism organism = null;
		Matcher matcher = pattern.matcher(values[1]);
		if (matcher.matches()) {
			organism = parseOrganism(data, matcher.group(1));
			if (organism == null) {
				organism = parseOrganism(data, matcher.group(3));
			}
		} else {
			organism = parseOrganism(data, values[1]);
		}
		if (organism == null) {
			throw new IOException(String.format("Unrecognized organism: %s", values[1])); //$NON-NLS-1$
		}
		query.setOrganism(organism);
	}
}
