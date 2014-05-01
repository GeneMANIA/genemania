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


package org.genemania.engine.actions.support;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import org.apache.log4j.Logger;
import org.genemania.dto.UploadNetworkEngineRequestDto;
import org.genemania.dto.UploadNetworkEngineResponseDto;
import org.genemania.engine.Constants;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.converter.sym.FileNetworkSymMatrixProvider;
import org.genemania.engine.converter.sym.INetworkSymMatrixProvider;
import org.genemania.engine.core.data.Network;
import org.genemania.engine.core.evaluation.ProfileToNetworkDriver;
import org.genemania.engine.core.evaluation.correlation.CorrelationFactory.CorrelationType;
import org.genemania.engine.exception.CancellationException;
import org.genemania.engine.matricks.MatrixCursor;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.exception.ApplicationException;
import org.genemania.type.DataLayout;
import org.genemania.type.NetworkProcessingMethod;
import org.genemania.util.ProgressReporter;

/**
 * transform, save, and apply any necessary precomputations
 * for user-uploaded networks
 *
 *   UserNetworkProcessor processor = new userNetworkProcessor(cache);
 *   // prepare request params ...
 *   response = processor.process(request);
 *
 */
public class UserNetworkProcessor {
    private static Logger logger = Logger.getLogger(UserNetworkProcessor.class);
    private DataCache cache;
    private String tempDirname;

    public UserNetworkProcessor(DataCache cache, String tempDirname) {
        this.cache = cache;
        this.tempDirname = tempDirname;
    }

    /*
     * validate request parameters,
     * returns without exception if all okay
     */
    private void checkRequest(UploadNetworkEngineRequestDto request) throws ApplicationException {
        if (request == null) { // surely this will never happen
            throw new ApplicationException("invalid null request");
        }

        if (request.getNamespace() == null || request.getNamespace().trim().equals("")) {
            throw new ApplicationException("no namespace specified");
        }

        if (request.getNetworkId() >= 0) {
            throw new ApplicationException("user networks must have id < 0");
        }

        // TODO: eventually we'll either agree that ids should be ints or just start
        // using longs everywhere ...
        if (request.getNetworkId() < Integer.MIN_VALUE) {
            throw new ApplicationException("network ids values must be in integer range, given id: " + request.getNetworkId());
        }

        // the processing method needs to match with data layout types
        if (request.getLayout() == DataLayout.PROFILE) {
            if (request.getMethod() != NetworkProcessingMethod.PEARSON) {
                throw new ApplicationException("unsupported processing method for PROFILE data layout");
            }
        }
        else if (request.getLayout() == DataLayout.WEIGHTED_NETWORK) {
            if (request.getMethod() != NetworkProcessingMethod.DIRECT) {
                throw new ApplicationException("unsupported processing method for WEIGHTED_NETWORK data layout");
            }
        }
        else if (request.getLayout() == DataLayout.SPARSE_PROFILE) {
            if (request.getMethod() != NetworkProcessingMethod.LOG_FREQUENCY) {
                throw new ApplicationException("unsupported processing method for SPARSE_PROFILE data layout");
            }
        }
        else if (request.getLayout() == DataLayout.BINARY_NETWORK) {
            if (request.getMethod() != NetworkProcessingMethod.LOG_FREQUENCY && request.getMethod() != NetworkProcessingMethod.DIRECT) {
                throw new ApplicationException("unsupported processing method for BINARY_NETWORK data layout");
            }
        }
        else {
            throw new ApplicationException("unsupported data layout"); // TODO: add binary, sparse_profile
        }
    }

    private void logRequestParams(UploadNetworkEngineRequestDto request) {
        logger.info(String.format("processing upload network request for user %s organism %d network id %d with layout %s using processing method %s", request.getNamespace(),
                request.getOrganismId(), request.getNetworkId(), request.getLayout(), request.getMethod()));
    }

    /*
     * main request processing routine
     */
    public UploadNetworkEngineResponseDto process(UploadNetworkEngineRequestDto request) throws ApplicationException {
        checkRequest(request);
        logRequestParams(request);

        request.getProgressReporter().setStatus(Constants.PROGRESS_UPLOAD_PROCESSING_MESSAGE);
        request.getProgressReporter().setProgress(Constants.PROGRESS_UPLOAD_PROCESSING);
        SymMatrix network = convertNetwork(request);

        if (request.getProgressReporter().isCanceled()) {
            throw new CancellationException();
        }

        // notice if request is cancelled, we can have
        // a user network on disk, but no precomputed data structures.
        // TODO: cleanup the user network also, or don't save till end?
        saveNetwork(request, network);
        precompute(request, network, request.getProgressReporter());
        UploadNetworkEngineResponseDto response = computeStats(network);

        return response;
    }

