/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2008-2011 University of Toronto.
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

package org.genemania.plugin.apps;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.mysql.jdbc.Driver;

public class ValidationSetMaker extends AbstractPluginApp {
	@Option(name = "--organism", usage = "taxonomy id of organism", required = true)
	private int fTaxonomyId;

	@Option(name = "--db", usage = "JDBC connection string for a GO database, defaults to EBI's MySQL GO mirror")
	String fConnectionString = "jdbc:mysql://mysql.ebi.ac.uk:4085/go_latest?user=go_select&password=amigo"; //$NON-NLS-1$
	
	@Option(name = "--query", usage = "name of the resulting query file", required = true)
	String fQueryFilename;
	
	@Option(name = "--branch", usage = "GO branch to consider (one of: 'all', 'bp', 'mf', 'cc').  Defaults to 'all'")
	String fBranch = "all"; //$NON-NLS-1$

	@SuppressWarnings("nls")
	String createGoQuery(long taxId) {
		String ignoreClause = "and evidence.code not in ('IEA', 'ND', 'RCA')";
		return "select" +
			" ancestor_term.term_type as ancestor_term_type, ancestor_term.acc as ancestor_acc," +
			" gene_product.symbol" +
			" from association, term as ancestor_term, term as descendent_term, gene_product, species, evidence, graph_path, dbxref" +
			" where species.ncbi_taxa_id = " +
			taxId +
			" and species.id = gene_product.species_id" +
			" and association.gene_product_id = gene_product.id" +
			" and ancestor_term.id = graph_path.term1_id" +
			" and descendent_term.id = graph_path.term2_id" +
			" and evidence.association_id = association.id " +
			ignoreClause +
			" and association.term_id = descendent_term.id" +
			" and gene_product.dbxref_id = dbxref.id" +
			" and association.is_not = 0" +
			" and descendent_term.is_obsolete = 0;";
	}
	
	@SuppressWarnings("nls")
	void makeQuery() throws DataStoreException, ApplicationException, IOException {
		checkWritable(fQueryFilename);
		
		Map<String, String> branches = new HashMap<String, String>();
		branches.put("all", "all");
		branches.put("bp", "biological_process");
		branches.put("mf", "molecular_function");
		branches.put("cc", "cellular_component");
		
		String targetBranch = branches.get(fBranch);
		if (targetBranch == null) {
			System.err.printf("Unrecognized GO branch: %s\n", fBranch);
			return;
		}
		
		String query = createGoQuery(fTaxonomyId);
		
		try {
			new Driver();
			
			System.out.println("Connecting...");
			Connection connection = DriverManager.getConnection(fConnectionString);
			Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			statement.setFetchSize(Integer.MIN_VALUE);
			
			long start = System.currentTimeMillis();
			System.out.println("Executing query...");
			ResultSet results = statement.executeQuery(query);
			
			PrintWriter writer = new PrintWriter(new File(fQueryFilename));
			try {
				long annotationsFetched = 0;
				Map<String, Set<String>> allCategories = new HashMap<String, Set<String>>();
				try {
					while (results.next()) {
						annotationsFetched++;
						if (annotationsFetched % 1000 == 0) {
							System.out.printf("Fetched %d annotations...\n", annotationsFetched);
						}
						String branch = results.getString("ancestor_term_type");
						String id = results.getString("ancestor_acc");
						String gene = results.getString("symbol");
	
						if (!(targetBranch.equals("all") || targetBranch.equals(branch))) {
							continue;
						}
						
						Set<String> category = allCategories.get(id);
						if (category == null) {
							category = new HashSet<String>();
							allCategories.put(id, category);
						}
						category.add(gene);
					}
					
					ArrayList<String> terms = new ArrayList<String>(allCategories.keySet());
					Collections.sort(terms);
					int totalCategories = 0;
					for (String term : terms) {
						Set<String> genes = allCategories.get(term);
						totalCategories++;
						writer.print(term);
						writer.print("\t");
						writer.print("+");
						for (String gene : genes) {
							writer.print("\t");
							writer.print(gene);
						}
						writer.println();
					}
					System.out.printf("Total GO categories: %d\n", totalCategories);
					System.out.println("Done.");
				} finally {
					long duration = System.currentTimeMillis() - start;
					System.out.printf("Elapsed time: %.2fs\n", duration / 1000.0);
					results.close();
					connection.close();
				}
			} finally {
				writer.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ApplicationException(e);
		}
	}
	
	public static void main(String[] args) throws Exception {
		ValidationSetMaker maker = new ValidationSetMaker();
        CmdLineParser parser = new CmdLineParser(maker);
        try {
        	parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.printf("\nUsage: %s options\n\n", ValidationSetMaker.class.getSimpleName()); //$NON-NLS-1$
            parser.printUsage(System.err);
            return;
        }
		maker.makeQuery();
	}
}
