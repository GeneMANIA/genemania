function resetValidationIcon(line){
	return; // not needed with new grey out mechanism
	$("#gene_validation_icons .icon").quickeq(line)
		.attr("type", "empty")
		.attr("class", "icon")
		.attr("tooltip", "")
		.attr("gene", "");
}

function setIcons(val_result){
	for(var line in val_result.genes){
		var possible_gene = val_result.genes[line];
		setIcon(line, possible_gene.type, possible_gene.name, possible_gene.other);
	}
	
	$("#gene_validation_icons .icon").quickeq( val_result.size ).nextAll().andSelf()
		.attr("type", "empty")
		.attr("class", "icon")
		.attr("tooltip", "")
		.attr("gene", "")
		.attr("other", "");
}

$(function(){
	$("#gene_validation_icons .icon").css("height", geneAreaLineHeight());
});

var _number_of_icons;
$(function(){
	get_number_of_icons();	
});

function get_number_of_icons(){
	if( _number_of_icons == null ){
		_number_of_icons = $("#gene_validation_icons .icon").size();
	}
	return _number_of_icons;
}

function adjustValidationIcons(only_past_this_line){
	
	//console.log("--\nadjusting validation icons: " + only_past_this_line); 
	
	if(only_past_this_line != undefined){
		//console.log("just enter / bkspc " + only_past_this_line); 
		
		if( only_past_this_line > 0 ){
			var inserted_icon = $('<div class="icon empty inserted" type="empty" line="' + only_past_this_line + '"></div>');
			inserted_icon.append('<div class="image"></div>');
			inserted_icon.css("height", geneAreaLineHeight());
			
			$("#gene_validation_icons .icon").quickeq( only_past_this_line )
			.after(inserted_icon)
			.nextAll().each(function(){
				var line = parseInt( $(this).attr("line") );
			
				//console.log("incr " + line);
				$(this).attr("line", line + 1);
			});
		} else if ( only_past_this_line < 0 ){
			var at_line = $("#gene_validation_icons .icon").quickeq( -1 * only_past_this_line );
				
			at_line.nextAll().each(function(){
				var next = $(this).next();
				
				if( next.size() != 0 ){
					var next_type = next.attr("type"); 
					var next_class = next.attr("class");
					var next_tooltip = next.attr("tooltip");
					var next_other = next.attr("other");
				
					//console.log("decr " + line);
					$(this).attr("type", next_type).attr("class", next_class).attr("tooltip", next_tooltip).attr("other", next_other);
				}

			});
		}
		
	
	}
	
}

function setLoadingIconsForGenes(){
	$("#gene_validation_icons .icon").addClass("loading");
	$("#gene_selection").addClass("loading");
}

function removeLoadingIconsForGenes(){
	$("#gene_validation_icons .loading").each(function(){
		$(this).removeClass("loading");
	});
	$("#gene_selection").removeClass("loading");
}

function setIcon(line, type, gene, str){
	var icons = $("#gene_validation_icons .icon");
	var icon = icons.quickeq(line);
	
	type = type.toLowerCase();
	gene = gene.toLowerCase();
	str = str.toLowerCase();
	
	if( icon.size() == 0 ){
		//console.log("No such icon with line " + line);
	}
	
	icon.removeClass( icon.attr("type") );
	
	icon.addClass(type);
	icon.attr("type", type);
	icon.attr("gene", gene);
	icon.attr("other", str);
	
	var tooltip;
	if( type == "valid" ){
		tooltip = "The gene, \"" + gene + "\", is recognized.";
	} else if ( type == "invalid"  ) {
		tooltip = "The gene, \"" + gene + "\", is unrecognized.";
	} else if( type == "synonym" ) {
		tooltip = "The gene, \"" + gene + "\", is a synonym" + ( str != undefined ? " of \"" + str + "\"" : "" ) + ".";
	} else if( type == "duplicate" ) {
		tooltip = "The gene, \"" + gene + "\", is a duplicate.";
	} else if( type == "loading" ) {
		tooltip = "The gene, \"" + gene + "\", is being checked.";
	} else if( type == "empty" ){
		tooltip = "This line is empty and contains no gene.";
	}
	
	icon.attr("tooltip", tooltip);

}
