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

package org.genemania.engine.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.VectorEntry;
import no.uib.cipr.matrix.sparse.CompColMatrix;
import no.uib.cipr.matrix.sparse.FlexCompColMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;

import org.apache.log4j.Logger;
import org.genemania.engine.core.evaluation.correlation.PearsonRow;

/**
 * various utility functions not already implemented
 * in the underlying matrix package
 * 
 * TODO: naming consistency of methods (eg be consistent on use of plurals etc)
 */
public class MatrixUtils {

    private static Logger logger = Logger.getLogger(MatrixUtils.class);
    
    // use delta as bound around for numeric comparisons when needed,
    // since we've dropped to using floats in our network representation 
    // instead of doubles, we choose a bound larger than float epsilon 
    // of ~6e-8
    private static double DELTA = Math.pow(2, -20);

    public static Vector columnSums(Matrix m) {
        Vector sums = new DenseVector(m.numColumns());
        for (MatrixEntry e: m) {
            sums.set(e.column(), e.get() + sums.get(e.column()));
        }

        return sums;
    }

    public static void columnSums(Matrix m, double [] sums) {
        for (MatrixEntry e: m) {
            sums[e.column()] = e.get() + sums[e.column()];
        }
    }

    public static Vector columnSumsIgnoreMissingData(Matrix m) {
        Vector sums = new DenseVector(m.numColumns());
        for (MatrixEntry e: m) {
            if (!Double.isNaN(e.get())) {
                sums.set(e.column(), e.get() + sums.get(e.column()));
            }
        }

        return sums;
    }

    public static Vector rowSumsIgnoreMissingData(PearsonRow[] values) {
        Vector sums = new DenseVector(values.length);

        for (int i = 0; i < values.length; i++) {
            PearsonRow r = values[i];
            double total = 0;

            for (int j = 0; j < r.getNumberOfElements(); j++) {
                double value = r.getValueAt(j);
                if (!Double.isNaN(value)) {
                    total += value;
                }
            }
            sums.set(i, total);
        }

        return sums;
    }

    public static Vector rowSums(Matrix m) {
        Vector sums = new DenseVector(m.numRows());
        for (MatrixEntry e: m) {
            sums.set(e.row(), e.get() + sums.get(e.row()));
        }

        return sums;
    }

    public static void rowSums(Matrix m, double [] result) {
        for (MatrixEntry e: m) {
            result[e.row()] =  e.get() + result[(e.row())];
        }        
    }

    public static Vector rowSumsIgnoreMissingData(Matrix m) {
        Vector sums = new DenseVector(m.numRows());
        for (MatrixEntry e: m) {
            if (!Double.isNaN(e.get())) {
                sums.set(e.row(), e.get() + sums.get(e.row()));
            }
        }

        return sums;
    }

    public static Vector columnMean(Matrix m) {

        int n = m.numRows();
        Vector means = columnSums(m);
        means = means.scale(1d / n);

        // apply correction
        Vector corrections = new DenseVector(m.numColumns());
        for (MatrixEntry e: m) {
            corrections.set(e.column(), corrections.get(e.column()) + e.get() - means.get(e.column()));
        }

        corrections = corrections.scale(1d / n);
        means = means.add(corrections);

        return means;

    }

    /**
     * return number of entries in each column which are not NaN
     *
     * @param m
     * @return
     */
    public static Vector columnCountsIgnoreMissingData(Matrix m) {
        Vector counts = new DenseVector(m.numColumns());
        for (MatrixEntry e: m) {
            if (!Double.isNaN(e.get())) {
                counts.set(e.column(), counts.get(e.column()) + 1);
            }
        }
        return counts;
    }

    /**
     * return number of entries in each column which are not NaN
     *
     * @param m
     * @return
     */
    public static Vector rowCountsIgnoreMissingData(List<Vector> values) {
        Vector counts = new DenseVector(values.size());

        for (int i = 0; i < values.size(); i++) {
            Vector v = values.get(i);
            double numNonZeroes = 0;

            for (int j = 0; j < v.size(); j++) {
                if (!Double.isNaN(v.get(j))) {
                    numNonZeroes += 1;
                }
            }
            counts.set(i, numNonZeroes);
        }
        return counts;
    }

    public static Vector rowCountsIgnoreMissingData(Matrix m) {
        Vector counts = new DenseVector(m.numRows());
        for (MatrixEntry e: m) {
            if (!Double.isNaN(e.get())) {
                counts.set(e.row(), counts.get(e.row()) + 1);
            }
        }
        return counts;
    }

    public static Vector columnMeanIgnoreMissingData(Matrix m, Vector counts) {

        Vector means = columnSumsIgnoreMissingData(m);
        means = elementDiv(means, counts);

        // apply correction
        Vector corrections = new DenseVector(m.numColumns());
        for (MatrixEntry e: m) {
            if (!Double.isNaN(e.get())) {
                corrections.set(e.column(), corrections.get(e.column()) + e.get() - means.get(e.column()));
            }
        }

        corrections = elementDiv(corrections, counts);
        means = means.add(corrections);

        return means;
    }

    public static Vector simpleColumnMeanIgnoreMissingData(Matrix m, Vector counts) {

        Vector means = columnSumsIgnoreMissingData(m);
        means = elementDiv(means, counts);

        return means;

    }

    public static Vector rowMeanIgnoreMissingDataPearsonRow(PearsonRow[] values, Vector counts) {

        Vector means = rowSumsIgnoreMissingData(values);
        means = elementDiv(means, counts);

        // apply correction
        Vector corrections = new DenseVector(values.length);
        for (int i = 0; i < values.length; i++) {
            PearsonRow r = values[i];
            double correction = 0;
            double mean = means.get(i);

            for (int j = 0; j < r.getNumberOfElements(); j++) {
                double value = r.getValueAt(j);
                if (!Double.isNaN(value)) {
                    correction += value - mean;
                }
            }
            corrections.add(i, correction);
        }

        corrections = elementDiv(corrections, counts);
        means = means.add(corrections);

        return means;
    }

