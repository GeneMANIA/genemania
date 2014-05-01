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
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.genemania.util.ProgressReporter;

public abstract class BaseNormalizer implements INormalizer {
	protected class ParsingContext {
		public String delimiter;
		public int droppedInteractions;
		public int totalInteractions;
		public Set<String> invalidSymbols;
		public List<Integer> idColumns;
		
		public ParsingContext(DataImportSettings settings) {
			delimiter = settings.getDelimiter();
			invalidSymbols = new HashSet<String>();
			idColumns = settings.getIdColumns();
		}
	}
	
	public NormalizationResult normalize(DataImportSettings classification, GeneCompletionProvider2 genes, Reader input, Writer output, ProgressReporter progress) throws IOException {
		ParsingContext context = new ParsingContext(classification);
		
		handleBeforeNormalize(context);
		PrintWriter writer = new PrintWriter(output);
		try {
			BufferedReader reader = new BufferedReader(input);
			try {
				String line = reader.readLine();
				while (line != null) {
					try {
						if (progress.isCanceled()) {
							return null;
						}
						progress.setDescription(String.format(Strings.installTextNetwork_description, context.totalInteractions, context.droppedInteractions));
						handleLine(context, line, genes, writer);
					} finally {
						line = reader.readLine();
					}
				}
			} finally {
				reader.close();
			}
		} finally {
			writer.close();
		}
		
		NormalizationResult result = new NormalizationResult();
		result.setDroppedEntries(context.droppedInteractions);
		result.setTotalEntries(context.totalInteractions);
		result.setInvalidSymbols(context.invalidSymbols);
		return result;
	}

	protected void handleBeforeNormalize(ParsingContext context) throws IOException {
	}

	protected abstract void handleLine(ParsingContext context, String line, GeneCompletionProvider2 genes, PrintWriter writer);
}
