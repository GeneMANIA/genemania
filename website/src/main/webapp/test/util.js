// #############################################################################
// SETUP
// #############################################################################

jQuery.extend(
    jQuery.expr[':'], {
        regex: function(a, i, m, r) {
            var r = new RegExp(m[3], 'i');
            return r.test(jQuery(a).text());
        }
    }
);

// #############################################################################
// CONSTANTS
// #############################################################################

var NUMBER_OF_ORGANISMS = 7;

var ARABIDOPSIS_ID = 1;
var WORM_ID = 2;
var FLY_ID = 3;
var HUMAN_ID = 4;
var MOUSE_ID = 5;
var YEAST_ID = 6;
var RAT_ID = 7;
var DEFAULT_ID = HUMAN_ID;

var default_organisms = ["", // no organism 0
                 "A. thaliana (arabidopsis)",
                 "C. elegans (worm)",
                 "D. melanogaster (fly)",
                 "H. sapiens (human)",
                 "M. musculus (mouse)",
                 "S. cerevisiae (baker's yeast)",
                 "R. norvegicus (rat)"];

var default_genes = ["", // no organism 0
                    "HFR1; PHYA; PHYB; ELF3; COP1; SPA1; FUS9; DET1; HY5; CIP1; CIP8", 
                    "cpg-2; cpg-1; tba-2; ima-2; tsr-1; mei-2; ran-1; mei-1; ebp-2; gei-17",
                    "aret; bcd; bic; cad; grk; gt; osk; Ser; byn; Bin1; bin3; rno",
                    "MRE11A; RAD51; MLH1; MSH2; DMC1; RAD51AP1; RAD50; MSH6; XRCC3; PCNA; XRCC2",
                    "Mll1; Sfpi1; Bcr; Dok2; AML1; Cebpa; Ctsg; Dok1",
                    "CDC27; APC11; APC4; XRS2; RAD54; APC2; RAD52; RAD10; MRE11; APC5",
                    "Gad1; Fyn; Pafah1b1; Psen2; Ncstn; Psen1; Lef1; Ctnnb1; Cdh1; Psenen"];

//#############################################################################
//BROWSER SIMULATING ACTIONS
//#############################################################################

var back = function(){
	document.getElementById('genemania_iframe').contentWindow['history'].go(-1); 
}

var reload = function(){
	document.getElementById('genemania_iframe').contentWindow['location'].reload(true); 
}

var reloadResultsPage = function(callback){
	$("#genemania iframe").one("load", function(){
		if( gm$("#error_page").size() > 0 ){
			ok(false, "Error getting results page");
			nextTestCase();
		} else if (gm$("#search_page").size() > 0){
			ok(false, "Error attempting to reload results page: went back to search page");
			nextTestCase();
		} else {
			gm$("body").one("cytoweb", function(){
				callback();
			});
		}
	});
	gm$("#resubmit_form").submit();
}

var resubmit = function(){
	gm$("#resubmit_form").submit();
}

//#############################################################################
//REPORT FUNCTIONS
//#############################################################################

function report(callback){
	gm$("#print_form").attr("target", "_self");
	
	clickMenuItem("#menu_generate_report", callback);
}

