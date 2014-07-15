
<div id="stats" class="line">
	Indexing 
	<fmt:formatNumber type="number" value="${stats.networks}" /> 
	
	association networks containing
	<fmt:formatNumber type="number" value="${stats.interactions}" />
	
	interactions mapped to
	<fmt:formatNumber type="number" value="${stats.genes}" />
	genes
	
	<br />and
	<fmt:formatNumber type="number" value="${stats.attributeGroups}" />  
	attribute groups
	
	containing
	<fmt:formatNumber type="number" value="${stats.attributes}" /> 
	attributes
	
	from
	<fmt:formatNumber type="number" value="${stats.organisms}" /> 
	organisms.       
</div>

<div id="page_links" class="line">
	<%@ include file="pageLinks.jsp" %>
</div>

<div id="copyright" class="line">
	&#169; <a href="http://www.utoronto.ca" target="_blank"><spring:message code="footer.link.uoft"/></a> <%= (new java.util.Date().getYear() + 1900) %>
</div>
