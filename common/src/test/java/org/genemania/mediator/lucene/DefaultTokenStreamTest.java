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

package org.genemania.mediator.lucene;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.junit.Test;

public class DefaultTokenStreamTest {
	@Test
	public void testTokenize() throws IOException {
		String symbol = "gene name";
		DefaultTokenStream stream = new DefaultTokenStream(new StringReader("gene name"));
		stream.reset();
		assertTrue(stream.incrementToken());
		TermAttribute term = stream.getAttribute(TermAttribute.class);
		assertNotNull(term);
		assertEquals(symbol, term.term());
		assertFalse(stream.incrementToken());
	}
}
