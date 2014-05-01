<div id="networks_tab" class="tab">

    <div class="header">
        <div>
            <div class="title"><spring:message code='networks_tab.sort_by'/></div> <a class="action_link" href="#" id="networks_tab_sort_by_name"><spring:message code='networks_tab.sort_by.name'/></a>, <a class="action_link" href="#" id="networks_tab_sort_by_weight"><spring:message code='networks_tab.sort_by.weight'/></a>
        </div>
        <div>
            <div class="title"><spring:message code='networks_tab.expand'/></div> <a class="action_link" href="#" id="networks_tab_expand_all"><spring:message code='networks_tab.expand.all'/></a>, <a class="action_link" href="#" id="networks_tab_expand_top_level"><spring:message code='networks_tab.expand.top_level'/></a>, <a class="action_link" href="#" id="networks_tab_expand_none"><spring:message code='networks_tab.expand.none'/></a>
        </div>
        <div id="networks_filter_menu">
            <div class="title"><spring:message code='networks_tab.enable'/></div> <a class="action_link" href="#" id="networks_tab_check_all"><spring:message code='networks_tab.enable.all'/></a>, <a class="action_link" href="#" id="networks_tab_check_none"><spring:message code='networks_tab.enable.none'/></a>
        </div>
    </div>
	<div class="content">
		<%@ include file="networksList.jsp" %>
	</div>
</div>