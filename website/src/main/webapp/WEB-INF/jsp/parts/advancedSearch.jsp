<div id="advanced_line" class="line">
	<c:if test="${!empty results}">
		<div id="results_count_notification" tooltip="<spring:message code="advanced_options.results_count_notification.tooltip"/>">
			
			<c:set var="nAttributes" value="${0}" />
			<c:forEach var="attrGr" items="${results.resultAttributeGroups}">
				<c:forEach var="attr" items="${attrGr.resultAttributes}">
					<c:set var="nAttributes" value="${nAttributes + 1}" />
				</c:forEach>
			</c:forEach>
			
			<spring:message code='advanced_options.results_count_notification' arguments="${params.resultsSize},${fn:length(results.resultGenes)},${nAttributes},${fn:length(vis.data.edges)}"/>
		</div>
	</c:if>

    <div id="advanced_options_open" class="hidden over_flash">

        <div class="line advanced_options_toggle">
            <a class="action_link" href="#" tabindex="4"><spring:message code='advanced_options.hide_message'/></a>
        </div>
        
        <div id="advanced_bg">
	        <div id="advanced_options" class="inset light_section ui-corner-bottom">
	            <div class="line">
	                <div id="networks_section_loading">
	                        <div class="icon"></div>
	                        <div class="message"><spring:message code="search_networks.loading_message"/></div>
	                    </div>
	                    
	                 <fieldset id="networks_section">
	                    <legend><spring:message code='search_networks.title'/><span id="networks_section_error"></span></legend>
	                    <table id="network_toggle" cellspacing="0" cellpadding="0">
	                        <tr>
	                            <td width="100%">
	                                <spring:message code='search_networks.controls.enable'/> <a class="action_link" href="#" id="network_selection_select_all"><spring:message code='search_networks.controls.all'/></a>, <a class="action_link" href="#" id="network_selection_select_none"><spring:message code='search_networks.controls.none'/></a>, <a class="action_link" href="#" id="network_selection_select_default"><spring:message code='search_networks.controls.default'/></a>
	                                &#160;<span class="text">(<span id="totalSelectedNetworksCount"></span> <spring:message code='search_networks.controls.of'/> <span id="totalNetworksCount"></span> <spring:message code='search_networks.controls.currently_enabled'/>)</span>
	                                <br/>
	                                <spring:message code='search_networks.controls.sort_by'/> <a class="action_link sort" href="#" by="first_author"><spring:message code='search_networks.controls.first_author'/></a>, <a class="action_link sort" href="#" by="last_author"><spring:message code='search_networks.controls.last_author'/></a>, <a class="action_link sort" href="#" by="date"><spring:message code='search_networks.controls.date'/></a>, <a class="action_link sort" href="#" by="interactions"><spring:message code='search_networks.controls.size'/></a>
	                            </td>
	                            <td>
	                                <a href="#" id="uploadHelpBtn" class="action_link"><spring:message code="search_networks.controls.upload_help"/></a>
	                            </td>
	                            <td>
	                            	<div id="uploadArea">
		                            	<input type="button" id="uploadBtn" class="widget" value="<spring:message code="search_networks.controls.upload_button"/>"/>
		                                <div id="uploadOverlay"></div>
		                                <div id="uploadDisabler"></div>
	                            	</div>
	                            </td>
	                        </tr>
	                    </table>
	                    <div id="network_list">
	                        <div id="networkTree">
	                            <table cellspacing="0" cellpadding="0"><tr>
	                            <td class="col1">
	                                <div id="groupsPanel">
	                                </div>
	                            </td>
	                            <td class="col2">
	                                <div id="networksPanel">
	                                </div>
	                            </td>
	                            </tr></table>
	                        </div>
	                    </div>
	                </fieldset>
	               
	            </div>

	            <div class="line">
	                <fieldset>
	                    <legend><spring:message code='weighting.title'/></legend>
	                    
	                   	<div class="network_weighting_group">
							
							<p><spring:message code="weighting.category.QUERY_DEPENDENT" /></p>
							
								<div class="network_weighting_radios">
							
			                   		<div class="network_weighting" tooltip="<spring:message code="weighting.tooltip.AUTOMATIC_SELECT"/>">
				                    	<input id="weighting_AUTOMATIC_SELECT" name="weighting" type="radio" value="AUTOMATIC_SELECT" <c:if test="${empty params.weighting || params.weighting == 'AUTOMATIC_SELECT'}">checked</c:if> />
					                	<label for="weighting_AUTOMATIC_SELECT"><spring:message code="weighting.method.AUTOMATIC_SELECT"/></label>
			                   		</div>
			                   		
			                   		<div class="network_weighting" tooltip="<spring:message code="weighting.tooltip.AUTOMATIC"/>">
				                    	<input id="weighting_AUTOMATIC" name="weighting" type="radio" value="AUTOMATIC" <c:if test="${params.weighting == 'AUTOMATIC'}">checked</c:if> />
					                	<label for="weighting_AUTOMATIC"><spring:message code="weighting.method.AUTOMATIC"/></label>
			                   		</div>
			                   		
		                   		</div>
		                   		
						</div>
						
	                   	<div class="network_weighting_group">
							
							<p><spring:message code="weighting.category.GO" /></p>
							
								<div class="network_weighting_radios">
							
			                   		<div class="network_weighting" tooltip="<spring:message code="weighting.tooltip.BP"/>">
				                    	<input id="weighting_BP" name="weighting" type="radio" value="BP" <c:if test="${params.weighting == 'BP'}">checked</c:if> />
					                	<label for="weighting_BP"><spring:message code="weighting.method.BP"/></label>
			                   		</div>
			                   		
			                   		<div class="network_weighting" tooltip="<spring:message code="weighting.tooltip.MF"/>">
				                    	<input id="weighting_MF" name="weighting" type="radio" value="MF" <c:if test="${params.weighting == 'MF'}">checked</c:if> />
					                	<label for="weighting_MF"><spring:message code="weighting.method.MF"/></label>
			                   		</div>
			                   		
			                   		<div class="network_weighting" tooltip="<spring:message code="weighting.tooltip.CC"/>">
				                    	<input id="weighting_CC" name="weighting" type="radio" value="CC" <c:if test="${params.weighting == 'CC'}">checked</c:if> />
					                	<label for="weighting_CC"><spring:message code="weighting.method.CC"/></label>
			                   		</div>
			                   		
		                   		</div>
		                   		
						</div>
						
						<div class="network_weighting_group">
							
							<p><spring:message code="weighting.category.EQUAL" /></p>
							
								<div class="network_weighting_radios">
							
			                   		<div class="network_weighting" tooltip="<spring:message code="weighting.tooltip.AVERAGE"/>">
				                    	<input id="weighting_AVERAGE" name="weighting" type="radio" value="AVERAGE" <c:if test="${params.weighting == 'AVERAGE'}">checked</c:if> />
					                	<label for="weighting_AVERAGE"><spring:message code="weighting.method.AVERAGE"/></label>
			                   		</div>
			                   		
			                   		<div class="network_weighting" tooltip="<spring:message code="weighting.tooltip.AVERAGE_CATEGORY"/>">
				                    	<input id="weighting_AVERAGE_CATEGORY" name="weighting" type="radio" value="AVERAGE_CATEGORY" <c:if test="${params.weighting == 'AVERAGE_CATEGORY'}">checked</c:if> />
					                	<label for="weighting_AVERAGE_CATEGORY"><spring:message code="weighting.method.AVERAGE_CATEGORY"/></label>
			                   		</div>
		                   		
		                   		</div>
		                   		
						</div>
	                    
	                </fieldset>
	            </div>
	
	            <div class="line">
	                <fieldset>
	                    <legend><spring:message code='results_number.title'/></legend>
	                    <div>
	                        <spring:message code='results.number.phrase.start'/>
	                        <select id="threshold" name="threshold" size="1">
	                        	<c:forEach var="th" items="0,10,20,50,100" varStatus="iteration">
	                        		<c:choose>
		                        		<c:when test="${ (empty params.resultsSize && th == 20) || (params.resultsSize == th) }">
		                        			<option value="${th}" selected="selected">${th}</option>
		                        		</c:when>
		                        		<c:otherwise>
		                        			<option value="${th}">${th}</option>
		                        		</c:otherwise>
	                        		</c:choose>
	                        	</c:forEach>
	                        </select>
	                        
	                        <spring:message code='results.number.phrase.middle'/>
	                        
	                        <select id="attrthreshold" name="attrThreshold" size="1">
	                        	<c:forEach var="th" items="0,10,20,50,100" varStatus="iteration">
	                        		<c:choose>
		                        		<c:when test="${ (empty params.attributeResultsSize && th == 20) || (params.attributeResultsSize == th) }">
		                        			<option value="${th}" selected="selected">${th}</option>
		                        		</c:when>
		                        		<c:otherwise>
		                        			<option value="${th}">${th}</option>
		                        		</c:otherwise>
	                        		</c:choose>
	                        	</c:forEach>
	                        </select>
	                        
	                        <spring:message code='results.number.phrase.end'/>
	                    </div>
	                </fieldset>
	            </div>
	        </div>
        </div>
    </div>
    <div id="advanced_options_closed">
        <div class="advanced_options_toggle light_section">
            <a class="action_link" href="#" tooltip="<spring:message code='advanced_options.show_message.tooltip'/>" tabindex="4"><spring:message code="advanced_options.show_message"/></a>
        </div>
    </div>
</div>
<%@ include file="networkUploadHelp.jsp" %>
