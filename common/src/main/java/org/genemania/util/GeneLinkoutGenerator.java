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

package org.genemania.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.genemania.domain.Gene;
import org.genemania.domain.Node;
import org.genemania.domain.Organism;

/**
 * compute link-outs to external sources for a given gene.
 *
 * maintains a registry of linkout generators for each organism.
 * each individual generator is responsible for examining a nodes
 * identifier & source data and returning link data when possible.
 *
 * to add new generators, subclass Generator and override generate()
 * for genes and/or nodes as appropriate, then register the new generator
 * to the required organisms in registerDefault().
 *
 * iterating over the resulting map will visit the links in the
 * order they were registered
 */
public class GeneLinkoutGenerator {

    // evil domain specific constants
    // TODO: is there already a place for these somewhere?
    private static long At_ORG_ID = 1;
    private static long Ce_ORG_ID = 2;
    private static long Dm_ORG_ID = 3;
    private static long Hs_ORG_ID = 4;
    private static long Mm_ORG_ID = 5;
    private static long Sc_ORG_ID = 6;
    private static long Rn_ORG_ID = 7;
    private static long Dr_ORG_ID = 8;
    private static long Ec_ORG_ID = 9;
    private static long Tt_ORG_ID = 10;

    private static String SOURCE_TAIR_ID = "TAIR ID";
    private static String SOURCE_ENTREZ_GENE_ID = "Entrez Gene ID";
    private static String SOURCE_ENTREZ_GENE_NAME = "Entrez Gene Name";
    private static String SOURCE_ENSEMBL_GENE_ID = "Ensembl Gene ID";
    private static String SOURCE_ENSEMBL_GENE_NAME = "Ensembl Gene Name";
    private static String SOURCE_GENE_NAME = "Gene Name";

    private static GeneLinkoutGenerator instance;

    public static GeneLinkoutGenerator instance() {
        if (instance == null) {  // not thread safe!
            instance = new GeneLinkoutGenerator();
            instance.registerDefault();
        }

        return instance;
    }

    /*
     * registry for linkout generators associated with each organism id
     */
    class Registry {

        Map<Long, List<Generator>> registeredGenerators = new HashMap<Long, List<Generator>>();
        List<Generator> fallbackGenerators = new ArrayList<Generator>();

        void register(long organismId, Generator generator) {
            if (registeredGenerators.containsKey(organismId)) {
                List<Generator> generatorList = registeredGenerators.get(organismId);
                generatorList.add(generator);
            }
            else {
                List<Generator> generatorList = new ArrayList<Generator>();
                generatorList.add(generator);
                registeredGenerators.put(organismId, generatorList);
            }
        }

        void registerFallback(Generator generator) {
        	fallbackGenerators.add(generator);
        }

        Iterator<Generator> iterator(long organismId) {
            if (registeredGenerators.containsKey(organismId)) {
                return registeredGenerators.get(organismId).iterator();
            }
            else { // unregistered organism, can at least use fallbacks
                return fallbackGenerators.iterator();
            }
        }
    }
    Registry registry = new Registry();

    /*
     * setup the standard linkouts for each organism
     */
    void registerDefault() {

        // plant
        registry.register(At_ORG_ID, bar);
        registry.register(At_ORG_ID, tairOrTairFromEntrez);
        registry.register(At_ORG_ID, entrezOrEnsembl);

        // worm
        registry.register(Ce_ORG_ID, wormbase);
        registry.register(Ce_ORG_ID, entrezOrEnsembl);

        // fly
        registry.register(Dm_ORG_ID, flybase);
        registry.register(Dm_ORG_ID, entrezOrEnsembl);

        // human
        registry.register(Hs_ORG_ID, entrezOrEnsembl);

        // mouse
        registry.register(Mm_ORG_ID, entrezOrEnsembl);

        // yeast
        registry.register(Sc_ORG_ID, sgd);
        registry.register(Sc_ORG_ID, entrezOrEnsembl);

        // Tetrahymena
        registry.register(Tt_ORG_ID, entrezOrEnsemblProtists);

        // fallback to entrezOrEnsembl if we add organisms until
        // we get around to adding custom linkouts for them
        registry.registerFallback(entrezOrEnsembl);
    }

    /*
     * main entry point for users, returns a map containing
     * link names to link urls for a given node by applying all
     * registered generators for the organism.
     */
    public Map<String, String> getLinkouts(Organism organism, Node node) {

        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();

        Iterator<Generator> generators = registry.iterator(organism.getId());
        while (generators.hasNext()) {
            Generator generator = generators.next();

            Linkout linkout = generator.generate(organism, node);
            if (linkout != null) {
                result.put(linkout.getName(), linkout.getUrl());
            }
        }

        return result;
    }

    /*
     * generate a linkout for a particular node. generate()
     * is the main entry point.
     */
    class Generator {

