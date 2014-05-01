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
package org.genemania.plugin.cytoscape26;

import giny.view.Label;

import org.genemania.plugin.cytoscape2.support.Compatibility;

import cytoscape.CyNetwork;
import cytoscape.visual.LabelPosition;
import cytoscape.visual.mappings.ContinuousMapping;
import cytoscape.visual.mappings.DiscreteMapping;
import cytoscape.visual.mappings.PassThroughMapping;

public class Cy26Compatibility implements Compatibility {
	@Override
	public Object createDefaultNodeLabelPosition() {
		return new LabelPosition(Label.SOUTH, Label.NORTH, Label.JUSTIFY_CENTER, 0, 0);
	}
	
	@Override
	public DiscreteMapping createDiscreteMapping(Object defaultObject, String attributeName, CyNetwork network, MappingType type) {
		DiscreteMapping mapping = new DiscreteMapping(defaultObject, type.getValue());
		mapping.setControllingAttributeName(attributeName, network, false);
		return mapping;
	}
	
	@Override
	public ContinuousMapping createContinuousMapping(Object defaultObject, String attributeName, CyNetwork network, MappingType type) {
		ContinuousMapping mapping = new ContinuousMapping(defaultObject , type.getValue());
		mapping.setControllingAttributeName(attributeName, network, false);
		return mapping;
	}
	
	@Override
	public PassThroughMapping createPassThroughMapping(Object defaultObject, String attributeName) {
		return new PassThroughMapping(defaultObject, attributeName);
	}
}
