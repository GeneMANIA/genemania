<%-- This is a template for static pages. --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:redirect url="http://pages.genemania.org/${page_id}" />
<!-- Redirect to the new CMS -->

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html contextpath="${pageContext.request.contextPath}" webversion="${version}" dbversion="${dbversion}">
	<head>
		<%@ include file="parts/pageHead.jsp" %>
	</head>
	<body>
		<div id="page">
	        <div id="info_page">
				<%@ include file="parts/pageHeader.jsp" %>
	            <div id="info_content" class="line">
	            	<jsp:include page="pages/${page_id}.jsp" />
	            </div>
	          	<%@ include file="parts/pageFooter.jsp" %>
			</div>
		</div>
	</body>
</html>