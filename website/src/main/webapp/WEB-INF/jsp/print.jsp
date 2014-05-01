<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html contextpath="${pageContext.request.contextPath}" class="print" webversion="${version}" dbversion="${dbversion}">
	<head>
		<%@ include file="parts/printHead.jsp" %>
	</head>
	<body id="print_page">

	
		<div id="print_area">
			<div class="logo">
				<script type="image/svg+xml">
					<%@ include file="../../img/logo/logo.svg" %>
				</script>
			</div>

			<div class="version_info">
				<p><span class="description">Created on:</span> <%= new java.text.SimpleDateFormat("d MMMMM yyyy HH:mm:ss").format(new java.util.Date()) %></p>
				<p><span class="description">Last database update:</span> ${dbversion}</p>
				<p><span class="description">Application version:</span> ${version}</p>
			</div>
		    
		    <div class="print_selection box">
		    	
		    	<div class="title">Include these sections when printing</div>
		    	
		    	<div class="item"><input section="svg" type="checkbox" checked /><label>Network image</label></div>
		    	<div class="item"><input section="params" type="checkbox" checked /><label>Search parameters</label></div>
		    	<div class="item"><input section="networks" type="checkbox" checked /><label>Networks</label></div>
		    	<div class="item"><input section="attributes" type="checkbox" checked /><label>Attributes</label></div>
		    	<div class="item"><input section="genes" type="checkbox" checked /><label>Genes</label></div>
		    	<div class="item"><input section="go" type="checkbox" checked /><label>Functions</label></div>
		    	<div class="item"><input section="interactions" type="checkbox" checked /><label>Interactions</label></div>
		    	
		    	<div class="button print">
		    		Print
		    	</div>
		    	
		    </div>
		    
		    <div class="print_pdf_instructions box">
		    	<p><span class="ui-icon ui-icon-info"></span> Printing as PDF</p>
		    	<p>
			    	You can save this document as a PDF by using a save-as-PDF option in your print dialog&mdash;as on Mac OS&mdash;or
			    	by using a PDF printer&mdash;as on Windows.
		    	</p>
		    	
		    	<p>
		    		Red and yellow print controls, like this yellow section and the red "Print" button are not included in the
		    		printout.
		    	</p>
		    </div>
		    
		    <h1>Report of GeneMANIA search</h1>
	    
	    	<div class="section" section="svg">
	    	
	    		<h2>Network image</h2>
	    
			    <div class="graph_area">		    	
					<script type="image/svg+xml">
						${svg}
			 		</script>
			    </div>
			    
			    <div class="legend_area">
			    	<div id="transferred_go_legend">
			    		${golegend}
			    	</div>
			    
			    	<div class="legend go">
			    		<div class="title">Functions legend</div>
			    		
			    	</div>
			    	
			    	<div class="legend networks">
			    		<div class="title">Networks legend</div>
			    		${netlegend}
			    	</div>
		    	</div> 
	    	
	    	</div>   
	    	
	    	<div class="info_area">
	    		
	    		<div class="section" section="params">
	    		
		    		<h2>Search parameters</h2>
			    	
			    	<p>
			    		<span class="description">Organism:</span>
			    		${params.organism.name} (${params.organism.description})
			    	</p>
			    	
			    	<p>
			    		<span class="description">Genes:</span>
			    		<c:forEach items="${params.genes}" var="gene" varStatus="iteration">
							${gene.symbol}<c:if test="${!iteration.last}">;</c:if>
						</c:forEach>
			    	</p>
			    	
			    	<div class="input_networks">
			    		<div class="description">Networks:</div>
			    		
			    		
							<div class="group">
								<div class="network_group">Attributes:</div>
								<c:forEach items="${attributeGroups}" var="attrGroup">
									<div class="network">
										${attrGroup.name}
									</div>
								</c:forEach>
							</div>
			    		
			    		<c:forEach items="${networkGroups}" var="networkGroup">
							<div class="group">
								<div class="network_group">${networkGroup.name}:</div>
								<c:forEach items="${networkGroup.networks}" var="network">
									<div class="network">
										${network}
									</div>
								</c:forEach>
							</div>
						</c:forEach>
			    		
			    	</div>
			    	
			    	<p>
			    		<span class="description">Network weighting:</span>
			    		<spring:message code="weighting.method.${params.weighting}"/>
			    		<c:if test="${results.weighting != params.weighting}">
			    			(<spring:message code="weighting.method.${results.weighting}"/>)
			    		</c:if>
			    	</p>
			    	
			    	<p>
			    		<span class="description">Number of gene results:</span>
			    		${params.resultsSize}
			    	</p>
		    	
		    	</div>
  
	    		
	    		<div class="section" section="networks">
		    		<h2>Networks</h2>
					<%@include file="parts/networksList.jsp" %>
				</div>
				
				<div class="section" section="attributes">
					<h2>Attributes</h2>
					<table id="attributes_table">
						<thead>
							<tr>
								<th class="attr">Attribute</th>
								<th class="gene">Gene</th>
							</tr>
						</thead>
						<tbody>
							<c:forEach items="${vis.data.edges}" var="edge">
								<c:if test="${edge != null && edge.attributeId != null}">
									<tr>
										<td class="attr">${edge.sourceName}</td>
										<td class="gene">${edge.targetName}</td>
									</tr>
								</c:if>		
							</c:forEach>
						</tbody>
					</table>
				</div>
				
				<div class="section" section="genes">
					<h2>Genes</h2>
					<%@include file="parts/genesList.jsp" %>
				</div>
				
				<div class="section" section="go">
					<h2>Functions</h2>
					<%@include file="parts/goList.jsp" %>
				</div>

				<div class="section" section="interactions">
					<h2>Interactions</h2>
					<table id="interactions_table">
						<thead>
							<tr>
								<th class="gene1">Gene 1</th>
								<th class="gene2">Gene 2</th>
								<th class="weight">Weight</th>
								<th class="group">Network group</th>
								<th class="networks">Networks</th>
							</tr>
						</thead>
						<tbody>
							<c:forEach items="${vis.data.edges}" var="edge">
								<c:if test="${edge != null && edge.attributeId == null}">
									<tr>
										<td class="gene1">${edge.sourceName}</td>
										<td class="gene2">${edge.targetName}</td>
										<td class="weight">${edge.weight}</td>
										<td class="group">${fn:replace(edge.networkGroupName, " ", "&nbsp;")}</td>
										<td class="networks">
											<c:forEach items="${edge.networkNames}" var="network">
												${network} <br/>
											</c:forEach>
										</td>
									</tr>
								</c:if>		
							</c:forEach>
						</tbody>
					</table>
				</div>
				
	    	</div>	
		    
		    <p class="description eof">Search results generated by the GeneMANIA algorithm (genemania.org)</p>
		    
	    </div>
	    
	    <%@ include file="parts/debugInfo.jsp" %>
	    <%@ include file="parts/exportForms.jsp" %>
	</body>
</html>	