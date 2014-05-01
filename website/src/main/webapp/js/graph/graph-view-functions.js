CytowebUtil.neighborsHighlighted = false;
CytowebUtil.geneHighlighted = false;
CytowebUtil.highlightedGenes = {};
CytowebUtil.highlightedGeneEdges = {};

CytowebUtil.highlightGene = function(gene_id){
	CytowebUtil.highlightedGenes = {};
	CytowebUtil.highlightedGenes[gene_id] = true;
	CytowebUtil.geneHighlighted = true;
	
	var fn = _vis.firstNeighbors( [_vis.node(gene_id)] , true);
	var neighbors = fn.neighbors;
	var edges = fn.edges;
	
	CytowebUtil.highlightedGeneEdges = {};
	$.each(edges, function(i, e){
		CytowebUtil.highlightedGeneEdges[e.data.id] = true;
	});
	
	CytowebUtil.halfHighlightedGenes = {};
	$.each(neighbors, function(i, n){
		CytowebUtil.halfHighlightedGenes[n.data.id] = true;
	});
	
	CytowebUtil.updateBypass();
};

CytowebUtil.unhighlightGene = function(gene_id){
	if( gene_id != null ){
		CytowebUtil.highlightedGenes[gene_id] = false;
		CytowebUtil.highlightedGeneEdges = {};
		CytowebUtil.halfHighlightedGenes = {};
	} else {
		CytowebUtil.highlightedGenes = {};
		CytowebUtil.highlightedGeneEdges = {};
		CytowebUtil.halfHighlightedGenes = {};
		CytowebUtil.geneHighlighted = false;
	}
	
	CytowebUtil.updateBypass();
};

CytowebUtil.highlightEdge = function(edge_id){
	CytowebUtil.geneHighlighted = true;

	var edge = _vis.edge(edge_id);
	var src_id = edge.data.source;
	var tgt_id = edge.data.target;
	
	CytowebUtil.halfHighlightedGenes = {};
	CytowebUtil.highlightedGenes = {};
	CytowebUtil.halfHighlightedGenes[src_id] = true;
	CytowebUtil.halfHighlightedGenes[tgt_id] = true;
	
	CytowebUtil.highlightedGeneEdges = {};
	CytowebUtil.highlightedGeneEdges[edge_id] = true;
	
	CytowebUtil.updateBypass();
};

CytowebUtil.unhighlightGenesAndEdges = function(){
	CytowebUtil.highlightedGenes = {};
	CytowebUtil.highlightedGeneEdges = {};
	CytowebUtil.halfHighlightedGenes = {};
	CytowebUtil.geneHighlighted = false;
	
	CytowebUtil.updateBypass();
}

CytowebUtil.unhighlightEdge = function(edge_id){
	if( edge_id != null ){
		CytowebUtil.highlightedGenes = {};
		CytowebUtil.highlightedGeneEdges[edge_id] = false;
		CytowebUtil.halfHighlightedGenes = {};
	} else {
		CytowebUtil.highlightedGenes = {};
		CytowebUtil.highlightedGeneEdges = {};
		CytowebUtil.halfHighlightedGenes = {};
		CytowebUtil.geneHighlighted = false;
	}
	
	CytowebUtil.updateBypass();
};

CytowebUtil.highlightNetworkGroup = function(group_id){
	CytowebUtil.highlightedGenes = {};
	CytowebUtil.highlightedGeneEdges = {};
	CytowebUtil.halfHighlightedGenes = {};
	CytowebUtil.geneHighlighted = true;
	
	var edges = CytowebUtil.edges();
	$.each(edges, function(i, e){
		if( e.data.networkGroupId == group_id ){
			CytowebUtil.highlightedGeneEdges[e.data.id] = true;
			CytowebUtil.halfHighlightedGenes[e.data.source] = true;
			CytowebUtil.halfHighlightedGenes[e.data.target] = true;
		}
	});
	
	CytowebUtil.updateBypass();
};

CytowebUtil.unhighlightNetworkGroup = function(){
	CytowebUtil.unhighlightGenesAndEdges();
};