    /*
     * apply p2n etc  to convert the requests data stream
     * into an interaction matrix
     * 
     * TODO: need to fix up to avoid the extra trip of temp user data to the filesystem. 
     * TODO: break up into separate processors per layout/method combo.
     * TODO: get to doing all the todo's!
     */
    SymMatrix convertNetwork(UploadNetworkEngineRequestDto request) throws ApplicationException {

        SymMatrix matrix = null;
        // handle profile data with pearson correlation
        if (request.getMethod() == NetworkProcessingMethod.PEARSON && request.getLayout() == DataLayout.PROFILE) {
            ProfileToNetworkDriver p2n = new ProfileToNetworkDriver();

            try {
                File tempFile = getTempFile(request.getNamespace(), (int)request.getOrganismId(), (int)request.getNetworkId());
                //tempFile.deleteOnExit();

                logger.debug("writing temp output to " + tempFile);
                Writer result = new BufferedWriter(new FileWriter(tempFile));

                try {
                    p2n.setSynReader(makeIdMapping((int) request.getOrganismId()));
                    p2n.setNoHeader(true);
                    p2n.setK(request.getSparsification());
                    p2n.setCorrelationType(CorrelationType.PEARSON);
                    p2n.setProfileType("CONTINUOUS");

                    // better to use a child progressreporter here instead?
                    p2n.setProgressReporter(request.getProgressReporter());

                    p2n.process(request.getData(), result);

                }
                finally {
                    result.close();
                }

                matrix = convertToMatrixRepresentation(request.getOrganismId(), request.getNetworkId(), request.getNamespace(), request.getProgressReporter(), false);
                
            }
            // wrap ioexeption in application exception, other application exceptions
            // pass on through
            catch (IOException e) {
                throw new ApplicationException("Failed to convert profile to network", e);
            }

            // TODO: proper resource cleanup (close file in finally)
            
        }
        // handle a direct weighted network load
        else if (request.getMethod() == NetworkProcessingMethod.DIRECT && request.getLayout() == DataLayout.WEIGHTED_NETWORK) {

            try {
                File tempFile = getTempFile(request.getNamespace(), (int)request.getOrganismId(), (int)request.getNetworkId());
                logger.debug("writing temp output to " + tempFile);
                Writer result = new BufferedWriter(new FileWriter(tempFile));

                try {
                    char[] buf = new char[1024];
                    Reader data = request.getData();
                    int n;
                    while ((n = data.read(buf)) > 0) {
                        result.write(buf, 0, n);
                    }
                }
                finally {
                    result.close();
                }

                matrix = convertToMatrixRepresentation(request.getOrganismId(), request.getNetworkId(), request.getNamespace(), request.getProgressReporter(), false);
            }
            catch (Exception e) {
                throw new ApplicationException("Failed to load direct network", e);
            }
        }
        // direct binary network load
        else if (request.getMethod() == NetworkProcessingMethod.DIRECT && request.getLayout() == DataLayout.BINARY_NETWORK) {

            try {
                File tempFile = getTempFile(request.getNamespace(), (int)request.getOrganismId(), (int)request.getNetworkId());
                logger.debug("writing temp output to " + tempFile);
                Writer result = new BufferedWriter(new FileWriter(tempFile));

                try {
                    char[] buf = new char[1024];
                    Reader data = request.getData();
                    int n;
                    while ((n = data.read(buf)) > 0) {
                        result.write(buf, 0, n);
                    }
                }
                finally {
                    result.close();
                }

                matrix = convertToMatrixRepresentation(request.getOrganismId(), request.getNetworkId(), request.getNamespace(), request.getProgressReporter(), true);
            }
            catch (Exception e) {
                throw new ApplicationException("Failed to load binary network", e);
            }
        }
        else if (request.getMethod() == NetworkProcessingMethod.LOG_FREQUENCY && request.getLayout() == DataLayout.SPARSE_PROFILE) {
            ProfileToNetworkDriver p2n = new ProfileToNetworkDriver();

            try {
                File tempFile = getTempFile(request.getNamespace(), (int)request.getOrganismId(), (int)request.getNetworkId());
                //tempFile.deleteOnExit();

                logger.debug("writing temp output to " + tempFile);
                Writer result = new BufferedWriter(new FileWriter(tempFile));

                try {
                    p2n.setSynReader(makeIdMapping((int) request.getOrganismId()));
                    p2n.setNoHeader(true);
                    p2n.setK(request.getSparsification());
                    p2n.setCorrelationType(CorrelationType.PEARSON_BIN_LOG_NO_NORM);
                    p2n.setProfileType("BINARY");
                    p2n.setKeepAllTies(true);
                    p2n.setLimitTies(true);

                    // better to use a child progressreporter here instead?
                    p2n.setProgressReporter(request.getProgressReporter());

                    p2n.process(request.getData(), result);

                }
                finally {
                    result.close();
                }

                matrix = convertToMatrixRepresentation(request.getOrganismId(), request.getNetworkId(), request.getNamespace(), request.getProgressReporter(), false);

            }
            // wrap ioexeption in application exception, other application exceptions
            // pass on through
            catch (IOException e) {
                throw new ApplicationException("Failed to convert profile to network", e);
            }

        }
        else if (request.getMethod() == NetworkProcessingMethod.LOG_FREQUENCY && request.getLayout() == DataLayout.BINARY_NETWORK) {
            ProfileToNetworkDriver p2n = new ProfileToNetworkDriver();

            try {
                File tempFile = getTempFile(request.getNamespace(), (int)request.getOrganismId(), (int)request.getNetworkId());
                //tempFile.deleteOnExit();

                logger.debug("writing temp output to " + tempFile);
                Writer result = new BufferedWriter(new FileWriter(tempFile));

                try {
                    p2n.setSynReader(makeIdMapping((int) request.getOrganismId()));
                    p2n.setNoHeader(true);
                    p2n.setK(request.getSparsification());
                    p2n.setCorrelationType(CorrelationType.PEARSON_BIN_LOG_NO_NORM);
                    p2n.setProfileType("NETWORK");
                    p2n.setKeepAllTies(true);
                    p2n.setLimitTies(true);

                    // better to use a child progressreporter here instead?
                    p2n.setProgressReporter(request.getProgressReporter());

                    p2n.process(request.getData(), result);

                }
                finally {
                    result.close();
                }

                matrix = convertToMatrixRepresentation(request.getOrganismId(), request.getNetworkId(), request.getNamespace(), request.getProgressReporter(), false);

            }
            // wrap ioexeption in application exception, other application exceptions
            // pass on through
            catch (IOException e) {
                throw new ApplicationException("Failed to convert profile to network", e);
            }
            
        }
        else {
            throw new ApplicationException("unsupported processing method/data layout combination"); // shouldn't actually get here due to request checking
        }
        
        return matrix;
    }

