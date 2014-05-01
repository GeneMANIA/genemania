<p>${rAttr.attribute.description}</p>

<p>
	<strong><spring:message code='networks_tab.source_title'/></strong>&nbsp;
	${rAttrGroup.attributeGroup.description}
	<spring:message code='networks_tab.from'/>
	<a target="_blank" class="external_link" href="${rAttrGroup.attributeGroup.publicationUrl}">${rAttrGroup.attributeGroup.publicationName}</a>
</p>

<p class="linkout">
	<strong><spring:message code="genes_tab.link_title"/></strong>&nbsp;
	<c:forEach items="${rAttr.links}" var="link" varStatus="iteration">
		<a href="${link.url}" class="external_link" target="_blank">${link.name}</a><c:if test="${ !iteration.last }">, </c:if>
	</c:forEach>
</p>