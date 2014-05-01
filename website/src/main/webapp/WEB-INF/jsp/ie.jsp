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
					<h1>A problem with your browser</h1>
				
					<p>You are using a very old version of Internet Explorer.  Unfortunately, Internet Explorer is slower, buggier, and less
					standards-compliant than other browsers.  Because of this, GeneMANIA does not support old versions of Internet Explorer.</p>
					
					<p>However, you can quickly upgrade your browser, and you'll be using GeneMANIA is no time!  We recommend you upgrade
					to an alternative browser than Internet Explorer, though all of the browsers listed below are supported.</p>
					
					<div id="browsers">
						<a href="http://www.google.com/chrome" target="_blank">
							<div class="ui-browserupdate-icon chrome"><span class="ui-browserupdate-name">Chrome</span></div>
						</a>
						
						<a href="http://www.apple.com/safari/download/" target="_blank">
							<div class="ui-browserupdate-icon safari"><span class="ui-browserupdate-name">Safari</span></div>
						</a>
						
						<a href="http://firefox.com" target="_blank">
							<div class="ui-browserupdate-icon firefox"><span class="ui-browserupdate-name">Firefox</span></div>
						</a>
						
						<a href="http://windows.microsoft.com/en-US/internet-explorer/products/ie/home" target="_blank">
							<div class="ui-browserupdate-icon msie"><span class="ui-browserupdate-name">Internet Explorer</span></div>
						</a>
					</div>
					
					
					<div class="error_details" exception="${exception}">
						
					</div>
					
			</div>
		</div>
		
	</body>
</html>