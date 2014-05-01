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

package org.genemania.util;

import org.genemania.AbstractTest;

public class GeneHelperTest extends AbstractTest {

	public void testRetrieve() {
/*		
		try {
			CassandraThriftConnector cassandraConnector = new CassandraThriftConnector();
			cassandraConnector.openSession();
			Map<String, String> sources = cassandraConnector.get("meta", "sources");
			Map<String, String> data = cassandraConnector.get("6", "HPA2");
			Iterator<String> iterator = data.keySet().iterator();
			while(iterator.hasNext()) {
				String nameSourceId = iterator.next();
				String synonym = data.get(nameSourceId);
System.out.println(sources.get(nameSourceId) + "->" + synonym);
			}
			cassandraConnector.closeSession();
		} catch (ApplicationException e) {
			e.printStackTrace();
		}
*/	
	}
	
/*	
	public void testSql2cass() {
		try {
			CassandraThriftConnector cassandraConnector = new CassandraThriftConnector();
			cassandraConnector.openSession();
			int organismId = 4;
			List<Gene> allGenes = geneMediator.getAllGenes(organismId);
			for(Gene gene: allGenes) {
				if(gene != null) {
					String key = gene.getSymbol();
					Node node = gene.getNode();
					Map<String, String> data = new Hashtable<String, String>();
					Iterator<Gene> jt = node.getGenes().iterator();
					while(jt.hasNext()) {
						Gene gene1 = jt.next();
						if((gene1 != null) && (!gene1.getSymbol().equals(key))) {
							data.put(String.valueOf(gene1.getNamingSource().getId()), gene1.getSymbol());
						}
					}
					cassandraConnector.insert(String.valueOf(organismId), key, data);
				}
			}
//			// testing
//			cassandraConnector.get("4", "TSPAN6");
			cassandraConnector.closeSession();
		} catch (ApplicationException e) {
			System.out.println(e.getMessage());
		}
	}
*/
/*
	public void testImportNameSources() {
		try {
			CassandraThriftConnector cassandraConnector = new CassandraThriftConnector();
			cassandraConnector.openSession();
			Map data = new Hashtable();
			data.put("1", "Entrez Gene ID");
			data.put("2", "RefSeq Protein ID");
			data.put("3", "Entrez Gene Name");
			data.put("4", "RefSeq mRNA ID");
			data.put("5", "Uniprot ID");
			data.put("6", "TAIR ID");
			data.put("7", "Synonym");
			data.put("8", "Ensembl Gene ID");
			data.put("9", "Ensembl Gene Name");
			data.put("10", "Ensembl Protein ID");
			cassandraConnector.insert("meta", "sources", data);
			cassandraConnector.closeSession();
		} catch (ApplicationException e) {
			System.out.println(e.getMessage());
		}
	}
*/	
	
}
