<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %><%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %><%@ page contentType="text/plain" %><% response.setHeader("Content-Disposition", "attachment"); response.setHeader("charset", "UTF-8"); %>{
	"version": "${dbversionstd}",
	"organism": "${params.organism.name}",
	"genes": [
		<c:forEach items="${params.genes}" var="gene" varStatus="iteration">"${gene.symbol}"<c:if test="${!iteration.last}">,
		</c:if></c:forEach>
	],
	"networks": {
		<c:forEach items="${networkGroups}" var="networkGroup" varStatus="iterationGroup">"${networkGroup.name}": [
			<c:forEach items="${networkGroup.networks}" var="network"  varStatus="iterationNetwork">"${network}"<c:if test="${!iterationNetwork.last}">,
			</c:if></c:forEach>
		]<c:if test="${!iterationGroup.last}">,</c:if>
		</c:forEach>
	},
	"attributeGroups": [
		<c:forEach items="${attributeGroups}" var="gr" varStatus="iteration">"${gr.name}"<c:if test="${!iteration.last}">,
		</c:if></c:forEach>
	],
	"selectedWeighting": "${fn:toLowerCase(params.weighting)}",
	"usedWeighting": "${fn:toLowerCase(results.weighting)}",
	"numberOfResultGenes": ${params.resultsSize},
	"numberOfResultAttributes": ${params.attributeResultsSize}
}