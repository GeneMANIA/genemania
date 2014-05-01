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

import java.sql.*;
import java.io.*;
import java.util.*;

/**
 * Parses results from VectorPredictor into tab delimited file with readable information.
 * 
 * TODO: add command-line argument support to specify databases
 * TODO: add a logger
 */

public class QueryExecutor {
	
	private static String derbyURL = "jdbc:derby:genemania.derby";
	private static String ensemblURL = "jdbc:mysql://localhost:3306/go";
	private static String driver1 = "org.apache.derby.jdbc.EmbeddedDriver";
	private static String driver2 = "com.mysql.jdbc.Driver";
	private static Connection conDerby;
	private static Connection conEnsembl;
	
	public static void establishConnections() {
		try {
			Class.forName(driver1);
			conDerby = DriverManager.getConnection(derbyURL, "genemania", "password");
			Class.forName(driver2);
			conEnsembl = DriverManager.getConnection(ensemblURL, "genemania", "password");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.out.print("JDBC Driver " + driver1 + " not found in CLASSPATH or");
			System.out.println("JDBC Driver " + driver2 + " not found in CLASSPATH");
		}
	}
	
	public static void close() {
		try {
			if (conDerby != null) {
				conDerby.close();
			}
			if (conEnsembl != null) {
				conEnsembl.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static ResultSet executeDerbyQuery(String query) {
		try {
			Statement stmt = conDerby.createStatement();
			return stmt.executeQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static ResultSet executeEnsemblQuery(String query) {
		try {
			Statement stmt = conEnsembl.createStatement();
			return stmt.executeQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String[] getGOInfo(String goID) {
		try {
			ResultSet rs = executeEnsemblQuery("SELECT name, term_type FROM term WHERE acc = \"" + goID + "\"");
			rs.next();
			String[] s = {rs.getString(1), rs.getString(2)};
			return s;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}	
	
	private static String[] getBatchStats(String batchFileFolder, String orgName, String goID) {
		try {
			Scanner sc = new Scanner(new File(batchFileFolder + "/" + orgName + ".batch.txt"));
			String[] batchStats = new String[3];

			while (sc.hasNextLine()) {
				String line = sc.nextLine();				
				String[] tokens = line.split("\t");
				if (tokens[0].startsWith(goID) && tokens[1].startsWith("-")) {					
					batchStats[0] = tokens[2].substring(0, tokens[2].length() - 1);
					batchStats[1] = tokens[3].substring(0, tokens[3].length() - 1);
					batchStats[2] = tokens[4];
					
					return batchStats;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		
		//argument 1: run folder
		//argument 2: input
		
		try {
			Scanner sc = new Scanner(new File(args[0] + "/" + args[1]));
			
			establishConnections();
			
			//output header
			System.out.println("Node ID\tGene Symbols\tOrg Name\tGO ID\tGO Name\tGO Hierarchy\t" +
					"AUC-ROC\tAUC-PR\tPR-10\tScore (DV)\tPercentile\tDescription");
			while (sc.hasNextLine()) {
				
				String line = sc.nextLine();				
				String[] tokens = line.split("\t");
				
				String goID = tokens[0];
				
				for (String token : tokens) {
					//if token is a negative gene
					if (!token.startsWith("GO")) {
						// get query and execute it
						try {
							
							String[] stats = token.split(":");
							
							String negID = stats[0];
							
							String query = "SELECT N.ID, Symbol, Organism_ID, GENE_DATA_ID, G.Naming_Source_ID FROM NODES N INNER JOIN GENES G ON N.ID = G.NODE_ID WHERE N.ID = " + negID;
							ResultSet rs = executeDerbyQuery(query);
							int nodeID = -1, orgID = -1, geneDataID = -1;
							List<String> symbols = new ArrayList<String>();
							while (rs.next()) {
								nodeID = rs.getInt(1);
								symbols.add(rs.getString(2));
								orgID = rs.getInt(3);
								geneDataID = rs.getInt(4);
							}
							
							// order Ensembl gene names first
							query = "SELECT Symbol, G.Naming_Source_ID FROM NODES N INNER JOIN GENES G ON N.ID = G.NODE_ID WHERE G.Naming_Source_ID in (5, 2) and N.ID = " + negID + " ORDER BY G.Naming_Source_ID";
							rs = executeDerbyQuery(query);
							while (rs.next()) {
								String symbol = rs.getString(1);
								symbols.remove(symbol);
								symbols.add(0, symbol);
							}
							
							// concatenate the symbols for output
							String geneNames = "";
							for (String s : symbols) {
								geneNames += s + " | ";
							}
							geneNames = geneNames.substring(0, geneNames.length() - 3);

							String[] goInfo = getGOInfo(goID);

							// get the organisms name
							query = "SELECT Name FROM ORGANISMS WHERE ID = " + orgID;
							rs = executeDerbyQuery(query);
							rs.next();
							String orgName = rs.getString(1);
							
							String[] batchStats = getBatchStats(args[0],
									orgName, goID);

							// get the description of the node ID
							query = "SELECT Description FROM GENE_DATA WHERE ID = "
									+ geneDataID;
							ResultSet geneDescr = executeDerbyQuery(query);
							geneDescr.next();

							System.out.println(nodeID + "\t" + geneNames + "\t"
									+ orgName + "\t" + goID + "\t" + goInfo[0]
									+ "\t" + goInfo[1] + "\t" + batchStats[0]
									+ "\t" + batchStats[1] + "\t" + batchStats[2]
									+ "\t" + stats[1] + "\t" + stats[2] 
									+ "\t" + geneDescr.getString(1));

						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}
}
