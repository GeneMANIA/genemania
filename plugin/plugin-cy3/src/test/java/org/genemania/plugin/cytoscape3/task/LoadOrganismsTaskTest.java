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
