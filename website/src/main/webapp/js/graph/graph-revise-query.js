CytowebUtil.searchWithSelectedGenes = function(addGenes){
	
	var useOnlySelectedGenes = !addGenes;
	
	if(_vis){
		var selected_genes = _vis.selected("nodes");
		var genes_box = $("#gene_area");
		var list = "";
		var addGenes = !useOnlySelectedGenes;
		
		var genes_to_add = [];

		if( addGenes ){
			list = "" + genes_box.val();
			
			// append newline if not already there for new genes
			if( list.charAt( list.length - 1 ) != "\n" ){
				list += "\n";
			}
		}
		
		$.each(selected_genes, function(i, gene){
			if( !gene.data.attribute ){
				genes_to_add.push( gene );
			}
		});
		
		var genes_we_have = {};
		if( addGenes ){			
			$.each( list.split("\n"), function(i, gene){
				genes_we_have[ gene.trim() ] = true;
			} );
		}
		
		$.each(genes_to_add, function(i, gene){
			var name = gene.data.symbol;
			
			if( !genes_we_have[ name ] ){
				list += name + (i < genes_to_add.length - 1 ? "\n" : "");
			}
		});
		
		genes_box.val( list ).blur();
		setTimeout(function(){
			$("#findBtn").click();
		}, 10);
	}
	
};

CytowebUtil.addSelectedGenesToSearch = function(){
	CytowebUtil.searchWithSelectedGenes(true);
};
