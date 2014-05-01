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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.genemania.data.normalizer.DataFileClassifier;
import org.genemania.data.normalizer.DataImportSettings;
import org.genemania.data.normalizer.DataNormalizer;
import org.genemania.data.normalizer.GeneCompletionProvider2;
import org.genemania.data.normalizer.NormalizationResult;
import org.genemania.data.normalizer.OrganismClassifier;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.dto.UploadNetworkEngineRequestDto;
import org.genemania.dto.UploadNetworkEngineResponseDto;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.plugin.FileUtils;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.ManiaUtils;
import org.genemania.plugin.data.IModelManager;
import org.genemania.plugin.data.IModelWriter;
import org.genemania.plugin.data.Namespace;
import org.genemania.util.NullProgressReporter;
import org.genemania.util.ProgressReporter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class NetworkImporter extends AbstractImportApp {
	private static final int DEFAULT_LINES_TO_SAMPLE = 200;

	public static void main(String[] args) throws Exception {
        Logger.getLogger("org.genemania").setLevel(Level.FATAL); //$NON-NLS-1$

		final NetworkImporter importer = new NetworkImporter();
        CmdLineParser parser = new CmdLineParser(importer);
        try {
        	parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println(String.format("\nUsage: %s options\n", NetworkImporter.class.getSimpleName())); //$NON-NLS-1$
            parser.printUsage(System.err);
            return;
        }
        importer.handleArguments();
	}

	private FileUtils fileUtils;
	
	public NetworkImporter() {
		fileUtils = new FileUtils();
	}

	private void handleArguments() throws ApplicationException, DataStoreException, IOException {
		initialize();
		
        if (fVerbose) {
            Logger.getLogger("org.genemania").setLevel(Level.INFO); //$NON-NLS-1$
        }

		if (fDescription == null) {
			fDescription = ""; //$NON-NLS-1$
		}

		long networkId = fData.getNextAvailableId(InteractionNetwork.class, fNamespace);
		InteractionNetwork network = new InteractionNetwork();
		network.setName(fNetworkName);
		network.setDescription(fDescription);
		network.setId(networkId);

		DataImportSettings settings = new DataImportSettings();
		settings.setSource(fFile.getName());
		settings.setNetworkGroup(fNetworkGroup);
		settings.setNetwork(network);
		
		OrganismClassifier organismClassifier = new OrganismClassifier(fData.getGeneClassifier());
		organismClassifier.classify(settings, fileUtils.getUncompressedReader(fFile), DEFAULT_LINES_TO_SAMPLE);
		settings.setOrganism(fOrganism);
		
		DataFileClassifier classifier = new DataFileClassifier();
		classifier.classify(settings, fileUtils.getUncompressedStream(fFile), DEFAULT_LINES_TO_SAMPLE);
		
		System.err.printf("Detected file type: %s\n", settings.getDataFormat()); //$NON-NLS-1$
		System.err.printf("Detected file layout: %s\n", settings.getDataLayout()); //$NON-NLS-1$
		System.err.printf("Processing method: %s\n", settings.getProcessingMethod()); //$NON-NLS-1$
		
		DataNormalizer normalizer = new DataNormalizer();
		GeneCompletionProvider2 provider = fData.getCompletionProvider(fOrganism);
		
		ProgressReporter progress = NullProgressReporter.instance();
		Reader rawReader = fileUtils.getUncompressedReader(fFile);
		try {
			File tempFile = File.createTempFile(getClass().getName(), ".txt"); //$NON-NLS-1$
			try {
				NormalizationResult result;
				Writer normalizedWriter = new FileWriter(tempFile);
				try {
					result = normalizer.normalize(settings, provider, rawReader, normalizedWriter, progress);
				} finally {
					normalizedWriter.close();
				}
				
				Reader normalizedReader = new FileReader(tempFile);
				try {
					UploadNetworkEngineRequestDto request = ManiaUtils.createRequest(settings, normalizedReader, progress);
					if (fNamespace.equals(Namespace.USER)) {
						request.setNamespace(GeneMania.DEFAULT_NAMESPACE);
					} else {
						request.setNamespace("CORE"); //$NON-NLS-1$
					}
					UploadNetworkEngineResponseDto response = fMania.uploadNetwork(request);
					
					IModelManager manager = fData.createModelManager(fNamespace);
					try {
						IModelWriter writer = manager.getModelWriter();
						if (fNetworkGroup.getId() == 0) {
							fNetworkGroup.setId(fData.getNextAvailableId(InteractionNetworkGroup.class, fNamespace));
							writer.addGroup(fNetworkGroup, fOrganism, fGroupColor);
						}
						manager.installNetworkModel(settings, response, result);
					} finally {
						manager.close();
					}
					printResults(result);
				} finally {
					normalizedReader.close();
				}

			} finally {
				tempFile.delete();
			}
		} finally {
			rawReader.close();
		}
	}

	private void printResults(NormalizationResult result) {
		if (result.getDroppedEntries() > 0) {
			System.err.printf("Dropped rows: %d/%d\n", result.getDroppedEntries(), result.getTotalEntries()); //$NON-NLS-1$
		}
		Set<String> invalidSymbols = result.getInvalidSymbols();
		if (invalidSymbols.size() > 0) {
			System.err.printf("Unrecognized gene symbols: %d\n", invalidSymbols.size()); //$NON-NLS-1$
			List<String> sorted = new ArrayList<String>(invalidSymbols);
			Collections.sort(sorted, new Comparator<String>() {
				public int compare(String o1, String o2) {
					return o1.compareToIgnoreCase(o2);
				}
			});
			for (String symbol : sorted) {
				System.err.printf("\t%s\n", symbol); //$NON-NLS-1$
			}
		}
	}
}
