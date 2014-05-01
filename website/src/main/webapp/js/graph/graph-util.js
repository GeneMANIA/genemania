

CytowebUtil._round_weight = function(w){
    w = Math.round(w*1000)/10;
    
    if( (w * 10) % 10 == 0 ){
        w += ".0";
    }
    
	w = w < 0.1 ? "&lt; 0.1" : w;
	
	return w;
};

CytowebUtil.body_click = function(){
    $("body").trigger("click");
};

CytowebUtil.visualization = function() {
	return _vis;
};


CytowebUtil._nodesById = null;

CytowebUtil.getNodeById = function(id) {
	if (CytowebUtil._nodesById == null) {
		CytowebUtil._nodesById = {};
		var nodes = CytowebUtil.nodes();
		$.each(nodes, function(i, n) {
			CytowebUtil._nodesById[n.data.id] = n;
		});
	}
	return CytowebUtil._nodesById[id];
};