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

package org.genemania.engine.matricks;

/**
 *
 */
public class Utils {

    /** From MTJ
     *
     * Searches for a key in a subset of a sorted array.
     *
     * @param index
     *            Sorted array of integers
     * @param key
     *            Key to search for
     * @param begin
     *            Start posisiton in the index
     * @param end
     *            One past the end position in the index
     * @return Integer index to key. -1 if not found
     */
    public static int binarySearch(int[] index, int key, int begin, int end) {
        end--;

        while (begin <= end) {
            int mid = (end + begin) >> 1;

            if (index[mid] < key) {
                begin = mid + 1;
            }
            else if (index[mid] > key) {
                end = mid - 1;
            }
            else {
                return mid;
            }
        }

        return -1;
    }

    /*
     * inspired by the bin search code in MTJ,
     * this version returns the index into the given array if found,
     * or a value < 0 if not found. When not found, -result-1 is the
     * element position before which the new data value should be inserted.
     */
    public static int myBinarySearch(int[] indices, int key, int begin, int end) {

        if (indices.length == 0 || begin == end) {
            return -1;
        }
        
        end--;

        int mid = begin;
        while (begin <= end) {
            mid = (end + begin) >> 1;

            if (indices[mid] < key) {
                begin = mid + 1;
            }
            else if (indices[mid] > key) {
                end = mid - 1;
            }
            else {
                return mid;
            }
        }

        if (indices[mid] < key)  {
            return -mid-2;
        }
        else {
            return -mid-1;
        }
    }
}
