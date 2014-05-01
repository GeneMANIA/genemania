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
import java.util.Collection;

import org.apache.log4j.Logger;
import org.genemania.domain.Ontology;
import org.genemania.domain.OntologyCategory;
import org.genemania.domain.Organism;
import org.genemania.engine.Constants;
import org.genemania.engine.config.Config;
import org.genemania.engine.core.data.CategoryIds;
import org.genemania.engine.core.data.GoAnnotations;
import org.genemania.engine.core.data.GoIds;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.engine.matricks.Matrix;
import org.genemania.exception.ApplicationException;
import org.kohsuke.args4j.Option;

/** 
 * use the already existing annotation files to create a filtered
 * set of annotations given a list of id's.
 *
 * can later make this take obo-files directly (would add a dependency on
 * OBO-Edit or perhaps BioJava).
 */
public class AnnotationSlimmer extends AbstractEngineApp {

    private static Logger logger = Logger.getLogger(CacheBuilder.class);
    @Option(name = "-pondir", usage = "processed ontologies dir")
    String pondir;
    @Option(name = "-orgId", usage = "optional organism id, otherwise will process all oganisms")
    private long orgId = -1;
    GoAnnotations annos;
    GoIds goIds;
    CategoryIds catIds;

    public String getPondir() {
        return pondir;
    }

    public void setPondir(String pondir) {
        this.pondir = pondir;
    }

    public long getOrgId() {
        return orgId;
    }

    public void setOrgId(long orgId) {
        this.orgId = orgId;
    }

    public void logParams() {
        logger.info("cache dir: " + getCacheDir());
        logger.info("processed ontologies dir: " + pondir);
    }

    @Override
    public void init() throws Exception {
    	super.init();
    	logParams();
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

        Ontology ontology = organism.getOntology();
        if (ontology == null) {
            logger.warn(String.format("No default ontology specified for %d (%s), skipping", organism.getId(), organism.getName()));
            return;
        }

        logger.info(String.format("processing ontology %d (%s) for organism %d (%s)", ontology.getId(), ontology.getName(), organism.getId(), organism.getName()));

        allocEnrichmentDataStructures(organism);

        loadAnnos(organism, Constants.ALL_ONTOLOGY);
        
        write();
        log();
    }

    protected void allocEnrichmentDataStructures(Organism organism) throws ApplicationException {

        // annotation matrix
        String ontologyName = organism.getOntology().getName();
        annos = new GoAnnotations(organism.getId(), "" + organism.getOntology().getId());

        int numCategories = organism.getOntology().getCategories().size();
        NodeIds nodeIds = cache.getNodeIds(organism.getId());
        int numGenes = nodeIds.getNodeIds().length;

        Matrix data = Config.instance().getMatrixFactory().sparseMatrix(numGenes, numCategories);

        annos.setData(data);

        // id lists, one by names and the other by database ids.
        goIds = new GoIds(organism.getId(), "" + organism.getOntology().getId());
        catIds = new CategoryIds(organism.getId(), organism.getOntology().getId());

        String[] catNames = new String[numCategories];
        long [] catIdList = new long[numCategories];

        Collection<OntologyCategory> categories = organism.getOntology().getCategories();
        int i = 0;
        for (OntologyCategory category: categories) {
            catNames[i] = category.getName();
            catIdList[i] = category.getId();
            i += 1;
        }

        goIds.setGoIds(catNames);
        catIds.setCategoryIds(catIdList);
    }

    /*
     * extract matching anno data to filtered data structure
     */
    protected void loadAnnos(Organism organism, String fromAnno) throws ApplicationException {

        GoAnnotations branchAnnos = cache.getGoAnnotations(organism.getId(), fromAnno);
        GoIds branchIds = cache.getGoIds(organism.getId(), fromAnno);

        String[] categoryNames = goIds.getGoIds();
        for (int i = 0; i < categoryNames.length; i++) {

            String categoryName = categoryNames[i];
            int indexForCat;

            try {
                indexForCat = branchIds.getIndexForId(categoryName);
                copyRec(branchAnnos.getData(), indexForCat, annos.getData(), i);
            }
            catch (ApplicationException e) {
                continue;
            }
        }
    }

    /*
     * todo: move this to matrix subpackage, add size check, maybe invent/use some
     * kind of rowcursor to optimize for sparsity
     */
    protected void copyRec(Matrix from, int fromCol, Matrix to, int toCol) {

        int numRows = from.numRows();

        for (int i = 0; i < numRows; i++) {
            double fromVal = from.get(i, fromCol);
            if (fromVal != 0) {
                to.set(i, toCol, fromVal);
            }
        }
    }

    protected void write() throws ApplicationException {
        cache.putGoAnnotations(annos);
        cache.putGoIds(goIds);
        cache.putCategoryIds(catIds);
    }

    /*
     * debug logging of annotion data
     */
    protected void log() throws ApplicationException {

        double [] annosPerCat = new double[annos.getData().numCols()];
        annos.getData().columnSums(annosPerCat);

        for (int catIndex=0; catIndex<annosPerCat.length; catIndex++) {
            logger.debug(String.format("Category index %d, id %d, name %s, genes annotated %f",
                    catIndex, catIds.getIdForIndex(catIndex), goIds.getIdForIndex(catIndex), annosPerCat[catIndex]));
        }
    }

    /*
     * the processed ontology file should be in the
     * pondir with name ontologyname.txt
     */
    protected String getPonFilename(Organism organism) {
        return pondir + File.separator + organism.getOntology() + ".txt";
    }
    
    public static void main(String[] args) throws Exception {

        AnnotationSlimmer slimmer = new AnnotationSlimmer();
        if (!slimmer.getCommandLineArgs(args)) {
            System.exit(1);
        }

        try {
        	slimmer.init();       	
            slimmer.process();
            slimmer.cleanup();
        }
        catch (Exception e) {
            logger.error("Fatal error", e);
            System.exit(1);
        }
    }
}
