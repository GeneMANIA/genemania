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

package org.genemania.engine.apps;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.genemania.domain.Organism;
import org.genemania.engine.Constants;
import org.genemania.engine.config.Config;
import org.genemania.engine.converter.SymbolCache;
import org.genemania.engine.core.data.GoAnnotations;
import org.genemania.engine.core.data.GoIds;
import org.genemania.engine.matricks.Matrix;
import org.genemania.exception.ApplicationException;
import org.kohsuke.args4j.Option;

/**
 * build cache versions of the full
 * annotation data structures. targetmatricesgenerator{2}
 * does something similar, but from data files that
 * are both more specific and more expensive to build.
 *
 * this code is intended to supercede that previous version
 */
public class AnnotationCacheBuilder extends AbstractEngineApp {

    private static Logger logger = Logger.getLogger(AnnotationCacheBuilder.class);
    @Option(name = "-annodir", usage = "annotation data dir")
    String annoDir;
    @Option(name = "-orgId", usage = "optional organism id, otherwise will process all oganisms")
    private long orgId = -1;

    private static int TERM_COL = 2;
    private static int GENE_SYMBOL_COL = 6;

    public String getAnnoDir() {
        return annoDir;
    }

    public void setAnnoDir(String annoDir) {
        this.annoDir = annoDir;
    }

    public long getOrgId() {
        return orgId;
    }

    public void setOrgId(long orgId) {
        this.orgId = orgId;
    }

    public void logParams() {
        logger.info("cache dir: " + getCacheDir());
        logger.info("annotations dir: " + annoDir);
    }

    @Override
    public void process() throws Exception {
        if (orgId != -1) {
            throw new ApplicationException("single organism processing not implemented");
        }
    	
        for (Organism organism: organismMediator.getAllOrganisms()) {
            processOrganism(organism);
        }
    }

    public void processOrganism(Organism organism) throws Exception {
        logger.info(String.format("processing organism %d (%s)", organism.getId(), organism.getName()));

        String annoFile =getAnnoFilename(organism.getId());
        GoIds goIds = loadTerms(organism, annoFile);
        cache.putGoIds(goIds);

        GoAnnotations goAnnos = loadAnnos(organism, annoFile, goIds);
        cache.putGoAnnotations(goAnnos);
    }

    String getAnnoFilename(long organismId) {
        return String.format("%s%s%d.annos.txt", annoDir, File.separator, organismId);
    }

    GoIds loadTerms(Organism organism, String annoFile) throws IOException {
        HashSet<String> uniqueTerms = new HashSet<String>();
        Scanner scanner = new Scanner(new File(annoFile));

        try {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                // ignore comment lines
                if (line.startsWith("#")) {
                    continue;
                }

                // TODO: header line?

                String[] tokens = line.split("\t");
                String termName = tokens[TERM_COL];

                // ignore the top level term "all"
                if ("all".equalsIgnoreCase(termName)) {
                    continue;
                }
                
                uniqueTerms.add(termName);

            }
        }
        finally {
            scanner.close();
        }

        logger.info("total number of unique terms extracted: " + uniqueTerms.size());
        
        GoIds goIds = new GoIds(organism.getId(), Constants.ALL_ONTOLOGY);
        String [] goIdList = uniqueTerms.toArray(new String[] {});
        goIds.setGoIds(goIdList);
        return goIds;
    }


    GoAnnotations loadAnnos(Organism organism, String annoFile, GoIds goIds) throws IOException, ApplicationException {
        Scanner scanner = new Scanner(new File(annoFile));

        SymbolCache symbolCache = new SymbolCache(organism, getGeneMediator(), getCache());

        int numCats = goIds.getGoIds().length;
        int numNodes = cache.getNodeIds(organism.getId()).getNodeIds().length;

        Matrix annoData = Config.instance().getMatrixFactory().sparseMatrix(numNodes, numCats);

        try {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                // ignore comment lines
                if (line.startsWith("#")) {
                    continue;
                }

                // TODO: header line?

                String[] tokens = line.split("\t");
                String termName = tokens[TERM_COL];
                String geneSymbol = tokens[GENE_SYMBOL_COL];

                // ignore top level term "all"
                if ("all".equalsIgnoreCase(termName)) {
                    continue;
                }

                Integer geneIndex = symbolCache.getIndexForSymbol(geneSymbol);

                if (geneIndex == null) {
                    continue;
                }

                int termIndex = goIds.getIndexForId(termName);

                annoData.set(geneIndex, termIndex, 1);
            }
        }
        finally {
            scanner.close();
        }

        GoAnnotations goAnnos = new GoAnnotations(organism.getId(), Constants.ALL_ONTOLOGY);
        goAnnos.setData(annoData);
        return goAnnos;
    }

    @Override
    public void init() throws Exception {
    	super.init();
    	logParams();
    }

    public static void main(String[] args) throws Exception {

        AnnotationCacheBuilder acb = new AnnotationCacheBuilder();

        if (!acb.getCommandLineArgs(args)) {
            System.exit(1);
        }

        acb.setupLogging();

        try {
        	acb.init();
            acb.process();
            acb.cleanup();
        }
        catch (Exception e) {
            logger.error("Fatal error", e);
            System.exit(1);
        }
    }
}
