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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;
import org.genemania.domain.Organism;
import org.genemania.plugin.cytoscape3.model.OrganismManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import junitx.framework.ListAssert;

public class ListOrganismsCommandTaskTest {

	@Mock
	private TaskMonitor taskMonitor;
	@Mock
	private OrganismManager organismManager;
	
	private Set<Organism> remoteOrganisms = new LinkedHashSet<>(
			Arrays.asList(new Organism[] {
					new Organism("A. thaliana", "arabidopsis", null, "Arabidopsis thaliana", null, 3702),
					new Organism("C. elegans", "worm", null, "Caenorhabditis elegans", null, 6239),
					new Organism("D. melanogaster", "fly", null, "Drosophila melanogaster", null, 7227),
			})
	);
	private Set<Organism> localOrganisms = new LinkedHashSet<>(
			Arrays.asList(new Organism[] {
					new Organism("D. rerio", "zebrafish", null, "Danio rerio", null, 7955),
					new Organism("E. coli", "escherichia coli", null, "Escherichia coli", null, 83333),
			})
	);
	private ListOrganismsCommandTask task;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(organismManager.getLocalOrganisms()).thenReturn(localOrganisms);
		when(organismManager.getRemoteOrganisms()).thenReturn(remoteOrganisms);
		
		// Set id's, or Organism.equals() may not work properly, giving false positives
		int i = 1;
		for (Organism org : remoteOrganisms)
			org.setId(i++);
		for (Organism org : localOrganisms)
			org.setId(i++);
		
		task = new ListOrganismsCommandTask(organismManager);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testListRemoteOrganisms() throws Exception {
		// (offline == false) must be default
		task.run(taskMonitor);
		
		final int size = 3;
		
		List<Organism> list = (List<Organism>) task.getResults(List.class);
		assertEquals(size, list.size());
		ListAssert.assertEquals(new ArrayList<>(remoteOrganisms), list);
		
		Set<Organism> set = (Set<Organism>) task.getResults(Set.class);
		assertEquals(size, set.size());
		
		Vector<Organism> vector = (Vector<Organism>) task.getResults(Vector.class);
		assertEquals(size, vector.size());
		
		Collection<Organism> collection = (Collection<Organism>) task.getResults(Collection.class);
		assertEquals(size, collection.size());
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testListLocalOrganisms() throws Exception {
		task.offline = true;
		task.run(taskMonitor);
		
		final int size = 2;
		List<Organism> list = (List<Organism>) task.getResults(List.class);
		assertEquals(size, list.size());
		ListAssert.assertEquals(new ArrayList<>(localOrganisms), list);
	}
	
	@Test
	public void testListOrganismsAsJSON() throws Exception {
		task.run(taskMonitor);
		
		JSONResult res = (JSONResult) task.getResults(JSONResult.class);
		List<Organism> list = new Gson().fromJson(res.getJSON(), new TypeToken<List<Organism>>() {}.getType());
		
		assertEquals(3, list.size());
		ListAssert.assertEquals(new ArrayList<>(remoteOrganisms), list);
	}
	
	@Test
	public void testListOrganismsAsText() throws Exception {
		task.offline = true;
		task.run(taskMonitor);
		
		String txt = (String) task.getResults(String.class);
		
		for (Organism org : localOrganisms) {
			assertTrue(txt.contains(org.getName()));
			assertTrue(txt.contains(org.getAlias()));
			assertTrue(txt.contains(org.getDescription()));
			assertTrue(txt.contains("" + org.getTaxonomyId()));
		}
	}
}
