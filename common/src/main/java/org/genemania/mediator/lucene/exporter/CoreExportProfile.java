package org.genemania.mediator.lucene.exporter;

import static org.genemania.mediator.lucene.exporter.Generic2LuceneExporter.NETWORK_ID;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.mediator.lucene.LuceneMediator;
import org.genemania.mediator.lucene.LuceneOrganismMediator;

public class CoreExportProfile implements ExportProfile {

	private Set<Long> includedNetworks;

	public CoreExportProfile(String basePath) throws IOException {
		String indexPath = String.format("%s%s%s", basePath, File.separator, "lucene_index");
		Searcher searcher = createSearcher(indexPath);
		LuceneOrganismMediator mediator = new LuceneOrganismMediator(searcher, LuceneMediator.createDefaultAnalyzer());

		includedNetworks = new HashSet<Long>();
		for (Organism organism : mediator.getAllOrganisms()) {
			for (InteractionNetworkGroup group : organism.getInteractionNetworkGroups()) {
				for (InteractionNetwork network : group.getInteractionNetworks()) {
					if (network.isDefaultSelected()) {
						includedNetworks.add(network.getId());
					}
				}
			}
		}
		searcher.close();
	}

	Searcher createSearcher(String indexPath) throws IOException {
		ArrayList<Searcher> searchers = new ArrayList<Searcher>();
		
		File indices = new File(indexPath);
		for (File file : indices.listFiles()) {
			if (!LuceneMediator.indexExists(file)) {
				continue;
			}
			FSDirectory directory = FSDirectory.open(file);
			searchers.add(new IndexSearcher(directory));
		}
		if (searchers.size() == 0) {
			Directory directory = createEmptyIndex();
			searchers.add(new IndexSearcher(directory));
		}
		return new MultiSearcher(searchers.toArray(new Searchable[searchers.size()]));		
	}
	
	private Directory createEmptyIndex() {
		RAMDirectory directory = new RAMDirectory();
		try {
			IndexWriter writer = new IndexWriter(directory, new StandardAnalyzer(Version.LUCENE_29), true, MaxFieldLength.UNLIMITED);
			writer.commit();
			writer.close();
		} catch (CorruptIndexException e) {
		} catch (LockObtainFailedException e) {
		} catch (IOException e) {
		}
		return directory;
	}

	@Override
	public boolean includesNetwork(String[] networkData) {
		long networkId = Long.parseLong(networkData[NETWORK_ID]);
		return includedNetworks.contains(networkId);
	}

}
