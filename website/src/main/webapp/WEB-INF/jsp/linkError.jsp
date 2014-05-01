<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html contextpath="${pageContext.request.contextPath}"  webversion="${version}" dbversion="${dbversion}">
	<head>
		<%@ include file="parts/errorHead.jsp" %>
	</head>
	<body id="error_page">
		<div id="page">
			<a href="${pageContext.request.contextPath}"><span id="logo_big"></span></a>
			<div class="message" id="info_content">
				<h1>We think you may have made a mistake</h1>
			
				<p>The deep linking feature requires that the parameters that you pass to it are valid.  If they are not
				valid, then GeneMANIA can not give you any results:</p>
				
				<p></p>
			
				<c:if test="${error == 'weighting'}">
					<p>The weighting method specified must be valid.  You specified <strong>"${m}"</strong> as a weighting method, which is
					unrecognised.  Please see the 
					<a href="http://pages.genemania.org/help/linking-to-genemania/">help section</a> for more information.</p>
				</c:if>
			
				<c:if test="${error == 'organism'}">
					<p>The organism specified must be valid.  You specified <strong>"${o}"</strong> as an organism, which is
					unrecognised.  Please see the 
					<a href="http://pages.genemania.org/help/linking-to-genemania/">help section</a> for more information.</p>
				</c:if>
				
				<c:if test="${error == 'genes'}">
					<p>The genes specified must be valid. You specified <strong>"${g}"</strong> as your pipe separated gene list, and no
					gene symbols in that list were recognised by GeneMANIA. Please see the 
					<a href="http://pages.genemania.org/help/linking-to-genemania/">help section</a> for more information.</p>
				</c:if>
				
				<c:if test="${error == 'threshold'}">
					<p>The number of result genes specified must be valid.  You specified <strong>"${r}"</strong> as the number of
					result genes that GeneMANIA should give you.  The value <strong>"${r}"</strong> was not recognised as a number.
					 Please see the 
					<a href="http://pages.genemania.org/help/linking-to-genemania/">help section</a> for more information.</p>
				</c:if>
			</div>
		</div>
		
		<%@ include file="parts/debugInfo.jsp" %>
		
	</body>
</html>