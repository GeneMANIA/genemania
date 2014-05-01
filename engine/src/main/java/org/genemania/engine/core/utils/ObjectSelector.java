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
package org.genemania.engine.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import no.uib.cipr.matrix.DenseVector;

import org.genemania.engine.core.MatrixUtils;
import org.genemania.engine.core.integration.Feature;
import org.genemania.engine.core.integration.FeatureList;
import org.genemania.exception.ApplicationException;

/*
 * T doesn't have to be Comparable.
 * 
 * Score comparison is exact, not epsilon deltas.
 */
public class ObjectSelector<T> {

    ArrayList<T> elements = new ArrayList<T>();
    ArrayList<Double> scores = new ArrayList<Double>();
    
    public void add(T element, Double score) {
        elements.add(element);
        scores.add(score);
    }
    
    public void add(List<T> elements, double [] scores) throws ApplicationException {
        if (elements.size() != scores.length) {
            throw new ApplicationException("inconsistent sizes");
        }
        
        this.elements.addAll(elements);
        for (double score: scores) {
            this.scores.add(score);
        }
    }

    public void add(FeatureList features, DenseVector scores) throws ApplicationException {
        if (elements.size() != scores.size()) {
            throw new ApplicationException("inconsistent sizes");
        }        

        this.elements.addAll(elements);
        for (int i=0; i<scores.size(); i++) {
            this.scores.add(scores.get(i));
        }
    }

    @SuppressWarnings("unchecked")
    public void add(ObjectSelector<Feature> list) {
        this.elements.addAll((Collection<? extends T>) list.getElements());
        this.scores.addAll(list.getScores());
    }
    
    public int size() {
        return elements.size(); // == scores.size(), object invariant
    }
    
    /*
     * a sort of soft selection, we want n, but up to max if 
     * the levels are the same, else we get less than n
     */
    public ObjectSelector<T> selectLevelledSmallestScores(int n, int maxSize) {

        ObjectSelector<T> result = new ObjectSelector<T>();
        if (size() == 0 || n == 0) {
            return result;
        }

        // run our tied-ranks utility on the scores. returns ranks
        // numbered starting from 1
        DenseVector ranks = toDenseVector();
        MatrixUtils.rank(ranks);

        // build a table to lookup table from rank to
        // index in the elements/scores arrays. so
        // scores.get(unrank[0]) = the score of the element 
        // ranked first
        int [] unrank = new int[size()];
        for (int i=0; i<ranks.size(); i++) {
            int ix = (int) Math.round(ranks.get(i))-1;
            unrank[ix] = i;
        }

        // build up result adding all elements with same rank
        // as a group, while watching the given size limits
        ArrayList<Integer> rankStarts = getRankStarts(unrank); 

        for (int i=0; i<rankStarts.size()-1; i++) {
            int rank_start_ix = rankStarts.get(i);
            int next_rank_start_ix = rankStarts.get(i+1);

            // safe to add?
            if (result.size() < maxSize) {                
                for (int iy = rank_start_ix; iy<next_rank_start_ix; iy++) {
                    result.add(elements.get(unrank[iy]), scores.get(unrank[iy]));                    
                }
            }
            // enough already?
            if (result.size() >= n) {
                break;
            }
        }

        return result;
    }
    
    /*
     * list of starting rank positions. add a pseudo-rank at the end
     * of the list.
     */
    private ArrayList<Integer> getRankStarts(int [] unrank) {
        
        ArrayList<Integer> rankStarts = new ArrayList<Integer>();
   
        if (size() == 0) {
            return rankStarts;
        }
        
        double s = scores.get(unrank[0]);
        rankStarts.add(0);
        
        for (int ix = 1; ix<unrank.length; ix++) {

            double s2 = scores.get(unrank[ix]);
            
            if (s2 > s) {
                s = s2;
                rankStarts.add(ix);                
            }
        }
        
        // finish adding an extra element to keep indexing simple
        // for the user
        rankStarts.add(size());
        return rankStarts;

    }
    
    /*
     * silly little helper, since the utility code we use
     * works with a different type
     */
    public DenseVector toDenseVector() {
        final int n = size();
        DenseVector dv = new DenseVector(size()); 
        for (int i=0; i<n; i++) {
            dv.set(i, scores.get(i));
        }
        return dv;
    }
    
    public ObjectSelector<T> lt(double v) {
        ObjectSelector<T> result = new ObjectSelector<T>();
        final int n = scores.size();
        for (int i=0; i<n; i++) {
            if (scores.get(i) < v) {
                result.add(elements.get(i), scores.get(i));
            }
        }
        return result;
    }
    
    public ObjectSelector<T> le(double v) {
        ObjectSelector<T> result = new ObjectSelector<T>();
        final int n = scores.size();
        for (int i=0; i<n; i++) {
            if (scores.get(i) <= v) {
                result.add(elements.get(i), scores.get(i));
            }
        }
        return result;
    }

    public ObjectSelector<T> gt(double v) {
        ObjectSelector<T> result = new ObjectSelector<T>();
        final int n = scores.size();
        for (int i=0; i<n; i++) {
            if (scores.get(i) > v) {
                result.add(elements.get(i), scores.get(i));
            }
        }
        return result;
    }
    
    public ObjectSelector<T> ge(double v) {
        ObjectSelector<T> result = new ObjectSelector<T>();
        final int n = scores.size();
        for (int i=0; i<n; i++) {
            if (scores.get(i) >= v) {
                result.add(elements.get(i), scores.get(i));
            }
        }
        return result;
    }

    public ArrayList<T> getElements() {
        return elements;
    }

    public ArrayList<Double> getScores() {
        return scores;
    }

    public T getElement(int i) {
        return elements.get(i);
    }
    
    public double getScore(int i) {
        return scores.get(i);
    }
}
