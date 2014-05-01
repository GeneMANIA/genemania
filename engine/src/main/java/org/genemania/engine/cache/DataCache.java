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

package org.genemania.engine.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import no.uib.cipr.matrix.DenseVector;
import org.apache.log4j.Logger;
import org.genemania.engine.Constants;
import org.genemania.engine.Constants.DataFileNames;
import org.genemania.engine.Constants.NetworkType;
import org.genemania.engine.core.data.AttributeGroups;
import org.genemania.engine.core.data.AttributeData;
import org.genemania.engine.core.data.CategoryIds;
import org.genemania.engine.core.data.CoAnnotationSet;
import org.genemania.engine.core.data.CombinedNetwork;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.DataSupport;
import org.genemania.engine.core.data.DatasetInfo;
import org.genemania.engine.core.data.FeatureTargetCorrelation;
import org.genemania.engine.core.data.GoAnnotations;
import org.genemania.engine.core.data.GoCoAnnotationCounts;
import org.genemania.engine.core.data.GoIds;
import org.genemania.engine.core.data.KtK;
import org.genemania.engine.core.data.KtKFeatures;
import org.genemania.engine.core.data.KtT;
import org.genemania.engine.core.data.Network;
import org.genemania.engine.core.data.NetworkIds;
import org.genemania.engine.core.data.NodeDegrees;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.engine.core.integration.Feature;
import org.genemania.engine.core.integration.FeatureList;
import org.genemania.engine.matricks.Matrix;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.exception.ApplicationException;

/**
 * read/write core data objects.
 *
 * TODO: come up with a more generic scheme with less custom code per object type
 */
public class DataCache {

    private static Logger logger = Logger.getLogger(DataCache.class);
    private IObjectCache cache;

    public String getCacheDir() throws ApplicationException {
        return cache.getCacheDir();
    }

    public DataCache(IObjectCache cache) {
        this.cache = cache;
    }

    /*
     * notice we don't allow writing core networks into a user's namespace,
     * this is probably an error elsewhere.
     */
    public void putNetwork(Network network) throws ApplicationException {
        if (!Data.CORE.equals(network.getNamespace()) && network.getId() >= 0) {
            throw new ApplicationException("can not write core network into user namespace");
        }
        cache.put(network.getKey(), network.getData(), DataSupport.isVolatile(network));
    }

    /*
     * we always retrieve core networks from the core namespace, even if called
     * with a user namespace
     */
    public Network getNetwork(String namespace, long organismId, long networkId) throws ApplicationException {
        Network network;
        if (!Data.CORE.equals(namespace) && networkId >= 0) {
            network = new Network(Data.CORE, organismId, networkId);
        }
        else {
            network = new Network(namespace, organismId, networkId);
        }
        SymMatrix data = (SymMatrix) cache.get(network.getKey(), DataSupport.isVolatile(network));
        network.setData(data);
        return network;
    }

    public NodeIds getNodeIds(long organismId) throws ApplicationException {
        NodeIds nodeIds = new NodeIds(organismId);
        nodeIds.setOrganismId(organismId);
        long [] data = (long[]) cache.get(nodeIds.getKey(), DataSupport.isVolatile(nodeIds));
        nodeIds.setNodeIds(data);
        return nodeIds;
    }

    public void putNodeIds(NodeIds nodeIds) throws ApplicationException {
        cache.put(nodeIds.getKey(), nodeIds.getNodeIds(), DataSupport.isVolatile(nodeIds));
    }

    public NetworkIds getNetworkIds(String namespace, long organismId) throws ApplicationException {
        NetworkIds networkIds = new NetworkIds(namespace, organismId);
        long [] data = (long[]) cache.get(networkIds.getKey(), DataSupport.isVolatile(networkIds));
        networkIds.setNetworkIds(data);
        return networkIds;
    }

    public void putNetworkIds(NetworkIds networkIds) throws ApplicationException {
        cache.put(networkIds.getKey(), networkIds.getNetworkIds(), DataSupport.isVolatile(networkIds));
    }

    public GoIds getGoIds(long organismId, String goBranch) throws ApplicationException {
        GoIds goIds = new GoIds(organismId, goBranch);
        String [] ids = (String []) cache.get(goIds.getKey(), DataSupport.isVolatile(goIds));
        goIds.setGoIds(ids);
        return goIds;        
    }

    public void putGoIds(GoIds goIds) throws ApplicationException {
        cache.put(goIds.getKey(), goIds.getGoIds(), DataSupport.isVolatile(goIds));
    }

