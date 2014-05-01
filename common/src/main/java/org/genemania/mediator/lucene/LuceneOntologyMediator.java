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

import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Searcher;
import org.genemania.domain.Ontology;
import org.genemania.domain.OntologyCategory;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.OntologyMediator;

public class LuceneOntologyMediator extends LuceneMediator implements OntologyMediator {

	public LuceneOntologyMediator(Searcher searcher, Analyzer analyzer) {
		super(searcher, analyzer);
	}

	public Ontology getOntology(long ontologyId) throws DataStoreException {
		return createOntology(ontologyId);
	}

	public OntologyCategory getCategory(long categoryId) throws DataStoreException {
		return createOntologyCategory(categoryId);
	}
	
	public List hqlSearch(String queryString) {
		return null;
	}

	public OntologyCategory getCategory(String name) throws DataStoreException {
		return createOntologyCategory(name);
	}

}
