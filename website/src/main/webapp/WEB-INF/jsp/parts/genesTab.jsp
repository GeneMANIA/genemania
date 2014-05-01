<div id="genes_tab" class="tab">
   <div class="header">
        <div>
            <div class="title"><spring:message code="genes_tab.sort_by"/></div> <a class="action_link" href="#" id="genes_tab_sort_by_name"><spring:message code="genes_tab.sort_by.name"/></a>, <a class="action_link" href="#" id="genes_tab_sort_by_score"><spring:message code="genes_tab.sort_by.score"/></a>
        </div>
        <div>
            <div class="title"><spring:message code="genes_tab.expand"/></div> <a class="action_link" href="#" id="genes_tab_expand_all"><spring:message code="genes_tab.expand.all"/></a>, <a class="action_link" href="#" id="genes_tab_expand_none"><spring:message code="genes_tab.expand.none"/></a>
        </div>
        <div>
            <div class="title"><spring:message code="genes_tab.select"/></div> <a class="action_link" href="#" id="genes_tab_select_all"><spring:message code="genes_tab.select.all"/></a>, <a class="action_link" href="#" id="genes_tab_select_query"><spring:message code="genes_tab.select.query"/></a>, <a class="action_link active" href="#" id="genes_tab_select_none"><spring:message code="genes_tab.select.none"/></a>
        </div>
        <div id="search_with_selected_button" class="ui-state-default button ui-corner-all ui-state-disabled" tooltip="<spring:message code="menu.search_selected.tooltip"/>">
        	<small>Go with selected genes</small>
        </div>
    </div>
	<div class="content">
		<%@ include file="genesList.jsp" %>
	</div>
</div>