    public CategoryIds getCategoryIds(long organismId, long ontologyId) throws ApplicationException {
        CategoryIds categoryIds = new CategoryIds(organismId, ontologyId);
        long [] ids = (long []) cache.get(categoryIds.getKey(), DataSupport.isVolatile(categoryIds));
        categoryIds.setCategoryIds(ids);
        return categoryIds;
    }

    public void putCategoryIds(CategoryIds categoryIds) throws ApplicationException {
        cache.put(categoryIds.getKey(), categoryIds.getCategoryIds(), DataSupport.isVolatile(categoryIds));
    }
    
    public GoCoAnnotationCounts getGoCoAnnotationCounts(long organismId, String goBranch) throws ApplicationException {
        GoCoAnnotationCounts annoCounts = new GoCoAnnotationCounts(organismId, goBranch);
        SymMatrix data = (SymMatrix) cache.get(annoCounts.getKey(), DataSupport.isVolatile(annoCounts));
        annoCounts.setData(data);
        return annoCounts;
    }

    public void putGoCoAnnotationCounts(GoCoAnnotationCounts annoCounts) throws ApplicationException {
        cache.put(annoCounts.getKey(), annoCounts.getData(), DataSupport.isVolatile(annoCounts));
    }

    public GoAnnotations getGoAnnotations(long organismId, String goBranch) throws ApplicationException {
        GoAnnotations annos = new GoAnnotations(organismId, goBranch);
        Matrix data = (Matrix) cache.get(annos.getKey(), DataSupport.isVolatile(annos));
        annos.setData(data);
        return annos;
    }

    public void putGoAnnotations(GoAnnotations annos) throws ApplicationException {
        cache.put(annos.getKey(), annos.getData(), DataSupport.isVolatile(annos));
    }

    public void putCoAnnotationSet(CoAnnotationSet annoSet) throws ApplicationException {
        putData(annoSet);
    }

    public CoAnnotationSet getCoAnnotationSet(long organismId, String goBranch) throws ApplicationException {
        CoAnnotationSet annoSet = new CoAnnotationSet(organismId, goBranch);
        return (CoAnnotationSet) cache.get(annoSet.getKey(), DataSupport.isVolatile(annoSet));
    }

    public void putCombinedNetwork(CombinedNetwork combined) throws ApplicationException {
        cache.put(combined.getKey(), combined, DataSupport.isVolatile(combined));
    }

    public CombinedNetwork getCombinedNetwork(String namespace, long organismId, String methodParamKey) throws ApplicationException {
        CombinedNetwork combined = new CombinedNetwork(namespace, organismId, methodParamKey);
        combined = (CombinedNetwork) cache.get(combined.getKey(), DataSupport.isVolatile(combined));
        combined.fix(); // backward compatibility
        return combined;
    }

    public void putKtK(KtK ktk) throws ApplicationException {
        putData(ktk);
    }

    public KtK getKtK(String namespace, long organismId, String goBranch) throws ApplicationException {
        KtK ktk = new KtK(namespace, organismId, goBranch);
        return (KtK) cache.get(ktk.getKey(), DataSupport.isVolatile(ktk));

    }

    public void putKtT(KtT ktt) throws ApplicationException {
        putData(ktt);
    }

    public KtT getKtT(String namespace, long organismId, String goBranch) throws ApplicationException {
        KtT ktt = new KtT(namespace, organismId, goBranch);
        return (KtT) cache.get(ktt.getKey(), DataSupport.isVolatile(ktt));
    }

    public void putDatasetInfo(DatasetInfo info) throws ApplicationException {
        putData(info);
    }

    public DatasetInfo getDatasetInfo(long organismId) throws ApplicationException {
        DatasetInfo info = new DatasetInfo(organismId);
        return (DatasetInfo) cache.get(info.getKey(), DataSupport.isVolatile(info));
    }

    public void removeOrganism(String namespace, long organismId) throws ApplicationException {
        cache.remove(Data.getOrganismKey(namespace, organismId));
    }

    public void removeNamespace(String namespace) throws ApplicationException {
        cache.remove(Data.getNamespaceKey(namespace));
    }

    public NodeDegrees getNodeDegrees(String namespace, long organismId) throws ApplicationException {
        NodeDegrees nodeDegrees = new NodeDegrees(namespace, organismId);
        DenseVector degrees = (DenseVector) cache.get(nodeDegrees.getKey(), DataSupport.isVolatile(nodeDegrees));
        nodeDegrees.setDegrees(degrees);
        return nodeDegrees;
    }

