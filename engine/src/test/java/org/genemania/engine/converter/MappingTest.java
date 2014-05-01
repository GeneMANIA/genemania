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

package org.genemania.engine.converter;

import org.junit.Test;
import static org.junit.Assert.*;

public class MappingTest {
	
    @Test
	public void testStringMapping() {		
		Mapping<String, String> mapping = new Mapping<String, String>();
		mapping.addAlias("Q9ZZX9", "Sc:119886");
		mapping.addAlias("AI2", "Sc:119891");
		mapping.addAlias("NP_009309", "Sc:119891");
		mapping.addAlias("P00856", "Sc:119896");
		
		assertEquals(3, mapping.size());
		assertEquals(1, mapping.getIndexForUniqueId("Sc:119891"));
		assertEquals(-1, mapping.getIndexForUniqueId("nobody home"));
		assertEquals("Sc:119886", mapping.getUniqueIdForAlias("Q9ZZX9"));
		assertNull(mapping.getUniqueIdForAlias("all alone"));
		assertEquals("Sc:119896", mapping.getUniqueIdForIndex(2));
		assertEquals("AI2", mapping.getPreferredAliasForUniqueId("Sc:119891"));
	}

}
