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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.genemania.type.DataLayout;
import org.genemania.type.ImportedDataFormat;
import org.genemania.type.NetworkProcessingMethod;

/**
 * Identifies the type of data contained within a file.
 */
public class DataFileClassifier {
	public DataFileClassifier() {
	}
	
	public void classify(DataImportSettings result, InputStream stream, int maxLinesToSample) throws IOException {
		for (String delimiter : new String[] { "\t", "," }) { //$NON-NLS-1$ //$NON-NLS-2$
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			
			Map<Integer, Integer> entriesPerLine = new HashMap<Integer, Integer>();
			int linesRead = 0;
			for (int i = 0; i < maxLinesToSample; i++) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				String[] parts = line.split(delimiter);
				int key = parts.length;
				int value;
				if (entriesPerLine.containsKey(key)) {
					value = entriesPerLine.get(key) + 1;
				} else {
					value = 1;
				}
				entriesPerLine.put(key, value);
				linesRead++;
			}
			
			Entry<Integer, Integer> entry = getMostFrequent(entriesPerLine);
			if (entry == null) {
				continue;
			}
			int columns = entry.getKey();
			double score = entry.getValue() / (double) linesRead;
			
			if (columns == 2 && score > 0.5) {
				result.dataFormat = ImportedDataFormat.NETWORK_DATA_TAB_DELIMITED;
				result.dataLayout = DataLayout.BINARY_NETWORK;
				result.processingMethod = NetworkProcessingMethod.DIRECT;
				result.setDelimiter(delimiter);
				return;
			}
			if (columns == 3 && score > 0.5) {
				result.dataFormat = ImportedDataFormat.NETWORK_DATA_TAB_DELIMITED;
				result.dataLayout = DataLayout.WEIGHTED_NETWORK;
				result.processingMethod = NetworkProcessingMethod.DIRECT;
				result.setDelimiter(delimiter);
				return;
			}
			if (columns > 3 && score > 0.5) {
				result.dataFormat = ImportedDataFormat.PROFILE_DATA_TAB_DELIMITED;
				result.dataLayout = DataLayout.GEO_PROFILE;
				result.processingMethod = NetworkProcessingMethod.PEARSON;
				result.setDelimiter(delimiter);
				return;
			}
		}
		result.dataFormat = ImportedDataFormat.UNKNOWN;
		result.dataLayout = DataLayout.UNKNOWN;
		result.processingMethod = NetworkProcessingMethod.UNKNOWN;
	}

	<T> Entry<T, Integer> getMostFrequent(Map<T, Integer> frequencies) {
		int maxCount = 0;
		Entry<T, Integer> mostFrequent = null;
		for (Entry<T, Integer> entry : frequencies.entrySet()) {
			if (entry.getValue() > maxCount) {
				maxCount = entry.getValue();
				mostFrequent = entry;
			}
		}
		return mostFrequent;
	}

}
