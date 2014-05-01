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

package org.genemania.plugin;

import static junit.framework.Assert.assertEquals;

import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class FileUtilsTest {
	private FileUtils fileUtils;

	@Before
	public void setUp() {
		fileUtils = new FileUtils();
	}
	
	@SuppressWarnings("nls")
	@Test
	public void testDataDescriptions() throws Exception {
		Reader reader = new StringReader(
			"# This is a comment\n" +
			"a=b\n" +
			"c =d\n" +
			"e= f\n" +
			"g = h\n" +
			"i = j#foo\n" +
			"k = l #bar\n" +
			"m = #baz\n"
		);
		Map<String, String> descriptions = fileUtils.getDataSetDescriptions(reader);
		assertEquals(7, descriptions.size());
		assertEquals("b", descriptions.get("a"));
		assertEquals("d", descriptions.get("c"));
		assertEquals("f", descriptions.get("e"));
		assertEquals("h", descriptions.get("g"));
		assertEquals("j", descriptions.get("i"));
		assertEquals("l", descriptions.get("k"));
		assertEquals("", descriptions.get("m"));
	}
}
