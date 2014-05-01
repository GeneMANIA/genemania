package org.genemania.engine.core.integration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;

import org.genemania.domain.Ontology;
import org.genemania.domain.OntologyCategory;
import org.genemania.domain.Organism;
import org.genemania.engine.Constants;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.config.Config;
import org.genemania.engine.converter.SymbolCache;
import org.genemania.engine.core.data.CategoryIds;
import org.genemania.engine.core.data.DatasetInfo;
import org.genemania.engine.core.data.GoAnnotations;
import org.genemania.engine.core.data.GoIds;
import org.genemania.engine.matricks.Matrix;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.GeneMediator;
import org.genemania.mediator.OntologyMediator;

/*
 * Build engine data structures encoding gene annotations used
 * for GO-based combining methods or enrichment analysis
 *
 * Input is a text file containing two columns (no header),
 * delimited by tabs. Column 1 is a string category identifier,
 * e.g. GO accession, and column 2 is a gene symbol. Any lines
 * starting with a '#' char are ignored as comment.
 *
 * Output is a GoIds object containing the list of categories,
 * and an GoAnnotations object with the actual annotations, these
 * are written to the data set cache files. Also, the DatasetInfo object
 * is updated with the category counts for each branch.
 *
 * A NodeIds and DatasetInfo object should already exist in the engine
 * data set for the given organism.
 *
 * usage:
 *
 *   AssocLoader gen = new AssocLoader(...);
 *   gen.load("filename.txt", "BP");
 *
 *   // output files are in cache, also available from gen object:
 *   GoAnnos annos = gen.getGoAnnos();
 *
 *   // load some more stuff
 *   gen.load("anotherfile.txt", "MF");
 *
 */
public class AssocLoader {

    // TODO: make configurable
    private static int DEFAULT_TERM_COL = 0;
    private static int DEFAULT_GENE_COL = 1;
    private static String DEFAULT_SEP = "\t";

    Organism organism;
    GeneMediator geneMediator;
    OntologyMediator ontologyMediator;
    DataCache cache;
    SymbolCache symbolCache;

    GoIds goIds;
    CategoryIds catIds; // only needed for when id source is db
    GoAnnotations goAnnos;

    public AssocLoader (Organism organism, GeneMediator geneMediator, OntologyMediator
            ontologyMediator, DataCache cache)
            throws ApplicationException {

        this.cache = cache;
        this.organism = organism;
        this.ontologyMediator = ontologyMediator;
        this.symbolCache = new SymbolCache(organism, geneMediator, cache);
    }

    /*
     * create engine data structures used for GO based weigthing measures.
     *
     */
    public void loadGoBranchAnnos(String annoFile, String goBranch)
            throws IOException, ApplicationException {

        loadTermsFromFile(annoFile, goBranch);
        loadAssocs(annoFile, goBranch);
        cache.putGoIds(goIds);
        cache.putGoAnnotations(goAnnos);

        int goBranchNum = Constants.getIndexForGoBranch(goBranch);
        DatasetInfo info = cache.getDatasetInfo(organism.getId());
        int numCategories = goIds.getGoIds().length;
        info.getNumCategories()[goBranchNum] = numCategories;
        cache.putDatasetInfo(info);
    }

    public void loadEnrichmentAnnos(String annoFile, long ontologyId)
            throws IOException, ApplicationException, DataStoreException {

        Ontology ontology = ontologyMediator.getOntology(ontologyId);
        loadTermsFromDb(ontology);

        loadAssocs(annoFile, "" + ontologyId);
        cache.putGoIds(goIds);
        cache.putGoAnnotations(goAnnos);
        cache.putCategoryIds(catIds);
    }

    private void loadTermsFromDb(Ontology ontology) {
        // annotation matrix


        int numCategories = ontology.getCategories().size();

        // id lists, one by names and the other by database ids.
        goIds = new GoIds(organism.getId(), "" + ontology.getId());
        catIds = new CategoryIds(organism.getId(), ontology.getId());

        String[] catNames = new String[numCategories];
        long [] catIdList = new long[numCategories];

        Collection<OntologyCategory> categories = ontology.getCategories();
        int i = 0;
        for (OntologyCategory category: categories) {
            catNames[i] = category.getName();
            catIdList[i] = category.getId();
            i += 1;
        }

        goIds.setGoIds(catNames);
        catIds.setCategoryIds(catIdList);
    }

    public void loadTermsFromFile(String annoFile, String goBranch) throws IOException {
        Scanner scanner = new Scanner(new File(annoFile));
        HashSet<String> terms = new HashSet<String>();

        try {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                // ignore comment lines
                if (line.startsWith("#")) {
                    continue;
                }

                String[] tokens = line.split("\t");
                String term = tokens[DEFAULT_TERM_COL];
                terms.add(term);
            }
        }
        finally {
            scanner.close();
        }

        goIds = new GoIds(organism.getId(), goBranch);
        String[] ids = terms.toArray(new String[0]);
        goIds.setGoIds(ids);
    }

    /*
     * load the gene-term associations from a tab delimited text file, once the
     * term ids structures already exist
     *
     * name can be a go branch name "BP", or an id like "1" corresponding to
     * an ontology in the db.
     *
     * The GoIds object for this name should already exist, along with a NodeIds
     * object. Any assocs not matching an existing Term (in GoIds)
     * or node (in NodeIds) are dropped.
     *
     */
    public void loadAssocs(String annoFile, String name) throws ApplicationException, IOException {
        Scanner scanner = new Scanner(new File(annoFile));

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

                String[] tokens = line.split(DEFAULT_SEP);
                String term = tokens[DEFAULT_TERM_COL];
                String gene = tokens[DEFAULT_GENE_COL];

                Integer geneIndex = symbolCache.getIndexForSymbol(gene);

                if (geneIndex == null) {
                    continue;
                }

                // any terms in the file but not already specified in goIds
                // are ignored
                int termIndex = -1;
                try {
                    termIndex = goIds.getIndexForId(term);
                }
                catch (ApplicationException e) {
                    // TODO: log, or count or something?
                }

                if (termIndex >= 0) {
                    annoData.set(geneIndex, termIndex, 1);
                }
            }
        }
        finally {
            scanner.close();
        }

        goAnnos = new GoAnnotations(organism.getId(), name);
        goAnnos.setData(annoData);
    }

    public GoIds getGoIds() {
        return goIds;
    }

    public GoAnnotations getGoAnnos() {
        return goAnnos;
    }
}
