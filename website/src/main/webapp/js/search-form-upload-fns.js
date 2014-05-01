function refreshUserNetworksEventHandlers() {
	// console.log("refresh handlers...");

	$("div[id^='descriptionFor']").click(
		function(evt) {
			evt.stopPropagation();
		}
	);
	
	var UNNAMED_STRING = "Unnamed network upload";
	
	$("input[id^='networkNameEdit']").blur( function() {
		var networkId = $(this).attr("id").substring("networkNameEdit".length);
		var networkName = $(this).val();
		if ($.trim(networkName) == "") { networkName = UNNAMED_STRING; }
		var networkDescription = $("#networkDescriptionEdit"+networkId).val();
		updateUserNetworkInfo(networkId, networkName, networkDescription);
	});
	
	$("input[id^='networkNameEdit']").keyup(
		function() {
			var netId = $(this).attr("id").substring("networkNameEdit".length);
			var name = $(this).val();
			if( $.trim(name) == "" ) { name = UNNAMED_STRING; }
			
			$("#" + netId + " label").html(name);
		}
	).bind("blur", function(){
		if( $.trim($(this)).val() == "" ){
			$(this).val(UNNAMED_STRING);
		}
	}).bind("focus", function(){
		if( $.trim($(this).val()) == UNNAMED_STRING ){
			$(this).val("");
		}
	}).bind("keydown", function(e){
		if( e.which == 13 ){
			return false;
		}
	});
	
	$("textarea[id^='networkDescriptionEdit']").blur(function() {
		var networkId = $(this).attr("id").substring("networkDescriptionEdit".length);
		var networkName = $("#networkNameEdit"+networkId).val();
		var networkDescription = $(this).val();
		updateUserNetworkInfo(networkId, networkName, networkDescription);
	});
	
	endNetworksLoader();
}

function showUploadProgress(networkName) {
	var org = $("#species_select").val();
	var progresDivEl = "<div class='uploading_query_network' organism='" + org + "' name='" + networkName + "'><span style='float:left'><span class='uploadStatusIcon upload_progress'/></span><label>" + networkName + "</label></div>";

	
	$(".query_networks[group=0][organism="+org+"]").append(progresDivEl);
	$(".query_network_group[group=0][organism="+org+"]").click();
}

function showUploadCompleted(data) {

	var name = $(".uploading_query_network").attr("name");
	var organism = $(".uploading_query_network").attr("organism");
	$(".uploading_query_network").remove();
	
	if( data.error && data.network == null ){
		var date = new Date();
		
		function pad(num){
			return ( num < 10 ? "0" : "" ) + num;
		}
		
		var date_str = date.getFullYear() + "-" + pad(date.getMonth() + 1) + "-" + pad(date.getDate()) + " at " + pad(date.getHours()) + ":" + pad(date.getMinutes()) + ":" + pad(date.getSeconds());
		
		data.network = {
			name: name,
			id: 0,
			metadata: {
				comment: date_str
			},
			tags: []
		};
	}

	var group = {
		id: 0
	};
	

	
	var html = makeNetworkHtml(organism, group, data.network, data.error);
	
	var container = $("#networksPanel .query_networks[group=" + group.id + "][organism=" + organism + "] table");
    container.append(html);
	refreshAllGroupCounts();
	updateChecktreeItem( $(".query_group_checkbox[group=" + group.id + "][organism=" + organism + "]") );
	validateTree();
}

function deleteUserNetwork( query_network_div ) {
	
	var container = query_network_div;
	var network_id = container.attr("network");
	var organism = query_network_div.attr("organism");
	
	if(network_id > 0){
		console.log("can't delete non user network");
		return;
	}
	
	function remove() {
		var organism_id = parseInt( $("#species_select").val() );
		
		container.addClass("deleting");
		
		if( network_id == 0 ){
			container.remove();
			console.log("don't post to the server the deletion of failed uploads");
			return;
		}
		
		$.ajax({
			dataType: "json",
			data: {
				organism_id: organism_id,
				network_id: network_id
			},
			error: function(request, status, error){
				console.log("delete error");
				container.remove();
				refreshAllGroupCounts();
				updateChecktreeItem( $(".query_group_checkbox[group=0][organism=" + organism + "]") );
			},
			success: function(data, status, request){
				
				if( data.error ){
					console.log(data.error);
				}
				
				container.remove();
				refreshAllGroupCounts();
				updateChecktreeItem( $(".query_group_checkbox[group=0][organism=" + organism + "]") );
			},
			type: "POST",
			url: absoluteUrl("json/delete_network")
		});
		
		
	}

	$('<div><p>Are you sure you want to remove this network?</p></div>').dialog({
		title: "Confirmation of removal",
		buttons: {
			"Yes, remove it.": function(){ remove(); $(this).dialog("close"); },
			"No, keep it.": function(){ $(this).dialog("close"); }
		},
		modal: true,
		closeOnEscape: true,
    	resizable: false,
    	width: 300,
    	minHeight: 0
	});
}

function updateUserNetworkInfo(networkId, networkName, networkDescription) {
	var organismId = $("#species_select").val();
	if (networkDescription == null) { networkDescription = '' };
	networkDescription = $.trim(networkDescription.replace(/(\n|\r)+/, '').replace(/(\n|\r)+$/, '')); // trim
																										// newline
																										// chars
																										// and
																										// spaces
	$.post("json/upload", { operation: "update", organism: organismId, networkId: networkId, networkName: networkName, networkDescription: networkDescription } );
}