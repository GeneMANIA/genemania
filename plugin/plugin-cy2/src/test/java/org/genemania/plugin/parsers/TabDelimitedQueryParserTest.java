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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.genemania.domain.Attribute;
import org.genemania.domain.AttributeGroup;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.mediator.AttributeMediator;
import org.genemania.plugin.apps.IQueryErrorHandler;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.Network;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class TabDelimitedQueryParserTest {
	private Organism organism;
	private TabDelimitedQueryParser parser;
	private IQueryErrorHandler handler;
	private Set<String> unrecognizedNetworks;
	private AttributeMediator attributeMediator;

	@Before
	public void setUp() {
		parser = new TabDelimitedQueryParser();
		organism = new Organism();
		unrecognizedNetworks = new HashSet<String>();
		handler = new IQueryErrorHandler() {
			public void warn(String message) {
			}
			
			public void handleUnrecognizedNetwork(String network) {
				unrecognizedNetworks.add(network);
			}
			
			public void handleUnrecognizedGene(String gene) {
			}
			
			public void handleSynonym(String gene) {
			}
			
			public void handleNetwork(InteractionNetwork network) {
			}
		};
		
		attributeMediator = new AttributeMediator() {
			@SuppressWarnings("rawtypes")
			@Override
			public List hqlSearch(String queryString) {
				return null;
			}
			
			@Override
			public boolean isValidAttribute(long organismId, long attributeId) {
				return false;
			}
			
			@Override
			public List<Attribute> findAttributesByGroup(long organismId, long attributeGroupId) {
				return Collections.emptyList();
			}
			
			@Override
			public List<AttributeGroup> findAttributeGroupsByOrganism(long organismId) {
				return Collections.emptyList();
			}
			
			@Override
			public AttributeGroup findAttributeGroup(long organismId, long attributeGroupId) {
				return null;
			}
			
			@Override
			public Attribute findAttribute(long organism, long attributeId) {
				return null;
			}
		};
		
		List<InteractionNetwork> groupANetworks = createNetworks(new String[] {"1", "2", "3"});
		InteractionNetworkGroup groupA = new InteractionNetworkGroup("Group A", "A", "", groupANetworks);
		groupA.setId(-1);

		// A:1 is a default network
		groupANetworks.get(0).setDefaultSelected(true);
		
		List<InteractionNetwork> groupBNetworks = createNetworks(new String[] {"4", "5", "6"});
		InteractionNetworkGroup groupB = new InteractionNetworkGroup("Group B", "B", "", groupBNetworks);
		groupB.setId(-2);
		
		List<InteractionNetwork> groupPreferredNetworks = createNetworks(new String[] {"7"});
		InteractionNetworkGroup groupPreferred = new InteractionNetworkGroup("Physical Interactions", "pi", "", groupPreferredNetworks);

		// B:4 is a default network
		groupBNetworks.get(0).setDefaultSelected(true);
		
		List<InteractionNetwork> groupCNetworks = createNetworks(new String[] {"1", "2", "3"});
		InteractionNetworkGroup groupC = new InteractionNetworkGroup("Group C", "C", "", groupCNetworks);
		groupC.setId(-3);
		
		Collection<InteractionNetworkGroup> groups = new ArrayList<InteractionNetworkGroup>();
		groups.add(groupA);
		groups.add(groupB);
		groups.add(groupC);
		groups.add(groupPreferred);
		organism.setInteractionNetworkGroups(groups);
	}

	private List<InteractionNetwork> createNetworks(String[] names) {
		List<InteractionNetwork> networks = new ArrayList<InteractionNetwork>();
		for (String name : names) {
			InteractionNetwork network = new InteractionNetwork();
			network.setName(name);
			networks.add(network);
		}
		return networks;
	}
	
	@Test
	public void testGroupCode() {
		String data = "B";
		String networks = flatten(parser.parseNetworks(data, organism, ",", handler, attributeMediator));
		Assert.assertEquals("B:4,B:5,B:6", networks);
	}

	@Test
	public void testGroupName() {
		String data = "Group B";
		String networks = flatten(parser.parseNetworks(data, organism, ",", handler, attributeMediator));
		Assert.assertEquals("B:4,B:5,B:6", networks);
	}

	@Test
	public void testAll() {
		String data = "all";
		String networks = flatten(parser.parseNetworks(data, organism, ",", handler, attributeMediator));
		Assert.assertEquals("A:1,A:2,A:3,B:4,B:5,B:6,C:1,C:2,C:3,pi:7", networks);
	}
	
	@Test
	public void testPreferred() {
		String data = "preferred";
		String networks = flatten(parser.parseNetworks(data, organism, ",", handler, attributeMediator));
		Assert.assertEquals("pi:7", networks);
	}

	@Test
	public void testDefault() {
		String data = "default";
		String networks = flatten(parser.parseNetworks(data, organism, ",", handler, attributeMediator));
		Assert.assertEquals("A:1,B:4", networks);
	}

	@Test
	public void testName() {
		String data = "2,5";
		String networks = flatten(parser.parseNetworks(data, organism, ",", handler, attributeMediator));
		Assert.assertEquals("A:2,B:5,C:2", networks);
	}

	@Test
	public void testMixed1() {
		String data = "preferred,1";
		String networks = flatten(parser.parseNetworks(data, organism, ",", handler, attributeMediator));
		Assert.assertEquals("A:1,C:1,pi:7", networks);
	}

	@Test
	public void testMixed2() {
		String data = "default,3";
		String networks = flatten(parser.parseNetworks(data, organism, ",", handler, attributeMediator));
		Assert.assertEquals("A:1,A:3,B:4,C:3", networks);
	}

	@Test
	public void testGroupCodes() {
		String data = "B,pi";
		String networks = flatten(parser.parseNetworks(data, organism, ",", handler, attributeMediator));
		Assert.assertEquals("B:4,B:5,B:6,pi:7", networks);
	}
	
	@Test
	public void testUnhandled() {
		String data = "A,foo";
		String networks = flatten(parser.parseNetworks(data, organism, ",", handler, attributeMediator));
		Assert.assertEquals("A:1,A:2,A:3", networks);
		Assert.assertTrue(unrecognizedNetworks.contains("foo"));
	}
	
	@Test
	public void testKey1() {
		String data = "C|1";
		String networks = flatten(parser.parseNetworks(data, organism, ",", handler, attributeMediator));
		Assert.assertEquals("C:1", networks);
	}
	
	@Test
	public void testKey2() {
		String data = "Group C|1";
		String networks = flatten(parser.parseNetworks(data, organism, ",", handler, attributeMediator));
		Assert.assertEquals("C:1", networks);
	}

	private String flatten(Collection<Group<?, ?>> groups) {
		List<String> networkList = new ArrayList<String>();
		for (Group<?, ?> group : groups) {
			for (Network<?> network : group.getNetworks()) {
				networkList.add(String.format("%s:%s", group.getCode(), network.getName()));
			}
		}
		Collections.sort(networkList);
		StringBuilder builder = new StringBuilder();
		for (String entry : networkList) {
			if (builder.length() > 0) {
				builder.append(",");
			}
			builder.append(entry);
		}
		return builder.toString();
	}
}
