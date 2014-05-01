<div id="export_forms">
	
	<form id="resubmit_form" method="post" action="${pageContext.request.contextPath}/">
		<%@ include file="postedParamsAsInputs.jsp" %>
	</form>
	
	<form id="text_form" method="post" action="${pageContext.request.contextPath}/export/text/genemania_network.txt">
		<%@ include file="postedParamsAsInputs.jsp" %>
	</form>
	
	<form id="params_form" method="post" action="${pageContext.request.contextPath}/export/params/search_parameters.txt">
		<%@ include file="postedParamsAsInputs.jsp" %>
	</form>
	
	<form id="params_json_form" method="post" action="${pageContext.request.contextPath}/export/params_json/search_parameters.json">
		<%@ include file="postedParamsAsInputs.jsp" %>
	</form>
	
	<form id="networks_form" method="post" action="${pageContext.request.contextPath}/export/networks/networks_list.txt">
		<%@ include file="postedParamsAsInputs.jsp" %>
	</form>
	
	<form id="genes_form" method="post" action="${pageContext.request.contextPath}/export/genes/genes_list.txt">
		<%@ include file="postedParamsAsInputs.jsp" %>
	</form>
	
	<form id="go_form" method="post" action="${pageContext.request.contextPath}/export/go/functions_list.txt">
		<%@ include file="postedParamsAsInputs.jsp" %>
	</form>
	
	<form id="interactions_form" method="post" action="${pageContext.request.contextPath}/export/interactions/interactions_list.txt">
		<%@ include file="postedParamsAsInputs.jsp" %>
	</form>
	
	<form id="print_form" method="post" action="${pageContext.request.contextPath}/print" target="_blank">
		<%@ include file="postedParamsAsInputs.jsp" %>
	</form>
	
   	<form id="svg_form" method="post" action="${pageContext.request.contextPath}/file/genemania_network.svg">
   		<input type='hidden' name='content' value='${svg}' />
   		<input type="hidden" name="type" value="image/svg+xml" />
   		<input type="hidden" name="disposition" value="attachment" />
   	</form>
   	
   	<form id="attributes_form" method="post" action="${pageContext.request.contextPath}/export/attributes/attributes_list.txt" target="_blank">
		<%@ include file="postedParamsAsInputs.jsp" %>
	</form>

</div>