function saveReportItems(reportItems){
	// Save these in an object called reportItems:
	// Network image (not implemented yet)
	
	// Search parameters
	var q_organism = gm$("#species_text").val();
	var q_search_genes = gm$("#gene_text").val();
	
	// Networks
	var q_network_groups = [];
	var q_network_group_scores = [];

	//gm$("#networks_tab_expand_top_level").click();
	gm$(".checktree_top_level").each(function(){
		q_network_groups.push(gm$(this).children('.label').children('.network_name').text());
		q_network_group_scores.push(removeWhiteSpace(gm$(this).children('.label').children('.per_cent_text').text()));
	});

	// Genes
	var q_genes = [];
	var q_gene_scores = [];
	gm$(".gene").each(function(){
		q_genes.push(gm$(this).children('.label').children('.gene_name').text());
		q_gene_scores.push(removeWhiteSpace(gm$(this).children('.label').children('.score_text').text()));
	});

	// Functions
	var q_functions = [];
	var q_function_qvalues = [];
	var q_function_coverages = [];
	gm$("#go_table > tbody > tr").each(function(){
		q_functions.push(gm$(this).children('.annotation').text());
		q_function_qvalues.push(removeWhiteSpace(gm$(this).children('.pval').text()).toLowerCase());
		q_function_coverages.push(removeWhiteSpace(gm$(this).children('.coverage').text()).toLowerCase());
	});
	
	// Interactions (not implemented yet)
	
	// Save them all into the reportItems object and return the object
	reportItems.q_organism = q_organism;
	reportItems.q_search_genes = q_search_genes;
	
	reportItems.q_network_groups = q_network_groups;
	reportItems.q_network_group_scores = q_network_group_scores;
	
	reportItems.q_genes = q_genes;
	reportItems.q_gene_scores = q_gene_scores;
	
	reportItems.q_functions = q_functions;
	reportItems.q_function_qvalues = q_function_qvalues;
	reportItems.q_function_coverages = q_function_coverages;
	
	return reportItems;
}

function verifyReportItems(reportItems){
	// Verify the:	
	// Network image (not implemented yet)
	
	// Search parameters
	ok(gm$("p:contains('" + reportItems.q_organism + "')").is(":visible"), "The report should have the correct organism, " + reportItems.q_organism);
	
	// Networks
	if (reportItems.q_network_groups != undefined){
		for (var i = 0; i < reportItems.q_network_groups.length; i++){
			// Label is needed first because selecting the entire .checktree_top_level includes the networks in the network group
			// and their descriptions, which may contain the network group name
			var current_network_group_label = gm$(".checktree_top_level > .label:contains('" + reportItems.q_network_groups[i] + "')");
			var current_network_group = current_network_group_label.parent();
			if(current_network_group == undefined){
				ok(false, "Network group " + reportItems.q_network_groups[i] + " was not found in the report");
			}
			else{
				ok(current_network_group.is(":visible"), "Network group " + reportItems.q_network_groups[i] + " should be in the report");
				same(removeWhiteSpace(current_network_group.children('.label').children('.per_cent_text').text()), reportItems.q_network_group_scores[i], reportItems.q_network_groups[i] + " network group score should be the same in the report");
			}
		}
	}
	
	// Genes
	if (reportItems.q_genes != undefined){
		for (var i = 0; i < reportItems.q_genes.length; i++){
			var current_gene;
			gm$(".gene:contains('" + reportItems.q_genes[i] + "')").each(function(){
				if (gm$(this).children('.label').children('.gene_name').text() == reportItems.q_genes[i]){
					current_gene = gm$(this);
				}
			});
			if(current_gene == undefined){
				ok(false, "Gene " + reportItems.q_genes[i] + " was not found in the report");
			}
			else{
				ok(current_gene.is(":visible"), "Gene " + reportItems.q_genes[i] + " should be in the report");
				same(removeWhiteSpace(current_gene.children('.label').children('.score_text').text()), reportItems.q_gene_scores[i], reportItems.q_genes[i] + " gene score should be the same in the report");
			}
		}
	}
	
	// Functions
	if (reportItems.q_functions != undefined){
		for (var i = 0; i < reportItems.q_functions.length; i++){
			var current_function;
			gm$("#go_table > tbody > tr:contains('" + reportItems.q_functions[i] + "')").each(function(){
				if (removeWhiteSpace(gm$(this).children('.annotation').text()) == removeWhiteSpace(reportItems.q_functions[i])){
					current_function = gm$(this);
				}
			});
			if(current_function == undefined){
				ok(false, "Function " + reportItems.q_functions[i] + " was not found in the report");
			}
			else{
				ok(current_function.is(":visible"), "Function " + reportItems.q_functions[i] + " should be in the report");
				same(removeWhiteSpace(current_function.children('.pval').text()).toLowerCase(), reportItems.q_function_qvalues[i], reportItems.q_functions[i] + " Q-value should be the same in the report");
				same(removeWhiteSpace(current_function.children('.coverage').text()).toLowerCase(), reportItems.q_function_coverages[i], reportItems.q_functions[i] + " coverage should be the same in the report");
			}
		}
	}
	
	// Interactions (not implemented yet)
}

