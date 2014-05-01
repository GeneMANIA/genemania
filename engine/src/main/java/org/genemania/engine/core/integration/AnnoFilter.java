package org.genemania.engine.core.integration;

import java.util.Collection;

import org.genemania.domain.OntologyCategory;
import org.genemania.domain.Organism;
import org.genemania.engine.Constants;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.config.Config;
import org.genemania.engine.core.data.CategoryIds;
import org.genemania.engine.core.data.GoAnnotations;
import org.genemania.engine.core.data.GoIds;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.engine.matricks.Matrix;
import org.genemania.exception.ApplicationException;

/**
 * build data structures for enrichment analysis from already
 * existing Go-Annotation data structures.
 *
 */
public class AnnoFilter {
    DataCache cache;

    GoAnnotations annos;
    GoIds goIds;
    CategoryIds catIds;

    public AnnoFilter(DataCache cache) {
        this.cache = cache;
    }

    public void filter(Organism organism) throws ApplicationException {
        alloc(organism);
        applyFilter(organism, Constants.ALL_ONTOLOGY);
        save();
    }

    protected void alloc(Organism organism) throws ApplicationException {

        // annotation matrix
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
    protected void applyFilter(Organism organism, String fromAnno) throws ApplicationException {

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

    protected void save() throws ApplicationException {
        cache.putGoAnnotations(annos);
        cache.putGoIds(goIds);
        cache.putCategoryIds(catIds);
    }
}
