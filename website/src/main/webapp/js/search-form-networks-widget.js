$(function(){
	
	if( $("#phrase_and_go").size() == 0 && $("#query_line").size() == 0 ){
		return; // ignore this js file if no query area
	}
	
    $(".default_networks_link").live("click", function(){
    	
    	$("#network_selection_select_default").trigger("click");
    	
    	var link = $(this);
   		
		if( link.hasClass("also_submit") ){
			link.parents(".qtip").hide();
			$("#findBtn").click();
		}
    	
    	return false;
    });
    
   
    
    $("#network_selection_select_all").click(function() {
    	$(".query_network_group input").filter("[organism=" + $("#species_select").val() + "]").removeClass("half_checked");
    	$("#networkTree input").filter("[organism=" + $("#species_select").val() + "]").attr("checked", true);
    	
    	var uploaded_group_input = $(".query_network_group input").filter("[organism=" + $("#species_select").val() + "][group=0]");
    	var uploaded_inputs = $(".query_network input").filter("[organism=" + $("#species_select").val() + "][group=0]");
    	if( uploaded_inputs.size() == 0 ){
    		uploaded_group_input.removeAttr("checked");
    	}
    	
    	validateTree();
    	refreshAllGroupCounts(); 
    	
    	return false;
    });
    
    $("#network_selection_select_none").click(function() {
    	$(".query_network_group input").filter("[organism=" + $("#species_select").val() + "]").removeClass("half_checked");
    	$("#networkTree input").filter("[organism=" + $("#species_select").val() + "]").attr("checked", false);
    	validateTree();
    	refreshAllGroupCounts(); 
    	
    	return false;
    });
    
    $("#network_selection_select_default").click(function() {
    	restoreDefaultNetworks();
    	validateTree();
    	refreshAllGroupCounts(); 
    	
    	return false;
    }).addClass("selected_sorting");
    
    $(".query_network, .query_network_group").live("mouseover", function() {
    	$(this).addClass("select_hover")	
    });
    
    $(".query_network, .query_network_group").live("mouseout", function() {
    	$(this).removeClass("select_hover")	
    });
    
	// query networks sorting
	$("#networks_section .sort").click(function(){
		
		var criteria = $(this).attr("by");
		sortQueryNetworks(criteria);
		return false;
	});
    
	addNetworkCheckBoxListeners();
	
    
    
});