//#############################################################################
//UPLOAD NETWORK FUNCTIONS
//#############################################################################

function upload(file_as_text, file_name, callback){
	function process(){
		if( callback != undefined ){
			gm$("body").one("uploadcomplete", callback);
		}
		document.getElementById('genemania_iframe').contentWindow.jsUpload(file_as_text, file_name);
	}
	
	if( !gm$("#advanced_options_open").is(":visible") ){
		openAdvancedOptions(function(){
			process();
		});
	} else {
		process();
	}
}

function uploadNetwork(url, name, callback){
	getNetwork(gm$("html").attr("contextpath") + "/test/Networks" + url, function(network_file){
		openAdvancedOptions(function(){
			removeAllUploadedNetworks();
			wait(function(){
				upload(network_file, name, function(){
					callback();
				});
			});
		});
	});
}

function checkUploadValid(network_name){
	ok(gm$("label:contains('" + network_name + "')").is(":visible"), "Uploaded network '" + network_name + "' should be visible");
	ok(!gm$("label:contains('" + network_name + "'):last").parent().children(".uploadError").is(":visible"), network_name + " should be a valid network");
}

function checkUploadInvalid(network_name){
	ok(gm$("label:contains('" + network_name + "')").is(":visible"), "Uploaded network '" + network_name + "' should be visible");
	ok(gm$("label:contains('" + network_name + "'):last").parent().children(".uploadError").is(":visible"), network_name + " should be an invalid network");
}

function removeAllUploadedNetworks(){
	if(gm$(".query_network_group:contains('Uploaded')").is(":visible")){
		gm$(".query_network_group:contains('Uploaded')").click();
		gm$(".ui-icon-trash").each(function(){
			gm$(this).trigger("mousedown");
			gm$("span:contains('Yes')").click();
		});
	}
}

//#############################################################################
//RUN QUERY FUNCTIONS
//#############################################################################

function runQuery(callback){
	$("#genemania iframe").one("load", function(){
		if( gm$("#error_page").size() > 0 ){
			ok(false, "Error getting results page");
			nextTestCase();
		} else if ( gm$("#search_page").size() > 0) {
			ok(false, "Went back to search page"); // Or put in extra code here if it's supposed to go back for any reason
			nextTestCase();
		}
		else {
			gm$("body").one("cytoweb", function(){
				callback();
			});
		}
	});
	wait(function(){
		clickGo();
		wait(function(){
			if (gm$(".qtip.qtip-red.qtip-active").is(":visible")){
				ok(false, "All of the genes are not valid for some reason; test failed");
				$("#genemania iframe").unbind("load");
				nextTestCase();
			}
		}, 1000);
	}, 100)
}

function clickDefaultGenes(callback){
	gm$(".default_genes_link").mousedown();
	if( callback != undefined ){
		waitForGeneValidation(callback);
	}
}

// only use on search page; also be careful testing networks before using this, since changing organism resets network choices
function runDefaultQuery(callback){
	var random_organism = Math.ceil(Math.random() * NUMBER_OF_ORGANISMS);
	setOrganismById(random_organism, function(){
		ok(true, "Organism is set to " + gm$("#species_text").val());	
		var genes = gm$("#species_select option[value=" + gm$("#species_select").val() + "]").attr("defgenes");
		setGenes(genes, function(){
			runQuery(callback);
		});
	});
}

//only use on search page
function runSpecificDefaultQuery(org, callback){
	setOrganismById(org, function(){
		ok(true, "Organism is set to " + gm$("#species_text").val());
		
		var genes = gm$("#species_select option[value=" + gm$("#species_select").val() + "]").attr("defgenes");
		setGenes(genes, function(){
			runQuery(callback);
		});
	});
}

//#############################################################################
//CYTOSCAPE FUNCTIONS
//#############################################################################

