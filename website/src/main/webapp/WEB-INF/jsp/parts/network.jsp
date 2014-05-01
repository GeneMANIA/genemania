<c:if test="${ !empty network.metadata.authors }">
	<p class="ref">
		<a target="_blank" class="external_link" href="${network.metadata.url}">${network.metadata.title}</a>
		${fn:substringBefore(network.metadata.authors, ",")} <spring:message code='networks_tab.et_al'/>.
		(${network.metadata.yearPublished}).
		<span class="publication">${network.metadata.publicationName}</span>.
	</p>
</c:if>

<c:if test="${ !empty network.metadata.comment && network.id > 0 }">
	<p>
		<strong><spring:message code='networks_tab.note_title'/></strong>&nbsp;
		${network.metadata.comment}
	</p>
</c:if>

<p>
	<strong><spring:message code='networks_tab.source_title'/></strong>&nbsp;
	<c:if test="${ !empty network.metadata.processingDescription }"><a target="_blank" href="http://pages.genemania.org/help/#GeneMANIA_network_categories">${network.metadata.processingDescription}</a></c:if>
	<c:if test="${ !empty network.metadata.processingDescription && !empty network.metadata.interactionCount }"><spring:message code='networks_tab.with'/></c:if>
	<c:if test="${ !empty network.metadata.interactionCount }"><fmt:formatNumber pattern="###,###,###,###" value="${network.metadata.interactionCount}"/> <spring:message code='networks_tab.interactions'/></c:if>
	
	<c:if test="${ !empty network.metadata.source && network.id > 0 }">
		<c:if test="${ !empty network.metadata.sourceUrl }"><spring:message code='networks_tab.from'/> <a target="_blank" class="external_link" href="${network.metadata.sourceUrl}"><spring:message code='network_source.${fn:toUpperCase(network.metadata.source)}'/></a></c:if>
		<c:if test="${ empty network.metadata.sourceUrl }"><spring:message code='networks_tab.from'/> <spring:message code='network_source.${fn:toUpperCase(network.metadata.source)}'/></c:if>
	</c:if>

	<c:if test="${ !empty network.metadata.comment && network.id < 0 }"><spring:message code='networks_tab.from'/> ${network.metadata.comment}</c:if>
</p>

<c:if test="${ !empty network.tags }">
<p>
	<strong><spring:message code='networks_tab.tags_title'/></strong>&nbsp;
	<span class="tags">
		<c:forEach items="${network.tags}" var="tag" varStatus="iteration">
			${fn:toLowerCase(tag.name)}<c:if test="${!iteration.last}">;&nbsp;</c:if>
		</c:forEach>
	</span>
</p>
</c:if>

<c:if test="${ !empty network.metadata.invalidInteractions && !empty network.metadata.invalidInteractions }">
	<strong><spring:message code='search_networks.info.invalid_interactions_title'/></strong>&nbsp;
	<c:forEach items="${network.metadata.invalidInteractions}" var="interaction" varStatus="iteration">
		${interaction}<c:if test="${!iteration.last}">;&#160;</c:if>
	</c:forEach>
</c:if>