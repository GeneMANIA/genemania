package org.genemania.mediator.lucene.exporter;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.genemania.mediator.lucene.LuceneMediator;

public class IndexUpdater {
	private File basePath;
	private Analyzer analyzer;

	public IndexUpdater(File basePath, Analyzer analyzer) {
		this.basePath = basePath;
		this.analyzer = analyzer;
	}
	
	public void updateNetworkStats(long organismId, long networkId, long newInteractionCount) throws IOException {
		IndexWriter writer = openIndexWriter(organismId);
		try {
			IndexReader reader = writer.getReader();
			IndexSearcher searcher = new IndexSearcher(reader);
			Document document;
			Document metadata;
			String metadataId;
			try {
				document = getFirst(searcher, new TermQuery(new Term(LuceneMediator.NETWORK_ID, String.valueOf(networkId))));
				if (document == null) {
					throw new IllegalArgumentException(String.format("Network with id=%d and organism id=%d does not exist", networkId, organismId));
				}
				metadataId = document.get(LuceneMediator.NETWORK_METADATA_ID);
				metadata = getFirst(searcher, new TermQuery(new Term(LuceneMediator.NETWORKMETADATA_ID, metadataId)));
				if (metadata == null) {
					throw new IllegalArgumentException(String.format("Metadata id=%s for network id=%d and organism id=%d does not exist", metadataId, networkId, organismId));
				}
			} finally {
				searcher.close();
                reader.close();
			}

			System.out.println(metadata.get(LuceneMediator.NETWORKMETADATA_INTERACTION_COUNT));
			metadata.removeField(LuceneMediator.NETWORKMETADATA_INTERACTION_COUNT);
			metadata.add(new Field(LuceneMediator.NETWORKMETADATA_INTERACTION_COUNT, String.valueOf(newInteractionCount), Store.YES, Index.ANALYZED));
			writer.updateDocument(new Term(LuceneMediator.NETWORKMETADATA_ID, String.valueOf(metadataId)), metadata , analyzer);
			writer.commit();
		} finally {
			writer.close();
		}
	}
	
	public void updateNetworkIsDefault(long organismId, long networkId, boolean isDefault) throws IOException {
        IndexWriter writer = openIndexWriter(organismId);
        try {
            IndexReader reader = writer.getReader();
            IndexSearcher searcher = new IndexSearcher(reader);
            Document document;
            try {
                document = getFirst(searcher, new TermQuery(new Term(LuceneMediator.NETWORK_ID, String.valueOf(networkId))));
                if (document == null) {
                    throw new IllegalArgumentException(String.format("Network with id=%d and organism id=%d does not exist", networkId, organismId));
                }
            }
            finally {
                searcher.close();
				reader.close();
            }
            
            document.removeField(LuceneMediator.NETWORK_DEFAULT_SELECTED);
            document.add(new Field(LuceneMediator.NETWORK_DEFAULT_SELECTED, isDefault ? "true" : "false", Store.YES, Index.ANALYZED));
            writer.updateDocument(new Term(LuceneMediator.NETWORK_ID, String.valueOf(networkId)), document, analyzer);
            
            writer.commit();
        }
        finally {
            writer.close();
        }
	}
	
	private Document getFirst(IndexSearcher searcher, Query query) throws IOException {
		TopDocs topDocs = searcher.search(query, 1);
		if (topDocs.scoreDocs.length == 0) {
			return null;
		}
		return searcher.doc(topDocs.scoreDocs[0].doc);
	}

	private IndexWriter openIndexWriter(long organismId) throws IOException {
		String path = String.format("%s%s%d", basePath.getPath(), File.separator, organismId);
		Directory directory = FSDirectory.open(new File(path));
		return new IndexWriter(directory, analyzer, MaxFieldLength.UNLIMITED);
	}
}