function allEdgesVisible(is_visible){ // Since checking ALL of the edges was too slow, we'll just check 5 random edges.
	for (var i = 0; i < 5; i++){
		var rand = Math.floor(Math.random() * vis().edges().length);
		if (vis().edges()[rand].visible != is_visible){
			return false;
		}
	}
	return true;
}

function sortNodesByScore(node_array){ // quicksort nodes by score
	var node_scores = [];
	for (var i = 0; i < node_array.length; i++){
		node_scores[i] = node_array[i].data.score;
	}
	return quickSort(node_scores);
}
//#############################################################################
//ADVANCED OPTIONS/NETWORK SELECTION FUNCTIONS
//#############################################################################

function openAdvancedOptions(callback){
	gm$("#networks_section").one("load", callback);
	gm$("#advanced_options_closed .advanced_options_toggle a").trigger("click");
}

function closeAdvancedOptions(){
	gm$("#advanced_options_open .advanced_options_toggle a").trigger("click");
}

function checkDefaultNetworks(organism_id){
	// This function asserts that all default networks are checked and that all non-default networks are not checked.
	function process(){
		var network_count = 0;
		gm$(".query_network_checkbox").each(function(){
			if(gm$(this).attr('organism') != organism_id){
				ok(false, gm$(this).parent().children('label').text() + " does not belong to " + default_organisms[organism_id]);
				return false; // break out of .each
			}
			else if (gm$(this).is(":checked")){	// It is checked, so it should be a default network
				if (gm$(this).attr('default') != 'true'){
					ok(false, gm$(this).parent().children('label').text() + " is checked but it is not a default network");
				}
				else{
					network_count++;
				}
			}
			else{	// It is not checked, so it should not be a default network
				if(gm$(this).attr('default') != 'false' && gm$(this).parent().attr('id') > 0){ // TODO: Extra check is required for uploaded networks; for some reason they are not seen as checked?
					ok(false, gm$(this).parent().children('label').text() + " is a default network but it is not checked");
				}
			}
		});
		ok(network_count > 0, "There are " + network_count + " networks that are checked by default for organism " + default_organisms[organism_id]);
	}
	
	if( !gm$("#advanced_options_open").is(":visible") ){
		openAdvancedOptions(function(){
			process();
		});
	}
	else {
		process();
	}
}

function allNetworkCheckboxesAreChecked(is_checked){
	gm$(".checktree_network").each(function(){
		if (gm$(this).children('.checkbox').children('input').is(':checked') != is_checked){
			return false;
		}
	});
	return true;
}

function selectOneRandomNetwork(callback){
	// This function deselects all networks and then selects one random NON-DEFAULT network to click.
	
	function process(){
		// Deselect all networks
		gm$("#network_selection_select_none").trigger('click');
		wait(function(){
			var current_org_id = gm$("#species_select").val();
			var all_checkboxes = gm$(".query_network input[default=false][organism="+ current_org_id +"]");
			var random_index = Math.floor(Math.random() * all_checkboxes.size());
			
			var network_checkbox = all_checkboxes.eq(random_index);
			var random_network_name = network_checkbox.parent().children('label').text(); 
			var network_id = network_checkbox.val();
			
			var network_group = network_checkbox.attr("group");
			network_checkbox.click();

			ok(true, "Random network chosen is: " + random_network_name);
			
			if(callback){
				wait(function(){
					callback(network_id);
				});
			}
		}, 2000);
	}
	
	if( !gm$("#advanced_options_open").is(":visible") ){
		openAdvancedOptions(function(){
			wait(function(){
				process();
			});
		});
	}
	else{
		process();
	}
}

function deselectNetworkByName(name){ // deselects a network by the name
	gm$(".checktree_network").each(function(){
		if (gm$(this).children('.label').children('.network_name').text() == name){
			gm$(this).children('.checkbox').click();
		}
	});
}


