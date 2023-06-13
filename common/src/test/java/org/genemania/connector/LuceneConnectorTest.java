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

/**
 * LuceneConnectorTest: unit test for LuceneConnector 
 * Created Jun 21, 2010
 * @author Ovi Comes
 */
package org.genemania.connector;

import org.genemania.exception.DataStoreException;

import junit.framework.TestCase;

public class LuceneConnectorTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testFindOrganismById() {
		try {
			var organism = LuceneConnector.getInstance().findOrganismById(1);
			assertNotNull("organism", organism);

			var groups = organism.getInteractionNetworkGroups();
			assertNotNull("groups", groups);
			assertTrue("group size", groups.size() > 0);

			var firstGroup = groups.iterator().next();
			assertNotNull("first group", firstGroup);
			assertNotNull("first group id", firstGroup.getId());
			assertNotNull("first group name", firstGroup.getName());
			assertNotNull("first group code", firstGroup.getCode());
		} catch (DataStoreException e) {
			fail(e.getMessage());
		}
	}
}
