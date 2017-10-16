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

package org.genemania.plugin;

import java.io.Reader;

import org.genemania.data.normalizer.DataImportSettings;
import org.genemania.dto.UploadNetworkEngineRequestDto;
import org.genemania.type.NetworkProcessingMethod;
import org.genemania.util.ProgressReporter;

public class ManiaUtils {
	public static final int QUERY_GENE_THRESHOLD = 6;
	private static final int DEFAULT_SPARSIFICATION = 50;

	public static UploadNetworkEngineRequestDto createRequest(DataImportSettings settings, Reader sourceData, ProgressReporter progress) {
		UploadNetworkEngineRequestDto request = new UploadNetworkEngineRequestDto();
		request.setLayout(settings.getDataLayout());
		request.setMethod(settings.getProcessingMethod());
		request.setNamespace(GeneMania.DEFAULT_NAMESPACE);
		request.setOrganismId(settings.getOrganism().getId());
		request.setNetworkId(settings.getNetwork().getId());
		request.setProgressReporter(progress);
		request.setData(sourceData);
		
		int sparsification = DEFAULT_SPARSIFICATION;
		switch (settings.getDataLayout()) {
		case GEO_PROFILE:
		case SPARSE_PROFILE:
		case PROFILE:
			request.setSparsification(sparsification);
			break;
		case BINARY_NETWORK:
			if (request.getMethod().equals(NetworkProcessingMethod.LOG_FREQUENCY)) {
				request.setSparsification(sparsification);
			}
			break;
		}
		
		return request;
	}
}