function selectAndCheckDefaultNetworks(){
	// Select "default"
	gm$("#network_selection_select_default").trigger('click');
	
	// Check to see if it matches the default
	gm$(".query_network_checkbox").each(function(i){
		if ( gm$(this).is(":checked") ){
			ok( gm$(this).attr("default") == "true", "The box is checked, so it should be a default checkbox" );
		}
		else if ( gm$(this).attr("default") != "false" )
		{
			ok( false, "A default checkbox should be checked, but it is not" );
		}
	});
}

function selectAndCheckAllNetworks(){
	// Select "all"
	gm$("#network_selection_select_all").trigger('click');
	// Go through each check-box to see if it is checked
	gm$(".query_network_checkbox").each(function(i){
		ok(gm$(this).is(":checked"), "Checkbox should be checked");
	});
}

function deselectAndCheckAllNetworks(){
	// Select "none"
	gm$("#network_selection_select_none").trigger('click');
	// Go through each check-box to see if it is checked
	gm$(".query_network_checkbox").each(function(i){
		ok(!gm$(this).is(":checked"), "Checkbox should be unchecked");
	});
}

function sortAndCheckNetworkBySize(callback){
	openAdvancedOptions(function(){	
		gm$("#network_sorting_sortByNetworkSize").click();
		wait(function(){	
			checkNetworkSorted("#networkInteractionCount");
			callback();
		});
	});
}

function sortAndCheckNetworkByDate(callback){
	openAdvancedOptions(function(){
		gm$("#network_sorting_sortByPubDate").click();
		wait(function(){
			checkNetworkSorted("#networkPubDate");
			callback();
		});
	});
}

function sortAndCheckNetworkByFirstAuthor(callback){
	openAdvancedOptions(function(){	
		gm$("#network_sorting_sortByFirstAuthor").click();
		wait(function(){	
			checkNetworkSorted("#networkAuthors");
			callback();
		});
	});
}

function sortAndCheckNetworkByLastAuthor(callback){
	openAdvancedOptions(function(){	
		gm$("#network_sorting_sortByLastAuthor").click();
		wait(function(){	
			checkNetworkSorted("#networkAuthors", lastauthor = true);
			callback();
		});
	});
}

function checkNetworkSorted(type, is_last_author){
	if ( is_last_author == undefined ){
		is_last_author = false;
	}
	var groups = gm$(".query_networks");
	for ( var i = 0; i < groups.size(); i++ ){	// Iterate through each network group
		var group = groups[i];
		for ( var j = 1; j < group.children.length; j++ ){	// Iterate through each network in the group to make sure it is sorted
			var current_network = group.children[j].id;
			var prev_network = group.children[j-1].id;
			if ( type == "#networkPubDate" || type == "#networkInteractionCount" ){	
				var current_value = 0;
				if (gm$(type + current_network).val() != undefined){
					current_value = parseInt(gm$(type + current_network).val()); // If we are sorting by date or size, then we need to use parseInt to compare numbers
				}
				var prev_value = 0;
				if (gm$(type + prev_network).val() != undefined){
					prev_value = parseInt(gm$(type + prev_network).val());
				}
				// In the selected group compare each network with the one before to make sure it is sorted
				ok(current_value <= prev_value, current_value + " is below " + prev_value);
			}
			else{ // Otherwise, it is either sorted by last or first author
				if ( is_last_author == true ){ // Last Author
					var current_author = getLastAuthorOfNetwork(gm$(type + current_network).val());
					var prev_author = getLastAuthorOfNetwork(gm$(type + prev_network).val());
				}
				else{ // First Author
					var current_author = getFirstAuthorOfNetwork(gm$(type + current_network).val());
					var prev_author = getFirstAuthorOfNetwork(gm$(type + prev_network).val());
				}
				current_author = current_author.toLowerCase();
				prev_author = prev_author.toLowerCase();
				
				var current_author_label = current_author;
				var prev_author_label = prev_author;
				if (current_author_label == ""){
					current_author_label = "'no author'";
				}
				if(prev_author_label == ""){
					prev_author_label = "'no author'";
				}
				ok(current_author >= prev_author || current_author == "", current_author_label + " is below " + prev_author_label);
			}
		}
	}
}

