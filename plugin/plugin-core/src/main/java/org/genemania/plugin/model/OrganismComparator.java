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

package org.genemania.plugin.model;

import java.util.Comparator;

import org.genemania.domain.Organism;

public class OrganismComparator implements Comparator<Organism> {
	private static Comparator<Organism> instance = new OrganismComparator();
	
	public static Comparator<Organism> getInstance() {
		return instance;
	}
	public int compare(Organism o1, Organism o2) {
		return o1.getName().compareTo(o2.getName());
	}
}
