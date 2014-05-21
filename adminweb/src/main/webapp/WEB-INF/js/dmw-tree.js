
var dmw = function(my, $) {

	my.setupTree = function(orgId) {
		
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
			
			renderNode : function(event, data) { //node, nodeSpan) {
			    //console.log("renderNode %o, %o", event, data);

			    node = data.node;
				$(node.span).find("span.fancytree-title")
						   .html(my.formatNodeTitle(node));
			},


			// customize icons after loading, based on status. could
			// also do this server side ...
			//postProcess: function(isReloading, isError, XMLHttpRequest, textStatus, errorThrown) {
			init: function(event, data) {
				data.tree.visit(function(node) {
//					var title = my.formatNodeTitle(node.data);
//					if (title) {
//					    node.title = title;
//					}

					if (node.data.type == my.NETWORK_NODE) {
						details = node.data.processingDetails;
						if (details && details.status == "OK") {
						    node.data.icon = "valid_file.png";
					    }
					}

					return true;
				});
			},

             activate: function(event, data) {
                var node = data.node;
			    console.log("activate %o, %o", event, node);
				//if (!flag) {
				//	return;
				//}

				my.computeNodeStats(node);
				$("#details").hide().html(my.formatNodeDetails(node)).fadeIn('fast');
				my.setupFileUpload(node);
				my.setupForm(node);
//				dmw.setupTabChangeCallbacks(node);				
			},

			click : function(event, data) {
			    var node = data.node;
				if (my.formNeedsSave()) {
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
								$("#networkForm").find('#resetButton').click();		
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
		
		my.setupSearchHandlers();
	}
	
	my.formNeedsSave = function() {
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
	my.computeNodeStats = function(node) {
		if (node.data.type === my.ORGANISM_NODE) {
			
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
	
	// networks don't have to have names, since names can
	// be automatically generated later in the process, 
	// put in a placeholder value
	my.formatNodeTitle = function(node) {
	    var nodeData = node.data;
		if (nodeData.type == "network") {
			newTitle = my.suggestNetworkName(node);
			
			if (nodeData.enabled == false) {
				newTitle = '<strike>' + newTitle + '</strike>';
			}
			return newTitle;
		} 
		else {
			return nodeData.title;
		}
	}

	my.setupFileUploadForType = function(element_name, node, formData, focusOnNewNode) {
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
	
	my.setupFileUpload = function(node) {
		switch (node.data.type) {
		case my.NETWORK_NODE:
			console.log("setup upload for network node");
			formData = {
				organismId : node.data.organismId,
				networkId : node.data.id,
			};
			my.setupFileUploadForType('#fileupload', node, formData, false);
			my.setupFileUploadForType('#fileupload2', node, formData, false);
			break;
		case my.GROUP_FOLDER_NODE:
			console.log("setup upload for group node");
			formData = {
				organismId : node.data.organismId,
				groupId : node.data.id,
			};
			my.setupFileUploadForType('#fileupload', node, formData, true);
			break;
		case my.IDENTIFIERS_FOLDER_NODE:
			console.log("setup upload for identifiers folder node");
			formData = {
				organismId : node.data.organismId,
			};
			my.setupFileUploadForType('#fileupload', node, formData, true);
			break;
		case my.IDENTIFIERS_NODE:
			console.log("setup upload for identifiers");
			formData = {
				organismId : node.data.organismId,
				identifiersId: node.data.id,
			};	
			my.setupFileUploadForType('#fileupload', node, formData, false);
			break;
		case my.ATTRIBUTES_FOLDER_NODE:
			console.log("setup upload for attributes folder node");
			formData = {
					organismId: node.data.organismId,
			}
			my.setupFileUploadForType('#fileupload', node, formData, true);
			break;
		case my.FUNCTIONS_FOLDER_NODE:
			console.log("setup upload for functions folder node");
			formData = {
					organismId: node.data.organismId,
			}
			my.setupFileUploadForType('#fileupload', node, formData, true);
			break;
		case my.FUNCTIONS_NODE:
			console.log("setup upload for functions node");
			formData = {
					organismId: node.data.organismId,
					functionsId: node.data.id,
			};
			my.setupFileUploadForType('#fileupload', node, formData, false);
			my.setupFileUploadForType('#fileupload2', node, formData, false);
			break;
		default:
			console.log("no upload available for node type: " + node.data.type);
		}
	}
		
	my.setupForm = function(node) {
		
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
	
	my.reloadTree = function(organism_id) {
		$("#tree").fancytree("getTree").options.source.url = "organism/all";
		$("#tree").fancytree("getTree").options.source.data.id = organism_id;
		$("#tree").fancytree("getTree").reload();
	}
	
	// alpha versions of the latest fancytree ('fancytree') have a filtering extension.
	// but for the current stable version we roll our own, reusing what we can
	my.applyTreeFilter = function(filter) {
		
		my.clearTreeFilter();
		console.log("filter is: '" + filter + "'");
		if(typeof filter === "string"){
			var match = _escapeRegex(filter), // make sure a '.' is treated literally
			    re = new RegExp(".*" + match + ".*", "i");
			filter = function(node){
				return !!re.exec(node.title);
			};
		}
		
		$("#tree").fancytree("getRootNode").visit(function(node) {
			//console.log('filtering node %o', node);
			if (filter(node.data)) {
				console.log("match " + node.data.title);
				node.dmw_match = true;
				
				node.visitParents(function(parent) {
					// console.log("    submatch " + parent.data.title);
					parent.dmw_submatch = true;
					return true;
				});				
			}
			
			return true;
		});

		$("#tree").fancytree("getRootNode").visit(function(node) {
			if (!!(node.dmw_match || node.dmw_submatch)) {
				$(node.li).show();
			}
			else {
				$(node.li).hide();
			}
			return true;
		});	
		
		$("#tree").fancytree("getRootNode").render();
	}

	function _escapeRegex(str){
		/*jshint regexdash:true */
	    return (str + "").replace(/([.?*+\^\$\[\]\\(){}|-])/g, "\\$1");
	}

	my.clearTreeFilter = function() {
		$("#tree").fancytree("getRootNode").visit(function(node) {
			delete node.dmw_match;
			delete node.dmw_submatch;
			$(node.li).show();
			
			return true;
		});
		
		$("#tree").fancytree("getRootNode").render();
	}
	
	my.setupSearchHandlers = function() {
		/*
		 * Event handlers for our little demo interface
		 */
		$("input[name=search]").keyup(function(e){
			var match = $(this).val();
			if(e && e.which === $.ui.keyCode.ESCAPE || $.trim(match) === ""){
				$("button#btnResetSearch").click();
				return;
			}
			// Pass text as filter string (will be matched as substring in the node title)
			// kz var n = tree.applyFilter(match);
			
			my.applyTreeFilter(match);
			n = 99;
			$("button#btnResetSearch").attr("disabled", false);
			//$("span#matches").text("(" + n + " matches)");
		}).focus();
		
		$("button#btnResetSearch").click(function(e){
			$("input[name=search]").val("");
			// $("span#matches").text("");
// kz			tree.clearFilter();
			my.clearTreeFilter();
		}).attr("disabled", true);
		
		$("input#hideMode").change(function(e){
			tree.options.filter.mode = $(this).is(":checked") ? "hide" : "dimm"; 
// kz			tree.clearFilter();
			my.clearTreeFilter();
			$("input[name=search]").keyup();
//			tree.render();
		});
	}
	
	return my;
}(dmw || {}, $);