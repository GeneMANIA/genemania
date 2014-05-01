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

package org.genemania.engine.actions;

import org.apache.log4j.Logger;
import org.genemania.dto.UploadNetworkEngineRequestDto;
import org.genemania.dto.UploadNetworkEngineResponseDto;
import org.genemania.engine.Constants;
import org.genemania.engine.actions.support.UserNetworkProcessor;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.utils.Logging;
import org.genemania.engine.exception.CancellationException;
import org.genemania.exception.ApplicationException;

/**
 * process an upload network request. 
 * 
 * Note: we don't implement support for multiple-concurrent
 * uploads, nor do we serialize concurrent uploads internally. 
 * API-caller is responsible for ensuring the safe (non-concurrent)
 * use of the upload network api. In practice its perfectly safe
 * to have multiple concurrent uploads for different users, or
 * even for different organisms for the same user, since the data
 * organized separately. Its not just not safe to have multiple
 * concurrent uploads for the same user (== namespace) and same
 * organism.
 *
 * TODO: can merge usernetworkprocessor into this class.
 */
public class UploadNetwork {

    private static Logger logger = Logger.getLogger(UploadNetwork.class);
    private DataCache cache;
    UploadNetworkEngineRequestDto request;

    private long requestStartTimeMillis;
    private long requestEndTimeMillis;

    public UploadNetwork(DataCache cache, UploadNetworkEngineRequestDto request) {
        this.cache = cache;
        this.request = request;
    }

    public UploadNetworkEngineResponseDto process() throws ApplicationException {
        try {
            requestStartTimeMillis = System.currentTimeMillis();
            request.getProgressReporter().setMaximumProgress(Constants.PROGRESS_UPLOAD_COMPLETE);
            request.getProgressReporter().setProgress(Constants.PROGRESS_UPLOAD_START);
            request.getProgressReporter().setStatus(Constants.PROGRESS_UPLOAD_START_MESSAGE);

            UserNetworkProcessor processor = new UserNetworkProcessor(cache, cache.getCacheDir());

            UploadNetworkEngineResponseDto response = processor.process(request);

            request.getProgressReporter().setProgress(Constants.PROGRESS_UPLOAD_COMPLETE);
            request.getProgressReporter().setStatus(Constants.PROGRESS_UPLOAD_COMPLETE_MESSAGE);

            requestEndTimeMillis = System.currentTimeMillis();
            logger.info("completed processing uploadNetwork request, duration = " + Logging.duration(requestStartTimeMillis, requestEndTimeMillis));
            return response;
        }
        catch (CancellationException e) {
            logger.info("upload network request was cancelled");
            return null;
        }
    }

}
