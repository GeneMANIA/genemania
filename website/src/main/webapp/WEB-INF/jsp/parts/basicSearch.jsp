<div id="posted_networks">
	<c:forEach items="${params.networks}" var="network">
	    <input type="checkbox" name="networks" value="${network.id}" checked="true" />
	</c:forEach>
	
	<c:forEach items="${params.attributeGroups}" var="attrGr">
	    <input type="checkbox" name="attrgroups" value="${attrGr.id}" checked="true" />
	</c:forEach>
</div>

<div class="line">
    <div class="phrase">
        <spring:message code="phrase.find_genes_in"/>
    </div>
    <div class="phrase" id="species_selection">
        <div id="species_open" class="hidden floating">
            <select id="species_select"
            name="organism" class="widget" size="${fn:length(organisms)}">
                <c:forEach items="${organisms}" var="org">
                    <option value="${org.id}" alias="${org.alias} (${org.description})" <c:if test="${( empty params.organism && defaultOrganism.id == org.id) || params.organism.id == org.id}"> selected="selected" </c:if> defgenes="<c:forEach var='gene' items='${org.defaultGenes}' varStatus="iteration">${gene.symbol}<c:if test="${!iteration.last}"><% out.print("\n"); %></c:if></c:forEach>">${org.name} (${org.description})</option>
                </c:forEach>
            </select>
        </div>
        <div id="species_closed">
            <input id="species_text" type="text" class="widget" autocomplete="off"
            <c:choose>
		      <c:when test="${empty params.organism}">value="${defaultOrganism.name} (${defaultOrganism.description})"</c:when>
		      <c:otherwise>value="${params.organism.name} (${params.organism.description})"</c:otherwise>
		    </c:choose>
            tooltip="<spring:message code='phrase.species_select.tooltip'/>" tabindex="1"/>
            <div class="widget_description"><spring:message code="phrase.species_instructions"/></div>
        </div>
    </div>

    <div class="phrase">
        <div>
            <input id="species_drop_down_closed" class="widget" type="button" value="&#9660;" tabindex="-1" />
        </div>
        <div>
            <input id="species_drop_down_open" class="widget hidden over_flash" type="button" value="&#9650;" tabindex="-1" />
        </div>
    </div>
    
    <div class="phrase">
        <spring:message code="phrase.related_to"/>
    </div>

    <div class="phrase" id="gene_selection">
        <div id="gene_error" class="error_msg">&nbsp;</div>
        <div id="gene_open" class="hidden floating over_flash">
            <div id="gene_validation_icons">
            	<c:forEach var="i" begin="0" end="499" step="1" varStatus ="status">
					<div class="icon" type="empty" line="${i}">
						<div class="image"></div>
					</div>
				</c:forEach>
            </div>
            <textarea  autocomplete="off" id="gene_area" name="genes" class="widget" tabindex="3" wrap="off">${geneLines}</textarea>
            <div class="instructions">
            	<div class="loading_icon"></div>
            	<span><spring:message code='phrase.gene_instructions_start'/><a href="#" class="action_link default_genes_link open"><spring:message code='phrase.gene_instructions_example'/></a><spring:message code='phrase.gene_instructions_end'/></span>
            	<div id="gene_count" tooltip="<spring:message code='phrase.gene_count.tooltip'/>"></div>
            </div>
        </div>
        
        <div id="gene_closed">
            <input autocomplete="off" type="text" id="gene_text" class="widget" tooltip="<spring:message code='phrase.genes.tooltip'/>" tabindex="2"/>
            <div class="widget_description"><spring:message code='phrase.gene_instructions_start'/><a href="#" class="action_link default_genes_link closed"><spring:message code='phrase.gene_instructions_example'/></a><spring:message code='phrase.gene_instructions_end'/></div>
        	<div class="loading_icon"></div>
        </div>
    </div>

   <div
    <c:choose>
     <c:when test="${empty params.organism}">organism="${defaultOrganism.id}"</c:when>
     <c:otherwise>organism="${params.organism.id}"</c:otherwise>
   </c:choose> 
   id="selected_networks">
   		<c:forEach items="${params.networks}" var="network">
            <span network="${network.id}"></span>
        </c:forEach>
        <c:forEach items="${params.attributeGroups}" var="attrGroup">
            <span attrgroup="${attrGroup.id}"></span>
        </c:forEach>
   </div>
    
</div>

