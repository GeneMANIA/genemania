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

package org.genemania.engine.core.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.VectorEntry;
import no.uib.cipr.matrix.sparse.SparseVector;

import org.junit.Test;
import static org.junit.Assert.*;

public class MiscTest {

    @Test
    public void test1 (){
        int bin; 

        bin = getBinNumber(0, 10, 0, 100);
        assertEquals( bin, 0);
        bin = getBinNumber(10, 10, 0, 100);
        assertEquals( bin, 0);
        bin = getBinNumber(11, 10, 0, 100);
        assertEquals( bin, 1);
        bin = getBinNumber(100, 10, 0, 100);
        assertEquals( bin, 9);

        bin = getBinNumber(1, 10, 1, 100);
        assertEquals( bin, 0);
        bin = getBinNumber(10, 10, 1, 100);
        assertEquals( bin, 0);
        bin = getBinNumber(11, 10, 1, 100);
        assertEquals( bin, 1);
        bin = getBinNumber(100, 10, 1, 100);
        assertEquals( bin, 9);

        bin = getBinNumber(3, 2, 1, 5);
        assertEquals( bin, 0);
        bin = getBinNumber(4, 2, 1, 5);
        assertEquals( bin, 1);

        Random rand = new Random(333); 

        //"gene1,0,4,0,8,0,4,4,0,4,0,4,0,8,4,0,0,4,0,4,0\n"
        for ( int i = 0; i < 3; i++ ){
            System.out.print("gene" + i);

            List<Integer> random = new ArrayList<Integer>(20);
            for ( int j = 0; j < 20; j++ ){
                random.add(j);
            }

            for ( int j = 0; j < 20; j++ ){
                System.out.print( "\t" + random.remove( rand.nextInt(random.size()) ));
            }
            System.out.println();
        }

    }

    private int getBinNumber( double value, int numBin, double start, double end ){
        return (int) Math.floor( (value - start) / ( (end-start+1) / numBin  ));
    }

    @Test
    public void test2(){
        boolean[] test = new boolean[5];
        if ( test[1] ){
            System.out.println("init to true");
        }else{
            System.out.println("init to false");
        }
    }

    @Test
    public void test3(){
        int numGene = 10;

        int k = 0;
        for ( int i = 0; i < numGene; i++ ){
            for ( int j = i + 1; j < numGene; j++ ){
                int end = numGene - 1;
                int start = numGene - i;
                int index = ((start + end)*(end-start+1)) / 2 + (j-i) - 1;

                assertEquals(k, index);
                k++;
            }
        }
    }

    @Test
    public void test4(){
        Vector v = new DenseVector(new double[]{1,2,3});
        Vector u = new DenseVector(new double[]{-1,-2,-3});
        v.add(-2, v);
        for ( int i = 0; i < v.size(); i++ ){
            assertEquals( v.get(i), u.get(i), 0d );
        }

        SparseVector sparse1 = new SparseVector (10, new int[] {2, 3},
                new double[] {2, 9} );

        for ( VectorEntry e: sparse1){
            System.out.println( e.index() );
        }
    }

    @Test
    public void test5(){
        Vector v = new SparseVector(5, new int[]{4,2}, new double[]{4.0, 2.0});

        for ( VectorEntry e: v){
            System.out.println(e.index());
        }
    }
}
