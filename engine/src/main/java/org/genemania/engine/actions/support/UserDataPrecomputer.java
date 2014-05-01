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

import no.uib.cipr.matrix.DenseMatrix;
import org.apache.log4j.Logger;
import org.genemania.engine.Constants;
import org.genemania.engine.Constants.DataFileNames;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.MatrixUtils;
import org.genemania.engine.core.data.CoAnnotationSet;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.DatasetInfo;
import org.genemania.engine.core.data.KtK;
import org.genemania.engine.core.data.KtT;
import org.genemania.engine.core.data.NetworkIds;
import org.genemania.engine.core.integration.gram.BasicGramBuilder;
import org.genemania.engine.exception.CancellationException;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.exception.ApplicationException;
import org.genemania.util.ProgressReporter;

/**
 *
 * take care of the calculations required to update
 * a users precomputed data set with a new interaction network.
 *
 * note this class doesn't handle creation or storage of the new
 * network itself, it takes care of updating the gram matrix and
 * related column mapping for the user data.
 *
 */
public class UserDataPrecomputer {

    private static Logger logger = Logger.getLogger(UserDataPrecomputer.class);
    String namespace;
    int organismId;
    DataCache cache;
    KtK userKtK;
    KtT[] userKtT = new KtT[Constants.goBranches.length];
    NetworkIds networkIds;
    DatasetInfo info;
    ProgressReporter progress;
    boolean hasPrecomputedData; // for user-added custom organisms, make these optional since we may not have go annotations and so don't need these structures

    public UserDataPrecomputer(String namespace, int organismId,
            DataCache cache, ProgressReporter progress) {
        this.namespace = namespace;
        this.organismId = organismId;
        this.cache = cache;
        this.progress = progress;
    }

    /*
     * is it okay to add a network with the given id?
     */
    protected void checkNetwork(long id) throws ApplicationException {

        // really a user network?
        if (id >= 0) {
            throw new ApplicationException("not a valid id for a user network: " + id);
        }

        if (networkIds.containsId(id)) {
            throw new ApplicationException("a network with the id " + id + " already exists in the users data set");
        }
    }
    
    /*
     * load user data set, or fall back to initializing a new one
     * if it doesn't already exist
     */
    public void load() throws ApplicationException {      
        try {
            info = cache.getDatasetInfo(organismId);
            networkIds = cache.getNetworkIds(namespace, organismId);
            
            hasPrecomputedData = loadPrecomputed();
        }
        catch (ApplicationException e) {
            // init fallback
            loadInit();
        }
    }
    
    /*
     * TODO could revise to do an explicit test for existence, for now just
     * trap exception.  
     */
    private boolean loadPrecomputed() {
        boolean exists = false;
        try {
            userKtK = cache.getKtK(namespace, organismId, DataFileNames.KtK_BASIC.getCode());
            for (int branch = 0; branch < Constants.goBranches.length; branch++) {
                userKtT[branch] = cache.getKtT(namespace, organismId, Constants.goBranches[branch]);
            }
            exists = true;
        }
        catch (ApplicationException e) {
            logger.debug("precomputed data structures don't appear to exist");            
        }
        
        return exists;
    }

    /*
     * get an initially empty precomputed data set
     */
    public void loadInit() throws ApplicationException {
        NetworkIds coreNetworkIds = cache.getNetworkIds(Data.CORE, organismId);
        networkIds = coreNetworkIds.copy(namespace);

        hasPrecomputedData = loadInitPrecomputed();
    }
    
    /*
     * see note with loadPrecomputed(),
     */
    public boolean loadInitPrecomputed() {

        boolean exists = false;
        try {
            KtK coreUserKtK = cache.getKtK(Data.CORE, organismId, DataFileNames.KtK_BASIC.getCode());
            userKtK = coreUserKtK.copy(namespace);

            for (int branch = 0; branch < Constants.goBranches.length; branch++) {
                KtT coreUserKtT = cache.getKtT(Data.CORE, organismId, Constants.goBranches[branch]);
                userKtT[branch] = coreUserKtT.copy(namespace);
            }
            exists = true;            
        }
        catch (ApplicationException e) {
            logger.debug("precomputed data structures don't appear to exist");            
        }
        
        return exists;
    }

    /*
     * write the data back out, overwriting previous versions.
     *
     * TODO: note there is no recovery here yet for if we fail in
     * say the second step, the data on disk will be inconsistent!
     */
    public void save() throws ApplicationException {
        cache.putNetworkIds(networkIds);

        if (hasPrecomputedData) {
            cache.putKtK(userKtK);
            for (int branch = 0; branch < Constants.goBranches.length; branch++) {
                cache.putKtT(userKtT[branch]);
            }
        }
    }