function getLastAuthorOfNetwork(str){ // These two methods are used in the Sort Networks by ____ tests
	if ( str == undefined ){
		return "";
	}
	// Look for last index of "," and get the substring after it, removing outside whitespace
	var a = str.lastIndexOf(",");
	if ( a != -1 ){
		str = str.substring(a+1);
		return removeWhiteSpace(str);
	}
	// Otherwise if there is no index of "," then there is only one author, return the author
	else{
		return str;
	}
}


function getFirstAuthorOfNetwork(str){ // These two methods are used in the Sort Networks by ____ tests
	if ( str == undefined ){
		return "";
	}
	// Look for first index of "," and get the substring before it, removing outside whitespace
	var a = str.indexOf(",");
	if ( a != -1 ){
		str = str.substring(0, a);
		return str.replace(/^\s+|\s+$/g, '');
	}
	// Otherwise if there is no index of "," then there is only one author, return the author
	else{
		return str;
	}
}

//#############################################################################
//SEARCH PAGE FUNCTIONS
//#############################################################################

function getOrganismName(str){ // i.e. returns "human" when input is "H. sapiens (human)"
	var a = str.indexOf('(');
	var b = str.indexOf(')');
	return str.substring(a+1, b);
}

function getOrganismId(){ // returns 1 if Arabidopsis, 4 if Human, etc.
	return gm$("#species_select").val();
}

function setOrganismById(id, callback){ // set organism by id number 1, 2, 3, etc.
	var text = gm$("#species_select option[value=" + id + "]").html();
	gm$("#species_select").val(id);
	gm$("#species_text").val(text).trigger("change").trigger("blur");
	
	if( callback != undefined ){
		wait(function(){
			callback();
		});
	}
}

function setOrganismText(text, callback){ // Function used to test auto-complete
	// Types "text" into the organism text and triggers the auto-complete. 
	wait(function(){ // Extra wait needed for IE for some reason
		gm$("#species_text").trigger("focus");
		gm$("#species_text").val(text);
		wait(function(){
			gm$("#species_text").trigger("keydown");
			wait(function(){
				if( callback != undefined ){
					callback();
				}
			});
		});
	});
}

function checkGeneValidation(genes, assertions, valid){ 
	// Sets genes to 'genes', then if the genes are supposed to be invalid, it waits for warning tool-tip and then runs assertions.
	// If they are supposed to be valid, then it runs the assertions directly.
	if ( valid == undefined ){
		valid = false;
	}
	setGenes(genes, function(){
		if ( valid == false ){
			// This is needed to trigger the warning q-tip.
			setTimeout(function(){ 
				gm$("#gene_text").trigger("focus");
				waitForVisible(".qtip", function(){
					assertions();
				}, 1000);
			}, 500);
		}
		else{
			assertions();
		}
	});
}

function setGenes(genes, callback){
		gm$("#gene_text").trigger("focus");
		gm$("#gene_area").val(genes);
		gm$("#gene_area").trigger("blur");

		if( callback != undefined ){
			waitForGeneValidation(callback);
		}
}

function waitForGeneValidation(callback){
	gm$("#gene_area").one("validationcomplete", callback);
}

function clickGo(){
	gm$("#findBtn").click();
}

//#############################################################################
//RESULTS PAGE/PANEL FUNCTIONS
//#############################################################################

function clickMenuItem(elem, callback){ // Used to click menu items such as "Reset Layout" on the results page

	var parent = gm$(elem).parents("li:first");
	var child = gm$(elem);
	
	parent.click();
	wait(function(){
		child.trigger("mouseenter");
		
		wait(function(){
			child.click();
			if (callback) { 
				wait(function(){
					callback();
				});
			}
		});
	});
}	

function chooseSevenFunctions(){ // Apply 7 colours to functions
	for (var i = 2; i <= 8; i++){ 
		gm$("#go_table > tbody > tr:nth-child(" + i + ") > .add_button .button").click();
	}
}

