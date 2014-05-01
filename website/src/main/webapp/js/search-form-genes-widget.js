$(function(){
	
	if( $("#phrase_and_go").size() == 0 && $("#query_line").size() == 0 ){
		return; // ignore this js file if no query area
	}
	
    /***************************************************************************
	 * gene entry box behaviour
	 **************************************************************************/
    var genesOpen = false;
    
    // on focus of text line, replace with text area (i.e. bigger box)
    $("#gene_text").focus(function(){

        // tab focus fix (i.e. press tab and focus gene_text from gene_area =>
		// creates bad state)
        if(genesOpen) {
            $(this).css("visibility", "visible").blur();
        } else {
        
            // replace widget
            $("#gene_closed").css("visibility", "hidden");
            $("#gene_open").show();
            
            // fancy ani
            $("#gene_area").fadeIn(ANI_SPD, function(){
                $(this).focus(); // must focus after ani
//                var scrollToVal = $(this).attr("scrollHeight");
//                $(this).attr("scrollTop", scrollToVal);
                genesOpen = true;
                
                $("#uploadArea").hide();
            });
        
        }
    });
    
    var gene_timeout;
    var gene_delay = 750;
    
    function update_text_from_area(){
		// replace text
        var text = new String( $("#gene_area").val() );

        // remove blank lines
        while( text.search(/\n(\s)*\n/) >= 0 ) {
            text = text.replace(/\n(\s)*\n/g, "\n");
        }
        // left trim
        text = $.trim(text);
                
        // newline <-> ; for gene_text box summary
        var newlineToSemi = text.replace(/\n/g, "; ");
        var noSemiAtEnd = $.trim(newlineToSemi).replace(/;$/, ""); 
        $("#gene_text").val(noSemiAtEnd);
    }
    
    // on blur of text area, show text line instead (i.e. shrink bigger box)
    var gene_area_val;
    
    function trigger_hide_tooltip(){
		$("#gene_area").trigger("hidetooltip");
    }
    
    $(window).bind("blur", function(e){
    	trigger_hide_tooltip();
    });
    
   
    $("body").bind("mousedown mouseup", function(e){ 
    
    	var target = $(e.target);

    	var inside_area = false;
    	var inside_tooltip = false;
    	target.parents().andSelf().each(function(){
    		if(  $(this).hasClass("gene_error_tooltip") ){
    			inside_tooltip = true;
    		}
    	
    		if( $(this).attr("id") == "gene_area" ){
    			inside_area = true;
    		}
    	});
    	
    	if( !inside_area && !inside_tooltip ){
    		trigger_hide_tooltip();
    	} 
    	
    	if( inside_tooltip ) {
    		return false;
    	}
    	
    });
    
    if( $("#gene_area").val() != "" ){
// console.log("some genes already entered");
    	update_text_from_area();
		validateGeneSymbols();
    }
    
    updateGeneCount();
    
    var too_many_genes_message = "<p>Because you have more than " + maxNumberOfGenes() + " genes in your list, the web " + 
	"version of GeneMANIA does not support your query.</p> " +
	"<p>We suggest you try the <a href='http://pages.genemania.org/plugin'>GeneMANIA Cytoscape plugin</a>, which can handle very large number of genes.</p>";

    function adjust_icons_from_scroll(){
    	
    	$("#gene_validation_icons").css({
    		top: -1 * $("#gene_area").scrollTop(),
    		left: -1 * $("#gene_area").scrollLeft()
    	});
    	
    }
    
    
    var last_gene_area_val = "";
    var last_organism = $("#species_select").val();
    var last_valid_max_gene_list = true;
    $("#gene_area").focus(function(){
    	$("#gene_area").trigger("showtooltip");
    	$("#gene_area").trigger("adjusttooltipposition");
    }).blur(function(e){
        // replace widget
    	$("#gene_closed").css("visibility", "visible");
        $("#gene_open").fadeOut(ANI_SPD);

		update_text_from_area();
		validateGeneSymbols();
        
        $(this).hide();
        genesOpen = false; // TODO remove this genesOpen variable and use
							// $("#genes_open").is(':visible') instead
		
    	$("#gene_list").val($("#gene_text").val());
    	
    	$("#uploadArea").show();
    	
    }).bind("keyup keydown paste change", function(e){	
		
		// console.log("--\ngene key event");
		// console.log(e);
		
		// console.log("caret location: ");
		// console.log("index: ");
		// console.log( $(this).caret() );
		// console.log( $(this).val()[ $(this).caret().end - 1 ] + "|" +
		// $(this).val()[ $(this).caret().end ] );
		
    	if( e.type != "keydown" ){
    	
	    	var number_of_genes = numberOfGenes();
	    	var max_number_of_genes = maxNumberOfGenes();
	    	
	    	updateGeneCount();
  
	    	if( number_of_genes > max_number_of_genes ){
	    		$("#gene_validation_icons .icon").attr("type", "empty").attr("class", "icon empty");
	    		
	    		$("#gene_selection").removeClass("loading");
	    		
	    		if( last_valid_max_gene_list ){
		    		showGeneError("error", "Too many genes", too_many_genes_message);
		    	}
	    		
	    		last_valid_max_gene_list = false;
	    		
	    		return;
	    	} else {
	    		last_valid_max_gene_list = true;
	    	}
    	}
    	
		function key_is_meta(code){
			switch(code){
				case 37: // left
				case 38: // up
				case 39: // right
				case 40: // down
				case 17: // control
				case 18: // alt
				case 91: // command
				case 27: // escape
				case 16: // shift
				case 20: // caps lock
					return true;
				default:
					return false;
			}
		}
		
		function key_is_enter(code){
			return code == 13;
		}
		
		function key_is_backspace(code){
			return code == 8;
		}
		
		function key_is_delete(code){
			return code == 46;
		}
		
		function key_edits(code){
			return !key_is_meta(code);
		}
		
		// don't trigger anything on just a meta key (shift, alt, etc)
		if( !key_edits(e.which) ){
			// do nothing
		} else if( $("#gene_area").is(":visible") ) {
			
			// clear all icons iff blank
			if( $("#gene_area").val() == "" || $("#gene_area").val().match(/^\s*$/g) ){
				// console.log("empty val icons");
				$("#gene_validation_icons .icon").attr("type", "empty").attr("class", "icon").attr("tooltip", "");
				
		    	adjust_icons_from_scroll();
			}
		
			function update_qtip_position(){
				// update position
				$(".qtip[qtipfor=gene_selection]").each(function(){
					$(this).qtip("api").updatePosition();
				});
			}
			
			var cursor_char = $(this).caret().end;
			var val = $(this).val();
			
			function lines(include_last_char){
				var cursor_ch = cursor_char;
				
				if( include_last_char == undefined ){
					include_last_char = true;
				}
				
				if( !include_last_char ){
					cursor_ch--;
				}
			
				var line = 0;
				for(var i = 0; i < val.length && i < cursor_ch; i++){
					if( val[i] == "\n" ){
						line++;
					}
				}
				line_cache = line;
				
				return line;
			}
						
			function adjust_to_line(forwards){		
				adjustValidationIcons( (forwards ? 1 : -1) * line() );
			}
			
			// handle new or removed lines
			if( e.type == "keydown" ){			
				if( key_is_enter(e.which) ){
				
					var line = lines(true);
					
					if( cursor_char > 0 &&  val[ cursor_char - 1 ] == "\n" ){
						// console.log("pressed enter key before newline");
						adjustValidationIcons( line - 1 );	
					} else if( val[ cursor_char ] == "\n" || val.substring(cursor_char).match(/^\s*\n.*/g) ){
						// console.log("pressed enter key after newline");
						adjustValidationIcons( line );
					} else {
						// console.log("just adjust the icons; normal enter key
						// press");
						resetValidationIcon( line );
						resetValidationIcon( line + 1 );
						adjustValidationIcons();
					}
					
					update_qtip_position();
				} else if( key_is_backspace(e.which) && cursor_char > 0 && val[cursor_char - 1] == "\n" ){
					var line = lines(false);
				
					// console.log("pressed backspace key after newline");
					
					if( (val[cursor_char - 1] + "") != "\n" ){
						resetValidationIcon( line );
					}
					
					adjustValidationIcons( -1 * line );
					update_qtip_position();
				} else if( key_is_delete(e.which) && val[cursor_char] == "\n" ){
					var line = lines(false);
				
					// console.log("pressed delete key before newline");
					
					if( ("" + val[cursor_char + 1]) != "\n" && cursor_char - 1 > 0 && val[cursor_char - 1] != "\n" ){
						resetValidationIcon( line );
					}
					
					resetValidationIcon( line + 1 );
					adjustValidationIcons( -1 * line );
					update_qtip_position();
				} 
			}
					
			if( e.type != "keydown" ){
				var new_ga_val = $("#gene_area").val();
				var new_org = $("#species_select").val();
				
		    	if( e.type == "paste" || new_org != last_organism || last_gene_area_val != new_ga_val ){
		    		clearTimeout(gene_timeout);
		    		clip_icons();
		    		setLoadingIconsForGenes();
		    		gene_timeout = setTimeout(function(){
						update_text_from_area();
						last_gene_area_val = new_ga_val;
						validateGeneSymbols();
					}, gene_delay);
		    	}
			}
    	
		}
    });
    
    function hscroll_on(){
    	// remove listener so we don't get a loop
		$("#gene_area").unbind("scroll", scroll_callback);
		
		var scroll = $("#gene_area").scrollLeft();
		var ret = false;
		
		$("#gene_area").scrollLeft(1);
		if( $("#gene_area").scrollLeft() == 1 ){
			$("#gene_area").scrollLeft(scroll);
			ret = true;
		}
		
		// add back the listener
		$("#gene_area").bind("scroll", scroll_callback);
		return ret;
	}
    
    function clip_icons(){
    	if( hscroll_on() ){
    		var height = $("#gene_area").height() - $.scrollBarSize();
    		var top = $("#gene_area").scrollTop();
    		
//    		console.log("scroll is on");
    		$("#gene_validation_icons").css("clip", "rect(" + top + "px,14px," + (top + height) + "px,0px)");
    	} else {
//    		console.log("scroll is off");
    		$("#gene_validation_icons").css("clip", "auto");
    	}
    }
    
    function scroll_callback(){
    	adjust_icons_from_scroll();
    	clip_icons();
    }
    $("#gene_area").bind("scroll", scroll_callback);

    // replace separators with newlines as you type
    // makes the cursor disappear on some browsers
    // won't work if genes need separators in their names
    // $("#gene_area").keypress(sepToNewLine).keydown(sepToNewLine).keyup(sepToNewLine);
    
    // Avoid bug introduced in FireFox 3.6 for Mac OS: the Flash player steals
	// the keydown event,
    // preventing the user from editing the textarea when it overlaps the
	// CytoWeb area.
    var ua = navigator.userAgent;
    var isBuggyFF = ua.indexOf("Firefox") > -1 && ua.indexOf("Mac OS X") > -1;
    var doNotExpand = isBuggyFF && $("#graphBox").length > 0;
     
    
    $("#gene_area").autoexpand({
        minLines: 5,
        maxLines: doNotExpand ? 5 : 20
    });
    
    /***************************************************************************
	 * escape key to blur
	 **************************************************************************/
    
    $("#gene_area").add("#species_select").add("#species_text").keydown(function(e) {
        if(e.which && e.which == 27) {
            $(this).trigger("blur");
        }
    });
    
    $(".default_genes_link").live("mousedown", function(){
    	loadDefaultOrganismGenes();
    	var link = $(this);
    	
    	$("#gene_area").trigger("edit").trigger("paste");
    	
    	update_text_from_area();
    	updateGeneCount();
    	
		// setTimeout(function(){
			
			if( !link.hasClass("open") ){
    			validateGeneSymbols();
    		}
    		
    		if( link.hasClass("also_submit") ){
    			link.parents(".qtip").hide();
    			$("#findBtn").click();
    		}
		// }, gene_delay);
    		
    }).live("click", function(){
    	return false;
    });
    
});