    private File getTempFile(String namespace, int organismId, int networkId) {
        String dirName = getUserTempDirname(namespace);
        File dir = new File(dirName);
        if (!dir.isDirectory()) {
            dir.mkdir();
        }

        String fileName = dirName + File.separator + String.format("%d.%d.txt", organismId, networkId);
        return new File(fileName);
    }

    private String getTempDirname() {
        return tempDirname;
    }

    private String getUserTempDirname(String namespace) {
        return getTempDirname() + File.separator + namespace;
    }

    /*
     * load up interactions from temp user file on disk and convert to matrix form
     */
    SymMatrix convertToMatrixRepresentation(long organismId, long networkId, String namespace, ProgressReporter progress, boolean isBinary) throws ApplicationException {

        INetworkSymMatrixProvider provider = new FileNetworkSymMatrixProvider(organismId, getUserTempDirname(namespace), cache.getNodeIds(organismId), isBinary);
        SymMatrix matrix = provider.getNetworkMatrix(networkId, progress);
        matrix.compact();
        return matrix;

    }



    /*
     * write the given matrix into cache
     */
    void saveNetwork(UploadNetworkEngineRequestDto request, SymMatrix network) throws ApplicationException {
        cache.initNamespace(request.getNamespace(), request.getOrganismId());
        Network networkObj = new Network(request.getNamespace(), request.getOrganismId(), request.getNetworkId());
        networkObj.setData(network);
        cache.putNetwork(networkObj);
    }

    /*
     * run through any additional computations to
     * facilitate/optimize future use of the given network
     */
    void precompute(UploadNetworkEngineRequestDto request, SymMatrix network, ProgressReporter progress) throws ApplicationException {
        UserDataPrecomputer precomputer = new UserDataPrecomputer(request.getNamespace(), (int) request.getOrganismId(), cache, request.getProgressReporter());
        precomputer.addNetwork((int) request.getNetworkId(), network);
    }

    /*
     * compute # of interactions and other parameters describing the network
     *
     * notice we aren't explicitly excluding zeros here, so if given a sparse matrix
     * they won't be included, but if given a dense matrix with zero elements, they
     * will be included when computing stats
     *
     * TODO: this is a bit inefficient, as the cursor will visit symmetric elements.
     */
    static UploadNetworkEngineResponseDto computeStats(SymMatrix network) {

        UploadNetworkEngineResponseDto response = new UploadNetworkEngineResponseDto();

        int numInteractions = 0;
        double minVal = Double.POSITIVE_INFINITY;
        double maxVal = Double.NEGATIVE_INFINITY;

        MatrixCursor cursor = network.cursor();
        while (cursor.next()) {
            double val = cursor.val();
            if (val == 0d) {
                continue; // ignore explicitly stored zeros
            }

            numInteractions += 1;

            if (val > maxVal) {
                maxVal = val;
            }

            if (val < minVal) {
                minVal = val;
            }
        }

        numInteractions = numInteractions/2; // ignore symmetrics

        response.setNumInteractions(numInteractions);
        response.setMinValue(minVal);
        response.setMaxValue(maxVal);

        return response;
    }

    /*
     * synthesize a mapping file that contains nodeId<tab>nodeId in
     * each record, to use as a mapping for profileToNetworkDriver
     * for input files that have had identifier mappings already applied
     *
     */
    Reader makeIdMapping(long organismId) throws ApplicationException {
        long [] mapping = cache.getNodeIds(organismId).getNodeIds();

        if (mapping == null) {
            throw new ApplicationException("Failed to read node mapping from cache");
        }

        StringBuilder builder = new StringBuilder();
        for (int i=0; i<mapping.length; i++) {
            builder.append(String.format("%s\t%s\n", mapping[i], mapping[i]));
        }

        Reader reader = new StringReader(builder.toString());
        return reader;
    }
}
