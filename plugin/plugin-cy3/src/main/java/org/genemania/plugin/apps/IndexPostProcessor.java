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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.FSDirectory;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.NetworkMetadata;
import org.genemania.exception.ApplicationException;
import org.genemania.mediator.NetworkMediator;
import org.genemania.mediator.lucene.LuceneMediator;
import org.genemania.mediator.lucene.LuceneNetworkMediator;
import org.genemania.plugin.data.IModelWriter;
import org.genemania.plugin.data.lucene.LuceneModelWriter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class IndexPostProcessor {
	@Option(name = "--source", usage = "source index directory", required = true)
	private String fSourcePath;
	
	@Option(name = "--target", usage = "target index directory", required = true)
	private String fTargetPath;

	public static void main(String[] args) throws Exception {
		IndexPostProcessor processor = new IndexPostProcessor();
		CmdLineParser parser = new CmdLineParser(processor);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			printUsage(parser);
			return;
		}
		
		processor.process();
	}

	private static void printUsage(CmdLineParser parser) {
		System.err.println(String.format("\nGeneral usage: java %s options\n", IndexPostProcessor.class.getName())); //$NON-NLS-1$
		parser.printUsage(System.err);
	}
	
	private void process() throws IOException, ApplicationException {
		File source = new File(fSourcePath);
		File target = new File(fTargetPath);
		
		Pattern pattern = Pattern.compile("\\d+"); //$NON-NLS-1$
		
		if (!source.isDirectory() || !target.isDirectory()) {
			throw new IllegalArgumentException("Both the source and target index directories must exist"); //$NON-NLS-1$
		}
		
		for (File file : target.listFiles()) {
			if (!file.isDirectory()) {
				continue;
			}
			
			String organismId = file.getName();
			Matcher matcher = pattern.matcher(organismId); 
			if (!matcher.matches()) {
				continue;
			}

			File sourceFile = new File(source.getPath() + File.separator + organismId);
			
			FSDirectory targetDirectory = FSDirectory.open(file);
			Searcher targetSearcher = new IndexSearcher(targetDirectory);
			Searcher sourceSearcher = new IndexSearcher(FSDirectory.open(sourceFile));
			
			Analyzer analyzer = LuceneMediator.createDefaultAnalyzer();
			NetworkMediator targetMediator = new LuceneNetworkMediator(targetSearcher, analyzer);
			NetworkMediator sourceMediator = new LuceneNetworkMediator(sourceSearcher, analyzer);
			
			Map<InteractionNetwork, InteractionNetworkGroup> networksToUpdate = new HashMap<InteractionNetwork, InteractionNetworkGroup>();
			for (InteractionNetwork targetNetwork : targetMediator.getAllNetworks()) {
				InteractionNetwork sourceNetwork = sourceMediator.getNetwork(targetNetwork.getId());
				if (hasChanges(sourceNetwork, targetNetwork)) {
					InteractionNetworkGroup group = sourceMediator.getNetworkGroupForNetwork(sourceNetwork.getId());
					networksToUpdate.put(sourceNetwork, group);
					System.out.printf("Updated %s\t%s\t%s.\n", group.getName(), sourceNetwork.getId(), sourceNetwork.getName()); //$NON-NLS-1$
				}
			}
			
			if (networksToUpdate.size() == 0) {
				return;
			}
			
			IndexWriter indexWriter = new IndexWriter(targetDirectory, analyzer, false, MaxFieldLength.UNLIMITED);
			IModelWriter modelWriter = new LuceneModelWriter(indexWriter);
			try {
				for (Entry<InteractionNetwork, InteractionNetworkGroup> entry : networksToUpdate.entrySet()) {
					InteractionNetwork network = entry.getKey();
					InteractionNetworkGroup group = entry.getValue();
					modelWriter.deleteNetwork(network);
					modelWriter.addNetwork(network, group);
				}
			} finally {
				modelWriter.close();
			}
		}
	}

	private boolean hasChanges(InteractionNetwork sourceNetwork, InteractionNetwork targetNetwork) {
		NetworkMetadata targetMetadata = targetNetwork.getMetadata();
		NetworkMetadata sourceMetadata = sourceNetwork.getMetadata();
		if (sourceMetadata != null && targetMetadata != null && sourceMetadata.getInteractionCount() != targetMetadata.getInteractionCount()) {
			return true;
		}
		
		if (sourceNetwork.isDefaultSelected() != targetNetwork.isDefaultSelected()) {
			return true;
		}
		return false;
	}
}