    /*
     * update the user data structures with the given network, allocating
     * space, performing computations, and saving the results
     *
     */
    public void addNetwork(long networkId, SymMatrix m) throws ApplicationException {

        progress.setStatus(Constants.PROGRESS_UPLOAD_PRECOMPUTING_MESSAGE);
        progress.setProgress(Constants.PROGRESS_UPLOAD_PRECOMPUTING);

        logger.debug("loading precomputed data structures");     
        load();
        
        checkNetwork(networkId);

        // update data structures with storage
        // for new data, and fill in precomputed values
        networkIds.addNetwork(networkId);

        /*
         * if we don't have precomputed data, don't need to do anything, except 
         * update the network ids data structure in the user's namespace
         */
        if (!hasPrecomputedData) {
            logger.debug("skipping precomputation");
        }
        else {        
            logger.debug("updating KtK");
            reallocKtK();
            updateKtK(networkId, m);

            logger.debug("updating KtT");
            reallocKtT();
            updateKtT(networkId, m);

            // we don't abort based on progress.isCancelled() once save() has
            // started
            logger.debug("writing updated precomputed data structures");
        }
        save();
    }


    /*
     * reallocate the user KtK adding an extra row/column
     */
    public void reallocKtK() throws ApplicationException {
        if (userKtK == null) {
            throw new ApplicationException("no precomputed user data available");
        }

        DenseMatrix newKtK = MatrixUtils.copyLarger(userKtK.getData(), 1, 1);
        userKtK.setData(newKtK);
    }

    /*
     * run through any additional computations to
     * facilitate/optimize future use of the given network
     */
    void updateKtK(long networkId, SymMatrix networkData) throws ApplicationException {

        int index = networkIds.getIndexForId(networkId)+1; // +1 because of bias

        double networkSum = networkData.elementSum();
        DenseMatrix userKtKData = userKtK.getData();
        
        userKtKData.set(index, 0, networkSum);
        userKtKData.set(0, index, networkSum);

        // compute element-wise product of user matrix with all the other,
        // matrices, both core and user
        for (int i=0; i<networkIds.getNetworkIds().length; i++) {
            long otherNetworkId = networkIds.getIdForIndex(i);
            int otherIndex = i+1; // +1 because of bias. bet you forgot already

            if (progress.isCanceled()) {
                throw new CancellationException();
            }

            logger.debug("computing product of user network " + networkId + " with network " + otherNetworkId);

            SymMatrix otherNetworkData = cache.getNetwork(namespace, organismId, otherNetworkId).getData();
            //Matrix m = provider.getNetworkMatrix(otherNetworkId, NullProgressReporter.instance());

            // TODO: optimize this by determining the sparser network, and
            // iterating over that first. for we just guess that the user network
            // will typically be very sparse
            double val = networkData.elementMultiplySum(otherNetworkData);
            //logger.debug("product was: " + val);

            userKtKData.set(index, otherIndex, val);
            userKtKData.set(otherIndex, index, val);
        }
    }

    public void reallocKtT() throws ApplicationException {
        if (userKtT == null) {
            throw new ApplicationException("no precomputed data available");
        }

        for (int branch = 0; branch < Constants.goBranches.length; branch++) {
            DenseMatrix userKtTData = userKtT[branch].getData();
            userKtTData = MatrixUtils.copyLarger(userKtTData, 1, 0);
            userKtT[branch].setData(userKtTData);
//            userKtT[branch] = MatrixUtils.copyLarger(userKtT[branch], 1, 0);
        }

    }

    /*
     * update KtT to include target component for the given network
     *
     * this is based on code in FastWeightCacheBuilder, but needs simplification
     * and refactoring TODO.
     */
    public void updateKtT(long networkId, SymMatrix network) throws ApplicationException {
        int index = networkIds.getIndexForId(networkId)+1; // +1 because of bias
        for (int branch = 0; branch < Constants.goBranches.length; branch++) {

            if (progress.isCanceled()) {
                throw new CancellationException();
            }

            CoAnnotationSet annoSet = cache.getCoAnnotationSet(organismId, Constants.goBranches[branch]);
            DenseMatrix KtTData = userKtT[branch].getData();

            SymMatrix coAnnotationMatrix = annoSet.GetCoAnnotationMatrix();
            int numberOfGenes = annoSet.GetCoAnnotationMatrix().numRows();
            double val = BasicGramBuilder.computeKttElement(numberOfGenes, network, coAnnotationMatrix, annoSet.GetBHalf(), annoSet.GetConstant());

            KtTData.set(index, 0, val);
        }
    }

    /*
     * remove the precomputed data structures associated with the
     * given network ... this doesn't remove the network binary itself
     */
    public void removeNetwork(int networkId) throws ApplicationException {
        load();
       
        if (!networkIds.containsId(networkId)) {
            throw new ApplicationException("user does not have network: " + networkId);
        }

        int index = networkIds.getIndexForId(networkId);

        if (hasPrecomputedData) { 
        	for (int branch = 0; branch < Constants.goBranches.length; branch++) {
        		userKtT[branch].removeNetworkAtIndex(index+1); // TODO: really need to fix the indexing so don't need these error prone +1's to skip around the bias column
        	}

        	userKtK.removeNetworkAtIndex(index+1);
        }
        
        networkIds.removeNetwork(networkId);

        save();
    }

    /*
     * clean up a users precomputed data structures associated with
     * an organism. 
     */
    public void removeOrganism() throws ApplicationException {
        cache.removeOrganism(namespace, organismId);
    }
}
