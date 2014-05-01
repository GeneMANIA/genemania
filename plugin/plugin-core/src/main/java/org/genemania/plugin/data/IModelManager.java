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

package org.genemania.plugin.data;

import org.genemania.data.normalizer.DataFileType;
import org.genemania.data.normalizer.DataImportSettings;
import org.genemania.data.normalizer.NormalizationResult;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.dto.UploadNetworkEngineResponseDto;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.util.ProgressReporter;

public interface IModelManager {
	void installNetwork(DataImportSettings settings, String filePath, DataFileType type, ProgressReporter progress) throws ApplicationException, DataStoreException;

	void installNetworkModel(DataImportSettings settings, UploadNetworkEngineResponseDto response, NormalizationResult result) throws ApplicationException;

	void uninstallNetwork(InteractionNetwork network) throws ApplicationException, DataStoreException;

	void updateNetwork(InteractionNetwork network, InteractionNetworkGroup group) throws ApplicationException, DataStoreException;
	
	void installGroup(Organism organism, InteractionNetworkGroup group, String color) throws ApplicationException;

	IModelWriter getModelWriter();
	
	void close() throws ApplicationException;

}
