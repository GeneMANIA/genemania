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
 * NodeMock: Node mock object
 * Created Jul 28, 2009
 * @author Ovi Comes
 */
package org.genemania.mock;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.genemania.domain.Gene;
import org.genemania.domain.Node;

public class NodeMock {

	// __[static]______________________________________________________________
	private static Logger LOG = Logger.getLogger(NodeMock.class);
	private static Node[] MOCKS = new Node[1];
	private static Node mockObject1 = new Node();
	static {
		mockObject1.setId(156780);
		mockObject1.setGeneData(GeneDataMock.getMockObject(0));
		mockObject1.setName("At:156780");
		Collection<Gene> mockGenes1 = new ArrayList<Gene>();
		mockGenes1.add(GeneMock.getMockObject(0));
		mockObject1.setGenes(mockGenes1 );
		MOCKS[0] = mockObject1 ;
	}
	
	
	public static Node getMockObject(int index) {
		Node ret = null; 
		if(index >= MOCKS.length) {
			LOG.error("I only have " + MOCKS.length + " mock objects. Please review your unit test");
		}
		return ret;
	}
	
}
