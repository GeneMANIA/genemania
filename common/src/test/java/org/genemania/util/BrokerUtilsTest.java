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

/**
 * BrokerUtilsTest: JUnit test class for BrokerUtils
 * Created Aug 06, 2009
 * @author Ovi Comes
 */
package org.genemania.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.genemania.AbstractTest;
import org.genemania.dto.InteractionDto;
import org.genemania.dto.NetworkDto;
import org.genemania.dto.RelatedGenesEngineResponseDto;
import org.genemania.dto.RelatedGenesWebResponseDto;
import org.genemania.exception.ApplicationException;
import org.genemania.message.RelatedGenesResponseMessage;
import org.genemania.mock.RelatedGenesResponseMessageMock;
import org.junit.Test;

public class BrokerUtilsTest extends AbstractTest {

	// __[constructors]________________________________________________________
	public BrokerUtilsTest() {
		super();
	}
	
	// __[test cases]__________________________________________________________
	@Test
	public void testMsg2dto() {
		try {
			RelatedGenesResponseMessage msg = RelatedGenesResponseMessageMock.getMockObject(1);
			RelatedGenesWebResponseDto actualDto = BrokerUtils.msg2dto(msg);
			assertNotNull("dto", actualDto);
			assertNotNull("networks", actualDto.getNetworks());
			assertNotNull("network weights", actualDto.getNetworkWeightsMap());
			assertNotNull("node scores", actualDto.getNodeScoresMap());
		} catch (ApplicationException e) {
			fail(e.getMessage());
		}
	}

	public void testDto2msg() {
		RelatedGenesEngineResponseDto dto = new RelatedGenesEngineResponseDto();
		List<NetworkDto> inputNetworks = new ArrayList<NetworkDto>();
		NetworkDto network = new NetworkDto();
		InteractionDto interaction = new InteractionDto();
		network.addInteraction(interaction);
		inputNetworks.add(network);
		dto.setNetworks(inputNetworks);
		RelatedGenesResponseMessage msg = BrokerUtils.dto2msg(dto);
		assertNotNull("RelatedGenesResponseMessage should not be null", msg);
		assertEquals("error code", 0, msg.getErrorCode());
		Collection<NetworkDto> outputNetworks = msg.getNetworks();
		assertNotNull("output networks", outputNetworks);
		assertEquals("output networks size", 1, outputNetworks.size());
		NetworkDto firstNetwork = outputNetworks.iterator().next();
		assertNotNull("first network", firstNetwork);
		Collection<InteractionDto> interactions = firstNetwork.getInteractions();
		assertNotNull("interactions", interactions);
		assertEquals("interactions size", 1, interactions.size());
		InteractionDto firstInteraction = interactions.iterator().next();
		assertNotNull("first interaction", firstInteraction);
	}
	
//	public void testBuildEnrichmentRequestFrom() {
//		try {
//			RelatedGenesEngineRequestDto rgRequestDto = new RelatedGenesEngineRequestDto(); 
//			
//			RelatedGenesEngineResponseDto rgResponseDto = new RelatedGenesEngineResponseDto();
//			List<NetworkVO> inputNetworks = new ArrayList<NetworkVO>();
//			NetworkVO network = new NetworkVO();
//			InteractionVO interaction = new InteractionVO();
//			network.addInteraction(interaction);
//			inputNetworks.add(network);
//			rgResponseDto.setNetworks(inputNetworks);
//			
//			EnrichmentEngineRequestDto eRequestDto = BrokerUtils.buildEnrichmentRequestFrom(rgRequestDto, rgResponseDto);
//			
//			assertNotNull("enrichment request", eRequestDto);
//		} catch (ApplicationException e) {
//			fail(e.getMessage());
//		}
//		
//	}
	
}
