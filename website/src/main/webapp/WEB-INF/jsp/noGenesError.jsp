<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html contextpath="${pageContext.request.contextPath}" webversion="${version}" dbversion="${dbversion}">
	<head>
		<%@ include file="parts/errorHead.jsp" %>
	</head>
	<body id="error_page">
		<div id="page">
			<a href="${pageContext.request.contextPath}"><span id="logo_big"></span></a>
			<div class="message" id="info_content">
					<h1>No valid genes specified</h1>
				
					<p>You did not specify any valid gene names in your search.  Please try again, ensuring that your gene list
					is validated correctly.  A valid gene name has a checkmark beside its name.</p>
					
					<p>
						<a href="${pageContext.request.contextPath}"><button id="back_button">Go back to the search page</button></a>
					</p>
					
					<div class="error_details" exception="${exception}">
						<%@ include file="parts/debugProperties.jsp" %>
						
						<label>Stack trace</label>
						<pre>${exception}
--
<c:forEach items="${exception.stackTrace}" var="frame">${frame}
</c:forEach></pre>
					</div>
					
			</div>
		</div>
		
	</body>
</html>