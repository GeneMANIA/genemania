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
 * GeneListAnalyzer: Lucene Analyzer for gene lists with user-defined gene separator  
 * Created Jan 26, 2010
 * @author Ovi Comes
 */
package org.genemania.completion.lucene;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.IndexReader;
import org.genemania.Constants;

public class GeneListAnalyzer extends Analyzer {

	// __[attributes]__________________________________________________________
	private String geneSeparator = Constants.DEFAULT_GENE_SEPARATOR;
	
	// __[constructors]________________________________________________________
	public GeneListAnalyzer(IndexReader in, String geneSeparator) {
		this.geneSeparator = geneSeparator;
	}

	// __[accessors]___________________________________________________________
	public String getGeneSeparator() {
		return geneSeparator;
	}

	public void setGeneSeparator(String geneSeparator) {
		this.geneSeparator = geneSeparator;
	}
	
	// __[public interface]____________________________________________________
	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		return new LowerCaseFilter(
			new CharTokenizer(reader) {
				@Override
				protected boolean isTokenChar(char c) {
					return !String.valueOf(c).equals(geneSeparator); 
				}
			}
		);
	}

}
