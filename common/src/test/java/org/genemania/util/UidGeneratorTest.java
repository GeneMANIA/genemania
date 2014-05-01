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
 * UidGeneratorTest: JUnit test class for UidGenerator
 * Created Nov 11, 2009
 * @author Ovi Comes
 */
package org.genemania.util;

import org.genemania.AbstractTest;
import org.junit.Test;

public class UidGeneratorTest extends AbstractTest {

	// __[constructors]________________________________________________________
	public UidGeneratorTest() {
		super();
	}
	
	// __[test cases]__________________________________________________________
	@Test
	public void testGetNegativeUid() {
		long uid1 = UidGenerator.getInstance().getNegativeUid();
		long uid2 = UidGenerator.getInstance().getNegativeUid();
		assertTrue("uid is negative:" + uid1, uid1 < 0);
		assertTrue("uid min test: " + uid1, uid1 > Integer.MIN_VALUE);
		assertTrue("uid max test:" + uid1, uid1 < Integer.MAX_VALUE);
		assertTrue("uid is negative:" + uid2, uid2 < 0);
		assertTrue("uid min test: " + uid2, uid2 > Integer.MIN_VALUE);
		assertTrue("uid max test:" + uid2, uid2 < Integer.MAX_VALUE);
		assertNotSame("ID must be unique", uid1, uid2);
	}

}
