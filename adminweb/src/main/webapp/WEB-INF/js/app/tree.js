
define(['jqueryui', 'fancytree', 'fileupload', 'bootbox', 'jqueryform',
    'app/constants', 'app/details'],
    function($, fancytree, fileupload, bootbox, jqueryform,
        constants, details) {

    var tree = {};

	tree.setupTree = function(orgId) {
		
		$("#tree").fancytree({
			debugLevel: 1, // 0:quiet, 1:normal, 2:debug
	        fx: { height: "toggle", duration: 100 },
			source: {
				url : "organism/all",
				data : {
					id : orgId
				},
			},
			minExpandLevel : 1,
			selectMode : 1,
			activeVisible: true,
			imagePath: "js/lib/skin/",

            extensions: ["filter"],
            filter: {
                mode: "hide"
            },

			renderNode : function(event, data) {
			    node = data.node;
			    $("#hithere");
				$(node.span).find("span.fancytree-title")
						   .html(tree.formatNodeTitle(node));
			},

			// customize icons after loading, based on status. could
			// also do this server side ...
			init: function(event, data) {
				data.tree.visit(function(node) {

					if (node.data.type == constants.NETWORK_NODE) {
					    var title = details.suggestNetworkName(node);
    					if (title) {
    					    node.title = title;
    					}

						procDetails = node.data.processingDetails;
						if (procDetails && procDetails.status == "OK") {
						    node.data.icon = "valid_file.png";
					    }
					}

					return true;
				});
			},

            activate: function(event, data) {
                var node = data.node;

				tree.computeNodeStats(node);
				$("#details").hide().html(details.formatNodeDetails(node)).fadeIn('fast');
                details.loadFileSnippet(node);
				tree.setupFileUpload(node);
				tree.setupForm(node);
//				dmw.setupTabChangeCallbacks(node);				
			},

			click : function(event, data) {
			    var node = data.node;
				if (tree.formNeedsSave()) {
					bootbox.dialog("Save changes?", 
						[{
							"label": "Save",
							"class": "btn-primary",
							"callback": function() {
								// press the save button for the user, but attaching
								// a flag to prevent the default save dialog from popping
								// up. don't change nodes since saving triggers a redisplay
								// of the saved node event asynchronously
								console.log("save pressed");
								$("#networkForm").find('#submitButton').data("quiet", true).click();
							}
						},
						{
							"label": "Discard",
							"class": "btn-danger",
							"callback": function() {
								console.log("discard pressed");
								// reset the form and fire the event again,
								// which should get us safely through to the
								// desired node
								$("#networkForm, #FunctionsForm").find('#resetButton').click();
								event.target.click();
							}
						},
						{
							"label": "Edit",
							"class": "btn",
							"callback": function() {
								// don't have to do anything, drop us back into
								// editing the displayed node
								console.log("edit pressed");
							}
						},				
					]);
					return false;
				}
				else {
					node.setSelected();
				}
			},
		});
	}
	
	tree.formNeedsSave = function() {
		e = $('form button[name="submitButton"]');
		if (e.length > 0 && e.attr('disabled') === undefined) {
			return true;
		}
		else {
			return false;
		}
		
	}
		
	// compute some stats for certain nodes used in the display,
	// like # of networks in an organism. should probably do
	// this server side, quick hack here. TODO	
	tree.computeNodeStats = function(node) {
		if (node.data.type === constants.ORGANISM_NODE && node.children) {

			network_groups = node.children[1].children;
			
			nw_counts = {};
			all_counts = [];

			for (i=0; i<network_groups.length; i++){
				group = network_groups[i];
				var one_count = {};
	
				if (group.children === null) {
					nw_counts[group.data.title] = 0;
					one_count['name'] = group.data.title;
					one_count['count'] = 0;
					//one_count[group.data.title] = 0;
				}
				else {
					nw_counts[group.data.title] = group.children.length;
					one_count['name'] = group.data.title;
					one_count['count'] = group.children.length;
				}
				all_counts.push(one_count);
			}

			node.data.nw_counts = all_counts;
			
		}
	}
	
	// strike out the names of networks that are disabled in the
	// treelist
	tree.formatNodeTitle = function(node) {
		if (node.data.type == constants.NETWORK_NODE) {
			if (node.data.enabled == false) {
				var newTitle = '<strike>' + node.title + '</strike>';
			    return newTitle;
			}
		}

		return node.title;
	}

	tree.setupFileUploadForType = function(element_name, node, formData, focusOnNewNode) {
		// button to upload new network at group level
		$(element_name).fileupload({
			dataType : 'json',
			autoUpload: true, 
			done : function(e, data) {
				
				$("#spinner").hide();
				
				// if we are getting a new node, key is
				// in the result, otherwise key is from 
				// current node 
				if (focusOnNewNode) {
					key = data.result.key;
				}
				else {
					key = node.key;
				}
				var promise = $("#tree").fancytree("getTree").reload()
				$.when(promise).then(function() {
					$("#tree").fancytree("getTree").activateKey(key);
					$("#tree").fancytree("getTree").getNodeByKey(key).setSelected();
				});
				
				if (data.result && data.result.error == "1") {
					bootbox.alert("Validation failed", function() {});					
				}
			},
			fail : function (e, data) {
				console.log("upload error");
				$("#spinner").hide();
				bootbox.alert("Upload failed", function() {});
			},
		});

		// bind file upload arguments on submit event
		$(element_name).bind('fileuploadsubmit', function(e, data) {
			$("#spinner").show();			
			data.formData = formData;
		});

		// progress??
//		$('#fileupload').bind('fileuploadprogress', function (e, data) {
//		    // Log the current bitrate for this upload:
//		    console.log(data.bitrate);
//		    console.log(data.loaded);
//		    console.log(data.total);
//		});
	}
	
	tree.setupFileUpload = function(node) {
		switch (node.data.type) {
		case constants.NETWORK_NODE:
			formData = {
				organismId : node.data.organismId,
				networkId : node.data.id,
			};
			tree.setupFileUploadForType('#fileupload', node, formData, false);
			tree.setupFileUploadForType('#fileupload2', node, formData, false);
			break;
		case constants.GROUP_FOLDER_NODE:
			formData = {
				organismId : node.data.organismId,
				groupId : node.data.id,
			};
			tree.setupFileUploadForType('#fileupload', node, formData, true);
			break;
		case constants.IDENTIFIERS_FOLDER_NODE:
			formData = {
				organismId : node.data.organismId,
			};
			tree.setupFileUploadForType('#fileupload', node, formData, true);
			break;
		case constants.IDENTIFIERS_NODE:
			formData = {
				organismId : node.data.organismId,
				identifiersId: node.data.id,
			};	
			tree.setupFileUploadForType('#fileupload', node, formData, false);
			break;
		case constants.ATTRIBUTES_FOLDER_NODE:
			formData = {
					organismId: node.data.organismId,
			}
			tree.setupFileUploadForType('#fileupload', node, formData, true);
			break;
		case constants.FUNCTIONS_FOLDER_NODE:
			formData = {
					organismId: node.data.organismId,
			}
			tree.setupFileUploadForType('#fileupload', node, formData, true);
			break;
		case constants.FUNCTIONS_NODE:
			formData = {
					organismId: node.data.organismId,
					functionsId: node.data.id,
			};
			tree.setupFileUploadForType('#fileupload', node, formData, false);
			tree.setupFileUploadForType('#fileupload2', node, formData, false);
			break;
		default:
			console.log("no upload available for node type: " + node.data.type);
		}
	}
		
	tree.setupForm = function(node) {
		
		// changing an input causes submit button to be enabled
		$('form input[type!="submit"],textarea').on('input', function (e) {
			console.log('hit change function');
		    $(this).parents('form').find(':submit').removeAttr('disabled');
		});
		$('form input[type!="submit"],textarea').change(function (e) {
			console.log('hit change function');
		    $(this).parents('form').find(':submit').removeAttr('disabled');
		});

		// reset button resets form and also disables save button
		$('#resetButton').click(function() {
			console.log("reset button was pressed");
			form = $(this).parents('form');
			console.log("form is %o", form);
			form[0].reset();
			form.find('#submitButton').prop('disabled', true);
		});
		
		$('form button[type="submit"]').on('click',function(e){

			var current = $(this);
			var form = current.parents('form');
			
			// for some reason doing ajaxSubmit() doesn't pick up
			// the selected button (there are multiple buttons), 
			// even though ajaxForm() does. but since we want to pop
			// up a confirmation dialog then use ajaxSubmit(), we work
			// around by adding a hidden element with the attributes
			// of the missing submit button, so the server side will do
			// the right thing.
			var button = current.val();
			$('<input>').attr({
			    type: 'hidden',
			    id: 'foo',
			    name: button,
			    value: button,
			    
			}).appendTo(form);
			
			submitFunction = function() {
				form.ajaxSubmit({
					success: function(data) { 
						console.log("processed networkForm, success callback data %o", data);
						$("#submit_spinner").hide();
						
						// if result data had a key, use it for focus
						if (data.key === undefined) {
							key = node.key;
							if (node.parent === undefined) {
								parentKey = null;
							}
							else {
								parentKey = node.parent.key;
							}
						}
						else {
							key = data.key;
						}
						var promise = $("#tree").fancytree("getTree").reload();
						$.when(promise).then(function() {
							
							// focus on the same node after reload, or
							// on parent on node if it was deleted
							var tree = $("#tree").fancytree("getTree");
							found = tree.getNodeByKey(key);
							if (found === null) {
								focusKey = parentKey;
							}
							else {
								focusKey = key;
							}
							tree.activateKey(focusKey);
							tree.getNodeByKey(focusKey).setSelected();
							
							// if the details panel consists of
							// multiple tabs, need to active the
							// correct one. we hardcode a test based
							// on the button press, but a more general
							// mechanism would be better. TODO
							if (button === 'updateValidationButton') {
								console.log("switching to analysis tab");
							    $('a[href=#analysis]').tab('show');
							}
						});
					},

					error: function(response, status, err) {
						
						// code duplication, cleanup! TODO
						console.log("form processing error");
						$("#submit_spinner").hide();
						key = node.key;
						parentKey = node.parent.key;
						var promise = $("#tree").fancytree("getTree").reload()
						$.when(promise).then(function() {
							
							// focus on the same node after reload, or
							// on parent on node if it was deleted
							var tree = $("#tree").fancytree("getTree");
							found = tree.getNodeByKey(key);
							parentFound = tree.getNodeByKey(parentKey);
							if (found === null) {
								focusKey = parentKey;
							}
							else {
								focusKey = key;
							}								
							tree.activateKey(focusKey);
							tree.getNodeByKey(focusKey).setSelected();
							
							// if the details panel consists of
							// multiple tabs, need to active the
							// correct one. we hardcode a test based
							// on the button press, but a more general
							// mechanism would be better. TODO
							console.log("switching to statistics tab");
							if (button === 'updateValidationButton') {
							    $('a[href=#analysis]').tab('show');
							}
						});
					},
					
					beforeSubmit: function(formData, jqForm, options) {
						console.log("before submit!");
						console.log("form data %o", formData);
						$("#submit_spinner").show();
						return true;
					},
				});						
			}
			
			quiet = $(this).data("quiet");
			console.log("have data quiet? %o", quiet);
			
			if (quiet === true) {
				console.log("processing quietly");
				submitFunction();
			}
			else {
				console.log("processing loudly");
				bootbox.confirm("Are you sure?", function(result) {
					if (result) {
						console.log("confirm result is " + result);
						submitFunction();
					}
				});
			}

			return false;
		});		
	}
	
	tree.reloadTree = function(organism_id) {
		$("#tree").fancytree("getTree").options.source.url = "organism/all";
		$("#tree").fancytree("getTree").options.source.data.id = organism_id;
		$("#tree").fancytree("getTree").reload();
	}

	tree.setupSearch= function() {
	    $("#filter-tree").on('keypress',function (e) {
            if (e.keyCode == 13) {
                var searchkey = e.target.value;
                console.log("got search request %o", searchkey);

                if (searchkey === "") {
                    $("#tree").fancytree("getTree").clearFilter();
                }
                else {
                    $("#tree").fancytree("getTree").applyFilter(searchkey);
                }
            }
        });

        $("#clear-search").click(function() {
            console.log("clearing search");
            $("#filter-tree").val('');
            $("#tree").fancytree("getTree").clearFilter();
        });
    }
	
	return tree;
});