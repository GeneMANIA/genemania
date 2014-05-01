package org.genemania.engine.actions;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.genemania.dto.AttributeDto;
import org.genemania.dto.InteractionVisitor;
import org.genemania.dto.NetworkCombinationRequestDto;
import org.genemania.dto.NetworkCombinationResponseDto;
import org.genemania.dto.NetworkDto;
import org.genemania.engine.cache.RandomDataCacheBuilder;
import org.genemania.engine.cache.RandomDataCacheConfig;
import org.genemania.engine.core.data.AttributeData;
import org.genemania.engine.core.data.AttributeGroups;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.Network;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.engine.matricks.Vector;
import org.genemania.exception.ApplicationException;
import org.genemania.util.NullProgressReporter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CombineNetworksTest {
    static RandomDataCacheBuilder cacheBuilder;
    RandomDataCacheConfig config = RandomDataCacheConfig.getStandardConfig2();
        
    @Before
    public void setUp() throws Exception {
        cacheBuilder = new RandomDataCacheBuilder(7132);
        cacheBuilder.setUp();

        // random organism 1
        cacheBuilder.addOrganism(config);
    }

    @After
    public void tearDown() throws Exception {
        cacheBuilder.tearDown();
    }

    @Test
    public void testProcess() throws ApplicationException {
 
        System.out.println("testing");
        NetworkCombinationRequestDto request = new NetworkCombinationRequestDto();
 
        String namespace = Data.CORE;
        int N = config.getOrg1numNetworks() + config.getNumAttributeGroups()*config.getNumAttributesPerGroup();
        double weight = 1d/N;
        
        List<NetworkDto> networks = getNetworks(namespace, config.getOrg1Id(), weight);
        Collection<AttributeDto> attributes = getAttributes(namespace, config.getOrg1Id(), weight);
        
        // work out a test value
        int i=1, j=2;
        NodeIds nodeIds = cacheBuilder.getCache().getNodeIds(config.getOrg1Id());
        final long ni = nodeIds.getIdForIndex(i);
        final long nj = nodeIds.getIdForIndex(j);
        
        final double expected_i_j = calculateExpected(namespace, config.getOrg1Id(), networks, attributes, i, j, weight);
        final int[] found = new int[1];
        
        // build and process request
        request.setNamespace(namespace);
        request.setOrganismId(config.getOrg1Id());
        request.setNetworks(networks);
        request.setAttributes(attributes);
        request.setInteractionVistor(new InteractionVisitor() {
            @Override
            public void visit(long node1, long node2, double weight) {
//                System.out.println(String.format("%d %d %s", node1, node2, weight));            
              
                // diagonals are zero
                if (node1 == node2) {
                    fail("should not get diagonal elements");
                }
                
                // check some values
                if (node1 == ni && node2 == nj) {
                    fail("should only get sub-diagonal elements of symmetric pair");
                }
                else if  (node1 == nj && node2 == ni) {
                    assertEquals(expected_i_j, weight, 1e-7);
                    found[0]++;                    
                }
            }
        });
        request.setProgressReporter(NullProgressReporter.instance());

        CombineNetworks action = new CombineNetworks(cacheBuilder.getCache(), request);
        NetworkCombinationResponseDto result = action.process();
        assertNotNull(result);
        assertEquals("expected values not in result", 1, found[0]);
    }

    /*
     * simple loop
     */
    private double calculateExpected(String namespace, long organismId, List<NetworkDto> networks, Collection<AttributeDto> attributes, int i, int j, double weight) throws ApplicationException {

        double val = 0;
        
        for (NetworkDto network: networks) {
            Network n = cacheBuilder.getCache().getNetwork(namespace, organismId, network.getId());
            val += n.getData().get(i, j) * network.getWeight();
        }

        AttributeGroups ag = cacheBuilder.getCache().getAttributeGroups(namespace, organismId);
        for (AttributeDto attribute: attributes) {
            AttributeData a = cacheBuilder.getCache().getAttributeData(namespace, organismId, attribute.getGroupId());
            int index = ag.getIndexForAttributeId(attribute.getGroupId(), attribute.getId());
           
            Vector sums = a.getData().columnSums();  // don't need to recompute this for every attribute, but its just a test
            val += a.getData().get(i, index) * a.getData().get(j, index) * attribute.getWeight() / (sums.get(index) - 1);
            
        }
        
        return val;
    }

    private Collection<AttributeDto> getAttributes(String namespace, long organismId, double weight) throws ApplicationException {
        Collection<AttributeDto> attributes = new ArrayList<AttributeDto>();
        
        AttributeGroups attributeGroups = cacheBuilder.getCache().getAttributeGroups(namespace, organismId);
        
        for (long groupId: attributeGroups.getAttributeGroups().keySet()) {
            ArrayList<Long> attributesForGroup = attributeGroups.getAttributeGroups().get(groupId);
            for (long id: attributesForGroup) {
                AttributeDto dto = new AttributeDto();
                dto.setGroupId(groupId);
                dto.setId(id);
                dto.setWeight(weight);
                
                attributes.add(dto);
            }
        }
        
        return attributes;
    }

    private List<NetworkDto> getNetworks(String namespace, long organismId, double weight) throws ApplicationException {
        List<NetworkDto> networks = new ArrayList<NetworkDto>();
        
        for (long id: config.getOrg1NetworkIds()) {
            NetworkDto dto = new NetworkDto();
            dto.setId(id);
            dto.setWeight(weight);
            networks.add(dto);
        }
        
        return networks;
    }

}
