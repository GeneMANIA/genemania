
define(['jquery', 'app/constants', 'app/details'], function($, constants, details) {

	var menu = {};

	menu.setupMenu = function() {
		$('#new-organism').on('click', function() {
			node = menu.newOrganismNode();
			$("#details").hide().html(details.formatNodeDetails(node)).fadeIn('fast');
			details.setupForm(node);
		});
	}
	
	menu.newOrganismNode = function() {
		nodeData = {
				title: "New organism",
		        tooltip: "This child node was added programmatically.",
		        isFolder: true,
				data: {
					type: constants.ORGANISM_NODE,
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

	return menu;
});