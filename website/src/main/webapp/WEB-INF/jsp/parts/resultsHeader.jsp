<div id="header">
    <div id="query_line" class="line">
		<a href="${pageContext.request.contextPath}/"><div id="logo" class="slice" tooltip="<spring:message code="logo.results.tooltip"/>"></div></a>
		<form id="relatedGenes" method="post" action="${pageContext.request.contextPath}/">
	        <%@ include file="form.jsp" %>
		</form>
    </div>
</div>