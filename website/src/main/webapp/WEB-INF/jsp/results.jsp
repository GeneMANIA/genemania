<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="title">
	search for <c:forEach items="${params.genes}" var="gene" varStatus="iteration">${gene.symbol}<c:if test="${!iteration.last}">, </c:if></c:forEach> in ${params.organism.name}
</c:set>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html contextpath="${pageContext.request.contextPath}" webversion="${version}" dbversion="${dbversion}">
	<head>
		<meta name="description" content="Gene function search results for <c:forEach items="${params.genes}" var="gene" varStatus="iteration">${gene.symbol}<c:if test="${!iteration.last}">, </c:if></c:forEach> in ${params.organism.name}" />
		<%@ include file="parts/resultsHead.jsp" %>
	</head>
	
	<body id="results_page">
		<script type="text/javascript">
		    $(function(){
		        progress_struct.ms_to_jquery = (new Date()).getTime() - progress_struct.start_ms;
		           progress("jquery");
		       });
		</script>
	
		<div id="progress">
		    <div id="progress_logo"></div>
		
		    <div class="inner">
	            <div id="progress_title">
	                Loading GeneMANIA results<span id="progress_dots">&nbsp;</span>
	            </div>
	            
	            <div id="progress_bar">
	                <div id="progress_colour" style="width: 0%;">
	                </div>
	            </div>
	            
	            <div id="progress_status">
		            <script type="text/javascript">
		            <!--
		                document.write( progress_struct.help_msg[ Math.round( Math.random() * (progress_struct.help_msg.length - 1) ) ] );
		            -->
		            </script>
		        </div>
	        </div>
	        
	        
	    </div>
	    
	    <div id="reloader">
	    	<div class="box">
		    	<div class="icon"></div>
		    	<div class="message">Submitting your search...</div>
	    	</div>
	    </div>
	    
	    <div id="cytoweb_error">
	    	<div class="box">
	    		<p>We are unable to draw your results.  We are being notified of the problem, and we will address
	    		it as soon as possible.  Thank you for your understanding.</p>
	    		
	    		<p class="sending">Sending details to the GeneMANIA team...</p>
	    		
	    		<p class="sent">Sent details to the GeneMANIA team</p>
	    		
	    		<p class="error">Could not send details to the GeneMANIA team; please send an email to 
	    		<a href="mailto:genemania.mailer@gmail.com">the GeneMANIA team</a> detailing your query</p>
	    	</div>
	    </div>
	    
		<%@ include file="parts/resultsHeader.jsp" %>
		<%@ include file="parts/vis.jsp" %>
		<%@ include file="parts/sideBar.jsp" %>
		<%@ include file="parts/resultsFooter.jsp" %>
		<%@ include file="parts/exportForms.jsp" %>
		<%@ include file="parts/debugInfo.jsp" %>
	 	<%@ include file="parts/notifications.jsp" %>
			
	</body>
</html>