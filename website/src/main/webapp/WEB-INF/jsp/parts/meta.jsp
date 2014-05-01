<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    
<c:choose>
 <c:when test="${title != null && title != ''}">
 	<title>GeneMANIA &raquo; ${title}</title>
 </c:when>
 <c:otherwise>
 	<title>GeneMANIA</title>
 </c:otherwise>
</c:choose>

<link rel="shortcut icon" href="<%=request.getContextPath()%>/img/favicon.ico" />