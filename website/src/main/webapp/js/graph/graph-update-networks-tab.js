CytowebUtil.updateNetworksTab = function() {
//console.log("update networks tab");
	
	// Get the networks legend:
	var legend = {/*color->networkGroupId*/};
	var edges = CytowebUtil.edges();
	var networksList = {};
	
	if (edges) {
		$.each(edges, function(i, e) {
			// Considering only one data attribute per color in the mapper!
			legend[e.color] = e.data['networkGroupId'];
			
			for(var id in e.data.networkIdToWeight){
				networksList[id] = true;
			}
		});
	}
//console.log(networksList);
	
	// Set network colors:
	for (var netColor in legend) {
	    var netId = legend[netColor];
		$("#networks_widget > .checktree_network_group[netid="+ netId +"]  .per_cent_bar > .bar").css('background-color', netColor);
    }
	// Greyed out networks with no correspondent edges:
	var spacer = '<div class="checkbox_spacer not_found">&nbsp;</div>';
	
	$("#networks_widget .checktree_network").each(function(){
		var id = $(this).attr("netid");
		
		if ( !networksList[id] ) {
			$(this).find(".bar:first").addClass("disabled");
			$(this).find(".checkbox").remove();
			$(this).find("input[type=checkbox]").remove();
			$(this).find(".arrow:first").after(spacer);
		}
	});

	$("#networks_widget .checktree_network_group").each(function(){
		if ($(this).find(".checktree_network .checkbox").length === 0) {
			// No enabled network for this network group:
			$(this).find(".bar:first").addClass("disabled");
			$(this).find(".checkbox").remove();
			$(this).find("input[type=checkbox]").remove();
			$(this).find(".arrow:first").after(spacer);
		}
	});
	
	var tooltip = $.i18n("networks_tab.not_found.tooltip");
	$("#networks_widget .not_found").attr("tooltip", tooltip);
	$("#networks_widget .not_found").parent().find(".per_cent_bar div").attr("tooltip", tooltip);
};