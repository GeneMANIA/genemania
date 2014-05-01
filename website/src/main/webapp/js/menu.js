$(function(){
    
	if( $("#results_page").size() == 0 ){
		return; // ignore this js file if on front page
	}
    
    /**********************************************
    menus
    **********************************************/
	
	
    $(".menu").menu({
        onMenuItemOpen: function(li){
            if( li.attr("id") == "menu_search" ) {
                //$("#menu_search_input").width( $("#menu_search_input").parent().width() * 0.95 );
            }
        },
        
        onMenuItemCheck: function(li){
        	if(li.hasClass("ui-state-disabled")){
        		return;
        	}
        	
            switch( li.attr("id") ) {
            case "menu_show_labels":
            	CytowebUtil.showNodeLabels(true);
            	track("Visualisation", "Show labels");
                break;
            case "menu_publication_labels":
            	CytowebUtil.showPublicationLabels(true);
            	track("Visualisation", "Enable publication labels");
                break;
            case "menu_show_panzoom":
            	CytowebUtil.showPanZoomControl(true);
            	track("Visualisation", "Show panzoom");
            	break;
            case "menu_merge_links":
            	CytowebUtil.mergeEdges(true);
            	track("Visualisation", "Merge edges");
            	break;
            case "menu_transparent_edges":
            	CytowebUtil.transparentEdges(true);
            	track("Visualisation", "Show edges as semitransparent");
            	break;
            }
        },
        
        onMenuItemUncheck: function(li){
        	if(li.hasClass("ui-state-disabled")){
        		return;
        	}
        	
            switch( li.attr("id") ) {
            case "menu_show_labels":
            	CytowebUtil.showNodeLabels(false);
            	track("Visualisation", "Hide labels");
                break;
            case "menu_publication_labels":
            	CytowebUtil.showPublicationLabels(false);
            	track("Visualisation", "Disable show publication labels");
                break;
            case "menu_show_panzoom":
            	CytowebUtil.showPanZoomControl(false);
            	track("Visualisation", "Hide panzoom");
            	break;
            case "menu_merge_links":
            	CytowebUtil.mergeEdges(false);
            	track("Visualisation", "Unmerge edges");
            	break;
            case "menu_transparent_edges":
            	CytowebUtil.transparentEdges(false);
            	track("Visualisation", "Show edges as opaque");
            	break;
            }
        },
        
        onMenuItemClick: function(li) {
        	if(li.hasClass("ui-state-disabled")){
        		return;
        	}
        	
            switch( li.attr("id") ) {
	            case "menu_remove_selected":
	        		CytowebUtil.removeSelectedGenes();
	        		track("Search", "Search removing selected genes");
	                break;
	            case "menu_search_selected":
	        		CytowebUtil.searchWithSelectedGenes();
	        		track("Search", "Search with selected genes");
	                break;
	            case "menu_add_selected":
	        		CytowebUtil.addSelectedGenesToSearch();
	        		track("Search", "Add selected genes to search");
	                break;
            	case "menu_generate_report":
            		CytowebUtil.generateReport();
            		track("File", "Generate report");
	                break;
            	case "menu_export_params":
            		CytowebUtil.exportParams();
            		track("File", "Save params as TXT");
	                break;
            	case "menu_export_params_json":
            		CytowebUtil.exportParamsJson();
            		track("File", "Save params as JSON");
	                break;
            	case "menu_export_networks":
            		CytowebUtil.exportNetworks();
            		track("File", "Save networks as TXT");
	                break;
            	case "menu_export_genes":
            		CytowebUtil.exportGenes();
            		track("File", "Save genes as TXT");
	                break;
            	case "menu_export_go":
            		CytowebUtil.exportGo();
            		track("File", "Save functions as TXT");
	                break;
            	case "menu_export_interactions":
            		CytowebUtil.exportInteractions();
            		track("File", "Save interactions as TXT");
	                break;
            	case "menu_export_network":
            		CytowebUtil.exportNetwork();
            		track("File", "Save as TXT");
	                break;
            	case "menu_export_attributes":
            		CytowebUtil.exportAttributes();
            		track("File", "Save attributes as TXT");
	                break;
	            case "menu_export_svg":
	            	CytowebUtil.exportSvg();
	            	track("File", "Save as SVG");
	                break;
	            case "menu_reset_layout":
	            	CytowebUtil.recomputeLayout();
	            	track("Visualisation", "Recompute layout");
	                break;
	            case "menu_neighbors":
	            	CytowebUtil.highlightFirstNeighbors();
	            	track("Visualisation", "Highlight first neighbours");
	            	break;
	            case "menu_neighbors_clear":
	            	CytowebUtil.clearFirstNeighborsHighlight();
	            	track("Visualisation", "Unhighlight first neighbours");
	            	break;
	            case "menu_close_tooltips":
	                $(".qtip").hide().remove();
	                $("#menu_close_tooltips").addClass("ui-state-disabled");
	                track("Tooltips", "Close");
	                break;
            }
        },
        
        onMenuItemSelect: function(li) {
        	if(li.hasClass("ui-state-disabled")){
        		return;
        	}
        	
            switch( li.attr("id") ) {
	            case "menu_legend":
	            	net_legend_popup();
	            	
	                $("#legend").fadeIn(ANI_SPD, function(){
	            		$("#legend .content").show();
	            	});
	                
	                track("Networks legend", "Show");
	                break;

	           case "menu_go_legend":
	                go_legend_popup();
	                
	                $("#go_legend").fadeIn(ANI_SPD, function(){
	            		$("#go_legend .content").show();
	            	});
	                
	                track("Functions legend", "Show");
	                break;
            }
        },
        
        onMenuItemDeselect: function(li) {
        	if(li.hasClass("ui-state-disabled")){
        		return;
        	}
        	
            switch( li.attr("id") ) {
	            case "menu_legend":
	                $("#legend .content").fadeOut(ANI_SPD, function(){
	                    $("#legend").effect("transfer", { to: "#menu_legend" }, ANI_SPD);
	                    $("#legend").fadeOut(ANI_SPD);
	                });
	                
	                $.tooltip.hide(toolTipOptions);
	                track("Networks legend", "Hide");
	                break;
	           case "menu_go_legend":
	                $("#go_legend .content").fadeOut(ANI_SPD, function(){
	                    $("#go_legend").effect("transfer", { to: "#menu_go_legend" }, ANI_SPD);
	                    $("#go_legend").fadeOut(ANI_SPD);
	                });
	                
	                $.tooltip.hide(toolTipOptions);
	                track("Functions legend", "Hide");
	                break;
	        }
        }
    });
    
    function net_legend_popup(){
    	var li = $("#menu_legend");
    	li.menuPopup("legend", { title: "Networks legend", 
            hiddenToolTip: "Use the toggle button above to restore the network legend when hidden." });

		$("#legend .content").html('<table> <thead><tr><th>Colour</th><th>Name</th></tr></thead> <tbody></tbody></table>');
		
		$("#networks_widget").children("li").each(function(){
			var colour = $(this).find(".bar:first").css("background-color");
			if (! $(this).find("input").attr("disabled")) {
				var name = $(this).find(".name:first").text();
				var weight = $(this).find(".per_cent_text:first").text();
				$("#legend .content tbody").append('<tr>' +
					'<td><div class="legend_square" style="background-color: ' + colour + '"></div></td>' +
					'<td>' + name + '</td>' +
					'</tr>');
			}
		});
		
		$("#legend table").tablesorter({
			sortList: [ [1,0] ]
		}); 
    }
    
    function go_legend_popup(){
        $("#menu_go_legend").menuPopup("go_legend", {
            title: "Functions legend", 
            hiddenToolTip: "Use the toggle button above to restore the functions legend when hidden.",
            resizable: false
        });
        
        if( $("#go_legend .instructions").size() == 0 ){
            $("#go_legend .content").append('<div class="header instructions"><p>No colorings have been added.</p><p><a class="action_link" href="#">Open the functions tab</a> to add colorings.</p></div>');
            
            $("#go_legend .instructions a").bind("click", function(){
                $("#side_bar .ui-tabs-nav a[href=#go_tab]").click();
                return false;
            });
        }
    }
    
    $("#menu_search_item").click(function(){
        return false;
    });
    
    $("#gene_search").focus(function(){
        $(this).removeClass("light");
        $(this).val("");
    }).blur(function(){
        $(this).addClass("light");
        $(this).val("Find a gene by typing its name here");
    }).blur();
    
    // default enabled/checked
    
    $(window).load(function(){
    	$("#menu_neighbors").addClass("ui-state-disabled");
    	$("#menu_neighbors_clear").addClass("ui-state-disabled");
    	$("#menu_search_selected").addClass("ui-state-disabled");
    	$("#menu_add_selected").addClass("ui-state-disabled");
    	$("#menu_remove_selected").addClass("ui-state-disabled");
    	$("#menu_show_labels").add("#menu_show_panzoom").add("#menu_transparent_edges").find(".ui-menu-check-icon").addClass("ui-menu-checked");
        go_legend_popup();
        net_legend_popup();
        $("#selected_info").hide();
        $("#go_legend").hide();
        $("#legend").hide();
        $("body").click();
    });
    
    // Do not close the menu when user clicks this kind of menu item:
    $(".menu-option").toggle(function(){ /*do nothing*/ }, function(){ /*do nothing*/ });
});