    public void putNodeDegrees(NodeDegrees nodeDegrees) throws ApplicationException {
        cache.put(nodeDegrees.getKey(), nodeDegrees.getDegrees(), DataSupport.isVolatile(nodeDegrees));
    }
    
    /*
     * load attribute groups from given namespace, without automatic fallback 
     */
    public AttributeGroups getAttributeGroups(String namespace, long organismId) throws ApplicationException {
        AttributeGroups attributeGroups = new AttributeGroups(namespace, organismId);
        HashMap<Long, ArrayList<Long>> data = (HashMap<Long, ArrayList<Long>>) cache.get(attributeGroups.getKey(), DataSupport.isVolatile(attributeGroups));
        attributeGroups.setAttributeGroups(data);
        return attributeGroups;        
    }
    
    public void putAttributeGroups(AttributeGroups attributeGroups) throws ApplicationException {
        cache.put(attributeGroups.getKey(), attributeGroups.getAttributeGroups(), DataSupport.isVolatile(attributeGroups));
    }
    
    public AttributeData getAttributeData(String namespace, long organismId, long attributeGroupId) throws ApplicationException {
        namespace = checkNamespace(namespace, attributeGroupId);

        AttributeData attributeData = new AttributeData(namespace, organismId, attributeGroupId);
        Matrix data = (Matrix) cache.get(attributeData.getKey(), DataSupport.isVolatile(attributeData));
        attributeData.setData(data);
        
        return attributeData;
    }
    
    public void putAttributeData(AttributeData attributeData) throws ApplicationException {
        cache.put(attributeData.getKey(), attributeData.getData(), DataSupport.isVolatile(attributeData));
    }
    
    public FeatureTargetCorrelation getFeatureAttributeCorrelation(String namespace, long organismId, long attributeGroupId) throws ApplicationException {
        return (FeatureTargetCorrelation) getData(new FeatureTargetCorrelation(namespace, organismId, attributeGroupId));       
    }
    
    /*
     * Note the compatibility kludge here, we previously assumed a structure for KtK based
     * on the list of networks, but now use an explicit mapping between the rows/cols of KtK and
     * features of various types (networks, attributes).
     * 
     * On an older data set, if the new structure isn't found we build using the conventions of the
     * implicit system. 
     */
    public KtKFeatures getKtKFeatures(String namespace, long organismId) throws ApplicationException {
        KtKFeatures ktkFeatures = null;
        if (!namespace.equals(Data.CORE)) {
//          logger.warn("no support for user-uploaded attributes, using CORE set only.");
            namespace = Data.CORE;
        }
        
        try {
            ktkFeatures = (KtKFeatures) getData(new KtKFeatures(namespace, organismId));
        }
        catch (ApplicationException e) {
            ktkFeatures = buildImplicitKtKFeatures(namespace, organismId);
        }
        return ktkFeatures;
    }
    
    /*
     * for compatibility for data cache's that didn't include this structure 
     */
    private KtKFeatures buildImplicitKtKFeatures(String namespace,
            long organismId) throws ApplicationException {
        FeatureList featureList = new FeatureList();
        featureList.addBias();
 
        if (!namespace.equals(Data.CORE)) {
//          logger.warn("no support for user-uploaded attributes, using CORE set only.");
            namespace = Data.CORE;
        }
        
        NetworkIds networkIds = getNetworkIds(namespace, organismId);
        
        long [] ids = networkIds.getNetworkIds();
        for (int i=0; i<ids.length; i++) {
            long networkId = ids[i];
            Feature feature = new Feature(NetworkType.SPARSE_MATRIX, Feature.FAKE_SPARSE_NETWORK_GROUP_ID, networkId);
            featureList.add(feature);
        }
        
        KtKFeatures ktkFeatures = new KtKFeatures(namespace, organismId);
        ktkFeatures.setFeatures(featureList);
        return ktkFeatures;
    }
    
