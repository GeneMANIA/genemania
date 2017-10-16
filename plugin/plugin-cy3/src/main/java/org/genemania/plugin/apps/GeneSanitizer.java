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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.genemania.data.normalizer.GeneCompletionProvider2;
import org.genemania.domain.Gene;
import org.genemania.domain.Organism;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.data.DataSetManager;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.xml.sax.SAXException;

public class GeneSanitizer extends AbstractPluginDataApp {
	@Option(name = "--organism", usage = "name of organism", required = true)
	private String fOrganismName;
	private NetworkUtils networkUtils;
	
	void initialize() throws ApplicationException {
		try {
			networkUtils = new NetworkUtils();
			DataSetManager manager = createDataSetManager();
			fData = manager.open(new File(fDataPath));
		} catch (SAXException e) {
			throw new ApplicationException(e);
		}
	}

	void sanitize() throws IOException, DataStoreException, ApplicationException {
		List<String> arguments = getArguments();

		Organism organism = parseOrganism(fData, fOrganismName);
		if (organism == null) {
			System.err.println(String.format("Unrecognized organism: %s", fOrganismName)); //$NON-NLS-1$
			return;
		}
		
		BufferedReader reader;
		if (arguments.size() > 0) {
			String path = arguments.get(0);
			checkFile(path);
			reader = new BufferedReader(new FileReader(path));
		} else {
			reader = new BufferedReader(new InputStreamReader(System.in));
		}
		
		GeneCompletionProvider2 provider = fData.getCompletionProvider(organism);
		String line = reader.readLine();
		Set<Long> nodeIds = new HashSet<Long>();
		while (line != null) {
			try {
				Gene gene = provider.getGene(line);
				if (gene == null) {
					System.out.println(String.format("%s\t", line)); //$NON-NLS-1$
					continue;
				}
				Long id = gene.getNode().getId();
				if (nodeIds.contains(id)) {
					System.out.println(String.format("%s\t", line)); //$NON-NLS-1$
					continue;
				}
				nodeIds.add(id);
				Gene preferredGene = networkUtils.getPreferredGene(gene.getNode());
				System.out.println(String.format("%s\t%s", line, preferredGene.getSymbol())); //$NON-NLS-1$
			} finally {
				line = reader.readLine();
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		GeneSanitizer sanitizer = new GeneSanitizer();
        CmdLineParser parser = new CmdLineParser(sanitizer);
        try {
        	parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println(String.format("\nUsage: %s options gene-list-file\n", GeneSanitizer.class.getSimpleName())); //$NON-NLS-1$
            parser.printUsage(System.err);
            return;
        }
		sanitizer.initialize();
		sanitizer.sanitize();
	}
}
