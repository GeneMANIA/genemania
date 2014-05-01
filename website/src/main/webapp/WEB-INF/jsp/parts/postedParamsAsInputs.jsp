<input type="hidden" name="organism" value="${params.organism.id}" />
<input type="hidden" name="genes" value="<c:forEach items="${params.genes}" var="gene" varStatus="iteration">${gene.symbol}<c:if test="${!iteration.last}"><% out.print("\n"); %></c:if></c:forEach>" />
<input type="hidden" name="weighting" value="${params.weighting}" />
<input type="hidden" name="threshold" value="${params.resultsSize}" />
<input type="hidden" name="attrThreshold" value="${params.attributeResultsSize}" />
<c:forEach items="${params.networks}" var="network">
    <input type="checkbox" name="networks" value="${network.id}" checked="true" />
</c:forEach>
<c:forEach items="${params.attributeGroups}" var="group">
    <input type="checkbox" name="attrgroups" value="${group.id}" checked="true" />
</c:forEach>