        /*
         * default implementation just loops over all symbols
         * associated with a node, processes each, and returns the
         *  first to return a non-null linkout.  otherwise returns null
         */
        Linkout generate(Organism organism, Node node) {
            if (node == null || node.getGenes() == null) {
                return null;
            }

            for (Gene gene: node.getGenes()) {
                Linkout linkout = generate(organism, gene);
                if (linkout != null) {
                    return linkout;
                }
            }

            return null;
        }

        /*
         * return a linkout for an individual gene when possible,
         * else return null
         */
        Linkout generate(Organism organism, Gene gene) {
            throw new RuntimeException("not implemented");
        }
    }

    /*
     * entrez linkout generator
     */
    Generator entrez = new Generator() {

        static final String DISPLAY_NAME = "Entrez";
        static final String URL_TEMPLATE = "http://www.ncbi.nlm.nih.gov/sites/entrez?db=gene&cmd=search&term=%s";

        @Override
        Linkout generate(Organism organism, Gene gene) {
            if (gene == null) {
                return null;
            }

            if (gene.getNamingSource() == null) {
                return null;
            }

            if (SOURCE_ENTREZ_GENE_ID.equalsIgnoreCase(gene.getNamingSource().getName())) {
                String url = String.format(URL_TEMPLATE, gene.getSymbol());
                return new Linkout(DISPLAY_NAME, url);
            }

            return null;
        }
    };

    /*
     * plant specific linkout generator for TAIR
     */
    Generator tair = new Generator() {

        static final String DISPLAY_NAME = "TAIR";
        static final String URL_TEMPLATE = "http://arabidopsis.org/servlets/TairObject?type=locus&name=%s";

        @Override
        Linkout generate(Organism organism, Gene gene) {
            if (gene == null) {
                return null;
            }

            if (gene.getNamingSource() == null) {
                return null;
            }

            if (SOURCE_TAIR_ID.equalsIgnoreCase(gene.getNamingSource().getName())) {
                String url = String.format(URL_TEMPLATE, gene.getSymbol());
                return new Linkout(DISPLAY_NAME, url);
            }

            return null;
        }
    };

    /*
     * TAIR link via entrez gene name, because sometimes we don't have the TAIR field
     * populated, but the entrez gene name is of the form AT{other chars}. check
     * for this pattern
     */

    Generator tairFromEntrez = new Generator() {

        static final String DISPLAY_NAME = "TAIR";
        static final String URL_TEMPLATE = "http://arabidopsis.org/servlets/TairObject?type=locus&name=%s";

        @Override
        Linkout generate(Organism organism, Gene gene) {
            if (gene == null) {
                return null;
            }

            if (gene.getNamingSource() == null) {
                return null;
            }

            if (SOURCE_ENTREZ_GENE_NAME.equalsIgnoreCase(gene.getNamingSource().getName())) {
                String symbol = gene.getSymbol();
                if (!symbol.startsWith("AT")) {
                    return null;
                }
                String url = String.format(URL_TEMPLATE, symbol);
                return new Linkout(DISPLAY_NAME, url);
            }

            if (SOURCE_GENE_NAME.equalsIgnoreCase(gene.getNamingSource().getName())) {
                String symbol = gene.getSymbol();
                if (!symbol.startsWith("AT")) {
                    return null;
                }
                String url = String.format(URL_TEMPLATE, symbol);
                return new Linkout(DISPLAY_NAME, url);
            }

            return null;
        }
    };


    /*
     * TAIR generator that combines the regular TAIR generator,
     * then falls back to the tairFromEntrez
     */
    Generator tairOrTairFromEntrez = new Generator() {

        @Override
        Linkout generate(Organism organism, Node node) {
            Linkout linkout = tair.generate(organism, node);
            if (linkout == null) {
                linkout = tairFromEntrez.generate(organism, node);
            }
            return linkout;
        }
    };



    /*
     * Ensembl linkout generator
     */
    Generator ensembl = new Generator() {

        static final String DISPLAY_NAME = "Ensembl";
        static final String URL_TEMPLATE = "http://www.ensembl.org/%s/geneview?gene=%s";

        @Override
        Linkout generate(Organism organism, Gene gene) {
            if (gene == null) {
                return null;
            }

            if (gene.getNamingSource() == null) {
                return null;
            }

            if (SOURCE_ENSEMBL_GENE_ID.equalsIgnoreCase(gene.getNamingSource().getName())) {
                String organismName = organism.getAlias();
                organismName = organismName.replace(" ", "_");
                String url = String.format(URL_TEMPLATE, organismName, gene.getSymbol());
                return new Linkout(DISPLAY_NAME, url);
            }

            return null;
        }
    };

