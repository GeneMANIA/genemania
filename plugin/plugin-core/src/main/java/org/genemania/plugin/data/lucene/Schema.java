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

package org.genemania.plugin.data.lucene;

import java.util.HashMap;
import java.util.Map;

import org.genemania.domain.Gene;
import org.genemania.domain.GeneData;
import org.genemania.domain.GeneNamingSource;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.NetworkMetadata;
import org.genemania.domain.Node;
import org.genemania.domain.Organism;
import org.genemania.mediator.lucene.LuceneMediator;

public class Schema {
	static Map<Class<?>, String> typeFields;
	static Map<Class<?>, String> idFields;
	
	static {
		typeFields = new HashMap<Class<?>, String>();
		typeFields.put(InteractionNetwork.class, LuceneMediator.NETWORK);
		typeFields.put(Organism.class, LuceneMediator.ORGANISM);
		typeFields.put(NetworkMetadata.class, LuceneMediator.NETWORKMETADATA);
		typeFields.put(InteractionNetworkGroup.class, LuceneMediator.GROUP);
		typeFields.put(Gene.class, LuceneMediator.GENE);
		typeFields.put(GeneData.class, LuceneMediator.GENEDATA);
		typeFields.put(Node.class, LuceneMediator.NODE);
		typeFields.put(GeneNamingSource.class, LuceneMediator.NAMINGSOURCE);
		
		idFields = new HashMap<Class<?>, String>();
		idFields.put(InteractionNetwork.class, LuceneMediator.NETWORK_ID);
		idFields.put(Organism.class, LuceneMediator.ORGANISM_ID);
		idFields.put(NetworkMetadata.class, LuceneMediator.NETWORKMETADATA_ID);
		idFields.put(InteractionNetworkGroup.class, LuceneMediator.GROUP_ID);
		idFields.put(Gene.class, LuceneMediator.GENE_ID);
		idFields.put(GeneData.class, LuceneMediator.GENEDATA_ID);
		idFields.put(Node.class, LuceneMediator.NODE_ID);
		idFields.put(GeneNamingSource.class, LuceneMediator.NAMINGSOURCE_ID);
	}

	public static <T> String getTypeField(Class<T> modelClass) {
		return typeFields.get(modelClass);
	}
	
	public static <T> String getIdField(Class<T> modelClass) {
		return idFields.get(modelClass);
	}
}
