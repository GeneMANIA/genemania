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
 * GeneSynonymsComparator: Compares genes based on their naming source rank.   
 * Created Nov 06, 2008
 * @author Ovi Comes
 */
package org.genemania.util;

import java.io.Serializable;
import java.util.Comparator;

import org.genemania.domain.Gene;

public class GeneSynonymsComparator implements Comparator, Serializable {

	// __[static]______________________________________________________________
	private static final long serialVersionUID = -520115591560644253L;

	// __[interface implementation]____________________________________________
	public int compare(Object o1, Object o2) {
		int ret = 0;
		if((o1 != null) && (o2 != null) && (o1 instanceof Gene) && (o2 instanceof Gene)) {
			Gene gene1 = (Gene)o1;
			Gene gene2 = (Gene)o2;
			if((gene1.getNamingSource() != null) && (gene2.getNamingSource() != null)) {
				byte rank1 = gene1.getNamingSource().getRank();
				byte rank2 = gene2.getNamingSource().getRank();
				ret = rank2 - rank1;
			}
		}
		return ret;
	}

}
