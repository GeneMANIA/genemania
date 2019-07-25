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

package org.genemania.connector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.genemania.dto.EnrichmentEngineRequestDto;
import org.genemania.dto.EnrichmentEngineResponseDto;
import org.genemania.dto.RelatedGenesEngineRequestDto;
import org.genemania.dto.RelatedGenesEngineResponseDto;
import org.genemania.dto.RelatedGenesWebRequestDto;
import org.genemania.dto.UploadNetworkEngineRequestDto;
import org.genemania.dto.UploadNetworkEngineResponseDto;
import org.genemania.dto.UploadNetworkWebRequestDto;
import org.genemania.engine.IMania;
import org.genemania.engine.Mania2;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.cache.FileSerializedObjectCache;
import org.genemania.engine.cache.MemObjectCache;
import org.genemania.exception.ApplicationException;
import org.genemania.util.ApplicationConfig;
import org.genemania.util.BrokerUtils;

public class SyncWorker {

	public static final String APP_VER = "appVer";
	
	private static Logger LOG = Logger.getLogger(SyncWorker.class);

	private String appVer;

	private IMania engine;
	private String cacheDir;
	
	public class SearchResult {
		public SearchResult(){}
		
		public RelatedGenesEngineRequestDto request;
		public RelatedGenesEngineResponseDto response;
		public EnrichmentEngineResponseDto enrichment;
	}
	
	public class UploadResult {
		public UploadResult(){}
		
		public UploadNetworkEngineRequestDto request;
		public UploadNetworkEngineResponseDto response;
	}

	public SyncWorker() {
		config();

		if (StringUtils.isEmpty(cacheDir)) {
			LOG.error("Worker thread missing required parameter: engine cache dir");
		}

		engine = new Mania2(new DataCache(new MemObjectCache(new FileSerializedObjectCache(cacheDir))));

		// output startup info
		LOG.info("GeneMANIA SyncWebWorker ver: " + appVer);
		LOG.info("Engine ver: " + engine.getVersion());
		LOG.info("cache dir: " + cacheDir);

	}

	private void config() {
		// read config data
		ApplicationConfig config = ApplicationConfig.getInstance();

		appVer = config.getProperty(APP_VER);
		cacheDir = config.getProperty(org.genemania.Constants.CONFIG_PROPERTIES.CACHE_DIR);
	}

	public SearchResult getRelatedGenes(RelatedGenesWebRequestDto req) throws ApplicationException {
		SearchResult ret = new SearchResult();

        RelatedGenesEngineRequestDto rgRequestDto = ret.request = BrokerUtils.dto2dto(req);
        RelatedGenesEngineResponseDto rgResponseDto = ret.response = engine.findRelated(rgRequestDto);

        EnrichmentEngineRequestDto eRequestDto = BrokerUtils.
                buildEnrichmentRequestFrom(rgRequestDto, rgResponseDto, req.getOntologyId());

        try {
        	ret.enrichment = engine.computeEnrichment(eRequestDto);
        } catch (Exception e) {
            LOG.error("Failed to compute enrichment", e);
        }

        return ret;
	}

	public UploadResult uploadNetwork(UploadNetworkWebRequestDto req) throws ApplicationException {
		UploadResult ret = new UploadResult();
		
		UploadNetworkEngineRequestDto engReq = ret.request = BrokerUtils.dto2dto(req);
		UploadNetworkEngineResponseDto engRes = ret.response = engine.uploadNetwork(engReq);
		
		ret.request = engReq;
		ret.response = engRes;
		
		return ret;
	}
}