CytowebUtil.highlightNetwork = function(net_id){
	CytowebUtil.highlightedGenes = {};
	CytowebUtil.highlightedGeneEdges = {};
	CytowebUtil.halfHighlightedGenes = {};
	CytowebUtil.geneHighlighted = true;
	
	var edges = CytowebUtil.edges();
	$.each(edges, function(i, e){
		if( e.data.networkIdToWeight[net_id] != null ){
			CytowebUtil.highlightedGeneEdges[e.data.id] = true;
			CytowebUtil.halfHighlightedGenes[e.data.source] = true;
			CytowebUtil.halfHighlightedGenes[e.data.target] = true;
		}
	});
	
	CytowebUtil.updateBypass();
};

CytowebUtil.unhighlightNetwork = function(){
	CytowebUtil.unhighlightGenesAndEdges();
};

CytowebUtil.highlightAttributeGroup = function(group_id){
	CytowebUtil.highlightedGenes = {};
	CytowebUtil.highlightedGeneEdges = {};
	CytowebUtil.halfHighlightedGenes = {};
	CytowebUtil.geneHighlighted = true;
	
	var edges = CytowebUtil.edges();
	$.each(edges, function(i, e){
		if( e.data.attributeGroupId == group_id ){
			CytowebUtil.highlightedGeneEdges[e.data.id] = true;
			CytowebUtil.halfHighlightedGenes[e.data.source] = true;
			CytowebUtil.halfHighlightedGenes[e.data.target] = true;
		}
	});
	
	CytowebUtil.updateBypass();
};

CytowebUtil.unhighlightAttributeGroup = function(){
	CytowebUtil.unhighlightGenesAndEdges();
};

CytowebUtil.highlightAttribute = function(attr_id){
	CytowebUtil.highlightedGenes = {};
	CytowebUtil.highlightedGeneEdges = {};
	CytowebUtil.halfHighlightedGenes = {};
	CytowebUtil.geneHighlighted = true;
	
	var edges = CytowebUtil.edges();
	$.each(edges, function(i, e){
		if( e.data.attributeId == attr_id ){
			CytowebUtil.highlightedGeneEdges[e.data.id] = true;
			CytowebUtil.halfHighlightedGenes[e.data.source] = true;
			CytowebUtil.halfHighlightedGenes[e.data.target] = true;
		}
	});
	
	CytowebUtil.updateBypass();
};

CytowebUtil.unhighlightAttribute = function(){
	CytowebUtil.unhighlightGenesAndEdges();
};

CytowebUtil.mergeEdges = function(value) {
	if (value === CytowebUtil.OPTIONS.edgesMerged) return;
	
	if(_vis) {
		CytowebUtil.OPTIONS.edgesMerged = value;
		_vis.edgesMerged(value);
	}
};

CytowebUtil.showNodeLabels = function(value) {
	if (value === CytowebUtil.OPTIONS.nodeLabelsVisible) return;

	if(_vis) {
		CytowebUtil.OPTIONS.nodeLabelsVisible = value;
		_vis.nodeLabelsVisible(value);
	}
};

CytowebUtil.showPanZoomControl = function(value) {
	if (value === CytowebUtil.OPTIONS.panZoomControlVisible) return;
	
	if(_vis) {
		CytowebUtil.OPTIONS.panZoomControlVisible = value;
		_vis.panZoomControlVisible(value);
	}
};

CytowebUtil.transparentEdges = function(value) {
	if(_vis) {
		var style = _vis.visualStyle();
		
		if( value ){
			style.edges.opacity = CytowebUtil.VISUAL_STYLE_OPACITY;
		} else {
			style.edges.opacity = CytowebUtil.DEF_EDGE_OPACITY;
		}

		_vis.visualStyle(style);
	}
};

CytowebUtil.showPublicationLabels = function(value){
	if( _vis ){
		var style = _vis.visualStyle();
		
		if( value ){
			style.nodes.labelGlowOpacity = 0;
			style.nodes.labelFontColor = "#000000";
			style.nodes.labelVerticalAnchor = "bottom";
		} else {
			style.nodes.labelGlowOpacity = 1;
			style.nodes.labelFontColor = "#ffffff";
			style.nodes.labelVerticalAnchor = "middle";
		}

		_vis.visualStyle(style);
	}
};