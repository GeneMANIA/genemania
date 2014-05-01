package org.genemania.mediator.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.genemania.domain.Gene;
import org.genemania.domain.GeneData;
import org.genemania.domain.GeneNamingSource;
import org.genemania.domain.Organism;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.GeneMediator;

/*
 * wrap the given gene mediator to provide caching of
 * symbol lookups: getNodeId(organism, symbol).
 *
 * intended as a short-lifetime throw-away cache for
 * processing steps that require fast repeated lookups, like
 * network loading. Only symbols for a single organism
 * are cached.
 */
public class CachingGeneMediator implements GeneMediator {

    private GeneMediator backingGeneMediator;
    private long cachedOrganismId;
    private HashMap<String, Long> cache = new HashMap<String, Long>();

    public CachingGeneMediator(GeneMediator backingGeneMediator) {
        this.backingGeneMediator = backingGeneMediator;
    }

    /*
     * cached lookup
     */
    @Override
    public Long getNodeId(long organismId, String symbol) {
        check(organismId);
        if (cache.containsKey(symbol)) {
            return cache.get(symbol);
        }
        else {
            Long nodeId = backingGeneMediator.getNodeId(organismId, symbol);
            cache.put(symbol, nodeId);
            return nodeId;
        }
    }

    /*
     * for correctness, flush cache if organism changes. not the expected use case.
     */
    private void check(long organismId) {
        if (this.cachedOrganismId != organismId) {
            cache = new HashMap<String, Long>();
            this.cachedOrganismId = organismId;
        }
    }

    // everything else just delegates to the wrapped mediator

    @Override
    public GeneNamingSource findNamingSourceByName(String arg0) {
        return backingGeneMediator.findNamingSourceByName(arg0);
    }

    @Override
    public List<Gene> getAllGenes(long arg0) {
        return backingGeneMediator.getAllGenes(arg0);
    }

    @Override
    public String getCanonicalSymbol(long arg0, String arg1) {
        return backingGeneMediator.getCanonicalSymbol(arg0, arg1);
    }

    @Override
    public Gene getGeneForSymbol(Organism arg0, String arg1) {
        return backingGeneMediator.getGeneForSymbol(arg0,  arg1);
    }

    @Override
    public List<Gene> getGenes(List<String> arg0, long arg1) throws DataStoreException {
        return backingGeneMediator.getGenes(arg0, arg1);
    }

    @Override
    public Set<String> getSynonyms(long arg0, String arg1) {
        return backingGeneMediator.getSynonyms(arg0, arg1);
    }

    @Override
    public Set<String> getSynonyms(long arg0, long arg1) {
        return backingGeneMediator.getSynonyms(arg0, arg1);
    }

    @Override
    public boolean isValid(long arg0, String arg1) {
        return backingGeneMediator.isValid(arg0, arg1);
    }

    @Override
    public void updateGeneData(Organism arg0, String arg1, GeneData arg2) {
        backingGeneMediator.updateGeneData(arg0, arg1, arg2);
    }
}
