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
package org.genemania.plugin.cytoscape2.support;

import cytoscape.CyNetwork;
import cytoscape.visual.mappings.ContinuousMapping;
import cytoscape.visual.mappings.DiscreteMapping;
import cytoscape.visual.mappings.PassThroughMapping;

/**
 * Provides uniform access to methods that have changed signatures in
 * Cytoscape 2.X.
 */
public interface Compatibility {
	public enum MappingType {
		EDGE(0),
		NODE(1);
		
		private byte value;

		private MappingType(int value) {
			this.value = (byte) value;
		}
		
		public byte getValue() {
			return value;
		}
	}
	
	/**
	 * Hack for ticket #1452.
	 */
	Object createDefaultNodeLabelPosition();

	/**
	 * DiscreteMapping's constructor changed in 2.8.
	 * @param defaultObject 
	 */
	DiscreteMapping createDiscreteMapping(Object defaultObject, String attributeName, CyNetwork network, MappingType type);

	ContinuousMapping createContinuousMapping(Object defaultObject, String attributeName, CyNetwork network, MappingType type);

	PassThroughMapping createPassThroughMapping(Object defaultObject, String attributeName);
}
