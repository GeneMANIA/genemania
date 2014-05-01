$(function(){
	
	if( $("#phrase_and_go").size() == 0 && $("#query_line").size() == 0 ){
		return; // ignore this js file if no query area
	}

    /***************************************************************************
	 * expandable advanced behaviour
	 **************************************************************************/
    // expand
	var width_set = false;
    $("#advanced_options_closed .advanced_options_toggle a").click(function() {
    	// refreshTreeFlag = false;
    	// switch
        $("#advanced_options_closed").hide();
        $("#advanced_options").show();
        $("#advanced_options_open").show();
        $("#relatedGenes").addClass("advanced_open");
        $("#cytoscape_lite").addClass("advanced_open");
        $("#side_bar").addClass("advanced_open");
        // DO NOT USE the slide option!!! It rebuilds the tree, causing a bug
		// that
        // prevents the default networks to be checked.
        // ***********************************************************************
        // $("#advanced_options_open").show("slide", { direction: "up" },
		// ANI_SPD);
        // ***********************************************************************

        if( !width_set ){
        	var padding = $("#advanced_options").outerWidth() - $("#advanced_options").width();
        	var width = $("#advanced_line").prev().width() - padding;
	        $("#advanced_options").width( width );
	        width_set = true;
        }
        
        track("Advanced options", "Open");
        
        adjustNetworksLoaderCSS();

        return false;
    });
    
    // close advanced on hide
    $("#advanced_options_open .advanced_options_toggle a").click(function() {	
        close_advanced_options();
        
        return false;

    });
    
    if( $("#results_page").size() > 0 ){
    
		$("#cytoscape_lite").add("#side_bar").add("#footer").add(".ui-layout-resizer").bind("mousedown", function(e){
			if( $("#advanced_options_open").is(":visible") ){
				close_advanced_options();
			}
		});
    
    }
    
    function close_advanced_options(){
        // switch
        $("#advanced_options_closed").show();
        $("#advanced_options_open").hide();
        // $("#advanced_options_open").fadeOut(ANI_SPD);
        $("#advanced_options").hide();
        $("#relatedGenes").removeClass("advanced_open");
        $("#cytoscape_lite").removeClass("advanced_open", ANI_SPD);
        $("#side_bar").removeClass("advanced_open", ANI_SPD);
        // DO NOT USE the slide option!!! It rebuilds the tree, causing a bug
		// that
        // prevents the default networks to be checked.
        // ***********************************************************************
        // $("#advanced_options").hide("slide", { direction: "up" }, ANI_SPD);
        // ***********************************************************************
        
        track("Advanced options", "Close");
    }
    
    
	$("#advanced_options_closed .advanced_options_toggle a").bind("click", function(){
		loadNetworks();
		loadAttributeGroups();
	});
});
