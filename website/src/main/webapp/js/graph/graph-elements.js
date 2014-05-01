CytowebUtil.edges = function(){
	if( CytowebUtil._edges == null ){
		CytowebUtil._edges = _vis.edges();
	}
	
	return CytowebUtil._edges;
};

CytowebUtil.nodes = function(){
	if( CytowebUtil._nodes == null ){
		CytowebUtil._nodes = _vis.nodes();
	}
	
	return CytowebUtil._nodes;
};

CytowebUtil.genes = function(){
	if( CytowebUtil._genes == null ){
		CytowebUtil._genes = $.grep( CytowebUtil.nodes(), function(node){
			return !node.data.attribute;
		} );
	}
	
	return CytowebUtil._genes;
};

CytowebUtil.node = function(id){
	if( CytowebUtil._nodesById == null ){
		var map = CytowebUtil._nodesById = [];
		
		$.each(CytowebUtil.nodes(), function(i, node){
			map[ node.data.id ] = node;
		});
	}
	
	return CytowebUtil._nodesById[id];
};

CytowebUtil.edge = function(id){
	if( CytowebUtil._edgesById == null ){
		var map = CytowebUtil._edgesById = [];
		
		$.each(CytowebUtil.edges(), function(i, edge){
			map[ edge.data.id ] = edge;
		});
	}
	
	return CytowebUtil._edgesById[id];
};