    /*
     * minimal data structures for a new namespace over an existing organism
     */
    public void initNamespace(String namespace, long organismId) throws ApplicationException {
        
        if (namespace == null || namespace.equals("")) {
            throw new ApplicationException("no namespace given");
        }
        
        // we don't yet do anything special for core ...
        if (namespace.equals(Data.CORE)) {
            return;
        }
        
        // there's no validation or fixing up of broken namespaces, if
        // the namespace entry exists, we assume all is good
        if (namespaceExists(namespace, organismId)) {
            return;
        }
        
        // setup a new namespace by copying any core objects needed, these
        // are bookkeeping objects that need to be extended without
        // any other user seeing
        
        // network ids
        NetworkIds coreNetworkIds = getNetworkIds(Data.CORE, organismId);
        NetworkIds networkIds = coreNetworkIds.copy(namespace);
        putNetworkIds(networkIds);
        
        // KtK, KtT
        boolean ok = false;
        KtK userKtK = null;
        KtT[] userKtT = new KtT[Constants.goBranches.length];
        try {
            KtK coreUserKtK = getKtK(Data.CORE, organismId, DataFileNames.KtK_BASIC.getCode());
            userKtK = coreUserKtK.copy(namespace);
          
            for (int branch = 0; branch < Constants.goBranches.length; branch++) {
                KtT coreUserKtT = getKtT(Data.CORE, organismId, Constants.goBranches[branch]);
                userKtT[branch] = coreUserKtT.copy(namespace);
            }
            ok = true;
        }
        catch (Exception e) {
            logger.debug("failed to load core KtK/KtT", e);
        }
        
        if (ok) {
            putKtK(userKtK);
            for (int branch = 0; branch < Constants.goBranches.length; branch++) {
                putKtT(userKtT[branch]);
            }
        }
        
        // AttributeGroups. need a deep copy, there's probably an easier way.
        // note the compat hack, if core doesn't have attributes groups, then
        // user doesn't need them either. but really we should init to empty
        // structures.
        AttributeGroups coreAttributeGroups = null;
        ok = false;
        try {
            coreAttributeGroups = getAttributeGroups(Data.CORE, organismId);
            ok = true;
        }
        catch (Exception e) {
            logger.debug("failed to load core attribute groups", e);
        }
        
        if (ok) {
            AttributeGroups attributeGroups = new AttributeGroups(namespace, organismId);
            HashMap<Long, ArrayList<Long>> groupIds= new HashMap<Long, ArrayList<Long>>();

            for (Entry<Long, ArrayList<Long>> entry: coreAttributeGroups.getAttributeGroups().entrySet()) {
                ArrayList<Long> value = new ArrayList<Long>();
                value.addAll(entry.getValue());
                groupIds.put(entry.getKey(), value);
            }
            attributeGroups.setAttributeGroups(groupIds);
            putAttributeGroups(attributeGroups);
        }
    }
    
    /*
     * can't just do cache.exists(key) where key = [namespace], because
     * that api only supports file objects (oops). our namespaces currently
     * always must have network objects, so workaround by checking that that
     * exists.
     */
    private boolean namespaceExists(String namespace, long organismId) throws ApplicationException {
        
        boolean exists = false;
        try {
            NetworkIds networkIds = getNetworkIds(namespace, organismId);
            exists = true;
        }
        catch (ApplicationException e) {
            // will return false
        }
        
        return exists;
    }

    /*
     * generic put
     */
    public void putData(Data object) throws ApplicationException {
        cache.put(object.getKey(), object, DataSupport.isVolatile(object));
    }

    /*
     * generic remove
     */
    public void removeData(Data object) throws ApplicationException {
        cache.remove(object.getKey());
    }

    /*
     * generic get
     */
    public Data getData(Data object) throws ApplicationException {
        return (Data) cache.get(object.getKey(), DataSupport.isVolatile(object));
    }
    
    /*
     * namespaces help split objects *within* a given organism between 'user'
     * and 'core' types.  motivation is to split an organism into multiple 
     * independent user views, where each user can update their view of the 
     * organism by adding e.g. networks & attributes without polluting
     * the core organism itself and colliding with each other's data.
     * 
     * the id's of objects that can be loaded into the user portion of a 
     * namespace (networks, attributes) are by convention < 0.
     * 
     * things get messy & confusing with organism ids, since certain parts
     * of an organism only ever exist in the core namespace (oops), e.g.
     * node-ids. 
     * 
     * this helper uses our conventions to determine the namespace an object
     * should be found. The object can be a network or attribute id.
     * 
     */
    private static String checkNamespace(final String namespace, final long objectId) throws ApplicationException {
        
        // if negative id, by convention this is a user object
        if (objectId < 0) {
            if (Data.CORE.equals(namespace)) {
                throw new ApplicationException("user objects not allowed in core namespace");
            }
            else {
                return namespace;
            }
        }
        // core object, always get from core namespace even if request is in user namespace 
        else {
            return Data.CORE;
        }
    }    
}
