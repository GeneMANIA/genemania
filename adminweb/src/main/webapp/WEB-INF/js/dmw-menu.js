
var dmw = function(my, $) {
	
	my.setupMenu = function() {
		$('#new-organism').on('click', function() {
			node = my.newOrganismNode();
			$("#details").hide().html(my.formatNodeDetails(node.data)).fadeIn('fast');			
			my.setupForm(node);
		});
	}
	
	my.newOrganismNode = function() {
		node = { 
				title: "New organism",
		        tooltip: "This child node was added programmatically.",
		        isFolder: true,
				data: {
					type: my.ORGANISM_NODE,
					organismId: '',
					title: 'New organism',
					code: '',
				}
		};
		
	    var rootNode = $("#tree").dynatree("getRoot");
	    rootNode.addChild(node);
	    key = node.key;
	    console.log(key);
		$("#tree").dynatree("getTree").activateKey(key);
		$("#tree").dynatree("getTree").getNodeByKey(key).select();
		return node;
	}
	return my;
}(dmw || {}, $);