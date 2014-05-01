$(function(){
	
	if( $("#results_page").size() == 0 ){
		return; // ignore this js file if on front page
	}
	
    /**********************************************
     * GENES TAB
     **********************************************/

    $("#genes_tab .checktree").checkTree({
        labelAction: "select",
        collapsedText: "&nbsp;",
        expandedText: "&nbsp;",
        onExpand: function(li){ update_sort_and_expand_by_arrow_state(); },
        onCollapse: function(li) { update_sort_and_expand_by_arrow_state(); }
    });
    
    $("#genes_tab .label").click(function(){
    	var nodeId = $(this).parent("li").attr("id").substring("gene".length);

    	if ($(this).hasClass("selected")) {
    		CytowebUtil.selectNode(nodeId);
    		track("Genes", "Select entry");
    	} else {
    		CytowebUtil.deselectNode(nodeId);
    		track("Genes", "Deselect entry");
    	}
    });
    
    var show_timeout;
    var clear_timout;
    var time = 150;
    
    $("#genes_tab .label").bind("mouseover", function(){
        var nodeId = $(this).parent("li").attr("gene");
        clearTimeout(clear_timout);
        
        show_timeout = setTimeout(function(){
            CytowebUtil.highlightGene(nodeId);
        }, time);
        
    }).bind("mouseout", function(e){
        var nodeId = $(this).parent("li").attr("gene");
        clearTimeout(show_timeout);
        
        clear_timout = setTimeout(function(){
        	CytowebUtil.unhighlightGene();
        }, time);
    });
	
	// TODO remove if we decide to show source gene scores
	// increase source genes score so sort by score puts them at the top
	// (the number isn't shown anyway)
	$("#genes_tab .score_text.source_score_true").each(function(){
	    var int_val = parseInt( $(this).text() );
	    
	    $(this).text( int_val + 1 );
	});
	
    $("#genes_tab_expand_all").click(function(){
        $("#genes_widget").modifyCheckTree({expandAll: true});
        $(this).addClass("active").siblings().removeClass("active");
        
        track("Genes", "Expand", "All");
        return false;
    });
    
    $("#genes_tab_expand_none").click(function(){
        $("#genes_widget").modifyCheckTree({expandNone: true});
        $(this).addClass("active").siblings().removeClass("active");
        
        track("Genes", "Expand", "None");
        return false;
    });
    
    $("#genes_tab_sort_by_name").click(function(){
        $("#genes_widget").listSort({
           value: ".gene_name:first",
           descending: false
       });
       $(this).addClass("active").siblings().removeClass("active");
       
       track("Genes", "Sort", "By name");
       return false;
    }).click();
    
    $("#genes_tab_sort_by_score").click(function(){
        $("#genes_widget").listSort({
            value: ".source_score_true",
            descending: true
        });
        $("#genes_widget").listSort({
        	value: ".score_text:first",
        	descending: $("#genes_tab .score_text").attr("score") == null
        });
        
        $(this).addClass("active").siblings().removeClass("active");
        
        track("Genes", "Sort", "By score");
        return false;
    }).click();
    
    
    
    // TODO: remove when we support p-values
    
    // ranks of result genes
    var rank = 0;
    var prevScore = null;
    $("#genes_widget .source_false .score_text").each(function(i){ 
    	var $ele = $(this);
    	var score = parseFloat( $ele.text() );
    	
    	if( score === prevScore ){ 
    		// then use the same rank as before
    	} else { 
    		rank++; // otherwise, we're the next in line
    	}
    	
    	$ele.attr("score", score);
    	$ele.html( rank );
    });
    
    
    
    // ranks of query genes are  0
    $("#genes_widget .source_true .score_text").each(function(i){
    	var $ele = $(this);
    	var score = parseFloat( $ele.text() );
    	var rank = 0;
    	
    	$ele.attr("score", score);
    	$ele.html( rank );
    });
    
	$("#genes_tab .arrow").bind("mousedown", function(){
		if( !$(this).hasClass("expanded") ){
			track("Genes", "Expand", "Arrow open");
		} else {
			track("Genes", "Expand", "Arrow close");
		}
	});
    
    function update_sort_and_expand_by_arrow_state(){
        if( $("#genes_tab .arrow.collapsed").size() == 0 ){
            $("#genes_tab_expand_all").addClass("active").siblings().removeClass("active");
        } else if( $("#genes_tab .arrow.expanded").size() == 0 ){
            $("#genes_tab_expand_none").addClass("active").siblings().removeClass("active");
        } else {
            $("#genes_tab_expand_none").add("#genes_tab_expand_all").removeClass("active");
        }
    }
    update_sort_and_expand_by_arrow_state();
    
    function empty_description(ele){
        return ele.text().match(/\s*[Nn]\/[Aa]\s*/);
    }
    
    $("#genes_tab .mini_description").each(function(){
        if( empty_description($(this)) ){
            $(this).hide();
        }
    });
    
    $("#genes_tab .description").each(function(){
        if( empty_description($(this)) ){
            $(this).html('<span class="ui-icon ui-icon-info"></span> No description is available.');
        }
    });
    
    $("#search_with_selected_button").click(function(){
    	
    	if( !$(this).hasClass("ui-state-disabled") ){
    		CytowebUtil.searchWithSelectedGenes();
    	}
    	
    });
    
    $("#genes_tab_select_all").click(function(){
    	CytowebUtil.selectAll();
    	return false;
    });
    
    $("#genes_tab_select_none").click(function(){
    	CytowebUtil.selectNone();
    	return false;
    });
    
    $("#genes_tab_select_query").click(function(){
    	CytowebUtil.selectQueryGenes();
    	return false;
    });
    
    
    
    progress("genes");
    
});
