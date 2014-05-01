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
 * GeneDataMock: GeneData mock object
 * Created Jul 28, 2009
 * @author Ovi Comes
 */
package org.genemania.mock;

import org.apache.log4j.Logger;
import org.genemania.domain.GeneData;

public class GeneDataMock {
	
	// __[static]______________________________________________________________
	private static Logger LOG = Logger.getLogger(GeneDataMock.class);
	private static GeneData[] MOCKS = new GeneData[1];
	private static GeneData mockObject1 = new GeneData("STE1 (STEROL 1); C-5 sterol desaturase", "820679", GeneNamingSourceMock.getMockObject(0));
	static {
		MOCKS[0] = mockObject1 ;
	}

	public static GeneData getMockObject(int index) {
		GeneData ret = null; 
		if(index >= MOCKS.length) {
			LOG.error("I only have " + MOCKS.length + " mock objects. Please review your unit test");
		}
		return ret;
	}
	
}
