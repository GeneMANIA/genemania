<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="targetBlank" value="true" />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html contextpath="${pageContext.request.contextPath}" webversion="${version}" dbversion="${dbversion}">
	<head>
		<meta name="description" content="GeneMANIA helps you predict the function of your favourite genes and gene sets." />
		<%@ include file="parts/searchHead.jsp" %>
	</head>
	<body id="search_page">
	    <div id="page">
	    
	    	<div id="bg"></div>
	    
	        <div id="query_area">
		        <%@ include file="parts/searchHeader.jsp" %>
		         
		        <form id="relatedGenes" method="post" action="${pageContext.request.contextPath}/">
				    <div id="phrase_and_go" class="line">
				        <%@ include file="parts/form.jsp" %>
				    </div>
				</form>
				
		        <%@ include file="parts/searchFooter.jsp" %>
	        </div>
	    </div>
	    <%@ include file="parts/debugInfo.jsp" %>
	    <%@ include file="parts/exceptions.jsp" %>
	    <%@ include file="parts/notifications.jsp" %>
	</body>
</html>