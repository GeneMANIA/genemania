<div id="cytoscape_lite" class="menu">
	<div class="floating normal_width button ui-state-default ui-corner-all" id="side_bar_toggle_closed"><span class="ui-icon ui-icon-arrowthickstop-1-w"></span></div>            
	<ul>		
		<li id="menu_save"><label><spring:message code='menu.export'/></label>
			<ul class="over_flash">
				<li id="menu_generate_report"><label tooltip="<spring:message code='menu.print.tooltip'/>"><spring:message code='menu.print'/></label></li>

				<li id="menu_export_network" class="ui-menu-item-new-section"><label tooltip="<spring:message code='menu.export_to_text.tooltip'/>"><spring:message code='menu.export_to_text'/></label></li>
				<li id="menu_export_svg"><label tooltip="<spring:message code='menu.export_svg.tooltip'/>"><spring:message code='menu.export_svg'/></label></li>
				
				<li id="menu_export_networks" class="ui-menu-item-new-section"><label tooltip="<spring:message code='menu.export_networks.tooltip'/>"><spring:message code='menu.export_networks'/></label></li>
				<li id="menu_export_attributes"><label tooltip="<spring:message code='menu.export_attributes.tooltip'/>"><spring:message code='menu.export_attributes'/></label></li>
				<li id="menu_export_genes"><label tooltip="<spring:message code='menu.export_genes.tooltip'/>"><spring:message code='menu.export_genes'/></label></li>
				<li id="menu_export_go"><label tooltip="<spring:message code='menu.export_go.tooltip'/>"><spring:message code='menu.export_go'/></label></li>
				<li id="menu_export_interactions"><label tooltip="<spring:message code='menu.export_interactions.tooltip'/>"><spring:message code='menu.export_interactions'/></label></li>
			
				<li id="menu_export_params" class="ui-menu-item-new-section"><label tooltip="<spring:message code='menu.export_params.tooltip'/>"><spring:message code='menu.export_params'/></label></li>
				<li id="menu_export_params_json"><label tooltip="<spring:message code='menu.export_params_json.tooltip'/>"><spring:message code='menu.export_params_json'/></label></li>
				
			</ul>
		</li>
		<li id="menu_layout"><label><spring:message code='menu.actions'/></label>
			<ul class="over_flash">
				<li id="menu_reset_layout"><label tooltip="<spring:message code='menu.layout.tooltip'/>"><spring:message code='menu.layout'/></label></li>
                <li id="menu_close_tooltips"><label><spring:message code='menu.close_tooltips'/></label></li>

                <li id="menu_neighbors" class="ui-menu-item-new-section"><label tooltip="<spring:message code='menu.neighbours.tooltip'/>"><spring:message code='menu.highlight_neighbours'/></label></li>
                <li id="menu_neighbors_clear"><label><spring:message code='menu.unhighlight_neighbours'/></label></li>
				
				<li id="menu_show_labels" class="ui-menu-checkable ui-menu-item-new-section"><label><spring:message code='menu.show_labels'/></label></li>
				<li id="menu_publication_labels" class="ui-menu-checkable"><label><spring:message code='menu.publication_labels'/></label></li>
                <li id="menu_show_panzoom" class="ui-menu-checkable"><label><spring:message code='menu.show_control'/></label></li>
			</ul>
		</li>
		<li id="menu_query"><label><spring:message code='menu.query'/></label>
			<ul class="over_flash">
				<li id="menu_add_selected"><label tooltip="<spring:message code='menu.add_selected.tooltip'/>"><spring:message code='menu.add_selected'/></label></li>
				<%-- <li id="menu_remove_selected"><label tooltip="<spring:message code='menu.remove_selected.tooltip'/>"><spring:message code='menu.remove_selected'/></label></li> --%>
				<li id="menu_search_selected"><label tooltip="<spring:message code='menu.search_selected.tooltip'/>"><spring:message code='menu.search_selected'/></label></li>
			</ul>
		</li>
		<%--
		<li id="menu_search"><label tooltip="Click here to search for genesor GO annotations.">Search</label>
			<ul class="over_flash">
				<li id="menu_search_item"><input id="menu_search_input" class="widget" type="text" /></li>
			</ul>
		</li>
		--%>
        <li id="menu_legend"><label><spring:message code='menu.networks_legend'/></label></li>
        <li id="menu_go_legend"><label><spring:message code='menu.go_legend'/></label></li>
	</ul>
	<div class="menu_area">
		<div class="content">
			<div id="graph">
				<div id="graphBox"></div>
				<div id="CytoscapeLitePanel"></div>	
			</div>
		</div>
	</div>
</div>     