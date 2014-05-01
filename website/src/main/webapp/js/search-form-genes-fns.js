function loadDefaultOrganismGenes() {
	clearGeneError();

	var genes_string = $("#species_select :selected").attr("defgenes");
	
	// console.log("**** "+genes)
	$("#gene_list").val(genes_string);
	$("#gene_text").val(genes_string.replace("\n", "; "));// console.log("&&&
															// "+$("#gene_text").val());
	$("#gene_area").val(genes_string);// console.log("###
													// "+$("#gene_area").val());
}