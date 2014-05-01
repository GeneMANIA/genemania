CytowebUtil._onSelectEdgesChanged = function(scroll) {    
	$("#networks_widget .label").removeClass("selected");
	var edgesArray = _vis.selected("edges");
	
	$.each(edgesArray, function(i, edge) {
		
		$.each(edge.data.networkIdToWeight, function(id, weight){
			var label = $("#network"+ id + " .label");
			label.addClass("selected");
		});
		
		
	});
};

CytowebUtil.selectAll = function(){
	if(_vis){
		var genes = CytowebUtil.genes();
		var gene_ids = [];
		
		$.each(genes, function(i, gene){
			gene_ids.push( gene.data.id );
		});
		
		_vis.select( gene_ids );
	}
};

CytowebUtil.selectNone = function(){
	if(_vis){
		_vis.deselect("nodes");
	}
};

CytowebUtil.selectNode = function(nodeId) {
	if(_vis) {
		_vis.select("nodes", [nodeId]);
	}
};

CytowebUtil.deselectNode = function(nodeId) {
	if(_vis) {
		_vis.deselect("nodes", [nodeId]);
	}
};

CytowebUtil.selectQueryGenes = function(){
	if(_vis){
		var query_nodes = [];
		var nodes = CytowebUtil.nodes();
		
		$.each(nodes, function(i, node){
			if( node.data.queryGene ){
				query_nodes.push(node);
			}
		});
		
		_vis.deselect("nodes");
		_vis.select(query_nodes);
	}
};

CytowebUtil._onSelectNodesChanged = function() {
	if (_vis) {
		var nodes = $.grep(_vis.selected("nodes"), function(node){
			return !node.data.attribute;
		});
		var attrs = $.grep(_vis.selected("nodes"), function(node){
			return node.data.attribute;
		});
		var selected = nodes != null && nodes.length > 0;
		var selectedAttrs = attrs != null && attrs.length > 0;
		var query_nodes = false;
		var all_query_nodes = true;
		var all_nodes = false;
		var no_nodes = true;
		var non_query_nodes = false;
		var vis_nodes = CytowebUtil.genes();
		
		var selected_query_nodes = [];
		$.each(nodes, function(i, node){
			if( node.data.queryGene ){
				query_nodes = true;
				selected_query_nodes.push(node);
			} else {
				non_query_nodes = true;
			}
			
			no_nodes = false;
		});
		
		var vis_query_nodes = [];
		$.each(vis_nodes, function(i, node){
			if( node.data.queryGene ){
				vis_query_nodes.push(node);
			}
		});
		
		all_query_nodes = vis_query_nodes.length == selected_query_nodes.length;
		var all_and_only_query_nodes = all_query_nodes && nodes.length == vis_query_nodes.length;
		
		all_nodes = vis_nodes.length == nodes.length;
		
		if( all_nodes ){
			$("#genes_tab_select_all").addClass("active").siblings().removeClass("active");
		} else if( all_and_only_query_nodes ){
			$("#genes_tab_select_query").addClass("active").siblings().removeClass("active");
		} else if( no_nodes ){
			$("#genes_tab_select_none").addClass("active").siblings().removeClass("active");
		} else {
			$("#genes_tab_select_none").removeClass("active").siblings().removeClass("active");
		}
		
		if( selected ){
			$("#search_with_selected_button").removeClass("ui-state-disabled");
		} else {
			$("#search_with_selected_button").addClass("ui-state-disabled");
		}
		
		if( selected || selectedAttrs ){
			$("#menu_neighbors").removeClass("ui-state-disabled");
		} else {
			$("#menu_neighbors").addClass("ui-state-disabled");
		}
		
		// Enable/disable related menu items:
		if (selected) {
			
			$("#menu_search_selected").removeClass("ui-state-disabled");
			
			$("#menu_add_selected").removeClass("ui-state-disabled");
			$("#menu_remove_selected").removeClass("ui-state-disabled");
		} else {
			
			$("#menu_search_selected").addClass("ui-state-disabled");
			$("#menu_add_selected").addClass("ui-state-disabled");
			$("#menu_add_selected").addClass("ui-state-disabled");
			$("#menu_remove_selected").addClass("ui-state-disabled");
		}
	}
};