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

package org.genemania.mediator.lucene;

import java.io.Reader;

import org.apache.lucene.analysis.CharTokenizer;

/**
 * A token stream that treats its entire input as a single token.  This class
 * is not meant to be used for full-text indexing.
 */
public class DefaultTokenStream extends CharTokenizer {
	public DefaultTokenStream(Reader input) {
		super(input);
	}

	@Override
	protected boolean isTokenChar(char c) {
		return true;
	}
}
