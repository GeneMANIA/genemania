<table id="go_table">
    <thead>
        <tr>
        	<th class="annotation"><label><spring:message code="go_tab.annotation_title"/></label></th>
        	<th class="pval" tooltip="<spring:message code="go_tab.q_val.tooltip"/>"><label><spring:message code="go_tab.q_val_title"/></label></th>
        	<th class="coverage" tooltip="<spring:message code="go_tab.coverage.tooltip"/>"><label><spring:message code="go_tab.coverage_title"/></label></th>
        </tr>
    </thead>
    <tbody>
        <tr class="query" ocid="-1">
            <td class="annotation" value="<spring:message code="go_tab.query_genes.description"/>" ocid="-1"><spring:message code="go_tab.query_genes.name"/></td>
            <td class="pval" value="0" tooltip="<spring:message code="go_tab.na.q_val.tooltip"/>"><spring:message code="go_tab.na.q_val"/></td>
            <td class="coverage" value="${fn:length(params.genes)}" tooltip="<spring:message code="go_tab.coverage.tooltip"/>">
            	${fn:length(params.genes)} / ${fn:length(params.genes)}
            </td>
        </tr> 
        
        <c:forEach items="${results.resultOntologyCategories}" var="rOCat">
            <tr class="go" ocid="${rOCat.ontologyCategory.id}">
                <td class="annotation" name="${rOCat.ontologyCategory.description}" value="${rOCat.ontologyCategory.description}" ocid="${rOCat.ontologyCategory.id}">
                    ${rOCat.ontologyCategory.description}
                </td>
                <td class="pval" tooltip="<spring:message code="go_tab.q_val.tooltip"/>" value="${rOCat.qValue}">
                    <fmt:formatNumber value="${rOCat.qValue}" pattern="#.##E0" />
                </td>
                <td class="coverage" tooltip="<spring:message code="go_tab.coverage.tooltip"/>" value="${rOCat.numAnnotatedInSample}">
                	${rOCat.numAnnotatedInSample} / ${rOCat.numAnnotatedInTotal}
                </td>
            </tr>
        </c:forEach>
        
        
        <%--
        We don't want attributes here right now
        
        <c:forEach items="${results.resultAttributeGroups}" var="rAttrGr">
        	<c:forEach items="${rAttrGr.resultAttributes}" var="rAttr">
	        	<tr class="attribute" attrid="${rAttr.attribute.id}" ocid="attr-${rAttr.attribute.id}">
		            <td class="annotation" value="${rAttr.attribute.name}" attrid="${rAttr.attribute.id}" ocid="attr-${rAttr.attribute.id}">${rAttr.attribute.name}</td>
		            <td class="pval" value="0" tooltip="<spring:message code="go_tab.na.q_val.tooltip"/>"><spring:message code="go_tab.na.q_val"/></td>
		            <td class="coverage" value="${rAttr.numAnnotatedInSample}" tooltip="<spring:message code="go_tab.coverage.tooltip"/>">
		            	${rAttr.numAnnotatedInSample} / ${rAttr.numAnnotatedInTotal}
		            </td>
		        </tr> 
	        </c:forEach>
        </c:forEach>
        
        --%>
        
    </tbody>
</table>