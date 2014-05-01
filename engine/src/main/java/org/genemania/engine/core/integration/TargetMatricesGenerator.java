package org.genemania.engine.core.integration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;

import org.genemania.domain.Organism;
import org.genemania.engine.Constants;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.config.Config;
import org.genemania.engine.converter.SymbolCache;
import org.genemania.engine.core.data.DatasetInfo;
import org.genemania.engine.core.data.GoAnnotations;
import org.genemania.engine.core.data.GoIds;
import org.genemania.engine.matricks.Matrix;
import org.genemania.exception.ApplicationException;
import org.genemania.mediator.GeneMediator;

/*
 * Build engine data structures encoding gene annotations used
 * for GO-based combining methods.
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
 *   TargetMatricesGenerator gen = new TargetMatricesGenerator(...);
 *   gen.load("filename.txt", "BP");
 *
 *   // output files are in cache, also available from gen object:
 *   GoAnnos annos = gen.getGoAnnos();
 *
 *   // load some more stuff
 *   gen.load("anotherfile.txt", "MF");
 *
 */
public class TargetMatricesGenerator {
    Organism organism;
    GeneMediator geneMediator;
    DataCache cache;
    SymbolCache symbolCache;

    GoIds goIds;
    GoAnnotations goAnnos;

    private static int TERM_COL = 0;
    private static int GENE_COL = 1;

    public TargetMatricesGenerator (Organism organism, GeneMediator geneMediator, DataCache cache)
            throws ApplicationException {
        this.cache = cache;
        this.organism = organism;
        this.symbolCache = new SymbolCache(organism, geneMediator, cache);
    }

    public void load(String annoFile, String goBranch) throws IOException, ApplicationException {
        loadTerms(annoFile, goBranch);
        loadAnnos(annoFile, goBranch);
        save(goBranch);
    }

    public void loadTerms(String annoFile, String goBranch) throws IOException {
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
                String term = tokens[TERM_COL];
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

    public void loadAnnos(String annoFile, String goBranch) throws ApplicationException, IOException {
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

                String[] tokens = line.split("\t");
                String term = tokens[TERM_COL];
                String gene = tokens[GENE_COL];

                Integer geneIndex = symbolCache.getIndexForSymbol(gene);

                if (geneIndex == null) {
                    continue;
                }

                int termIndex = goIds.getIndexForId(term);

                annoData.set(geneIndex, termIndex, 1);
            }
        }
        finally {
            scanner.close();
        }

        goAnnos = new GoAnnotations(organism.getId(), goBranch);
        goAnnos.setData(annoData);
    }

    private void save(String goBranch) throws ApplicationException {
        cache.putGoIds(goIds);
        cache.putGoAnnotations(goAnnos);
        DatasetInfo info = cache.getDatasetInfo(organism.getId());

        // update category count
        int numCategories = goIds.getGoIds().length;
        int goBranchNum = Constants.getIndexForGoBranch(goBranch);
        info.getNumCategories()[goBranchNum] = numCategories;
        cache.putDatasetInfo(info);
    }

    public GoIds getGoIds() {
        return goIds;
    }

    public GoAnnotations getGoAnnos() {
        return goAnnos;
    }
}