function checkNetworkPanelMatchesQueryNetworks(){
	function process(){	// Check that the side panel networks are all checked in the advanced options
		
		// Check that the side panel networks are all checked in the advanced options
		gm$("#networks_tab_expand_top_level").click();
		gm$(".checktree_top_level").each(function(){
			var network_group = gm$(this).children('.label').children('.network_name').text();
			
			// Select the appropriate network group in advanced options
			gm$("#groupsPanel > div:contains('" + network_group + "')").click();

			// Check that each side panel network is checked in the advanced options
			gm$(this).children('ul').children('.checktree_network').each(function(){
				var network_name = gm$(this).children('.label').children('.network_name').text();
				var pass = false;
				var checked_network = gm$('.query_network > label:contains(' + network_name + ')').parent();
				
				if (checked_network.is(':visible') && checked_network.children('input').attr('checked')){
					ok(true, network_name + " is in the side panel, and it is checked in the advanced options");
					pass = true;
				}

				if (!pass){
					if (!checked_network.children('input').attr('checked')){
						ok(false, network_name + " is in the side panel, but not checked in the advanced options");
					}
					else{
						ok(false, network_name + " is in the side panel, but not visible in the advanced options");
					}
				}
			});
		});
	}
	
	if( !gm$("#advanced_options_open").is(":visible") ){
		openAdvancedOptions(function(){
			process();
		});
	}
	else {
		process();
	}
}

//#############################################################################
//MISCELLANEOUS FUNCTIONS
//#############################################################################

function rgbToHex(rgb) {
    if (  rgb.search("rgb") == -1 ) {
         return rgb;
    } else {
         rgb = rgb.match(/^rgba?\((\d+),\s*(\d+),\s*(\d+)(?:,\s*(\d+))?\)$/);
         function hex(x) {
              return ("0" + parseInt(x).toString(16)).slice(-2);
         }
         return "#" + hex(rgb[1]) + hex(rgb[2]) + hex(rgb[3]); 
    }
}

function quickSort(array){
	if(array.length <= 1){
		return array;
	}
	else{
		var below_pivot = [];
		var above_pivot = [];
		var pivot = [array[0]];
		array.splice(0,1);
		for (var i = 0; i < array.length; i++){
			if (array[i] <= pivot[0]){
				below_pivot.push(array[i]);
			}
			else{
				above_pivot.push(array[i]);
			}
		}
		return quickSort(below_pivot).concat(pivot.concat(quickSort(above_pivot)));
	}
}

function verifyGeneAreaWarningText(text){
	same(removeWhiteSpace(gm$("#gene_error .ellipsis_text").text()), text, "Warning above text should be correct");
}

function verifyGeneAreaWarningVisible(){
	ok(gm$("#gene_error .ellipsis_text").is(":visible"), "Warning above text should be visible");
}

function verifyGeneAreaWarningNotVisible(){
	ok(!gm$("#gene_error .ellipsis_text").is(":visible"), "Warning above text should be not visible; all genes are valid");
}

function verifyGeneTooltipContent(content){
	ok(gm$(".gene_error_tooltip .qtip-content").text().indexOf(content) != -1, "Q-tip warning shows '" + content + "'");
}

function verifyGeneTooltipTitle(title){
	same(removeWhiteSpace(gm$(".gene_error_tooltip .qtip-title").text()), title, "Q-tip warning title should be correct")
}

function verifyGeneTooltipVisible(){
	ok(gm$(".gene_error_tooltip").is(":visible"), "Q-tip warning should be visible");
}

function removeWhiteSpace(str){
	return str.replace(/^\s+|\s+$/g, '');
}

function waitForVisible(object, callback, timeout){
	if ( timeout == undefined ){
		timeout = 100;
	}
	var timer_fcn = function(){
		if( gm$(object).is(":visible") ){
			callback();
		} else if (timeout == 0){
			ok(false, object + " took too long to load: Timed out.");
			nextTestCase(); // Reaching here means timeout exception; skip to next test
		} else {
			timeout--;
			setTimeout(timer_fcn, 10);
		}
	};
	setTimeout(timer_fcn, 100);
}

