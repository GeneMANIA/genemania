function updateErrorMessage(){
			// Message and details:
			var details = "";
			var errMsg = "";
			var type = "warning";
			var msg = "";
			
			function gene_names(icons, use_other){
				var ret = "";
				var name_map = {}; // name => count
				var other_map = {}; // name => other
				
				icons.each(function(){
					var name = $(this).attr("gene").toLowerCase();
					var other = $(this).attr("other").toLowerCase();
					
					if( name_map[name] != null ){
						name_map[name] = name_map[name] + 1;
					} else {
						name_map[name] = 1;
					}
					
					other_map[name] = other;
				});
				
				for(var name in name_map){
					var count = name_map[name];
					var other = other_map[name];
					
					ret += "<strong>" + name + "</strong>" + ( count > 1 ? " (" + (use_other ? other : count + "x" ) + ")" : "" ) + "; ";
				}
 				
				return ret;
			}
			
			var synonyms = $("#gene_validation_icons .icon[type=synonym]");
			var synonyms_size = synonyms.size();
			
			var duplicates = $("#gene_validation_icons .icon[type=duplicate]");
			var duplicates_size = duplicates.size();
			
			var invalids = $("#gene_validation_icons .icon[type=invalid]");
			var invalids_size = invalids.size();
			
			var valids = $("#gene_validation_icons .icon[type=valid]");
			var valids_size = valids.size();
			
			var invalid_types = 0;
			
			if (synonyms_size > 0) { // synonyms
				type = "warning";
				errMsg = "There " + (synonyms_size == 1 ? "is a" : "are" ) + " duplicated gene symbol" + (synonyms_size > 1 ? "s" : "") + ".";
				
				invalid_types++;
			} 
			
			if (duplicates_size > 0) { // duplicates
				type = "warning";
				errMsg = "There " + (duplicates_size == 1 ? "is a" : "are" ) + " duplicated gene symbol" + (duplicates_size > 1 ? "s" : "") + ".";
				
				invalid_types++;
			} 
			
			if ( invalids_size > 0 ) { // invalid symbols
				
				if (valids_size <= 0) {
					type = "error";
					errMsg = "None of the symbols entered were recognized.";
				} else {
					type = "warning";
					errMsg = "There " + (valids_size == 1 ? "is an" : "are" ) + " unrecognized gene symbol" + (valids_size > 1 ? "s" : "") + ".";
				}
				
				invalid_types++;
			}
			
	
			if( !validMinGeneList() ){
				invalid_types++;
			}
			
			if ( invalid_types > 1 ) { // multiple errors
				type = "warning";
				errMsg = "There are multiple warnings.";
			}
			
			
			if (invalids_size > 0) {
				details += "<p>Unrecognized gene symbols ("+invalids_size+"):</p><p class='invalid_genes_list ui-corner-all'>"+gene_names(invalids)+"</p>";
			}
			if (synonyms_size > 0) {
				details += "<p>Synonyms ("+synonyms_size+"):</p><p class='invalid_genes_list ui-corner-all'>"+gene_names(synonyms, true)+"</p>";
			}
			if (duplicates_size > 0) {
				details += "<p>Duplicates ("+duplicates_size+"):</p><p class='invalid_genes_list ui-corner-all'>"+gene_names(duplicates)+"</p>";
			}

			
			// show tooltip
			if ( invalid_types >= 1 ) {
				showGeneError(type, errMsg, details);
			} else if ( synonyms_size == 0 && invalids_size == 0 && duplicates_size == 0 ) {
				// Clear only if there is no client side errors already set:
				clearGeneError();
			}
}
