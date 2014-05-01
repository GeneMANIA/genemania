<%-- Export all networks as HTML --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<% response.setHeader("charset", "UTF-8"); %>
<c:forEach items="${organisms}" var="organism">
	<h2>${organism.name} (${organism.description})</h2>
	
	<c:forEach items="${organism.interactionNetworkGroups}" var="group">
		<h3>${group.name}</h3>
		
		<c:forEach items="${group.interactionNetworks}" var="network">
			<p><strong>${network.name}</strong></p>
			
			<div class="indented">
				<%@ include file="parts/network.jsp" %>
			</div>
		</c:forEach>
		
	</c:forEach>
	
</c:forEach>