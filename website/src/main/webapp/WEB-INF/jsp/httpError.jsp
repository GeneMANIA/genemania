<%-- THIS PAGE WILL NOT WORK UNLESS DEPLOYED TO A SERVER AT THE ROOT DIR (http://host/) --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html contextpath="${pageContext.request.contextPath}" webversion="${version}" dbversion="${dbversion}">
	<head>
		<%@ include file="parts/errorHead.jsp" %>
	</head>
	<body id="error_page" class="error_${type}">
		<div id="page">
			<a href="${pageContext.request.contextPath}"><span id="logo_big"></span></a>
			<div class="message" id="info_content">
				<h1>Something is wrong here</h1>

				<c:if test="${type == '400'}">
				    <p>The page requested had invalid parameter values passed to it, so the page can not be displayed.</p>
				    <p></p>You're probably trying to use the deep linking feature, so take a look at the 
				    <a href="http://pages.genemania.org/help/">help documentation</a>
				    to see how to properly form the parameters.  Please note you need at least one valid gene name in order
				    for your search to be processed.</p>
				</c:if>

				<c:if test="${type == '404'}">
				    <p>The page at the current address does not exist.  Please check that you have the address right.</p>
				</c:if>
				
				<c:if test="${type == '405'}">
				    <p>The request type for this page is incorrect.</p>
				</c:if>
				
				<c:if test="${type == '500'}">
				    <p>An error occurred such that we are unable to display the page.</p>
				</c:if>
				
				<p>
					<a href="${pageContext.request.contextPath}/"><input class="widget" type="button" value="Go back to the search page"/></a>
				</p>
			</div>
		</div>
		
		<%@ include file="parts/debugInfo.jsp" %>
	</body>
</html>