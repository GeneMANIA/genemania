CytowebUtil.updateBypass = function(){
	if(_vis){
		var bypass = {};
		
		var highlightNodeOpacity = 1;
		var highlightEdgeOpacity = 1;
		var unhighlightNodeOpacity = 0.125;
		var halfHighlightNodeOpacity = 1;
		var unhighlightEdgeOpacity = 0.125;
		var highlightBorderColor = "#aae6ff";
		var highlightBorderWidth = 3;
		
		
		function assign(group, id, property, value){
			if( bypass[group] == null ){
				bypass[group] = {};
			}
			
			if( bypass[group][id] == null ){
				bypass[group][id] = {};
			}
			
			if( value != null ){
				bypass[group][id][property] = value;
				
				if( group == "edges" && property == "opacity" ){
					bypass[group][id]["mergeOpacity"] = value;
				}
			} else {
				delete bypass[group][id][property];
				
				if( group == "edges" && property == "opacity" ){
					delete bypass[group][id]["mergeOpacity"];
				} 
			}
		}
		
		$.each(CytowebUtil.goColor, function(id, color){
			assign("nodes", id, "color", color);
			assign("nodes", id, "labelGlowColor", color);
			assign("nodes", id, "borderColor", color);
		});
		
		$.each(CytowebUtil.goEdgesColor, function(id, color){
			assign("edges", id, "color", color);
		});
		
		if( CytowebUtil._highlight ){
			$.each(CytowebUtil.goHighlight, function(id, highlight){
				assign("nodes", id, "opacity", highlight ? highlightNodeOpacity : unhighlightNodeOpacity);
			});
			
			$.each(CytowebUtil.goEdgesHighlight, function(id, highlight){
				assign("edges", id, "opacity", highlight ? highlightEdgeOpacity : unhighlightEdgeOpacity);
			});
		} else if( CytowebUtil.geneHighlighted ){
			var edges = CytowebUtil.edges();
			$.each(edges, function(i, edge){
				assign("edges", edge.data.id, "opacity", unhighlightEdgeOpacity);
			});
			
			var nodes = CytowebUtil.nodes();
			$.each(nodes, function(i, node){
				assign("nodes", node.data.id, "opacity", unhighlightNodeOpacity);
			});
			
			$.each(CytowebUtil.highlightedGenes, function(id, highlight){
				assign("nodes", id, "opacity", highlight ? highlightNodeOpacity : unhighlightNodeOpacity);
				assign("nodes", id, "borderColor", highlight ? highlightBorderColor : null);
				assign("nodes", id, "borderWidth", highlight ? highlightBorderWidth : null);
				assign("nodes", id, "selectionBorderColor", highlight ? highlightBorderColor : null);
			});
			$.each(CytowebUtil.highlightedGeneEdges, function(id, highlight){
				assign("edges", id, "opacity", highlight ? highlightEdgeOpacity : unhighlightEdgeOpacity);
			});
			$.each(CytowebUtil.halfHighlightedGenes, function(id, highlight){
				assign("nodes", id, "opacity", highlight ? halfHighlightNodeOpacity : unhighlightNodeOpacity);
			});
		} else if( CytowebUtil.neighborsHighlighted ){
			var edges = CytowebUtil.edges();
			$.each(edges, function(i, edge){
				assign("edges", edge.data.id, "opacity", unhighlightEdgeOpacity);
			});
			
			var nodes = CytowebUtil.nodes();
			$.each(nodes, function(i, node){
				assign("nodes", node.data.id, "opacity", unhighlightNodeOpacity);
			});
			
			$.each(CytowebUtil.firstNeighborsNodes, function(id, highlight){
				assign("nodes", id, "opacity", highlight ? highlightNodeOpacity : unhighlightNodeOpacity);
			});
			$.each(CytowebUtil.firstNeighborsEdges, function(id, highlight){
				assign("edges", id, "opacity", highlight ? highlightEdgeOpacity : unhighlightEdgeOpacity);
			});
		}
		
		_vis.visualStyleBypass(bypass);
	}
};