    public static Vector rowMeanIgnoreMissingData(Matrix m) {

        Vector counts = rowCountsIgnoreMissingData(m);
        recip(counts);
        Vector means = rowSumsIgnoreMissingData(m);

        means = elementMult(means, counts);

        // apply correction
        Vector corrections = new DenseVector(m.numRows());
        for (MatrixEntry e: m) {
            if (!Double.isNaN(e.get())) {
                corrections.set(e.row(), corrections.get(e.row()) + e.get() - means.get(e.row()));
            }
        }

        corrections = elementMult(corrections, counts);
        means = means.add(corrections);

        return means;

    }

    public static Vector columnVariance(Matrix m, Vector means) {
        Vector variances = new DenseVector(m.numColumns());
        Vector corrections = new DenseVector(m.numColumns());

        for (MatrixEntry e: m) {
            double d = e.get() - means.get(e.column());

            variances.set(e.column(), variances.get(e.column()) + d * d);
            corrections.set(e.column(), corrections.get(e.column()) + d);
        }

        MatrixUtils.elementMult(corrections, corrections);
        corrections.scale(1d / m.numRows());

        variances.add(-1d, corrections);
        variances.scale(1d / m.numRows());

        return variances;
    }

    public static Vector columnVarianceIgnoreMissingData(Matrix m, Vector means) {
        Vector variances = new DenseVector(m.numColumns());
        Vector corrections = new DenseVector(m.numColumns());

        for (MatrixEntry e: m) {
            if (!Double.isNaN(e.get())) {
                double d = e.get() - means.get(e.column());

                variances.set(e.column(), variances.get(e.column()) + d * d);
                corrections.set(e.column(), corrections.get(e.column()) + d);
            }
        }

        MatrixUtils.elementMult(corrections, corrections);

        Vector counts = columnCountsIgnoreMissingData(m);
        recip(counts);

        elementMult(corrections, counts);

        variances.add(-1d, corrections);
        elementMult(variances, counts);

        return variances;
    }

    public static Vector simpleColumnVarianceIgnoreMissingData(Matrix m, Vector means) {
        Vector variances = new DenseVector(m.numColumns());

        for (MatrixEntry e: m) {
            if (!Double.isNaN(e.get())) {
                double d = e.get() - means.get(e.column());

                variances.set(e.column(), variances.get(e.column()) + d * d);
            }
        }

        Vector counts = columnCountsIgnoreMissingData(m);
        recip(counts);

        elementMult(variances, counts);

        return variances;
    }

    public static Vector rowVarianceIgnoreMissingDataPearsonRow(PearsonRow[] values, Vector means, Vector counts) {
        Vector variances = new DenseVector(values.length);
        Vector corrections = new DenseVector(values.length);

        for (int i = 0; i < values.length; i++) {
            PearsonRow r = values[i];
            double mean = means.get(i);
            double variance = 0;
            double correction = 0;

            for (int j = 0; j < r.getNumberOfElements(); j++) {
                double value = r.getValueAt(j);

                if (!Double.isNaN(value)) {
                    double diff = value - mean;

                    variance += diff * diff;
                    correction += diff;
//					corrections.add(i, corrections.get(i) + diff);		
                }
            }
            variances.add(i, variance);
            corrections.add(i, correction);
        }

        MatrixUtils.elementMult(corrections, corrections);

        Vector countCopy = counts.copy();
        recip(countCopy);

        elementMult(corrections, countCopy);

        variances.add(-1d, corrections);        
        elementMult(variances, countCopy);

        return variances;
    }

    /**
     *
     * x = x .* y
     *
     * @param x
     * @param y
     * @return
     */
    public static Vector elementMult(Vector x, Vector y) {
        for (VectorEntry e: x) {
            e.set(e.get() * y.get(e.index()));
        }
        return x;
    }

    public static Vector elementDiv(Vector x, Vector y) {
        for (VectorEntry e: x) {
            e.set(e.get() / y.get(e.index()));
        }
        return x;
    }

    public static Vector recip(Vector x) {
        for (VectorEntry e: x) {
            e.set(1d / e.get());
        }
        return x;  // TODO: since this is in-place, maybe we should not return anything
    }

    /**
     *
     * x = x .* y
     *
     * @param x
     * @param y
     * @return
     */
    public static Matrix elementMult(Matrix x, Matrix y) {
        for (MatrixEntry e: x) {
            e.set(e.get() * y.get(e.row(), e.column()));
        }
        return x;
    }

    /**
     * kinda specialized, but ought to be faster than allocating an
     * intermdiate matrix of abs values, then summing on those.
     *
     * would be better to have a function that takes some kind of
     * operator object as an additional arg.
     *
     * @param m
     * @return
     */
    public static Vector absRowSums(Matrix m) {
        Vector sums = new DenseVector(m.numRows());
        for (MatrixEntry e: m) {
            sums.set(e.row(), e.get() + Math.abs(sums.get(e.row())));
        }

        return sums;
    }

    public static int countMatches(Vector v, double x) {
        int c = 0;

        for (VectorEntry e: v) {
            if (e.get() == x) { // should do abs(e.get()-x) < epsilon instead?
                c += 1;
            }
        }

        return c;
    }

    public static double sum(Matrix m) {
        double sum = 0d;
        for (MatrixEntry e: m) {
            sum += e.get();
        }
        return sum;
    }

    public static double sum(Vector v) {
        double sum = 0d;
        for (VectorEntry e: v) {
            sum += e.get();
        }
        return sum;
    }

    public static void setMatches(Vector v, double from, double to) {
        for (VectorEntry e: v) {
            if (e.get() == from) { // should do abs(e.get()-from) < epsilon instead?
                e.set(to);
            }
        }
    }

    /**
     * return indices of elements in given vector matching target value
     *
     * notice this allocates then throws away an int array of length same as v
     *
     * @param v
     * @param x
     * @return
     */
    public static int[] find(Vector v, double x) {
        int[] indices = new int[v.size()];
        int i = 0;
        for (VectorEntry e: v) {
            if (e.get() == x) { // TODO: worry about epsilon
                indices[i] = e.index();
                i += 1;
            }
        }
        int[] result = new int[i];
        System.arraycopy(indices, 0, result, 0, i);
        return result;
    }

