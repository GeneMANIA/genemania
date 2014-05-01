
<div id="stats" class="line">
	<spring:message code="stats.indexing"/> <fmt:formatNumber type="number" value="${stats.networks}" /> <spring:message code="stats.association_networks_containing"/> <fmt:formatNumber type="number" value="${stats.interactions}" /> <spring:message code="stats.interactions_mapped_to"/> <fmt:formatNumber type="number" value="${stats.genes}" /> <spring:message code="stats.genes_from"/> <fmt:formatNumber type="number" value="${stats.organisms}" /> <spring:message code="stats.organisms"/><spring:message code="stats.period"/>       
</div>

<div id="page_links" class="line">
	<%@ include file="pageLinks.jsp" %>
</div>

<div id="copyright" class="line">
	&#169; <a href="http://www.utoronto.ca" target="_blank"><spring:message code="footer.link.uoft"/></a> <%= (new java.util.Date().getYear() + 1900) %>
</div>