function waitForNotVisible(object, callback, timeout){
	if ( timeout == undefined ){
		timeout = 100;
	}
	var timer_fcn = function(){
		if( !gm$(object).is(":visible") ){
			callback();
		} else if (timeout == 0){
			ok(false, object + " took too long to become not visible: Timed out.");
			nextTestCase(); // Reaching here means timeout exception; skip to next test
		} else {
			timeout--;
			setTimeout(timer_fcn, 10);
		}
	};
	setTimeout(timer_fcn, 100);
}

function waitForChecked(object, callback, timeout){
	if ( timeout == undefined ){
		timeout = 100;
	}
	var timer_fcn = function(){
		if( gm$(object).is(":checked") ){
			callback();
		} else if (timeout == 0){
			ok(false, object + " took too long to load: Timed out.");
			nextTestCase(); // Reaching here means timeout exception; skip to next test
		} else {
			timeout--;
			setTimeout(timer_fcn, 10);
		}
	};
	setTimeout(timer_fcn, 100);
}

function largeGeneList(num, callback){
	if (num == undefined){
		num = "400";
	}
	var url = gm$("html").attr("contextpath") + "/test/LargeGeneLists" + num + ".txt";
	
	$.get(url, function(data){
		callback(data);
	});
}

function getNetwork(url, callback){
	$.get(url, function(data){
		callback(data);
	});
}

function wait(callback, time){
	if( time == undefined ){
		time = 1000;
	}
	setTimeout(callback, time);
}

//#############################################################################
//TEST SUITE SETUP
//#############################################################################

//google analytics for tests
function track(test_case, pass){
//	console.log("track (Test, " + test_case + ", " + pass + ")");
	
	_gaq.push(['_trackEvent', "Test", test_case, (pass ? "Pass" : "Fail")]);
}

var current_module_name = "";

QUnit.moduleStart = function(name){
	current_module_name = name;
}

QUnit.testDone = function(name, failures, total){
	if( failures == 0 ){
		track(current_module_name + ": " + name, true);
	} else {
		track(current_module_name + ": " + name, false);
	}
}

/**
* Just a shortcut for getting DOM elements from inside GeneMANIA's iFrame.
*/
var gm$ = function(a, b, c, d, e, f, g, h, i, j, k){
	if( a == undefined ){
		 return document.getElementById('genemania_iframe').contentWindow['jQuery'];
	}
	
	return document.getElementById('genemania_iframe').contentWindow['jQuery'](a, b, c, d, e, f, g, h, i, j, k);
}

var vis = function(){
	return document.getElementById('genemania_iframe').contentWindow['_vis']; 
}

function nextTestCase(){
	setTimeout(function(){
		loadSearchPage(function(){
			start();
		});
	}, 1000); // for ff?
}

function loadSearchPage(callback){
	var gm = $("#genemania iframe");
	
	gm.one("load", function(){
		callback();
	});
	
	// Load the iFrame--it should trigger the first set of tests:
	var genemaniaUrl = $("html").attr("contextpath");
	
	if( genemaniaUrl == "" ){
		genemaniaUrl = "/";
	}
	
	gm.attr("src", genemaniaUrl);
}

var tests = [];
function testCase(name, callback){
	tests.push({
		name: name,
		callback: callback,
		module: last_module
	});
}

var last_module = "";
function testModule(name){
	last_module = name;
}

function testSuite(callback){
	callback();

	var url = window.location.href + "";
	var in_order = url.search("#inorder") >= 0;
	var small = url.search("#small") >= 0;
	if( !in_order ){
		var reordered_tests = [];
		while( tests.length > 0 && ( !small || reordered_tests.length < 5 ) ){
			var random_index = Math.round((tests.length - 1) * Math.random());
			var random_test = tests.splice(random_index, 1)[0];
			reordered_tests.push(random_test);
		}
		tests = reordered_tests;
	}
	
	for(var i = 0; i < tests.length; i++){
		var test = tests[i];
		
		module(test.module);
		asyncTest(test.name, test.callback);
	}
}
