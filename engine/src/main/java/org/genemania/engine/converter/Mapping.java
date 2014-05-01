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

package org.genemania.engine.converter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;

/**
 * The engine core represents networks as matrices, where the values
 * are the weights between edges, and the rows/cols represent the nodes (genes).
 *
 * This Mapping class is used to create a correspondence between an external
 * representation of nodes, and the simple indexing used within the engine.
 * Note that in an external representation, multiple objects (eg gene 
 * identifiers) can map to the same underlying indexed node.
 *
 * AliasObject refers to something like a 'gene', and UniqueObject is a 'node',
 * where multiple genes can map to a node.  Strings may be used for both. So
 * for the genemania domain representation we can use:
 *
 *   Mapping mapping = new Mapping<Gene, Node>();
 *
 * or if we want to store the node id's instead of the node objects:
 *
 *   Mapping mapping = new Mapping<Gene, Integer>();
 *
 * or alternatively if just using simple strings (like when doing file
 * processing outside of the domain model) one can use:
 *
 *   Mapping mapping = new Mapping<String, String>();
 *
 * TODO: add description of how a mapping can be built up with alias/unique
 * objects being added the the mapping explicitly and indexes incremented
 * implicitly (so order matters).
 *
 * @param <AliasObject>
 * @param <UniqueObject>
 */
public class Mapping <AliasObject, UniqueObject>{

	// could dump all these maps and build up a graph of domain objects, eg nodes, genes ...
	private LinkedHashMap<AliasObject, UniqueObject> aliasToUniqueMap = new LinkedHashMap<AliasObject, UniqueObject>();
	private LinkedHashMap<UniqueObject, Integer> uniqueToIndexMap = new LinkedHashMap<UniqueObject, Integer>();
	private Vector<UniqueObject> indexToUniqueObjectTable = new Vector<UniqueObject>();
	private HashMap<UniqueObject, AliasObject> preferredAlias = new HashMap<UniqueObject, AliasObject>();
	
	public void addAlias(AliasObject alias, UniqueObject uniqueId) {
		if (aliasToUniqueMap.containsKey(alias)) {
			UniqueObject currentUniqueId = aliasToUniqueMap.get(alias);
			if (!currentUniqueId.equals(uniqueId)) {
				System.out.println(String.format("Consistency error, alias %s with uniqueId %s was already mapped to uniqueId %s, ignoring", 
						alias.toString(), uniqueId,toString(), currentUniqueId.toString())); // TODO: log or throw exception 
			}
			return;
		}
		else { 
			addUniqueId(uniqueId);
			aliasToUniqueMap.put(alias, uniqueId);
			// some day we may grow a preferred bit in the mapping data, for now the
			// first alias for a given id is marked as preferred
			offerPreferredAlias(alias, uniqueId);
		}
	}
	
	private boolean offerPreferredAlias(AliasObject alias, UniqueObject uniqueId) {
		if (preferredAlias.containsKey(uniqueId)) {
			return false;
		}
		else {
			preferredAlias.put(uniqueId, alias);
			return true;
		}
	}
		
	public AliasObject getPreferredAliasForUniqueId(UniqueObject uniqueId) {
		return preferredAlias.get(uniqueId);		
	}
	
	public AliasObject getPreferredAliasForIndex(int index) {
		UniqueObject uniqueId = getUniqueIdForIndex(index);
		if (index == -1) {
			return null;
		}
		return getPreferredAliasForUniqueId(uniqueId);
	}
	
	public void addUniqueId(UniqueObject uniqueId) {
		if (!uniqueToIndexMap.containsKey(uniqueId)) {
			int nextIndex = size();
			uniqueToIndexMap.put(uniqueId, nextIndex);
			indexToUniqueObjectTable.add(uniqueId);
		}
	}
	
	public UniqueObject getUniqueIdForAlias(AliasObject alias) {
		return aliasToUniqueMap.get(alias);
	}
	
	public int getIndexForUniqueId(UniqueObject uniqueId) {
		if (uniqueToIndexMap.containsKey(uniqueId)) {
			return uniqueToIndexMap.get(uniqueId);
		}
		else {
			return -1;  // TODO: not such a good idea
		}
	}
	
	public UniqueObject getUniqueIdForIndex(int index) {
		if (index>= 0 && index < size()) {
			return indexToUniqueObjectTable.get(index);
		}
		else {
			return null;
		}
	}
	
	public int getIndexForAlias(AliasObject alias) {
		UniqueObject uniqueId = getUniqueIdForAlias(alias);
		if (uniqueId == null) {
			return -1;
		}
		else {
			return getIndexForUniqueId(uniqueId);
		}
	}
	
	public int size() {
		return indexToUniqueObjectTable.size();
	}
	
	public Object [] getIndexToUniqueIDTable() {
		Object [] table = null;
		table = indexToUniqueObjectTable.toArray();
		return table;
	}
}
