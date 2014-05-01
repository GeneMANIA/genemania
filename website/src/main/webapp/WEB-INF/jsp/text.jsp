<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %><%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %><%@ page contentType="text/plain" %><% response.setHeader("Content-Disposition", "attachment"); response.setHeader("charset", "UTF-8"); %># Organism: ${params.organism.name}
# Application version: ${version}
# Database version: ${dbversion}
# Network generated on: <%= new java.text.SimpleDateFormat("d MMMMM yyyy").format(new java.util.Date()) %>
# Author: GeneMANIA (genemania.org)
# Notes: Network weight reflects the data source relevance for predicting the function of interest
Entity 1	Entity 2	Weight	Network group	Networks
<c:forEach items="${vis.data.edges}" var="edge">${edge.sourceName}	${edge.targetName}	${edge.weight}	${edge.networkGroupName}	${edge.networkNames}
</c:forEach>