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
 * GeneNamingSourceMock: NamingSource mock object
 * Created Jul 28, 2009
 * @author Ovi Comes
 */
package org.genemania.mock;

import org.apache.log4j.Logger;
import org.genemania.domain.GeneNamingSource;

public class GeneNamingSourceMock {

	// __[static]______________________________________________________________
	private static Logger LOG = Logger.getLogger(GeneNamingSourceMock.class);
	private static GeneNamingSource[] MOCKS = new GeneNamingSource[1];
	private static GeneNamingSource mockObject1 = new GeneNamingSource( "Entrez Gene Name", (byte)9, "Entrez"); 
	static {
		MOCKS[0] = mockObject1 ;
	}
	
	public static GeneNamingSource getMockObject(int index) {
		GeneNamingSource ret = null; 
		if(index >= GeneNamingSourceMock.MOCKS.length) {
			LOG.error("I only have " + MOCKS.length + " mock objects. Please review your unit test");
		}
		return ret;
	}
	
}
