
CytowebUtil._lastFilter = null; // filter function

var _filterTimeout = null;
CytowebUtil.filterNetworks = function() {

	clearTimeout(_filterTimeout);
	_filterTimeout = setTimeout(function(){
		filter();
	}, 25);
	
	function filter(){
		if (_vis) {
			// Get checked network groups:
			var checkedGr = { netid: {}, attrid: {} };
			var halfcheckedGr = { netid: {}, attrid: {} };
			var count = 0;
			var edges = CytowebUtil.edges();
			var nodes = CytowebUtil.nodes();
			
			$("#networks_tab .checktree_top_level").each(function() {
				var $this = $(this);
				var id = $this.attr("netid") || $this.attr("attrid");
				var idAttr = $this.attr("netid") ? "netid" : "attrid";
				
				if( $this.hasClass("checktree_attr_group") ){
					id = $this.attr("attrid");
				}
				
				var $checkbox = $this.find(".checkbox:first");
				if ( $checkbox.hasClass("checked") ) {
					checkedGr[idAttr][id] = true;
				}
				if ( $checkbox.hasClass("half_checked") ) {
					halfcheckedGr[idAttr][id] = true;
				}
				if ( $checkbox.size() > 0 ) {
					count++;
				}
			});
	
			// Get checked individual networks: 
			var checkedNet = { netid: {}, attrid: {} };
			for(var grId in halfcheckedGr['netid']){			
				$("#networks_tab .checktree_network_group[netid=" + grId + "]").find(".checktree_network").each(function() {
					var id = $(this).attr("netid");
					if ($(this).find(".checked").length > 0) {
						checkedNet['netid'][id] = true;
					}
				});
			}
			for(var grId in halfcheckedGr['attrid']){	
				$("#networks_tab .checktree_attr_group[attrid=" + grId + "]").find(".checktree_attr").each(function() {
					var id = $(this).attr("attrid");
					if ($(this).find(".checked").length > 0) {
						checkedNet['attrid'][id] = true;
					}
				});
			}
			//console.log("individual networks");
//			console.log(checkedGr, halfcheckedGr, checkedNet);
			//console.log("groups");
			
			function filterFunction(edge){
				//console.log("Checking edge:");
				//console.log(edge);

//				if( edge.data.networkGroupName === "Uploaded" ){
//					console.log( edge );
//					console.log( checkedGr );
//				}	
				
				var grId = edge.data.networkGroupId;
				var idAttr = "netid";
				
				if( grId === null || grId === undefined || grId === "" || (edge.data.attributeId != null && edge.data.attributeId + 0 === edge.data.attributeId) ){
					grId = edge.data.attributeGroupId;
					idAttr = "attrid";
				}
				
				//console.log("Check the group " + edge.data.networkGroupId + " first, which is faster:");
				if ( checkedGr[idAttr][grId] ) {
					//console.log("all of group " + grId + " checked");
//					console.log('checked group');
					return true;
					
				} else if( halfcheckedGr[idAttr][grId] ){
//					console.log('half checked group');
					
					if( $("#networks_tab .checktree_top_level["+ idAttr +"=" + grId + "]").find(".checkbox.half_checked").size() > 0 ){
						//console.log("Still have to verify each individual network since group " + grId + " is half checked");
						var networksMap = edge.data.networkIdToWeight;
						//console.log("Edge has networks: ");
						//console.log(networksMap);
						
						for (var id in networksMap) {
						    if ( checkedNet[idAttr][id] ) {
						    	//console.log("The edge (" + edge.data.id + ") has at least one checked network (" + id + ")");
						    	return true;
						    }
						}
						
						if( checkedNet[idAttr][edge.data.attributeId] ){
							return true;
						}
					} else {
						//console.log("Group " + grId + " is fully checked so edge is OK");
						return true;
					}
				} else {
					//console.log("No networks checked for edge");
					return false;
				}
			}
			
			var filterList = [];
			$.each(edges, function(i, edge){
				if( filterFunction(edge) ){
					//console.log('call ff true', edge);
					filterList.push(edge.data.id);
				} else {
					//console.log('call ff false', edge);
				}
			});
			
			$.each(nodes, function(i, node){
				if( node.data.attribute ){
					if( $("#networks_widget .checktree_attr[attrid="+ node.data.attributeId +"] :checkbox:first").is(":checked") ){
						filterList.push(node.data.id);
					}
				} else {
					filterList.push(node.data.id);
				}
			});
			
			if (count === $("#networks_tab .checktree_top_level > .checkbox.checked").length) {
				//console.log("All of the enabled checkboxes are checked");
				_vis.removeFilter(false);
				CytowebUtil._lastFilter = null;
			} else {
				CytowebUtil._lastFilter = filterList;
				_vis.filter(filterList, true);
			}
		}
	}
};
