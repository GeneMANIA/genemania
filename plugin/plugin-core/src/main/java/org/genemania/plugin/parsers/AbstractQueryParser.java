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

package org.genemania.plugin.parsers;

import java.util.List;

import org.genemania.domain.Organism;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.OrganismMediator;
import org.genemania.plugin.apps.IQueryErrorHandler;
import org.genemania.plugin.data.DataSet;
import org.genemania.type.CombiningMethod;

public abstract class AbstractQueryParser implements IQueryParser {
	
	protected static String[] preferredGroupCodes = {
			"coexp", // co-expression //$NON-NLS-1$
			"pi", // physical interaction //$NON-NLS-1$
			"gi" // genetic interaction //$NON-NLS-1$
	};

    protected CombiningMethod parseCombiningMethod(String combiningMethod, Query query, IQueryErrorHandler handler) {
		if (combiningMethod == null) {
			CombiningMethod method = CombiningMethod.AUTOMATIC_SELECT;
			return method;
		}
		CombiningMethod method = CombiningMethod.fromCode(combiningMethod);
		if (method == CombiningMethod.UNKNOWN) {
			method = CombiningMethod.AUTOMATIC;
			handler.warn(String.format("Unrecognized combining method \"%s\".  Defaulting to: %s", combiningMethod, method)); //$NON-NLS-1$
		}
		return method;
	}

	public Organism parseOrganism(DataSet data, String name) throws DataStoreException {
		String filtered = name.toLowerCase();
		OrganismMediator mediator = data.getMediatorProvider().getOrganismMediator();
		List<Organism> organisms = mediator.getAllOrganisms();
		
		Long taxonId = null;
		try {
			taxonId = Long.parseLong(name);
		} catch (NumberFormatException e) {
		}
		
		for (Organism organism : organisms) {
			if (taxonId != null && taxonId == organism.getTaxonomyId()) {
				return organism;
			}
			String organismName = organism.getName();
			if (organismName != null && organismName.toLowerCase().equals(filtered)) {
				return organism;
			}
			String organismAlias = organism.getAlias();
			if (organismAlias != null && organismAlias.toLowerCase().equals(filtered)) {
				return organism;
			}
		}
		return null;
	}
}
