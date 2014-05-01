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
 * ValidationUtils: TODO add description
 * Created May 07, 2010
 * @author Ovi Comes
 */
package org.genemania.util;

import org.genemania.Constants;
import org.genemania.exception.ValidationException;

public class ValidationUtils {

	public static void validateEnrichmentParameters(int minCategories, long ontologyId, double qValueThreshold) throws ValidationException {
		if(minCategories < 0) {
			throw new ValidationException(Constants.ERROR_CODES.INVALID_ENRICHMENT_MIN_CATEGORIES);
		} else if(ontologyId <= 0) {
			throw new ValidationException(Constants.ERROR_CODES.INVALID_ONTOLOGY);
		} else if(qValueThreshold <= 0) {
			throw new ValidationException(Constants.ERROR_CODES.INVALID_ENRICHMENT_Q_VALUE);
		}
	}

}
