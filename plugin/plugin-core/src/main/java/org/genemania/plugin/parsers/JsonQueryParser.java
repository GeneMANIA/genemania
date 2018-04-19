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

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.exception.DataStoreException;
import org.genemania.plugin.OneUseIterable;
import org.genemania.plugin.Strings;
import org.genemania.plugin.apps.IQueryErrorHandler;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.Network;
import org.genemania.plugin.model.impl.InteractionNetworkGroupImpl;
import org.genemania.plugin.model.impl.InteractionNetworkImpl;
import org.genemania.type.ScoringMethod;

public class JsonQueryParser extends AbstractQueryParser {
	
	public JsonQueryParser(Set<Organism> organisms) {
		super(organisms);
	}
	
	public JsonQueryParser(DataSet data) {
		super(data);
	}

	@Override
	public Query parse(Reader reader, IQueryErrorHandler handler) throws IOException {
		Query query = new Query();
		JsonFactory jsonFactory = new MappingJsonFactory();
		JsonParser parser = jsonFactory.createJsonParser(reader);
		JsonNode root;
		
		try {
			root = parser.readValueAsTree();
		} catch (IOException e) {
			throw new IOException(Strings.jsonQueryParser_error, e);
		}
		
		try {
			Organism organism = parseOrganism(root.get("organism").getTextValue()); //$NON-NLS-1$
			query.setOrganism(organism);
			query.setCombiningMethod(parseCombiningMethod(root.get("selectedWeighting").getTextValue(), query, handler)); //$NON-NLS-1$
			query.setGeneLimit(root.get("numberOfResultGenes").getIntValue()); //$NON-NLS-1$
			query.setGenes(parseGenes(root.get("genes"))); //$NON-NLS-1$
			query.setGroups(parseNetworks(root.get("networks"), organism)); //$NON-NLS-1$
			query.setScoringMethod(ScoringMethod.DISCRIMINANT);
		} catch (DataStoreException e) {
			throw new IOException(e.getMessage());
		}
		
		return query;
	}

	private Collection<Group<?, ?>> parseNetworks(JsonNode root, Organism organism) {
		List<Group<?, ?>> groups = new ArrayList<>();
		final Collection<InteractionNetworkGroup> allGroups;
		
		if (data != null)
			allGroups = data.getMediatorProvider().getNetworkMediator().getNetworkGroupsByOrganism(organism.getId());
		else
			allGroups = organism.getInteractionNetworkGroups();
		
		for (String groupName : new OneUseIterable<>(root.getFieldNames())) {
			InteractionNetworkGroup targetGroup = null;
			
			for (InteractionNetworkGroup group : allGroups) {
				if (group.getName().equalsIgnoreCase(groupName) || group.getCode().equalsIgnoreCase(groupName)) {
					targetGroup = group;
					break;
				}
			}
			
			if (targetGroup == null)
				continue;
			
			List<Network<InteractionNetwork>> networks = new ArrayList<>();
			
			for (JsonNode node : new OneUseIterable<>(root.get(groupName).getElements())) {
				String networkName = node.getTextValue();
				
				for (InteractionNetwork network : targetGroup.getInteractionNetworks()) {
					if (network.getName().equalsIgnoreCase(networkName))
						networks.add(new InteractionNetworkImpl(network, 0));
				}
			}
			
			if (networks.size() > 0)
				groups.add(new InteractionNetworkGroupImpl(targetGroup, networks));
		}
		
		return groups;
	}

	private List<String> parseGenes(JsonNode root) {
		List<String> genes = new ArrayList<>();
		
		for (JsonNode node : new OneUseIterable<>(root.getElements()))
			genes.add(node.getTextValue());
		
		return genes;
	}
}
