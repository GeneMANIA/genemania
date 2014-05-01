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

package org.genemania.engine.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;

import org.apache.log4j.Logger;
import org.genemania.engine.converter.Mapping;

import au.com.bytecode.opencsv.CSVReader;

public class FileUtils {
	private static Logger logger = Logger.getLogger(FileUtils.class);

	/**
	 * Load identifiers from a mapping file.
	 *
	 * field numbers are 0-indexed. the id field are unique identifiers, the synonym field are any
	 * identifier. there is a many-to-one mapping between synonym and unique identifiers. TODO: should
	 * drop the 'synonym' terminology since it has other meanings for gene identifiers (but here we mean
	 * any identifier like like systematic id or name etc)
	 *
	 * It appears its possible that identifiers can include double-quote chars. since the files are under
	 * our control, disable quoted field handling in opencsv by using null char, else have to escape the
	 * double quotes upstream
	 *
	 * @param source
	 * @param delim
	 * @param idFieldNum
	 * @param synonymFieldNum
	 * @throws IOException
	 */
	public static Map<String, String> loadSynonyms(Reader source, char delim, int idFieldNum, int synonymFieldNum, boolean forceUpper) throws IOException {
		Map<String, String> synonyms = new LinkedHashMap<String, String>();

		CSVReader reader = new CSVReader(source, delim, NULL_CHAR);
		String [] nextLine;

		while ((nextLine = reader.readNext()) != null) {
			String id = nextLine[idFieldNum].trim();
			String synonym = nextLine[synonymFieldNum].trim().toUpperCase();

            if (forceUpper) {
                synonym = synonym.toUpperCase();
            }

			if (synonyms.containsKey(synonym)) {
				// check consistency
				String storedId = synonyms.get(synonym);
				if (!storedId.equalsIgnoreCase(id)) {
					logger.info("consistency error: the same identifier belongs to two distinct unique ids: " + synonym);
				}
			}
			else {
				// add the new synonym in
				synonyms.put(synonym, id);
			}
		}

		logger.info("total number of identifiers loaded from mapping file: " + synonyms.size());
		return synonyms;
	}

	// TODO: future opencsv releases will include this, temp for now, see http://sourceforge.net/tracker/index.php?func=detail&aid=3125474&group_id=148905&atid=773544
	public static final char NULL_CHAR = '\0';

	/**
	 * Load identifiers from a mapping file.
	 *
	 * field numbers are 0-indexed. the id field are unique identifiers, the synonym field are any
	 * identifier. there is a many-to-one mapping between synonym and unique identifiers. TODO: should
	 * drop the 'synonym' terminology since it has other meanings for gene identifiers (but here we mean
	 * any identifier like like systematic id or name etc)
	 *
	 * It appears its possible that identifiers can include double-quote chars. since the files are under
	 * our control, disable quoted field handling in opencsv by using null char, else have to escape the
	 * double quotes upstream
	 *
	 * ID's from the mapping file are tripletss of the form id, symbol, source, where id is an genemania internal
	 * identifier of the form OrgPrefix:numericId eg Hs:1234. if stripIds is true, then we load the id as just 1234.
	 *
	 * This version returns a mapping object instead of just the map
	 *
	 * @param source
	 * @param delim
	 * @param idFieldNum
	 * @param synonymFieldNum
	 * @throws IOException
	 */
	public static Mapping<String, String> loadMapping(Reader source, char delim, int idFieldNum, int synonymFieldNum, boolean convertToUpperCase) throws IOException {
		Mapping<String, String> mapping = new Mapping<String, String>();

		CSVReader reader = new CSVReader(source, delim, NULL_CHAR);
		String [] nextLine;

		while ((nextLine = reader.readNext()) != null) {
			String id = nextLine[idFieldNum].trim();
			String synonym = nextLine[synonymFieldNum].trim();
			if (convertToUpperCase) {
				synonym = synonym.toUpperCase();
			}
			mapping.addAlias(synonym, id);
		}

		logger.info("total number of identifiers loaded from mapping file: " + mapping.size());
		return mapping;
	}

	/**
	 * Write out a network in the format. matrix must be symmetric and only
	 * the lower triangle is written out. Returns the total number of interactions
	 * written.
	 *
	 *   GeneSymbol GeneSymbol Weight
	 *   GeneSymbol GeneSymbol Weight
	 *   ...
	 *
	 * @param network
	 * @param genes
	 * @param writer
	 */
	public static int dump(PrintWriter writer, Matrix network, Mapping mapping, char delim, boolean outputUIDs, boolean stripUIDs) {

		int n = 0;
		for (MatrixEntry e: network) {
			if (e.get() != 0d && e.row() < e.column()) {

				String rowName;
				String colName;
				if (outputUIDs) {
					rowName = mapping.getUniqueIdForIndex(e.row()).toString();
					colName = mapping.getUniqueIdForIndex(e.column()).toString();
					if (stripUIDs) {
					    rowName = rowName.split(":")[1];
					    colName = colName.split(":")[1];
					}
				}
				else {
					rowName = mapping.getPreferredAliasForIndex(e.row()).toString();
					colName = mapping.getPreferredAliasForIndex(e.column()).toString();
				}

				String line = rowName + "\t" + colName + "\t" + e.get();
				writer.println(line);
				n++;
			}
		}

		return n;
	}

	/**
	 * Load up all records into a vector. The source is eg a text reader,
	 * where each line is a delim separated list of fields
	 *
	 * lines that start with the ignore symbol are ignored as comment lines,
	 * eg use ignore='#', or ignore=null if none comment char
	 *
	 * @param source
	 * @param delim
	 * @return
	 * @throws IOException
	 */
	public static Vector<String[]> loadRecords(Reader source, char delim, char ignore) throws IOException {
		Vector<String[]> queries = new Vector<String[]>();

		CSVReader reader = new CSVReader(source, delim);
		String [] nextLine;

		while ((nextLine = reader.readNext()) != null) {
			if (nextLine[0].startsWith("#")) {
				continue;
			}
			queries.add(nextLine);
		}

		logger.info(String.format("total %d records", queries.size()));
		return queries;
	}
	public static Vector<String[]> loadRecords(Reader source, char delim, char ignore, int upperLimit, int lowerLimit) throws IOException {
		Vector<String[]> queries = new Vector<String[]>();

		CSVReader reader = new CSVReader(source, delim);
		String [] nextLine;

		while ((nextLine = reader.readNext()) != null) {
			if (nextLine[0].startsWith("#")) {
				continue;
			}

			queries.add(nextLine);
		}

		logger.info(String.format("total %d records", queries.size()));
		return queries;
	}

	public static Map<String, List<String[]>> loadRecordsByOrganism(Reader source,
			                                                        char delim,
			                                                        char ignore) throws IOException {
		Map<String, List<String[]>> queries = new HashMap<String, List<String[]>>();

		CSVReader reader = new CSVReader(source, delim);
		String [] nextLine;

		while ((nextLine = reader.readNext()) != null) {
			if (nextLine[0].startsWith("#") || nextLine.length < 2) {
				continue;
			}

			String[] query = new String[nextLine.length-1];
			for ( int i = 1; i < nextLine.length; i++ ){
				query[i-1] = nextLine[i];
			}

			if ( !queries.containsKey(nextLine[0]) ){
				queries.put(nextLine[0], new ArrayList<String[]>());
			}
			queries.get(nextLine[0]).add(query);
			System.out.println("Adding query " + nextLine[1] + " to " + nextLine[0]);
		}

		return queries;
	}
}