    /**
     * return indices of elements of given vector greater than
     * given target value
     *
     * @param v
     * @param x
     * @return
     */
    public static int[] findGT(Vector v, double x) {
        int[] indices = new int[v.size()];
        int i = 0;
        for (VectorEntry e: v) {
            if (e.get() > x) {
                indices[i] = e.index();
                i += 1;
            }
        }
        int[] result = new int[i];
        System.arraycopy(indices, 0, result, 0, i);
        return result;
    }

    public static int[] findGE(Vector v, double x) {
        int[] indices = new int[v.size()];
        int i = 0;
        for (VectorEntry e: v) {
            if (e.get() >= x) {
                indices[i] = e.index();
                i += 1;
            }
        }
        int[] result = new int[i];
        System.arraycopy(indices, 0, result, 0, i);
        return result;
    }

    public static int[] findLT(Vector v, double x) {
        int[] indices = new int[v.size()];
        int i = 0;
        for (VectorEntry e: v) {
            if (e.get() < x) {
                indices[i] = e.index();
                i += 1;
            }
        }
        int[] result = new int[i];
        System.arraycopy(indices, 0, result, 0, i);
        return result;
    }

    public static int[] subArray(int[] array, int[] indices) {
        int[] result = new int[indices.length];
        for (int i = 0; i < indices.length; i++) {
            result[i] = array[indices[i]];
        }
        return result;
    }

