
CytowebUtil.firstNeighborsNodes = {};
CytowebUtil.firstNeighborsEdges = {};

CytowebUtil.highlightFirstNeighbors = function(nodes) {
	if (_vis) {
		CytowebUtil.firstNeighborsNodes = {};
		CytowebUtil.firstNeighborsEdges = {};
		
		if (nodes == null) {
			nodes = _vis.selected("nodes");
		}
		
		if (nodes != null && nodes.length > 0) {
			var fn = _vis.firstNeighbors(nodes, true);
			var neighbors = fn.neighbors;
			var edges = fn.edges;
			
			$.each(edges, function(i, edge){
				CytowebUtil.firstNeighborsEdges[edge.data.id] = true;
			});
			
			$.each(neighbors, function(i, node){
				CytowebUtil.firstNeighborsNodes[node.data.id] = true;
			});
			
			$.each(nodes, function(i, node){
				CytowebUtil.firstNeighborsNodes[node.data.id] = true;
			});
			
			CytowebUtil.neighborsHighlighted = true;
			$("#menu_neighbors_clear").removeClass("ui-state-disabled");
			CytowebUtil.updateBypass();
		}
	}
};

CytowebUtil.clearFirstNeighborsHighlight = function() {
	if (_vis) {
		
		CytowebUtil.firstNeighborsNodes = {};
		CytowebUtil.firstNeighborsEdges = {};
		
		CytowebUtil.neighborsHighlighted = false;
		$("#menu_neighbors_clear").addClass("ui-state-disabled");
		CytowebUtil.updateBypass();
	}
};