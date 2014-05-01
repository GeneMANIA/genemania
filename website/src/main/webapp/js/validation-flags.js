var _genes_touched;
$(function(){
	if( $("#phrase_and_go").size() == 0 && $("#query_line").size() == 0 ){
		return; // ignore this js file if no query area
	}
	
	_genes_touched = $("#results_page").size() > 0 || $("#user_network_exception").size() > 0 || $("#gene_area").val().length > 0;

	$("#gene_text").add("#gene_area").bind("focus edit", function(){
		$(this).trigger("touch");
	}).one("touch", function(){
		//console.log("--\ntouch triggered on gene_text");
		_genes_touched = true;
	}).bind("keydown", function(){
		$(this).trigger("edit");
	}).bind("edit", function(){
		//console.log("--\nedit triggered on genes");
		_between_edit_and_validated = true;
	});
});

function genesTouched(){
	return _genes_touched;
}

function inErrorState(){
	var error = $(".input_error").length > 0;
	return error;
}

function inGeneErrorState(){
	return $("#gene_text").hasClass("input_error");
}

function noGenesEntered(){
	var no_genes = $("#gene_text").val() == "";
	return no_genes;
}

function isSubmittable(){
	return _genes_touched && !inErrorState() && !noGenesEntered() && !isValidating() && !areNetworksReloading() && validMaxGeneList() && !uploadingNetwork();
}

function validMinGeneList() {
	return $("#gene_area").val().match(/\S/);
}

function validMaxGeneList() {
	return numberOfGenes() <= maxNumberOfGenes();
}

function noNetworksSelected(){
	return $("#networks_section").hasClass("input_error");
}

function uploadingNetwork(){
	return _uploading;
}

function invalidateGeneForOrganismChange(){
	_lastValidationGeneSet = "";
	_lastValidationOrg = "";
}

function isValidating(){
	return _validating_active_count != 0 || _between_edit_and_validated;
}