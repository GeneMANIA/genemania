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

package org.genemania.engine.validation;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * Parses results from QueryExecutor into annotation format specifed by Gene Ontology
 * 
 * TODO: add command-line argument support to specify databases
 * TODO: add a logger
 */

public class GeneAnnotator {
	
	//private static String ensemblURL = "jdbc:mysql://ensembldb.ensembl.org:5306/ensembl_go_54";
	//don't use Ensembl database for large amount of queries 
	
	private static String ensemblURL = "jdbc:mysql://localhost:3306/go";
	private static String driver = "com.mysql.jdbc.Driver";
	private static Connection con;
	
	public static void connectToEnsembl() {
		try {
			Class.forName(driver);
			con = DriverManager.getConnection(ensemblURL, "genemania", "password");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.out.print("JDBC Driver " + driver + " not found in CLASSPATH");
		}
	}
	
	public static ResultSet executeQuery(String query) {
		try {
			Statement stmt = con.createStatement();
			return stmt.executeQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static int getDatabaseID(String synonyms, int speciesID) {		
		try {
			String[] symbols = synonyms.split(" \\| ");
			for (String symbol : symbols) {
				String query = "SELECT dbxref_id FROM gene_product WHERE symbol =\""
						+ symbol + "\" and species_id = " + speciesID;
				
				ResultSet rs = executeQuery(query);
				if (rs.next())
					return rs.getInt(1);
			}
			
			System.out.println("symbol" + synonyms + " in derby not found in GO database");
			return -1;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public static String[] getDatabaseInfo(int dbID) {
		try {			
			String query = "SELECT xref_dbname, xref_key FROM dbxref WHERE id =" + dbID;			
			ResultSet rs = executeQuery(query);
			
			if (rs.next()) {
				String[] s = {rs.getString(1),rs.getString(2)};
				return s;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static int[] getOrganismIDs(String[] orgName) {
		try {
			String query = "SELECT id, ncbi_taxa_id FROM species WHERE genus =\"" + orgName[0] + "\" and species =\"" + orgName[1] + "\"";
			ResultSet rs = executeQuery(query);
			rs.next();
			int[] i = {rs.getInt(1), rs.getInt(2)};
			return i;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		
		try {
			Scanner sc = new Scanner(new File(args[0]));
			sc.nextLine(); //skip the headers
			
			connectToEnsembl();
			
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] tokens = line.split("\t");
				
				String synonyms = tokens[1];
				String geneName = synonyms.split(" \\| ")[0];
				
				String symbols;
				//remove the first gene symbol from synonyms
				if (synonyms.length() > geneName.length()) {
					symbols = synonyms.substring(geneName.length() + 2);
					StringTokenizer st = new StringTokenizer(symbols, " ");
					synonyms = "";
					while (st.hasMoreTokens()) {
						synonyms += st.nextToken();
					}
				} else {
					symbols = "";
				}
				//also remove all spaces from synonyms
				
				String[] orgName = tokens[2].split(" "); //0->Genus, 1->species
				String goTerm = tokens[3];
				
				//get the species and taxon IDs for the organism
				int[] orgIDs = getOrganismIDs(orgName);
				String taxon = "taxon:" + orgIDs[1];
				
				//get database info
				String[] dbInfo = getDatabaseInfo(getDatabaseID(tokens[1], orgIDs[0]));
				
				if (dbInfo == null)
					continue;
				
				String reference = "PMID:18613948";
				String evidenceCode = "IEA"; //inferred from electronic analysis
				String withFrom = goTerm;
				
				String goBranch = tokens[6];
				
				String aspect = goBranch.equals("cellular_component") ? "C" : (goBranch.equals("biological_process") ? "P" : "F");
				String objectType = "gene";
				
				//get the date
				Calendar c = Calendar.getInstance();
				String year = "" + c.get(Calendar.YEAR);
				String month = "" + (c.get(Calendar.MONTH) + 1);
				String day = "" + c.get(Calendar.DAY_OF_MONTH);
				
				//pad day and month
				month = month.length() > 1 ? month : "0" + month;
				day = day.length() > 1 ? day : "0" + day;
				
				String date = year + month + day;
				
				String assignedBy = "GeneMANIA"; 
				
				String geneAnnotation = dbInfo[0] + "\t" + dbInfo[1] + "\t" 
					+ geneName + "\t\t" + goTerm + "\t" + reference + "\t" 
					+ evidenceCode + "\t" + withFrom + "\t" + aspect + "\t\t" + synonyms + "\t" 
					+ objectType + "\t" + taxon + "\t" + date + "\t" + assignedBy;
				
				System.out.println(geneAnnotation);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

