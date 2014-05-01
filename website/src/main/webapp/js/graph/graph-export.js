CytowebUtil.exportNetwork = function() {
		if( !CytowebUtil.checkVersionFor("text network") ){
		return;
	}
	
	$("#text_form").submit();
};

CytowebUtil.exportParams = function() {
	$("#params_form").submit();
};

CytowebUtil.exportParamsJson = function() {
	$("#params_json_form").submit();
};

CytowebUtil.exportNetworks = function() {
	if( !CytowebUtil.checkVersionFor("networks") ){
		return;
	}
	
	$("#networks_form").submit();
};

CytowebUtil.exportGenes = function() {
	if( !CytowebUtil.checkVersionFor("genes") ){
		return;
	}
	
	$("#genes_form").submit();
};

CytowebUtil.exportGo = function() {
	if( !CytowebUtil.checkVersionFor("functions") ){
		return;
	}
	
	$("#go_form").submit();
};

CytowebUtil.exportInteractions = function() {
	if( !CytowebUtil.checkVersionFor("interactions") ){
		return;
	}
	
	$("#interactions_form").submit();
};

CytowebUtil.exportSvg = function(use_svg_from_cytoweb) {
	if( use_svg_from_cytoweb == undefined || use_svg_from_cytoweb ){
		$("#svg_form [name=content]").val( _vis.svg() );
	}
	$("#svg_form").submit();
};

CytowebUtil.exportAttributes = function() {
	if( !CytowebUtil.checkVersionFor("attributes") ){
		return;
	}
	
	$("#attributes_form").submit();
};