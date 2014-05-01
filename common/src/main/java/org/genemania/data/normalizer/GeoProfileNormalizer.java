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

package org.genemania.data.normalizer;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.genemania.data.profile.ProfileCursor;
import org.genemania.data.profile.SoftProfileCursor;
import org.genemania.exception.ApplicationException;
import org.genemania.type.DataLayout;
import org.genemania.util.ProgressReporter;

public class GeoProfileNormalizer implements INormalizer {

	public NormalizationResult normalize(DataImportSettings settings, GeneCompletionProvider2 genes, Reader input, Writer output, ProgressReporter progress) throws IOException, ApplicationException {
		PrintWriter writer = new PrintWriter(output);
		Set<String> invalidSymbols = new HashSet<String>();
		int keptRows = 0;
		int droppedRows = 0;
		try {
			ProfileCursor cursor = createProfileCursor(settings, input);
			for (int i = 0; i < cursor.getTotalHeaders(); i++) {
				if (progress.isCanceled()) {
					return null;
				}
				if (i > 0) {
					writer.print("\t"); //$NON-NLS-1$
				}
				writer.print(cursor.getHeader(i));
			}
			writer.println();
			try {
				while (cursor.next()) {
					if (progress.isCanceled()) {
						return null;
					}
					progress.setDescription(String.format(Strings.installProfile_description, keptRows, droppedRows));
					String symbol = cursor.getId();
					
					Long nodeId = genes.getNodeId(symbol);
					if (nodeId == null) {
						invalidSymbols.add(symbol);
						droppedRows++;
						continue;
					}

					writer.print(nodeId);
					for (int i = 0; i < cursor.getTotalValues(); i++) {
						writer.print("\t"); //$NON-NLS-1$
						double value = cursor.getValue(i);
						if (Double.isNaN(value) || Double.isInfinite(value)) {
							writer.print("null"); //$NON-NLS-1$
						} else {
							writer.print(value);
						}
					}
					writer.println();
					keptRows++;
				}
			} finally {
				cursor.close();
			}
		} finally {
			writer.close();
		}
		
		// We changed the layout through normalization so we'll indicate that
		// here.
		settings.setDataLayout(DataLayout.PROFILE);
		
		NormalizationResult result = new NormalizationResult();
		result.setDroppedEntries(droppedRows);
		result.setTotalEntries(keptRows);
		result.setInvalidSymbols(invalidSymbols);
		return result;
	}
	
	private static ProfileCursor createProfileCursor(DataImportSettings settings, Reader reader) throws IOException, ApplicationException {
		return new SoftProfileCursor(reader);
	}
}
