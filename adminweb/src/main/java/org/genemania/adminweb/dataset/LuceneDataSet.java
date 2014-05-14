package org.genemania.adminweb.dataset;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.FSDirectory;
import org.genemania.mediator.AttributeMediator;
import org.genemania.mediator.GeneMediator;
import org.genemania.mediator.NetworkMediator;
import org.genemania.mediator.NodeMediator;
import org.genemania.mediator.OntologyMediator;
import org.genemania.mediator.OrganismMediator;
import org.genemania.mediator.StatsMediator;
import org.genemania.mediator.lucene.LuceneAttributeMediator;
import org.genemania.mediator.lucene.LuceneGeneMediator;
import org.genemania.mediator.lucene.LuceneMediator;
import org.genemania.mediator.lucene.LuceneNetworkMediator;
import org.genemania.mediator.lucene.LuceneNodeMediator;
import org.genemania.mediator.lucene.LuceneOntologyMediator;
import org.genemania.mediator.lucene.LuceneOrganismMediator;
import org.genemania.mediator.lucene.LuceneStatsMediator;

public class LuceneDataSet {
    Analyzer analyzer;
    Searcher searcher;
    OrganismMediator organismMediator;
    NetworkMediator networkMediator;
    GeneMediator geneMediator;
    NodeMediator nodeMediator;
    OntologyMediator ontologyMediator;
    StatsMediator statsMediator;
    AttributeMediator attributeMediator;

    public static LuceneDataSet instance(String indexDir) throws IOException {
        LuceneDataSet luceneDataSet = new LuceneDataSet();
        luceneDataSet.init(indexDir);
        return luceneDataSet;
    }

	public void init(String indexDir) throws IOException {
		analyzer = LuceneMediator.createDefaultAnalyzer();
		searcher = createSearcher(indexDir);

		setGeneMediator(new LuceneGeneMediator(searcher, analyzer));
		setNetworkMediator(new LuceneNetworkMediator(searcher, analyzer));
		setOrganismMediator(new LuceneOrganismMediator(searcher, analyzer));
		setNodeMediator(new LuceneNodeMediator(searcher, analyzer));
		setOntologyMediator(new LuceneOntologyMediator(searcher, analyzer));
		setStatsMediator(new LuceneStatsMediator(searcher, analyzer));
		setAttributeMediator(new LuceneAttributeMediator(searcher, analyzer));
	}

	protected static Searcher createSearcher(String indexPath) throws IOException {
		ArrayList<Searcher> searchers = new ArrayList<Searcher>();

		File indices = new File(indexPath);
		for (File file : indices.listFiles()) {
			if (!LuceneMediator.indexExists(file)) {
				continue;
			}
			try {
				FSDirectory directory = FSDirectory.open(file);
				searchers.add(new IndexSearcher(directory));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (searchers.size() == 0) {
			throw new IOException("No indices found");
		}
		return new MultiSearcher(searchers.toArray(new Searchable[searchers.size()]));
	}

    public GeneMediator getGeneMediator() {
        return geneMediator;
    }

    public void setGeneMediator(GeneMediator geneMediator) {
        this.geneMediator = geneMediator;
    }

    public NetworkMediator getNetworkMediator() {
        return networkMediator;
    }

    public void setNetworkMediator(NetworkMediator networkMediator) {
        this.networkMediator = networkMediator;
    }

    public NodeMediator getNodeMediator() {
        return nodeMediator;
    }

    public void setNodeMediator(NodeMediator nodeMediator) {
        this.nodeMediator = nodeMediator;
    }

    public OrganismMediator getOrganismMediator() {
        return organismMediator;
    }

    public void setOrganismMediator(OrganismMediator organismMediator) {
        this.organismMediator = organismMediator;
    }

    public OntologyMediator getOntologyMediator() {
        return ontologyMediator;
    }

    public void setOntologyMediator(OntologyMediator ontologyMediator) {
        this.ontologyMediator = ontologyMediator;
    }

    public StatsMediator getStatsMediator() {
		return statsMediator;
	}

	public void setStatsMediator(StatsMediator statsMediator) {
		this.statsMediator = statsMediator;
	}

	public AttributeMediator getAttributeMediator() {
        return attributeMediator;
    }

    public void setAttributeMediator(AttributeMediator attributeMediator) {
        this.attributeMediator = attributeMediator;
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    public Searcher getSearcher() {
        return searcher;
    }

    public void close() throws IOException {
        searcher.close();
        analyzer.close();
    }
}
