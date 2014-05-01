package org.genemania.engine.actions;

import org.genemania.dto.AttributeDto;
import org.genemania.dto.InteractionVisitor;
import org.genemania.dto.NetworkCombinationRequestDto;
import org.genemania.dto.NetworkCombinationResponseDto;
import org.genemania.dto.NetworkDto;
import org.genemania.engine.Constants.NetworkType;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.engine.core.integration.CombineNetworksOnly;
import org.genemania.engine.core.integration.CombinedKernelBuilder;
import org.genemania.engine.core.integration.Feature;
import org.genemania.engine.core.integration.FeatureWeightMap;
import org.genemania.engine.matricks.MatrixCursor;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.engine.matricks.custom.MultiOPCSymMatrix;
import org.genemania.engine.matricks.custom.OuterProductComboSymMatrix;
import org.genemania.exception.ApplicationException;

public class CombineNetworks {

    DataCache cache;
    NetworkCombinationRequestDto request;
    
    public CombineNetworks(DataCache cache, NetworkCombinationRequestDto request) {
        this.cache = cache;
        this.request = request;
    }
    
    public NetworkCombinationResponseDto process() throws ApplicationException {
        
        SymMatrix combined = combine();
        visit(combined);
        
        return new NetworkCombinationResponseDto();
    }
    
    private SymMatrix combine() throws ApplicationException {
        FeatureWeightMap weights = buildWeightMap();
        
        SymMatrix combined = CombineNetworksOnly.combine(weights, request.getNamespace(),
                request.getOrganismId(), cache, request.getProgressReporter());

        CombinedKernelBuilder builder = new CombinedKernelBuilder(cache);
        combined = builder.build(request.getOrganismId(), request.getNamespace(), 
                combined, weights);
        
        return combined;
    }
    
    /*
     * weight map in format suitable for combining code
     */
    private FeatureWeightMap buildWeightMap() {

        FeatureWeightMap weightMap = new FeatureWeightMap();

        for (NetworkDto network: request.getNetworks()){

            if (network.getWeight() != 0) {
                Feature feature = new Feature(NetworkType.SPARSE_MATRIX, 
                        Feature.FAKE_SPARSE_NETWORK_GROUP_ID, network.getId());
                weightMap.put(feature, network.getWeight());
            }
        }
        
        for (AttributeDto attribute: request.getAttributes()) {
            if (attribute.getWeight() != 0) {
                Feature feature = new Feature(NetworkType.ATTRIBUTE_VECTOR, 
                        attribute.getGroupId(), attribute.getId());
                weightMap.put(feature, attribute.getWeight());
            }            
        }

        return weightMap;
    }
    
    /*
     * its awkward to implement cursor style iteration over the separated
     * data storage for combined networks made from a combination of
     * attributes and regular sparse networks. so specialized handling
     * for that (the usual) case.
     */
    private void visit(SymMatrix combined) throws ApplicationException {       
    
        if (combined instanceof MultiOPCSymMatrix) {
            visitMultiOPCSymMatrix((MultiOPCSymMatrix) combined);
        }
        else {
            visitSymMatrix(combined);
        }
    }
    
    /* 
     * can't iterate directly over the multi-thing matrix. handle by first
     * adding the attribute parts to the flex part, then iterate over that.
     * 
     * this changes the underlying data of the combined matrix (to save storage
     * by not taking a copy), so don't re-use combined after!!
     */
    private void visitMultiOPCSymMatrix(MultiOPCSymMatrix combined) throws ApplicationException {
        OuterProductComboSymMatrix[] combos = combined.getCombos();
        
        SymMatrix matrix = combined.getMatrix();
        
        for (OuterProductComboSymMatrix combo: combos) {
            matrix.add(1, combo);
        }
        
        visitSymMatrix(matrix);
    }
    
    /*
     * visit all subdiagonal interactions in the network
     */
    private void visitSymMatrix(SymMatrix combined) throws ApplicationException {
        InteractionVisitor visitor = request.getInteractionVistor();
        NodeIds nodeIds = cache.getNodeIds(request.getOrganismId());
        
        MatrixCursor cursor = combined.cursor();
        while (cursor.next()) {
            final int row = cursor.row();
            final int col = cursor.col();
            if (row > col) {
                final long node1 = nodeIds.getIdForIndex(row);
                final long node2 = nodeIds.getIdForIndex(col);
                visitor.visit(node1, node2, cursor.val());
            }
        }      
    }
}
