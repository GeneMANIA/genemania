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
import java.util.List;

/**
 * Try a hand-coded heap with max k elements, customized for our data. For
 * speed.
 * 
 * the heap is organized with the minimum-element at top, and stores both a
 * value & associated integer id. some old micro-benchmarks (ya i know)
 * suggested it would faster to store both the id and the value in the same
 * double array and cast (maybe due to cache locality during access?) but for
 * code clarity it's would probably be better to revise this into two separate
 * arrays (TODO)
 * 
 * our particular use case requires us to keep all id values that match the min
 * value stored in the heap, even to exceed the max k element count (possibly by
 * any arbitrarily large count!), so we implement the overflow list
 * additonalData.
 */
public class KHeap {
	private int maxSize;
	private double [] data;
	private int size;
	private boolean extensible = false;
	private List<Double> additionalData;
	
	public KHeap(int maxSize) {
		this.maxSize = maxSize;
		this.data = new double[2*maxSize];
	}
	
	public KHeap(int maxSize, boolean extensible) {
		this.maxSize = maxSize;
		this.data = new double[2*maxSize];
		this.extensible = extensible;
	}
	
	public boolean offer(final int id, final double weight) {
		
	    // if we have space in the main body of the heap, no problem
	    // just add the new value in
	    if (size < maxSize) {			
			data[2*size] = weight;
			data[2*size+1] = id;
			size++;
			siftUp(size-1);
			return true;
		}
		
	    // heap is full, if extensible we need to be careful
	    // to maintain the overflow in the lowest level in the heap
		if ( extensible ){
			if ( weight < data[0] ){
				return false;
			} else if ( weight == data[0] ){ // store the ties
				if ( additionalData == null ){
					additionalData = new ArrayList<Double>();
				}
				additionalData.add(weight); // this is redundant, its already in data[0]. TODO: remove, only keep ids
				additionalData.add((double)id);
				return true;
			} else {
			    // the offered value is larger than the current min, but we need to be careful
			    // when adding to the heap. the current min may need to get pushed onto 
			    // the extension if the heap min stays at the same level
			    double minWeight = data[0];
			    double idForMinWeight = data[1];			    
			    
		        data[0] = weight;
		        data[1] = id;
		        siftDown(0);
			    
		        if (data[0] > minWeight) { 
		            additionalData = null; // throw away the stored tied values
		        }
		        else {
		            if (additionalData == null) {
		                additionalData = new ArrayList<Double>();
		            }
		            additionalData.add(weight);
		            additionalData.add(idForMinWeight);
		        }
				
				return true;
			}
		} else {
			// can ignore if heap full and new value is smaller than current min
			if (weight <= data[0]) { // data[0] always contains smallest weight
			    return false;
			}
		}

		// the heap is full but we don't maintain an overflow at the last level, 		
		// just overwrite the current min, and heapify
		data[0] = weight;
		data[1] = id;
		siftDown(0);
		return true;
	}
	
	private void swap(final int i, final int j) {
		double weight = data[2*i];
		double id = data[2*i+1];
		data[2*i] = data[2*j];
		data[2*i+1] = data[2*j+1];
		data[2*j] = weight;
		data[2*j+1] = id;
	}
	
	private void siftUp(final int index) {
		if (index <= 0) {
			return;
		}
		
		int ip = (index-1)/2;
		
		if (data[2*ip] > data[2*index]) {
			swap(ip, index);			
		}
		
		siftUp(ip);
		
	}
	
	private void siftDown(final int index) {
		if (index >= size) {
			return;
		}
		
		int ip = index;
		int ilc = 2*index+1;
		int irc = 2*index+2;
		
		if (ilc > size-1) {
			return;
		}
		
		if (irc > size-1) {
			if (data[2*ilc] < data[2*ip]) {
				swap(index, ilc);
				siftDown(ilc);
				return;			
			}
		}
		else {
			if (data[2*ilc] < data[2*ip] && data[2*ilc] <= data[2*irc]) {
				swap(index, ilc);
				siftDown(ilc);
				return;
			}
			else if (data[2*irc] < data[2*ip] && data[2*irc] < data[2*ilc]) {
				swap(index, irc);
				siftDown(irc);
				return;
			}
		}	
		// no sifting needed
	}
	
	public int size() {
		int additional = additionalData == null ? 0 : additionalData.size() / 2;
		return size + additional;
	}
	
	public void dump() {
		for (int i=0; i<size; i++) {
			System.out.print(" " + getWeight(i));			
		}
		
		if ( additionalData != null ){
			for ( double e : additionalData ){
				System.out.print(" " + e);		
			}
		}
		System.out.println("");
	}

	public long getId(final int i) {
		if ( i < size ){
			return Math.round(data[2*i+1]);
		} else if ( additionalData != null ){
			return Math.round( additionalData.get(2*(i - size) + 1) );
		} else {
			throw new IndexOutOfBoundsException();
		}
	}
	
	public double getWeight(final int i) {
		if ( i < size ){
			return data[2*i];
		}else if ( additionalData != null ){
			return additionalData.get( 2*(i - size) );
		}else{
			throw new IndexOutOfBoundsException();
		}
	}
	
	public int getExtensionSize() {
	   if (additionalData == null) {
	       return 0;
	   }
	   return additionalData.size()/2; 
	}
	
	/*
	 * remove minimum element. overwrite the min element with 
	 * the tail of the heap, and sift.
	 * 
	 *  returns true if a value was popped
	 */
    public boolean pop() {
        if (size() == 0) {
            return false;
        }        
        
        if (size() == 1) {
            size--;
            return true;
        }        

        // remove elements from additional data first, if we have any
        if (additionalData != null) {
            int additionalSize = additionalData.size();
            if (additionalSize == 2) {
                additionalData = null;
                return true;
            }

            additionalData.remove(additionalSize-1);
            additionalData.remove(additionalSize-2);
            return true;
        }
        
        size--;        
        data[0] = data[2*size];        
        data[1] = data[2*size+1];        
        additionalData = null;
         
        siftDown(0);        
        return true;
    }
    
    /*
     * pop values off of heap that are less than or equal to
     * the given threshold. special-cased so that popped the
     * additional-values is fast
     * 
     * returns the # of values popped
     */
    public int popLE(double threshold) {
        
        int numPopped = 0;
        
        // check additional values, can pop in one-shot
        if (getWeight(0) <= threshold && additionalData != null) {
            numPopped = additionalData.size()/2;
            additionalData = null;
        }
        
        // rest of heap
        boolean popped = true;
        while (popped & size > 0 && getWeight(0) <= threshold) {
            popped = pop();
            if (popped) {
                numPopped += 1;
            }
        }
        
        return numPopped;
    }
}
