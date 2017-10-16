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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.genemania.data.normalizer.DataFileType;
import org.genemania.data.normalizer.DataImportSettings;
import org.genemania.data.normalizer.DataNormalizer;
import org.genemania.data.normalizer.GeneCompletionProvider2;
import org.genemania.data.normalizer.NormalizationResult;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.NetworkMetadata;
import org.genemania.domain.Organism;
import org.genemania.dto.RemoveNetworkEngineRequestDto;
import org.genemania.dto.UploadNetworkEngineRequestDto;
import org.genemania.dto.UploadNetworkEngineResponseDto;
import org.genemania.engine.IMania;
import org.genemania.engine.Mania2;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.cache.IObjectCache;
import org.genemania.engine.cache.MemObjectCache;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.plugin.FileUtils;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.ManiaUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.IConfirmationHandler;
import org.genemania.plugin.data.IModelManager;
import org.genemania.plugin.data.IModelWriter;
import org.genemania.plugin.data.Namespace;
import org.genemania.type.NetworkProcessingMethod;
import org.genemania.util.ChildProgressReporter;
import org.genemania.util.NullProgressReporter;
import org.genemania.util.ProgressReporter;

public class LuceneModelManager implements IModelManager {
	private final DataSet data;
	private final IConfirmationHandler confirmationHandler;
	private final IModelWriter modelWriter;
	private final Namespace namespace;
	private final FileUtils fileUtils;
	
	public LuceneModelManager(DataSet data, IConfirmationHandler handler, IModelWriter modelWriter, Namespace namespace, FileUtils fileUtils) {
		this.data = data;
		this.modelWriter = modelWriter;
		confirmationHandler = handler;
		this.namespace = namespace;
		this.fileUtils = fileUtils;
	}
	
	public void installNetwork(DataImportSettings settings, String filePath, DataFileType type, ProgressReporter progress) throws ApplicationException, DataStoreException {
		installGroup(settings.getOrganism(), settings.getNetworkGroup(), settings.getColor());
		if (type.equals(DataFileType.EXPRESSION_PROFILE)) {
			installProfileNetwork(settings, filePath, progress);
		} else if (type.equals(DataFileType.INTERACTION_NETWORK)) {
			installTextNetwork(settings, filePath, progress);
		} else {
			throw new ApplicationException(String.format(Strings.unknownFileType_error, type.name()));
		}
	}
	
	public void installGroup(Organism organism, InteractionNetworkGroup group, String color) throws ApplicationException {
		if (group.getId() == -1) {
			group.setId(data.getNextAvailableId(InteractionNetworkGroup.class, Namespace.USER));
			modelWriter.addGroup(group, organism, color);
		}
	}

