$(function(){

	if( $("#results_page").size() == 0 ){
		return; // ignore this js file if on front page
	}

    /**********************************************
     * NETWORKS TAB
     **********************************************/

	var _ignoreFilter = false;
	var _filterNetworks = function() {
		if (!_ignoreFilter) {
			CytowebUtil.filterNetworks();
		}
	};
	
	$("#networks_tab .checktree").checkTree({
        labelAction: "",
        checkedText: "<input type=\"checkbox\" class=\"widget\" checked=\"true\" />",
        halfCheckedText: "<input type=\"checkbox\" class=\"widget semitransparent\" checked=\"true\" />",
        uncheckedText: "<input type=\"checkbox\" class=\"widget\" />",
        collapsedText: "&nbsp;",
        expandedText: "&nbsp;",
        onCheck: function onCheckHandler(li) { _filterNetworks(); update_enable_by_checkbox_state(); },
    	onUnCheck: function onUnCheckHandler(li) { _filterNetworks(); update_enable_by_checkbox_state(); },
    	onExpand: function onExpandHandler(li) { update_expand_by_arrow_state(); },
    	onCollapse: function onCollapseHandler(li) { update_expand_by_arrow_state(); }
    });
	
	$("#networks_tab .checkbox").bind("mousedown", function(){
		if( !$(this).hasClass("checked") ){
			track("Networks", "Enable", "Check");
		} else {
			track("Networks", "Enable", "Uncheck");
		}
	});
	
	$("#networks_tab .arrow").bind("mousedown", function(){
		if( !$(this).hasClass("expanded") ){
			track("Networks", "Expand", "Arrow open");
		} else {
			track("Networks", "Expand", "Arrow close");
		}
	});
	
    // tool tips for networks tab check boxes
    $("#networks_tab").find(".checkbox").each(function(){
        $(this).attr("tooltip", "These checkboxes toggle whether networks are displayed.");
        $(this).addClass("checked");
    });
    
    $("#networks_tab").find(".widget").each(function(){
        $(this).attr("checked", "true");
    });
	
    // tool tips for check boxes in advanced
    $("#advanced_options").find(".checkbox").each(function(){
        $(this).attr("tooltip", "These checkboxes toggle whether networks are used by GeneMANIA.");
    });
    
    // bar graphs - build per cent bar based on % value found in tree
    $(".per_cent_bar").each(function(){
        var weight = $(this).parent().closest(":has(.per_cent_text)").find(".per_cent_text:first").attr("weight");
        var width = parseFloat(weight) * 100;
		if (! isNaN(width)) {
			$(this).find(".bar").css("width", width + "%");
		}
    });

    
    $("#networks_tab_check_all").click(function(){
        // It avoids the plugin calling the filter function for each top level checkbox:
    	_ignoreFilter = true;
    	$("#networks_widget").modifyCheckTree({checkAll: true});
    	// Now we cal filter the network, but only once:
    	_ignoreFilter = false;
    	_filterNetworks();
    	
    	$(this).addClass("active").siblings().removeClass("active");
    	
    	track("Networks", "Enable", "All");
    	return false;
    });
    
    $("#networks_tab_check_none").click(function(){
    	_ignoreFilter = true;
        $("#networks_widget").modifyCheckTree({checkNone: true});
        _ignoreFilter = false;
    	_filterNetworks();
    	
    	$(this).addClass("active").siblings().removeClass("active");
    	
    	track("Networks", "Enable", "None");
    	return false;
    });
    
    function update_enable_by_checkbox_state(){
        if( $("#networks_tab .checkbox.checked").size() == 0 ){
            $("#networks_tab_check_none").addClass("active").siblings().removeClass("active");
        } else if( $("#networks_tab .checkbox.checked").size() == $("#networks_tab .checkbox").size() ){
            $("#networks_tab_check_all").addClass("active").siblings().removeClass("active");
        } else {
            $("#networks_tab_check_none").add("#networks_tab_check_all").removeClass("active");
        }
    }
    update_enable_by_checkbox_state();
    
    $("#networks_tab_expand_all").click(function(){
        $("#networks_widget").modifyCheckTree({expandAll: true});
        
        $(this).addClass("active").siblings().removeClass("active");
        
        track("Networks", "Expand", "All");
    	return false;
    });
    
    $("#networks_tab_expand_top_level").click(function(){
        $("#networks_widget").modifyCheckTree({expandTopLevel: true});
        
        $(this).addClass("active").siblings().removeClass("active");
        
        track("Networks", "Expand", "Only top level");
    	return false;
    });
    
    $("#networks_tab_expand_none").click(function(){
        $("#networks_widget").modifyCheckTree({expandNone: true});
        
        $(this).addClass("active").siblings().removeClass("active");
        
        track("Networks", "Expand", "None");
    	return false;
    });
    
    function update_expand_by_arrow_state(){
        if( $("#networks_tab .arrow.collapsed").size() == 0 ){
            $("#networks_tab_expand_all").addClass("active").siblings().removeClass("active");
        } else if( $("#networks_tab .arrow.expanded").size() == 0 ){
            $("#networks_tab_expand_none").addClass("active").siblings().removeClass("active");
        } else if( $("#networks_tab .checktree_top_level > .arrow.expanded").size() == $("#networks_tab .checktree_top_level > .arrow").size() 
        && $("#networks_tab .checktree_top_level > .arrow.expanded").size() == $("#networks_tab .arrow.expanded").size() ) {
            $("#networks_tab_expand_top_level").addClass("active").siblings().removeClass("active");
        } else {
            $("#networks_tab_expand_none").add("#networks_tab_expand_all").add("#networks_tab_expand_top_level").removeClass("active");
        }
    }
    update_expand_by_arrow_state();
    
    $("#networks_tab_sort_by_weight").click(function(){
        $("#networks_widget").listSort({
            value: ".per_cent_text:first",
            descending: true,
            attribute: "weight"
        });
        
        $(this).addClass("active").siblings().removeClass("active");
        
    	track("Networks", "Sort", "By weight");
    	return false;
    }).click();
    
    $("#networks_tab_sort_by_name").click(function(){
        $("#networks_widget").listSort({
            value: ".name:first",
            descending: false
        });
        
        $(this).addClass("active").siblings().removeClass("active");
        
        track("Networks", "Sort", "By name");
    	return false;
    });
   
    var highlightTimeout;
    var highlightDelay = 150;
    
    function unhighlight(){
    	clearTimeout(highlightTimeout);
    	highlightTimeout = setTimeout(function(){
    		CytowebUtil.unhighlightGenesAndEdges();
    	}, highlightDelay);
    }
    
    $("#networks_widget .checktree_network_group > .label").bind("mouseover", function(){
    	$(this).addClass("highlight");
    	var id = $(this).parent().attr("netid");
    	
    	clearTimeout(highlightTimeout);
    	highlightTimeout = setTimeout(function(){
    		CytowebUtil.highlightNetworkGroup( id );
    	}, highlightDelay);
    }).bind("mouseout", function(){
    	$(this).removeClass("highlight");
    	
    	unhighlight();
    });
    
    $("#networks_widget .checktree_network > .label").bind("mouseover", function(){
    	$(this).addClass("highlight");
    	var id = $(this).parent().attr("netid");
    	
    	clearTimeout(highlightTimeout);
    	highlightTimeout = setTimeout(function(){
    		CytowebUtil.highlightNetwork( id );
    	}, highlightDelay);
    }).bind("mouseout", function(){
    	$(this).removeClass("highlight");
    	
    	unhighlight();
    });
    
    $("#networks_widget .checktree_attr_group > .label").bind("mouseover", function(){
    	$(this).addClass("highlight");
    	var id = $(this).parent().attr("attrid");
    	
    	clearTimeout(highlightTimeout);
    	highlightTimeout = setTimeout(function(){
    		CytowebUtil.highlightAttributeGroup( id );
    	}, highlightDelay);
    }).bind("mouseout", function(){
    	$(this).removeClass("highlight");
    	
    	unhighlight();
    });
    
    $("#networks_widget .checktree_attr > .label").bind("mouseover", function(){
    	$(this).addClass("highlight");
    	var id = $(this).parent().attr("attrid");
    	
    	clearTimeout(highlightTimeout);
    	highlightTimeout = setTimeout(function(){
    		CytowebUtil.highlightAttribute( id );
    	}, highlightDelay);
    }).bind("mouseout", function(){
    	$(this).removeClass("highlight");
    	
    	unhighlight();
    });
    
    progress("networks");
    
});