    // TODO: deal with code dup with above ensembl generator
    Generator ensemblProtists = new Generator() {

        static final String DISPLAY_NAME = "Ensembl";
        static final String URL_TEMPLATE = "http://protists.ensembl.org/%s/geneview?gene=%s";

        @Override
        Linkout generate(Organism organism, Gene gene) {
            if (gene == null) {
                return null;
            }

            if (gene.getNamingSource() == null) {
                return null;
            }

            if (SOURCE_ENSEMBL_GENE_ID.equalsIgnoreCase(gene.getNamingSource().getName())) {
                String organismName = organism.getAlias();
                organismName = organismName.replace(" ", "_");
                String url = String.format(URL_TEMPLATE, organismName, gene.getSymbol());
                return new Linkout(DISPLAY_NAME, url);
            }

            return null;
        }
    };

    /*
     * yeast-specific linkout generator for sgd
     */
    Generator sgd = new Generator() {

        static final String DISPLAY_NAME = "SGD";
        static final String URL_TEMPLATE = "http://www.yeastgenome.org/cgi-bin/locus.fpl?locus=%s";

        @Override
        Linkout generate(Organism organism, Gene gene) {
            if (gene == null) {
                return null;
            }

            if (gene.getNamingSource() == null) {
                return null;
            }

            if (SOURCE_ENSEMBL_GENE_NAME.equalsIgnoreCase(gene.getNamingSource().getName())) {
                String url = String.format(URL_TEMPLATE, gene.getSymbol());
                return new Linkout(DISPLAY_NAME, url);
            }

            if (SOURCE_GENE_NAME.equalsIgnoreCase(gene.getNamingSource().getName())) {
                String url = String.format(URL_TEMPLATE, gene.getSymbol());
                return new Linkout(DISPLAY_NAME, url);
            }

            return null;
        }
    };

    /*
     * plant specific linkout generator for BAR
     */
    Generator bar = new Generator() {

        static final String DISPLAY_NAME = "BAR";
        static final String URL_TEMPLATE = "http://bar.utoronto.ca/efp/cgi-bin/efpWeb.cgi?modeInput=Absolute&ncbi_gi=%s";

        @Override
        Linkout generate(Organism organism, Gene gene) {
            if (gene == null) {
                return null;
            }

            if (gene.getNamingSource() == null) {
                return null;
            }

            if (SOURCE_TAIR_ID.equalsIgnoreCase(gene.getNamingSource().getName())) {
                String url = String.format(URL_TEMPLATE, gene.getSymbol());
                return new Linkout(DISPLAY_NAME, url);
            }

            return null;
        }
    };

    /*
     * fly specific linkout generator
     */
    Generator flybase = new Generator() {

        static final String DISPLAY_NAME = "FlyBase";
        static final String URL_TEMPLATE = "http://flybase.org/reports/%s.html";

        @Override
        Linkout generate(Organism organism, Gene gene) {
            if (gene == null) {
                return null;
            }

            if (gene.getNamingSource() == null) {
                return null;
            }

            if (SOURCE_ENSEMBL_GENE_ID.equalsIgnoreCase(gene.getNamingSource().getName())) {
                String url = String.format(URL_TEMPLATE, gene.getSymbol());
                return new Linkout(DISPLAY_NAME, url);
            }

            return null;
        }
    };

    /*
     * worm specific linkout generator
     */
    Generator wormbase = new Generator() {

        static final String DISPLAY_NAME = "WormBase";
        static final String URL_TEMPLATE = "http://www.wormbase.org/db/gene/gene?class=Gene&name=%s";

        @Override
        Linkout generate(Organism organism, Gene gene) {
            if (gene == null) {
                return null;
            }

            if (gene.getNamingSource() == null) {
                return null;
            }

            if (SOURCE_ENSEMBL_GENE_ID.equalsIgnoreCase(gene.getNamingSource().getName())) {
                String url = String.format(URL_TEMPLATE, gene.getSymbol());
                return new Linkout(DISPLAY_NAME, url);
            }

            return null;
        }
    };

    /*
     * in most cases we want to generate either an entrez (preferred) linkout,
     * or else ensembl when entrez is not possible.
     */
    Generator entrezOrEnsembl = new Generator() {

        @Override
        Linkout generate(Organism organism, Node node) {
            Linkout linkout = entrez.generate(organism, node);
            if (linkout == null) {
                linkout = ensembl.generate(organism, node);
            }
            return linkout;
        }
    };

    Generator entrezOrEnsemblProtists = new Generator() {

        @Override
        Linkout generate(Organism organism, Node node) {
            Linkout linkout = entrez.generate(organism, node);
            if (linkout == null) {
                linkout = ensemblProtists.generate(organism, node);
            }
            return linkout;
        }
    };


    /*
     * container for the display & link parts of
     * a url
     */
    static class Linkout {

        String name;
        String url;

        public Linkout(String name, String url) {
            this.name = name;
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
