
// javascript 'module' thing
var dmw = function(my) {
	
	// constants
	my.INIT_ORGANISM_ID = 1;
	my.ANIMATION_SPEED = "fast";
	
	// these match the node type in the TreeNode objects from the server
	my.GROUP_FOLDER_NODE = "group_folder_node";
	my.NETWORKS_FOLDER_NODE = "networks_folder_node";
	my.IDENTIFIERS_FOLDER_NODE = "identifiers_folder_node";
	my.FUNCTIONS_FOLDER_NODE = "functions_folder_node";
	my.FUNCTIONS_NODE = "functions_node";
	my.ATTRIBUTES_FOLDER_NODE = "attributes_folder_node";
	my.NETWORK_NODE = "network";
	my.IDENTIFIERS_NODE = "identifiers";
	my.ORGANISM_NODE = "organism";
	
	// this is actually synchronous, how do we coordinate 
	// initialization asynchronously?
	function load_template(template) {
		jqXHR = $.ajax({
		     url: template,
		     global: false,
			 async: false,
			 cache: false, // TODO want to enable caching for production, but inconvenient for testing
		});
		return jqXHR.responseText;
	}
	
	// initialize module ... currently just loading resources
	my.init = function() {
		my.network_details_template = load_template("js/templates/network_details.html");
		my.identifier_details_template = load_template("js/templates/identifier_details.html");
		my.identifier_folder_details_template = load_template("js/templates/identifier_folder_details.html");
		my.organism_details_template = load_template("js/templates/organism_details.html");
		my.group_details_template = load_template("js/templates/group_details.html");
		my.functions_folder_details_template = load_template("js/templates/functions_folder_details.html");
		my.functions_details_template = load_template("js/templates/functions_details.html");
	}
	
	return my;
}(dmw || {});