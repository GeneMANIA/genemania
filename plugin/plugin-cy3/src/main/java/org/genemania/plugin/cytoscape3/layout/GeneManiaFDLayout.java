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
package org.genemania.plugin.cytoscape3.layout;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

/**
 * Copied and modified from Prefuse Force Directed layout.
 */
public class GeneManiaFDLayout extends AbstractLayoutAlgorithm {

	public static final String ALGORITHM_ID = "genemania-force-directed";
	static final String ALGORITHM_DISPLAY_NAME = "GeneMANIA Force Directed Layout";

	public GeneManiaFDLayout(UndoSupport undo) {
		super(ALGORITHM_ID, ALGORITHM_DISPLAY_NAME, undo);
	}

	@Override
	public TaskIterator createTaskIterator(final CyNetworkView networkView, final Object context,
			final Set<View<CyNode>> nodesToLayOut, final String attrName) {
		return new TaskIterator(new GeneManiaFDLayoutTask(toString(), networkView, nodesToLayOut,
				(GeneManiaFDLayoutContext) context, attrName, undoSupport));
	}

	@Override
	public Object createLayoutContext() {
		return new GeneManiaFDLayoutContext();
	}

	@Override
	public Set<Class<?>> getSupportedEdgeAttributeTypes() {
		final Set<Class<?>> ret = new HashSet<Class<?>>();

		ret.add(Integer.class);
		ret.add(Double.class);

		return ret;
	}

	@Override
	public boolean getSupportsSelectedOnly() {
		return true;
	}
}
