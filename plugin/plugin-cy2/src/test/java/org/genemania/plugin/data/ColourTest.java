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

package org.genemania.plugin.data;

import static org.junit.Assert.assertEquals;

import org.genemania.plugin.data.Colour;
import org.junit.Test;

public class ColourTest {
	@Test
	public void testRgb() {
		Colour colour = new Colour(0x030201);
		assertEquals(3, colour.getRed());
		assertEquals(2, colour.getGreen());
		assertEquals(1, colour.getBlue());
	}
	
	@SuppressWarnings("nls")
	@Test
	public void testRgb2() {
		int value = Integer.parseInt("030201", 16);
		Colour colour = new Colour(value);
		assertEquals(3, colour.getRed());
		assertEquals(2, colour.getGreen());
		assertEquals(1, colour.getBlue());
	}

	@Test
	public void testComponents() {
		Colour colour = new Colour(4, 5, 6);
		assertEquals(4, colour.getRed());
		assertEquals(5, colour.getGreen());
		assertEquals(6, colour.getBlue());
	}
}