	void installProfileNetwork(DataImportSettings settings, String filePath, ProgressReporter progress) throws ApplicationException, DataStoreException {
		try {
			progress.setMaximumProgress(3);
			int stage = 0;
	
			IObjectCache cache = data.getObjectCache(NullProgressReporter.instance(), false);
			IMania mania = new Mania2(new DataCache(new MemObjectCache(cache)));
			
			File networkFile = File.createTempFile("network", "txt"); //$NON-NLS-1$ //$NON-NLS-2$
			try {
				progress.setStatus(Strings.convertProfileToNetwork_status);
				ChildProgressReporter childProgress = new ChildProgressReporter(progress);
				DataNormalizer normalizer = new DataNormalizer();
				GeneCompletionProvider2 genes = data.getCompletionProvider(settings.getOrganism());
				Reader reader = fileUtils.getUncompressedReader(filePath);
				Writer writer = new FileWriter(networkFile);
				NormalizationResult result = normalizer.normalize(settings, genes, reader, writer, childProgress);
				childProgress.close();
				stage++;
				
				if (progress.isCanceled()) {
					return;
				}
				
				if (result.hasErrors() && !confirmationHandler.acceptPartialImport(settings, result)) {
					progress.cancel();
					return;
				}
				
				progress.setStatus(Strings.convertProfileToNetwork_status2);
				childProgress = new ChildProgressReporter(progress);
				
				Reader normalizedReader = new FileReader(networkFile);
				
				UploadNetworkEngineRequestDto request = ManiaUtils.createRequest(settings, normalizedReader, childProgress);
				UploadNetworkEngineResponseDto response = mania.uploadNetwork(request);
				childProgress.close();
				stage++;
				
				if (progress.isCanceled()) {
					return;
				}
				
				// Add network to index
				progress.setStatus(Strings.installTextNetwork_status3);
				progress.setProgress(stage++);
				installNetworkModel(settings, response, result);
			} finally {
				networkFile.delete();
			}
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}

	void installTextNetwork(DataImportSettings settings, String networkFile, ProgressReporter progress) throws ApplicationException, DataStoreException {
		int stage = 0;
		progress.setMaximumProgress(3);
		
		IObjectCache cache = data.getObjectCache(NullProgressReporter.instance(), false);
		IMania mania = new Mania2(new DataCache(new MemObjectCache(cache)));
		
		// Convert data to common format
		progress.setStatus(Strings.installTextNetwork_status);
		
		DataNormalizer normalizer = new DataNormalizer();
		GeneCompletionProvider2 genes = data.getCompletionProvider(settings.getOrganism());
		
		try {
			File normalizedFile = File.createTempFile("temp", "normalized_network.txt"); //$NON-NLS-1$ //$NON-NLS-2$
			try {
				Reader reader = fileUtils.getUncompressedReader(networkFile);
				try {
					Writer writer = new FileWriter(normalizedFile);
					ChildProgressReporter childProgress = new ChildProgressReporter(progress);
					NormalizationResult result = normalizer.normalize(settings, genes, reader, writer, childProgress);
					childProgress.close();
					stage++;
					
					if (progress.isCanceled()) {
						return;
					}
					
					if (result.hasErrors() && !confirmationHandler.acceptPartialImport(settings, result)) {
						progress.cancel();
						return;
					}
					
					// Import network
					progress.setStatus(Strings.installTextNetwork_status2);
					childProgress = new ChildProgressReporter(progress);
			
					Reader normalizedReader = new FileReader(normalizedFile);
					
					UploadNetworkEngineRequestDto request = ManiaUtils.createRequest(settings, normalizedReader, childProgress);
					request.setSparsification(50);
					UploadNetworkEngineResponseDto response = mania.uploadNetwork(request);
					childProgress.close();
					stage++;
					
					if (response == null || progress.isCanceled()) {
						return;
					}
					
					// Add network to index
					progress.setStatus(Strings.installTextNetwork_status3);
					progress.setProgress(stage++);
					installNetworkModel(settings, response, result);
				} finally {
					reader.close();
				}
				
			} finally {
				normalizedFile.delete();
			}
		} catch (IOException e) {
			throw new DataStoreException(e);
		}
	}

	public void installNetworkModel(DataImportSettings settings, UploadNetworkEngineResponseDto response, NormalizationResult result) throws ApplicationException {
		InteractionNetwork network = settings.getNetwork();
		InteractionNetworkGroup group = settings.getNetworkGroup();

		NetworkMetadata metadata = new NetworkMetadata();
		metadata.setId(data.getNextAvailableId(NetworkMetadata.class, namespace));
		metadata.setInteractionCount((long) response.getNumInteractions());
		metadata.setOther(network.getDescription());
		metadata.setProcessingDescription(getProcessingDescription(settings));
		metadata.setSource(settings.getSource());
		
		StringBuilder details = new StringBuilder();
		if (result.getDroppedEntries() > 0) {
			details.append(String.format("%d/%d dropped rows", result.getDroppedEntries(), result.getDroppedEntries() + result.getTotalEntries())); //$NON-NLS-1$
		}
		int totalInvalidSymbols = result.getInvalidSymbols().size();
		if (totalInvalidSymbols > 0) {
			if (details.length() > 0) {
				details.append("; "); //$NON-NLS-1$
			}
			details.append(String.format("%d unrecognized gene symbols", totalInvalidSymbols)); //$NON-NLS-1$
		}
		if (details.length() > 0) {
			metadata.setOther(details.toString());
		}
		network.setMetadata(metadata);
		
		modelWriter.addNetwork(network, group);
	}
	
	private String getProcessingDescription(DataImportSettings settings) {
		NetworkProcessingMethod method = settings.getProcessingMethod();
		if (NetworkProcessingMethod.DIRECT.equals(method)) {
			return Strings.processingMethodDirect_label;
		}
		if (NetworkProcessingMethod.PEARSON.equals(method)) {
			return Strings.processingMethodPearson_label;
		}
		if (NetworkProcessingMethod.LOG_FREQUENCY.equals(method)) {
			return Strings.processingMethodLogFrequency_label;
		}
		return Strings.processingMethodUnknown_label;
	}

	public void uninstallNetwork(InteractionNetwork network) throws ApplicationException, DataStoreException {
		// Remove network data
		IMania mania = new Mania2(new DataCache(new MemObjectCache(data.getObjectCache(NullProgressReporter.instance(), false))));
		
		InteractionNetworkGroup group = data.getNetworkGroup(network.getId());
		Organism organism = data.getOrganism(group.getId());
		if (organism == null) {
			return;
		}
		
		RemoveNetworkEngineRequestDto request = new RemoveNetworkEngineRequestDto();
		request.setOrganismId(organism.getId());
		request.setNamespace(GeneMania.DEFAULT_NAMESPACE);
		request.setNetworkId(network.getId());
		mania.removeUserNetworks(request);
		
		// Remove network metadata
		modelWriter.deleteNetwork(network);
	}
	
	public void updateNetwork(InteractionNetwork network, InteractionNetworkGroup group) throws ApplicationException, DataStoreException {
		modelWriter.deleteNetwork(network);
		modelWriter.addNetwork(network, group);
	}
	
	public void close() throws ApplicationException {
		modelWriter.close();
	}

	public IModelWriter getModelWriter() {
		return modelWriter;
	}
}
