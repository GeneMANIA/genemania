<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %><%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %><%@ page contentType="text/plain" %><% response.setHeader("Content-Disposition", "attachment"); response.setHeader("charset", "UTF-8"); %>Organism	${params.organism.name} (${params.organism.description})

Genes	<c:forEach items="${params.genes}" var="gene" varStatus="iteration">${gene.symbol}<c:if test="${!iteration.last}">	</c:if></c:forEach>

Networks
<c:forEach items="${networkGroups}" var="networkGroup">${networkGroup.name}	<c:forEach items="${networkGroup.networks}" var="network">${network}<c:if test="${!iteration.last}">	</c:if></c:forEach><c:if test="${!iteration.last}">
</c:if></c:forEach>
Network weighting	<spring:message code="weighting.method.${params.weighting}"/> <c:if test="${results.weighting != params.weighting}"> (<spring:message code="weighting.method.${results.weighting}"/>)</c:if>
Network weighting code	${params.weighting} <c:if test="${results.weighting != params.weighting}"> (${results.weighting})</c:if>

Attribute groups	<c:forEach items="${attributeGroups}" var="gr" varStatus="iteration">"${gr.name}"<c:if test="${!iteration.last}">	</c:if></c:forEach>

Number of gene results	${params.resultsSize}

Number of attribute results	${params.attributeResultsSize}

Version	${dbversionstd}