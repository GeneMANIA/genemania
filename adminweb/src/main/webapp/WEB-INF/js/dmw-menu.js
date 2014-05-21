
var dmw = function(my, $) {
	
	my.setupMenu = function() {
		$('#new-organism').on('click', function() {
			node = my.newOrganismNode();
			$("#details").hide().html(my.formatNodeDetails(node.data)).fadeIn('fast');			
			my.setupForm(node);
		});
	}
	
	my.newOrganismNode = function() {
		nodeData = {
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
		
	    var rootNode = $("#tree").fancytree('getRootNode');
	    var newNode = rootNode.addNode(nodeData, 'child');
	    key = newNode.key;
		$("#tree").fancytree("getTree").activateKey(key);
		$("#tree").fancytree("getTree").getNodeByKey(key).setSelected();
		return newNode;
	}
	return my;
}(dmw || {}, $);