    public static int[] arrayJoin(int[] a, int[] b) {
        int[] result = new int[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    /**
     * So, given that we've extracted the indices of some nodes of interest in
     * a network (eg top scoring nodes from some analysis), return a subnetwork
     * of interactions involving only those nodes. The dimensions of the matrix returned
     * is the same as the original network, just the unwanted values have been
     * removed.
     *
     * This would just be a one-liner
     * in matlab using its nice indexing operations ...
     *
     * TODO: trivial: the code and comments here should have only referred to matrices and
     * elements not networks and weights. fix it.
     *
     * TODO: this makes assumptions of wanting a certain storage layout ... need to
     * factor that assumption out somehow
     *
     * @param combinedNetwork
     * @param wantedNodeIndices
     * @return
     */
    public static Matrix getSubMatrix(Matrix network, int[] wantedRowIndices, int[] wantedColumnIndices) {
        FlexCompColMatrix wanted = new FlexCompColMatrix(network.numRows(), network.numColumns());

        // hmm, it could be faster to do this, when network is very sparse, by looping
        // over the elements of network and checking for desired indices ... but anyhow
        // we loop over the desired indices and examine the network. Doesn't assume symmetry
        // so all elements are examined.
        for (int ix = 0; ix < wantedRowIndices.length; ix++) {
            int x = wantedRowIndices[ix];
            for (int iy = 0; iy < wantedColumnIndices.length; iy++) {
                int y = wantedColumnIndices[iy];

                double weight = network.get(x, y);
                if (weight != 0d) {
                    wanted.add(x, y, weight);
                }
            }
        }

        // TODO: there may be no benefit for the conversion to CCS here,
        // consider just returning the flex version.
        return new CompColMatrix(wanted);
    }

    public static void setDiagonalZero(Matrix m) {
        int x = Math.min(m.numRows(), m.numColumns());
        for (int i = 0; i < x; i++) {
            if (m.get(i, i) != 0) {  // the test here is to make this safe for sparse
                // matrices, which give an error when setting an
                // element not in the compressed representation
                m.set(i, i, 0);
            }
        }
    }

    /**
     * return sum(sum(a .* b))
     *
     * @param a
     * @param b
     * @return
     */
    public static double elementMultiplySum(Matrix a, Matrix b) {
        // assume a, b are sparse, this at least iterates only over the
        // non-zero elements of a,
        double sum = 0;
        for (MatrixEntry e: a) {

            sum += e.get() * b.get(e.row(), e.column());
        }
        return sum;
    }

    /**
     * return array with all occurances of the value x removed
     * @param a
     * @param x
     * @return
     */
    public static int[] filter(int[] a, int x) {
        int[] intermediate = new int[a.length];

        int j = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i] != x) {
                intermediate[j] = a[i];
                j++;
            }
        }
        int result[] = new int[j];
        System.arraycopy(intermediate, 0, result, 0, j);
        return result;
    }

    public static void sqrt(Vector v) {
        for (VectorEntry e: v) {
            e.set(Math.sqrt(e.get()));
        }
    }

    public static void sqrt(Matrix m) {
        for (MatrixEntry e: m) {
            e.set(Math.sqrt(e.get()));
        }
    }

    public static void log(Matrix m) {
        for (int i = 0; i < m.numRows(); i++) { // can't use MatrixEntry since 0's in sparse matrices would be silently ignored, prefer an error
            for (int j = 0; j < m.numColumns(); j++) {
                m.set(i, j, Math.log(m.get(i, j)));
            }      
        }        
    }
    
    public static void log(Vector v) {
        for (int i = 0; i < v.size(); i++) { // can't use VectorEntry since 0's in sparse matrices would be silently ignored, prefer an error
            v.set(i, Math.log(v.get(i)));
        }
    }

    public static void add(Vector v, final double x) {
        for (int i = 0; i < v.size(); i++) { // error for sparse
            v.set(i, v.get(i) + x);
        }
    }

    public static void add(Matrix m, final double x) {
        for (int i = 0; i < m.numRows(); i++) { // error for sparse
            for (int j = 0; j < m.numColumns(); j++) {
                m.set(i, j, m.get(i, j) + x);
            }
        }
    }

    public static Vector findAllNoneZero(Vector v) {

        int i = 0;
        for (int j = 0; j < v.size(); j++) {
            if (v.get(j) != 0) {
                i++;
            }

        }
//		System.out.println("i = "+ i);
        Vector temp = new DenseVector(i);
        i = 0;
        for (int j = 0; j < v.size(); j++) {
            if (v.get(j) != 0) {
                temp.set(i, j);
                i++;
            }

        }

        return temp;
    }

    /**
     * Given a map, return a map with iteration order of the values with
     * the given comparator applied.
     *
     * @param <K>
     * @param <V>
     * @param unsorted
     * @param comparator
     * @return
     */
    public static <K, V> Map<K, V> makeValueSortedMap(Map<K, V> unsorted, final Comparator<V> comparator) {

        Set<Map.Entry<K, V>> entrySet = unsorted.entrySet();
        ArrayList<Map.Entry<K, V>> list = new ArrayList<Map.Entry<K, V>>(entrySet);
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {

            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                V v1 = o1.getValue();
                V v2 = o2.getValue();

                return comparator.compare(v1, v2);
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> e: list) {
            result.put(e.getKey(), e.getValue());
        }

        return result;
    }

    /**
     * @param scores
     * @param indicesForPositiveNodes
     * @param limitResults
     * @param remove all discriminant scores <= threshold
     * @return
     */
    public static int[] getIndicesForTopScores(Vector scores, List<Integer> indicesForPositiveNodes, int limitResults, double threshold) {

        /**
         * inner class holds both score and node index, and implements
         * compareTo by comparing score in descending order. Use this to sort values.
         *
         */
        class IndexedScore implements Comparable {

            int index;
            double score;

            public int compareTo(Object o) {
                IndexedScore oo = (IndexedScore) o;
                return -Double.compare(this.score, oo.score); // the minus sign is for descending order
            }
        }

        // build up the indexed score objects and sort
        java.util.Vector<IndexedScore> indexedScores = new java.util.Vector<IndexedScore>();

        for (VectorEntry e: scores) {
            int i = e.index();
            double score = e.get();
            IndexedScore is = new IndexedScore();
            is.index = i;
            is.score = score;
            indexedScores.add(is);
        }

        Collections.sort(indexedScores);

        // extract the top limitresult scores not including
        // the positive nodes, adding those in as well, in order.
        //
        // we create a set containing the positive nodes,
        // and remove from the set as they are encountered
        HashSet<Integer> positiveSet = new HashSet<Integer>();
        positiveSet.addAll(indicesForPositiveNodes);
        int numQueryNodes = positiveSet.size();

        int totalResults = limitResults + positiveSet.size();
        // TODO: what about totalResults = Math.min(totalResults, scores.size());

        int[] result = new int[totalResults]; // this may be an overallocation of space

        int i = 0; // index into sorted scores
        int j = 0; // index into results
        int numQueryNodesAdded = 0;
        int numNonQueryNodesAdded = 0;

        while (i < scores.size() && (numQueryNodesAdded < numQueryNodes || numNonQueryNodesAdded < limitResults)) {
            IndexedScore is = indexedScores.get(i);

            // query genes
            if (positiveSet.contains(is.index)) {
                    positiveSet.remove(is.index);
                    numQueryNodesAdded += 1;
                    result[j] = is.index;
                    j += 1;
            }
            // non-query genes
            else if (numNonQueryNodesAdded < limitResults && is.score > (threshold + DELTA)) {
                    numNonQueryNodesAdded += 1;
                    result[j] = is.index;
                    j += 1;
            }

            i += 1;
        }

        // resize
         if (j < totalResults) {
            int[] tmp = new int[j];
            System.arraycopy(result, 0, tmp, 0, j);
            result = tmp;
        }

        return result;
    }

    static class IndexedScore implements Comparable {

        int index;
        double score;

        public int compareTo(Object o) {
            IndexedScore oo = (IndexedScore) o;
            return -Double.compare(this.score, oo.score); // the minus sign is for descending order
        }
    }

    /**
     * TODO: add asc/desc flag
     *
     * @param v
     * @return
     */
    public static int[] getIndicesForSortedValues(Vector v) {
        /**
         * inner class holds both score and node index, and implements
         * compareTo by comparing score in descending order. Use this to sort values.
         *
         */
        // build up the indexed score objects and sort
        java.util.Vector<IndexedScore> indexedScores = new java.util.Vector<IndexedScore>();

        for (VectorEntry e: v) {
            int i = e.index();
            double score = e.get();
            IndexedScore is = new IndexedScore();
            is.index = i;
            is.score = score;
            indexedScores.add(is);
        }

        Collections.sort(indexedScores);

        int[] result = new int[indexedScores.size()];
        // extract the first limitResult scores into the desired Map structure
        for (int i = 0; i < indexedScores.size(); i++) {
            IndexedScore is = indexedScores.get(i);
            result[i] = is.index;
        }

        return result;

    }

    public static Vector extractColumnToVector(Matrix m, int columnIndex) {
        Vector result = new DenseVector(m.numRows());
        for (int i = 0; i < m.numRows(); i++) {
            result.set(i, m.get(i, columnIndex));
        }

        return result;
    }

    public static Vector extractRowToVector(Matrix m, int rowIndex) {
        Vector result = new DenseVector(m.numColumns());
        for (int i = 0; i < m.numColumns(); i++) {
            result.set(i, m.get(rowIndex, i));
        }

        return result;
    }

    /**
     * a <- max(a,a')
     *
     * This does the computation in place. TODO: is it safe to mutate the
     * matrix being iterated over? it appears to be, at a small penalty
     * of possibly revisiting some elements ... but verify
     *
     * @param a
     * @param b
     */
    public static void setToMaxTranspose(Matrix a) {
        // TODO: bail if not square, or define more precise semantics for non-square case

        for (MatrixEntry e: a) {
            double u = e.get();
            double v = a.get(e.column(), e.row());
            if (u > v) {
                a.set(e.column(), e.row(), u);
            }
            else if (v > u) {
                e.set(v);
            }
        }
    }

    /**
     * return new matrix b = max(a, a'). a is unchanged
     *
     * @param a
     * @return
     */
    public static FlexCompColMatrix computeMaxTranspose(Matrix a) {
        // TODO: bail if not square, or define more precise semantics for non-square case
        FlexCompColMatrix b = new FlexCompColMatrix(a.numRows(), a.numColumns());

        for (MatrixEntry e: a) {
            double u = e.get();
            double v = a.get(e.column(), e.row());
            double max = Math.max(u, v);
            // why was i checking this again?
            //if (max == 0d) {
            //	System.out.println("zero1");
            //}
            b.set(e.row(), e.column(), max);
            b.set(e.column(), e.row(), max);
        }

        return b;
    }

    /**
     * fill in NaNs with given value
     * @param m
     * @param value
     */
    public static void replaceMissingData(Matrix m, double value) {
        for (MatrixEntry e: m) {
            if (Double.isNaN(e.get())) {
                e.set(value);
            }
        }
    }

    /**
     * fill in NaNs with given value
     * @param m
     * @param value
     */
    public static void replaceMissingData(List<Vector> m, double value) {
        for (Vector v: m) {
            for (VectorEntry e: v) {
                if (Double.isNaN(e.get())) {
                    v.set(e.index(), value);
                }
            }
        }
    }

    /**
     * Returns a boolean mask, true for good columns and false for bad. Bad columns are those where
     * more than thresholdPercentage of the rows are NaNs.
     *
     * @param m
     * @param thresholdPercentage
     */
    public static boolean[] checkColumnsforMissingDataThreshold(Matrix m, double thresholdPercentage) {

        int numRows = m.numRows();
        int maxNaNs = (int) (thresholdPercentage * numRows / 100.0);

        Vector counts = columnCountsIgnoreMissingData(m);
        boolean goodCols[] = new boolean[m.numColumns()];

        for (int col = 0; col < counts.size(); col++) {
            if ((numRows - counts.get(col)) > maxNaNs) {
                logger.info("column " + col + " has " + (numRows - counts.get(col) + " zeros, threshold is " + maxNaNs));
                goodCols[col] = false;
            }
            else {
                goodCols[col] = true;
            }
        }

        return goodCols;
    }

    public static double max(Vector v) {
        double m = Double.NEGATIVE_INFINITY;
        for (int i=0; i<v.size(); i++) {
            double d = v.get(i);
            if (d>m) {
                m = d;
            }            
        }
        
        return m;    
    }
    
    public static void setColumns(Matrix m, int col, double value) {
        for (int row = 0; row < m.numRows(); row++) {
            m.set(row, col, value);
        }
    }

    public static void setColumn(Matrix m, int col, Vector values) {
        //TODO: check sizes
        for (int row = 0; row < m.numRows(); row++) {
            m.set(row, col, values.get(row));
        }
    }

    public static void setRow(Matrix m, int row, Vector values) {
        //TODO: check sizes
        for (int col = 0; col < m.numColumns(); col++) {
            m.set(row, col, values.get(col));
        }
    }

    public static void setColumn(Matrix m, int col, double[] values) {
        //TODO: check sizes
        for (int row = 0; row < m.numRows(); row++) {
            m.set(row, col, values[row]);
        }
    }

    public static void setColumn(Matrix m, int col, Matrix from, int fromCol) {
        //TODO: check sizes
        for (int row = 0; row < m.numRows(); row++) {
            m.set(row, col, from.get(row, fromCol));
        }
    }

    public static void maskFalseColumns(Matrix m, boolean[] columnMasks, double value) {
        for (int col = 0; col < columnMasks.length; col++) {
            if (columnMasks[col] == false) {
                setColumns(m, col, value);
            }
        }
    }

    /*
     * Here's a one-pass alg for pearson with supposedly reasonable numeric stability, quoted from:
     *
     * http://groups.google.com/group/sci.stat.math/browse_frm/thread/1ccaf0de9592bd0b/535754e805f703f9?lnk=st&q=group%3Asci.stat.*+author%3Amiller+jennrich&rnum=1#535754e805f703f9
     *
     * ----
     *     n = 0
     *     xbar = 0, ybar = 0
     *     sxx = 0, sxy = 0, syy = 0
     *     repeat
     *       read x, y
     *       n = n + 1
     *       devx = x - xbar, devy = y - ybar
     *       xbar = xbar + devx/n, ybar = ybar + devy/n
     *       sxx = sxx + devx*(x - xbar)
     *       sxy = sxy + devx*(y - ybar)
     *       syy = syy + devy*(y - ybar)
     *     until end of data
     *
     *     The earliest reference to this algorithm which I have found is:
     *     Jennrich, R.I. (1977) `Stepwise regression', pages 58-75 in the book
     *     `Statistical methods for digital computers' edited by Enslein, Ralston &
     *     Wilf, and published by Wiley.
     * ----
     *
     * Ought to double-check. Its updated to filter out any NaN's found in
     * the input data, which adds a branch to the loop.
     *
     */
    public static double pearson(double[] xlist, double[] ylist) {
        double xbar = 0, ybar = 0;
        double sxx = 0, sxy = 0, syy = 0;
        double devx = 0, devy = 0;
        double x, y;

        int size = Math.min(xlist.length, ylist.length);

        int n = 0;
        for (int i = 0; i < size; i++) {
            x = xlist[i];
            y = ylist[i];

//			if (x!=x || y!=y) { // faster NaN test ... apparently not. probably the branch that is killing performance
//				continue;				
//			}

            if (Double.isNaN(x) || Double.isNaN(y)) {
                continue;
            }

            n += 1;

            devx = x - xbar;
            devy = y - ybar;
            xbar = xbar + devx / n;
            ybar = ybar + devy / n;

            sxx = sxx + devx * (x - xbar);
            sxy = sxy + devx * (y - ybar);
            syy = syy + devy * (y - ybar);
        }

        return sxy / Math.sqrt(sxx * syy);
    }

    /**
     * skip the NaN test
     *
     * @param xlist
     * @param ylist
     * @return
     */
    public static double pearsonCleanData(double[] xlist, double[] ylist) {
        double xbar = 0, ybar = 0;
        double sxx = 0, sxy = 0, syy = 0;
        double devx = 0, devy = 0;
        double x, y;

        int size = Math.min(xlist.length, ylist.length);

        int n = 0;
        for (int i = 0; i < size; i++) {
            x = xlist[i];
            y = ylist[i];

            n += 1;

            devx = x - xbar;
            devy = y - ybar;
            xbar = xbar + devx / n;
            ybar = ybar + devy / n;

            sxx = sxx + devx * (x - xbar);
            sxy = sxy + devx * (y - ybar);
            syy = syy + devy * (y - ybar);
        }

        return sxy / Math.sqrt(sxx * syy);
    }

    public static double dotprod(double[] xlist, double[] ylist) {

        double result = 0.0;
        int size = Math.min(xlist.length, ylist.length);

        int n = 0;
        for (int i = 0; i < size; i++) {
            result += xlist[i] * ylist[i];
        }

        return result;
    }

    public static int[] permutation(int n) {
        return permutation(n, null);
    }
    
    /**
     * return a permutation array, a randomly shuffled array
     * of the index values 0 ... n-1
     *
     * @param n
     * @return
     */
    public static int[] permutation(int n, Random random) {
        // isn't there a simple lib function to random shuffle
        // a primitive array?

        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < n; i++) {
            list.add(i);
        }

        if (random != null) {
            Collections.shuffle(list, random);
        }
        else {
            Collections.shuffle(list);
        }
        
        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            result[i] = list.get(i);
        }

        return result;
    }

    /**
     * Replaces the vector with its rank value. The starting rank is 1.
     * The method first sorts the expression levels in O(nlogn), then assign the ranks in O(n).
     * Uses merge sort to sort the values. The merge sort takes in two arrays - one storing the values,
     * and one storing the indices. The sort is performed on the array with values, and the indices
     * are updated accordingly.
     * The average is used when there are tied ranks. E.g. If two values both rank #2, 2.5 is used for both values.
     *
     * If vector is a sparseVector, then tiedRank will only rank the specified values and return
     * the ranks in a sparseVector.
     * @param vector The vector of values to be ranked
     */
    public static void tiedRank(Vector vector) {
        int valueSize;
        boolean isSparse = false;
        if (vector instanceof DenseVector) {
            valueSize = vector.size();
        }
        else if (vector instanceof SparseVector) {
            valueSize = ((SparseVector) vector).getUsed();
            isSparse = true;
        }
        else {
            throw new RuntimeException("unexpected vector type of " + vector.getClass().getName());
        }

        // get the values into an array
        double[] values = new double[valueSize];

        int k = 0;
        for (VectorEntry e: vector) {
            values[k] = e.get();
            k++;
        }

        tiedRank(values);

        if (isSparse) {
            // sparse vector
            int[] indices = ((SparseVector) vector).getIndex();
            for (int i = 0; i < values.length; i++) {
                vector.set(indices[i], values[i]);
            }
        }
        else {
            // dense vector
            for (int i = 0; i < values.length; i++) {
                vector.set(i, values[i]);
            }
        }
    }

    /**
     * Replace the array with its rank value. The starting rank is 1.
     * The method first sorts the expression levels in O(nlogn) in ascending order, then assign the ranks in O(n).
     * Uses merge sort to sort the values. The merge sort takes in two arrays - one storing the values,
     * and one storing the indices. The sort is performed on the array with values, and the indices
     * are updated accordingly.
     * The average is used when there are tied ranks. E.g. If two values both rank #2, 2.5 is used for both values.
     *
     * @param values The array to be ranked
     */
    public static void tiedRank(double[] values) {
        double[] copy = values.clone();
        boolean hasNaN = false;
        double NaNrank = 0;

        int startRank = 1;

        // get the values into an array
        int[] indices = new int[copy.length];

        for (int i = 1; i < copy.length; i++) {
            indices[i] = i;
        }

        mergeSort(copy, indices);

        // go through the sorted values and assign ranks
        for (int i = 0; i < copy.length;) {
            List<Integer> tieValueIndices = new ArrayList<Integer>();
            double currentValue = copy[i];
            int currentIndex = indices[i];
            i++;
            while (i < copy.length && copy[i] == currentValue) {
                tieValueIndices.add(indices[i]);
                i++;
            }
            // next value is bigger than currentValue; no more ties
            tieValueIndices.add(currentIndex);

            int endRank = startRank + tieValueIndices.size() - 1;
            // calculating the actual rank of these indices. E.g. two ties for rank #1, the actual rank is 1.5 for both indices
            double actualRank = (double) ((startRank + endRank) * (endRank - startRank + 1)) / (2 * tieValueIndices.size());

            for (Integer e: tieValueIndices) {
                if (!hasNaN && Double.isNaN(values[e])) {
                    hasNaN = true;
                    NaNrank = actualRank;
                }
                values[e] = actualRank;
            }

            startRank += tieValueIndices.size();
        }

        for (int i = 0; i < values.length; i++) {
            if (values[i] == NaNrank) {
                values[i] = Double.NaN;
            }
        }
    }

    /**
     * Performs a merge sort on the array a, while keeping track of the indices in array indices.
     * @param a
     * @param indices
     */
    public static void mergeSort(double[] a, int[] indices) {
        double[] tmpArray = new double[a.length];
        int[] tmpIndices = new int[a.length];
        mergeSort(a, tmpArray, indices, tmpIndices, 0, a.length - 1);
    }

    /**
     * Internal method that makes recursive calls.
     * @param a an array of Comparable items.
     * @param tmpArray an array to place the merged result.
     * @param left the left-most index of the subarray.
     * @param right the right-most index of the subarray.
     * 
     * http://www.java-tips.org/java-se-tips/java.lang/merge-sort-implementation-in-java.html
     */
    private static void mergeSort(double[] a, double[] tmpArray, int[] aIndices, int[] tmpIndices,
            int left, int right) {
        if (left < right) {
            int center = (left + right) / 2;
            mergeSort(a, tmpArray, aIndices, tmpIndices, left, center);
            mergeSort(a, tmpArray, aIndices, tmpIndices, center + 1, right);
            merge(a, tmpArray, aIndices, tmpIndices, left, center + 1, right);
        }
    }

    /**
     * Internal method that merges two sorted halves of a subarray.
     * @param a an array of Comparable items.
     * @param tmpArray an array to place the merged result.
     * @param leftPos the left-most index of the subarray.
     * @param rightPos the index of the start of the second half.
     * @param rightEnd the right-most index of the subarray.
     * 
     * http://www.java-tips.org/java-se-tips/java.lang/merge-sort-implementation-in-java.html
     */
    private static void merge(double[] a, double[] tmpArray, int[] aIndices, int[] tmpIndices,
            int leftPos, int rightPos, int rightEnd) {
        int leftEnd = rightPos - 1;
        int tmpPos = leftPos;
        int numElements = rightEnd - leftPos + 1;

        // Main loop
        while (leftPos <= leftEnd && rightPos <= rightEnd) {
            if (a[leftPos] <= a[rightPos]) {
                tmpArray[tmpPos] = a[leftPos];
                tmpIndices[tmpPos++] = aIndices[leftPos++];
            }
            else {
                tmpArray[tmpPos] = a[rightPos];
                tmpIndices[tmpPos++] = aIndices[rightPos++];
            }
        }

        while (leftPos <= leftEnd) {    // Copy rest of first half
            tmpArray[tmpPos] = a[leftPos];
            tmpIndices[tmpPos++] = aIndices[leftPos++];
        }

        while (rightPos <= rightEnd) {  // Copy rest of right half
            tmpArray[tmpPos] = a[rightPos];
            tmpIndices[tmpPos++] = aIndices[rightPos++];
        }

        // Copy tmpArray back
        for (int i = 0; i < numElements; i++, rightEnd--) {
            a[rightEnd] = tmpArray[rightEnd];
            aIndices[rightEnd] = tmpIndices[rightEnd];
        }
    }

    /**
     * Performs a merge sort on the array a, while keeping track of the classes.
     * @param a
     * @param classes
     */
    public static void mergeSort(double[] a, boolean[] classes) {
        double[] tmpArray = new double[a.length];
        boolean[] tmpClasses = new boolean[a.length];
        mergeSort(a, tmpArray, classes, tmpClasses, 0, a.length - 1);
    }

    /**
     * Internal method that makes recursive calls.
     * @param a an array of Comparable items.
     * @param tmpArray an array to place the merged result.
     * @param left the left-most index of the subarray.
     * @param right the right-most index of the subarray.
     *
     * http://www.java-tips.org/java-se-tips/java.lang/merge-sort-implementation-in-java.html
     */
    private static void mergeSort(double[] a, double[] tmpArray, boolean[] aIndices, boolean[] tmpClasses,
            int left, int right) {
        if (left < right) {
            int center = (left + right) / 2;
            mergeSort(a, tmpArray, aIndices, tmpClasses, left, center);
            mergeSort(a, tmpArray, aIndices, tmpClasses, center + 1, right);
            merge(a, tmpArray, aIndices, tmpClasses, left, center + 1, right);
        }
    }

    /**
     * Internal method that merges two sorted halves of a subarray.
     * @param a an array of Comparable items.
     * @param tmpArray an array to place the merged result.
     * @param leftPos the left-most index of the subarray.
     * @param rightPos the index of the start of the second half.
     * @param rightEnd the right-most index of the subarray.
     *
     * http://www.java-tips.org/java-se-tips/java.lang/merge-sort-implementation-in-java.html
     */
    private static void merge(double[] a, double[] tmpArray, boolean[] aClasses, boolean[] tmpClasses,
            int leftPos, int rightPos, int rightEnd) {
        int leftEnd = rightPos - 1;
        int tmpPos = leftPos;
        int numElements = rightEnd - leftPos + 1;

        // Main loop
        while (leftPos <= leftEnd && rightPos <= rightEnd) {
            if (a[leftPos] <= a[rightPos]) {
                tmpArray[tmpPos] = a[leftPos];
                tmpClasses[tmpPos++] = aClasses[leftPos++];
            }
            else {
                tmpArray[tmpPos] = a[rightPos];
                tmpClasses[tmpPos++] = aClasses[rightPos++];
            }
        }

        while (leftPos <= leftEnd) {    // Copy rest of first half
            tmpArray[tmpPos] = a[leftPos];
            tmpClasses[tmpPos++] = aClasses[leftPos++];
        }

        while (rightPos <= rightEnd) {  // Copy rest of right half
            tmpArray[tmpPos] = a[rightPos];
            tmpClasses[tmpPos++] = aClasses[rightPos++];
        }

        // Copy tmpArray back
        for (int i = 0; i < numElements; i++, rightEnd--) {
            a[rightEnd] = tmpArray[rightEnd];
            aClasses[rightEnd] = tmpClasses[rightEnd];
        }
    }

    /**
     * Replaces the vector with its rank value. The starting rank is 1. In case of ties, one of the
     * element has the next rank. I.e. if values = [2 5 5], the ranks are [1 2 3].
     * The method first sorts the expression levels in O(nlogn), then assign the ranks in O(n).
     * Uses merge sort to sort the values. The merge sort takes in two arrays - one storing the values,
     * and one storing the indices. The sort is performed on the array with values, and the indices
     * are updated accordingly.
     *
     * If vector is a sparseVector, then rank will only rank the specified values and return
     * the ranks in a sparseVector.
     * @param vector The vector of values to be ranked
     */
    public static void rank(Vector vector) {
        int valueSize;
        boolean isSparse = false;
        if (vector instanceof DenseVector) {
            valueSize = vector.size();
        }
        else if (vector instanceof SparseVector) {
            valueSize = ((SparseVector) vector).getUsed();
            isSparse = true;
        }
        else {
            throw new RuntimeException("unexpected vector type of " + vector.getClass().getName());
        }

        // get the values into an array
        double[] values = new double[valueSize];

        int k = 0;
        for (VectorEntry e: vector) {
            values[k] = e.get();
            k++;
        }

        rank(values);

        if (isSparse) {
            // sparse vector
            int[] indices = ((SparseVector) vector).getIndex();
            for (int i = 0; i < values.length; i++) {
                vector.set(indices[i], values[i]);
            }
        }
        else {
            // dense vector
            for (int i = 0; i < values.length; i++) {
                vector.set(i, values[i]);
            }
        }
    }

    /**
     * Replace the array with its rank value. The starting rank is 1. In case of ties, one of the
     * element has the next rank. I.e. if values = [2 5 5], the ranks are [1 2 3].
     * The method first sorts the expression levels in O(nlogn), then assign the ranks in O(n).
     * Uses merge sort to sort the values. The merge sort takes in two arrays - one storing the values,
     * and one storing the indices. The sort is performed on the array with values, and the indices
     * are updated accordingly.
     *
     * @param values The array to be ranked
     */
    public static void rank(double[] values) {
        double[] copy = values.clone();
        int startRank = 1;

        // get the values into an array
        int[] indices = new int[copy.length];

        for (int i = 1; i < copy.length; i++) {
            indices[i] = i;
        }

        mergeSort(copy, indices);

        // go through the sorted values and assign ranks
        for (int i = 0; i < copy.length; i++) {
            int currentIndex = indices[i];

            if (!Double.isNaN(values[currentIndex])) {
                values[currentIndex] = startRank;
                startRank++;
            }
        }
    }

    /**
     * given a matrix m of interactions of size n-by-n, return an array
     * of length n where the i'th element is 1 if the corresponding row (col)
     * of m has at least one interaction with another element, and 0 otherwise.
     *
     * Hmm, why did i write this? could just use row-sums or column-sums intead.
     *
     * @param m
     * @return
     * @throws java.lang.Exception
     */
    public static int[] coverage(Matrix m) throws Exception {

        int[] x = new int[m.numRows()];

        for (MatrixEntry e: m) {
            x[e.row()] = 1;
        }

        return x;
    }

    public static void mergeSortIgnoreZero(double[] a, int[] indices) {
        double[] tmpArray = new double[a.length];
        int[] tmpIndices = new int[a.length];
        mergeSortIgnoreZero(a, tmpArray, indices, tmpIndices, 0, a.length - 1);
    }

    public static boolean overlap(Matrix m, int[] c) {
        // temp, for actormania hack
        for (MatrixEntry e: m) {
            if (c[e.row()] == 1 && c[e.column()] == 1) {
                return true;
            }
        }

        return false;
    }

    /**
     * Internal method that makes recursive calls.
     * @param a an array of Comparable items.
     * @param tmpArray an array to place the merged result.
     * @param left the left-most index of the subarray.
     * @param right the right-most index of the subarray.
     * 
     * http://www.java-tips.org/java-se-tips/java.lang/merge-sort-implementation-in-java.html
     */
    private static void mergeSortIgnoreZero(double[] a, double[] tmpArray, int[] aIndices, int[] tmpIndices,
            int left, int right) {
        if (left < right) {
            int center = (left + right) / 2;
            mergeSortIgnoreZero(a, tmpArray, aIndices, tmpIndices, left, center);
            mergeSortIgnoreZero(a, tmpArray, aIndices, tmpIndices, center + 1, right);
            mergeIgnoreZero(a, tmpArray, aIndices, tmpIndices, left, center + 1, right);
        }
    }

    /**
     * Internal method that merges two sorted halves of a subarray.
     * @param a an array of Comparable items.
     * @param tmpArray an array to place the merged result.
     * @param leftPos the left-most index of the subarray.
     * @param rightPos the index of the start of the second half.
     * @param rightEnd the right-most index of the subarray.
     * 
     * http://www.java-tips.org/java-se-tips/java.lang/merge-sort-implementation-in-java.html
     */
    private static void mergeIgnoreZero(double[] a, double[] tmpArray, int[] aIndices, int[] tmpIndices,
            int leftPos, int rightPos, int rightEnd) {
        int leftEnd = rightPos - 1;
        int tmpPos = leftPos;
        int numElements = rightEnd - leftPos + 1;

        // Main loop
        while (leftPos <= leftEnd && rightPos <= rightEnd) {
            // a[leftPos] is truly <= a[rightPos] only when a[leftPos] != 0
            // and a[leftPost] is truly < a[rightPos] only when a[rightPost] != 0
            if ((a[leftPos] <= a[rightPos] && a[leftPos] != 0) || (a[leftPos] > a[rightPos] && a[rightPos] == 0)) {
                tmpArray[tmpPos] = a[leftPos];
                tmpIndices[tmpPos++] = aIndices[leftPos++];
            }
            else {
                tmpArray[tmpPos] = a[rightPos];
                tmpIndices[tmpPos++] = aIndices[rightPos++];
            }
        }

        while (leftPos <= leftEnd) {    // Copy rest of first half
            tmpArray[tmpPos] = a[leftPos];
            tmpIndices[tmpPos++] = aIndices[leftPos++];
        }

        while (rightPos <= rightEnd) {  // Copy rest of right half
            tmpArray[tmpPos] = a[rightPos];
            tmpIndices[tmpPos++] = aIndices[rightPos++];
        }

        // Copy tmpArray back
        for (int i = 0; i < numElements; i++, rightEnd--) {
            a[rightEnd] = tmpArray[rightEnd];
            aIndices[rightEnd] = tmpIndices[rightEnd];
        }
    }

    /*
     * return a new densematrix, with given number
     * of extra rows and cols allocated, and with data
     * in given matrix copied over. do not change given matrix.
     *
     * always returns a DenseMatrix
     */
    public static DenseMatrix copyLarger(Matrix m, int extraRows, int extraCols) {
       DenseMatrix result = new DenseMatrix(m.numRows() + extraRows, m.numColumns() + extraCols);

       for (MatrixEntry e: m) {
           result.set(e.row(), e.column(), e.get());
       }

       return result;
    }
    
    /**
     * rescale result from [-1, 1] to [0, 1] range
     *
     * @param score
     * @return
     */
    public static Vector rescale(Vector score) {
        Vector scaled = score.copy();
        
        MatrixUtils.add(scaled, 1.0d);
        scaled.scale(0.5);
        
        return scaled;
    }
    
    public static void normalizeNetwork(Matrix m) {
        Vector sums = MatrixUtils.columnSums(m); // == row sums since m = m'
        for (VectorEntry e: sums) {
            if (e.get() > 0.0d) {
                e.set(Math.sqrt(e.get()));  //  think about the little epsilon in prototypes to prevent division by zero    
            }
            else {
                e.set(1.0d);
            }
        }
        
        for (MatrixEntry e: m) {
            e.set(e.get() / (sums.get(e.row()) * sums.get(e.column())) );   
        }       
    }
        
}
