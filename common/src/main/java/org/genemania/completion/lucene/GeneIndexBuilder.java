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

package org.genemania.completion.lucene;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.genemania.domain.Gene;
import org.genemania.domain.Node;
import org.genemania.domain.Organism;
import org.genemania.exception.ApplicationException;
import org.genemania.mediator.GeneMediator;
import org.genemania.mediator.lucene.LuceneMediator;
import org.genemania.util.ProgressReporter;

public class GeneIndexBuilder extends IndexBuilder {
	public static final String GENE_FIELD = "gene";
	public static final String NODE_ID_FIELD = "node";

	String basePath;
	Collection<Organism> organisms;
	GeneMediator geneMediator;
	Organism organism;
	
	public GeneIndexBuilder(String basePath, GeneMediator geneMediator) {
		this(basePath, null, geneMediator);
	}
	
	public GeneIndexBuilder(String basePath, Collection<Organism> organisms, GeneMediator geneMediator) {
		this.basePath = basePath;
		this.organisms = organisms;
		this.geneMediator = geneMediator;
	}
	
	public void setOrganisms(Collection<Organism> organisms) {
		this.organisms = organisms;
	}
	
	public void setGeneMediator(GeneMediator geneMediator) {
		this.geneMediator = geneMediator;
	}
	
	IndexReader getIndexReader(Organism organism) throws IOException {
		String path = getIndexPath(organism);
		FSDirectory directory = FSDirectory.open(new File(path));
		return IndexReader.open(directory, true);
	}
	
	private String getIndexPath(Organism organism) {
		String ret = "";
		if(organism != null) {
			ret = String.format("%s", organism.getId());
			if (basePath != null) {
				ret = String.format("%s%s%s", basePath, File.separator, organism.getId());
			}
		}
		return ret;
	}

	public void createIndex(ProgressReporter progress) throws ApplicationException {
		Analyzer analyzer = createAnalyzer();
		int total = organisms.size();
		int index = 1;
		for (Organism organism : organisms) {
			if (progress.isCanceled()) {
				break;
			}
			progress.setStatus(String.format("Indexing organism %d/%d", index, total));
			String path = getIndexPath(organism);
			this.organism = organism;
			createIndex(path, analyzer, progress);
			index++;
		}
	}
	
	@Override
	protected void createIndex(IndexWriter writer, ProgressReporter progress) throws ApplicationException {
		List<Gene> genes = geneMediator.getAllGenes(organism.getId());
		
		int total = genes.size();
		int index = 0;
		progress.setMaximumProgress(total);
		try {
			for (Gene gene : genes) {
				if (progress.isCanceled()) {
					return;
				}
				Node node = gene.getNode();
				long nodeId = node.getId();
				progress.setProgress(index);
				Document document = new Document();
				document.add(new Field(NODE_ID_FIELD, String.valueOf(nodeId), Store.YES, Index.ANALYZED));
				document.add(new Field(GENE_FIELD, gene.getSymbol(), Store.YES, Index.ANALYZED));
				writer.addDocument(document);
				index++;
			}
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}
	
	public GeneCompletionProvider createCompletionProvider(Organism organism, ProgressReporter progress, boolean rebuild) throws ApplicationException {
		Analyzer analyzer = createAnalyzer();
		String indexPath = getIndexPath(organism);
		File indexFile = new File(indexPath);
		if (!indexFile.exists()) {
			if (!rebuild) {
				throw new ApplicationException(String.format("Index not found: %s", indexFile.getPath()));
			}
			this.organism = organism;
			createIndex(indexPath, analyzer, progress);
		}
		if (progress.isCanceled()) {
			return null;
		}
		try {
			IndexReader reader = getIndexReader(organism);
			return new GeneCompletionProvider(reader, analyzer);
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	protected Analyzer createAnalyzer() {
		return LuceneMediator.createDefaultAnalyzer();
	}
}
