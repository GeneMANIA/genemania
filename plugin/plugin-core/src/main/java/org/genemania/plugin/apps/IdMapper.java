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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.genemania.domain.Gene;
import org.genemania.domain.GeneNamingSource;
import org.genemania.domain.Node;
import org.genemania.domain.Organism;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.NodeMediator;
import org.genemania.plugin.data.DataSetManager;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.xml.sax.SAXException;

public class IdMapper extends AbstractPluginDataApp {
	public static final String NODE_ID = "node-id"; //$NON-NLS-1$
	
	@Option(name = "--organism", usage = "name of organism", required = true)
	private String fOrganismName;

	private GeneNamingSource nodeIdNamingSource = new GeneNamingSource(NODE_ID, Byte.MAX_VALUE, NODE_ID);

	private void initialize() throws ApplicationException {
		try {
			DataSetManager manager = createDataSetManager();
			fData = manager.open(new File(fDataPath));
		} catch (SAXException e) {
			throw new ApplicationException(e);
		}
	}

	private void printMappings() throws DataStoreException, ApplicationException {
		List<String> arguments = getArguments();
		
		Organism organism = parseOrganism(fData, fOrganismName);
		if (organism == null) {
			System.err.println(String.format("Unrecognized organism: %s", fOrganismName)); //$NON-NLS-1$
			return;
		}
		
		List<GeneNamingSource> sources = extractNamingSources(arguments);
		
		NodeMediator mediator = fData.getMediatorProvider().getNodeMediator();
		List<Long> nodeIds = fData.getNodeIds(organism.getId());
		
		List<String> ids = new ArrayList<String>();
		for (Long nodeId : nodeIds) {
			Node node = mediator.getNode(nodeId, organism.getId());
			for (GeneNamingSource source : sources) {
				if (source == nodeIdNamingSource) {
					ids.add(node.getName());
					continue;
				}
				String symbol = null;
				for (Gene gene : node.getGenes()) {
					if (source.getId() == gene.getNamingSource().getId()) {
						symbol = gene.getSymbol();
						continue;
					}
				}
				ids.add(symbol);
			}
			boolean first = true;
			for (String id : ids) {
				if (!first) {
					System.out.print("\t"); //$NON-NLS-1$
				} else {
					first = false;
				}
				if (id != null) {
					System.out.print(id);
				}
			}
			System.out.println();
			ids.clear();
		}
	}

	private List<GeneNamingSource> extractNamingSources(List<String> arguments) throws ApplicationException {
		List<GeneNamingSource> sources = new ArrayList<GeneNamingSource>();
		
		List<GeneNamingSource> namingSources = fData.getAllNamingSources();
		namingSources.add(nodeIdNamingSource);
		for (String name : arguments) {
			GeneNamingSource foundSource = null;
			for (GeneNamingSource source : namingSources) {
				if (name.equalsIgnoreCase(source.getName()) || name.equalsIgnoreCase(source.getShortName())) {
					foundSource = source;
					break;
				}
			}
			if (foundSource == null) {
				throw new ApplicationException(String.format("Unrecognized identifier type: %s", name)); //$NON-NLS-1$
			}
			sources.add(foundSource);
		}		
		return sources;
	}

	public static void main(String[] args) throws Exception {
        Logger.getLogger("org.genemania").setLevel(Level.FATAL); //$NON-NLS-1$

        IdMapper mapper = new IdMapper();
        CmdLineParser parser = new CmdLineParser(mapper);
        try {
        	parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println(String.format("\nUsage: java %s options source-id-type target-id-type1 [target-id-type2 ...]\n", IdMapper.class.getName())); //$NON-NLS-1$
            parser.printUsage(System.err);
            return;
        }
        try {
			mapper.initialize();
			mapper.printMappings();
        } catch (ApplicationException e) {
        	String message = e.getMessage();
        	if (message != null && message.length() > 0) {
        		System.err.println(message);
        	} else {
        		e.printStackTrace();
        	}
        }
	}
}
