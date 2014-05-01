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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.genemania.exception.ApplicationException;
import org.genemania.util.ProgressReporter;

public abstract class IndexBuilder {
	protected Analyzer createAnalyzer() {
		return new StandardAnalyzer(Version.LUCENE_29);
	}
	
	void write(String path, Directory source, Analyzer analyzer, MaxFieldLength length) throws IOException {
		FSDirectory directory = FSDirectory.open(new File(path));
		try {
			IndexReader reader = IndexReader.open(source, true);
			try {
				IndexWriter writer = new IndexWriter(directory, analyzer, length);
				try {
					writer.addIndexes(new IndexReader[] { reader });
				} finally {
					writer.close();
				}
			} finally {
				reader.close();
			}
		} finally {
			directory.close();
		}
	}
	
	protected void createIndex(String indexPath, Analyzer analyzer, ProgressReporter progress) throws ApplicationException {
		try {
			RAMDirectory directory = new RAMDirectory();
			IndexWriter writer = new IndexWriter(directory, analyzer, MaxFieldLength.LIMITED);
			try {
				createIndex(writer, progress);
			} finally {
				writer.close();
			}
			if (progress.isCanceled()) {
				return;
			}
			write(indexPath, directory, analyzer, MaxFieldLength.LIMITED);
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}

	protected abstract void createIndex(IndexWriter writer, ProgressReporter progress) throws ApplicationException;
}
