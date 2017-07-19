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

import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.cytoscape.work.TaskMonitor;
import org.genemania.domain.Organism;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class LoadOrganismsTaskTest {
	
	@Mock
	private TaskMonitor taskMonitor;
	
	private LoadRemoteOrganismsTask task;
	
	@Before
	public void setUp() {
		task = new LoadRemoteOrganismsTask();
	}
	
	@Test
	public void testListOrganisms() throws Exception {
		task.run(taskMonitor);
		Set<Organism> organisms = task.getOrganisms();
		assertTrue(organisms.size() >= 9);
	}
}
