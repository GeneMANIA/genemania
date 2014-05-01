$(function(){
	
	if( $("#phrase_and_go").size() == 0 && $("#query_line").size() == 0 ){
		return; // ignore this js file if no query area
	}
	
	// find button setup
	   
	$("#stopBtn").hide().bind("click", function(){
		$("#loading_line").css("visibility", "hidden");
		
		if( window.stop ){
			window.stop();
		} else if( $.browser.msie ){
			document.execCommand("Stop");
		}
		
		$("#findBtn").show();
    	$("#stopBtn").hide();
	});
    
    $("#findBtn").click(function(){
// console.log("--\n find button clicked");
    
    	if( isSubmittable() ){
    		$("#loading_line").css("visibility", "visible");
    		
    		// disable stop button for now
    		// $("#findBtn").hide();
    		// $("#stopBtn").show();
// console.log("find button in submittable state");
// console.log("remove networks for other organisms");
    		 $(".query_networks[organism!=" + $("#species_select").val() + "]").remove();
    		 $(".query_network_group[organism!=" + $("#species_select").val() + "]").remove();
    		 
    		$("#relatedGenes").submit();
    	} else {
// console.log("find button in NON submitable state");
    		
    		$("#loading_line").css("visibility", "hidden");
    		
    		function show_tooltip(msg, type){
				$("#findBtn").qtip({
					content: {
						text: msg,
						title: {
							text: 'Warning',
							button: '<div class="ui-state-error ui-corner-all"> <span class="ui-icon ui-icon-close"></span> </div>'
						}
					},
					show: {
						delay: 0,
						when: false,
						effect: { type: "fade", length: 0 },
						ready: true // Show the tooltip when ready
					},
					hide: {
						delay: 0,
						effect: { type: "fade", length: 0 },
						when: { event: "unfocus" }, // Hide when clicking
													// anywhere else
						fixed: true // Make it fixed so it can be hovered over
					},
					style: {
					   border: { width: 1, radius: 8 },
					   width: { min: 0
					   },
					   screen: true,
					   padding: 8, 
					   textAlign: 'left',
					   name: 'red', 
					   tip: true      // Give it a speech bubble tip with
										// automatic corner detection
					},
					position: {
						type: "absolute",
						adjust: { 
							screen: true
						},
						corner: {
						 	target: 'topMiddle',
						 	tooltip: 'bottomRight'
					  	}
					}
				});
				
				var qtip = $("body .qtip:last");
				
				qtip.qtip("api").onHide = function(){
					qtip.qtip("api").destroy();
				};
				
				qtip.attr("qtipfor", "findBtn");
				qtip.attr("errortype", type);
			}
    		
    		if( isValidating() ){
    			$("#loading_line").css("visibility", "visible");
// console.log("validating find button form submission");
    			
    			var interval = setInterval(function(){  			
    				if( !isValidating() ){
    					clearInterval(interval);
    					
    					if( inGeneErrorState() ){
    						$("#loading_line").css("visibility", "hidden");
// console.log("found error after waiting for find button validtion");
    					} else if( isSubmittable() ) {
// console.log("submit after waiting for find button validation");
    						$("#relatedGenes").submit();
    					} else {
// console.log("try again after waiting for find button validation");
    						$("#findBtn").click();
    					}
    					
    					
    				}
    			}, 100);
    		} else if( !validMaxGeneList() ){
    			show_tooltip(too_many_genes_message, "too_many_genes");
    		} else if( noGenesEntered() ){
    			validateGeneSymbols();
    			show_tooltip('<p>Please enter at least one gene, or <a href="#" class="action_link default_genes_link also_submit">try this example gene list</a>.</p>', "empty");
    		} else if( inGeneErrorState() ){
    			show_tooltip('<p>Please enter at least one valid gene, and then try again.</p>', "no_valid_genes");
    		} else if( areNetworksReloading() ) {
    			$("#loading_line").css("visibility", "visible");
// console.log("find button validation waiting for networks to reload");
    			
    			var interval = setInterval(function(){  			
    				if( !areNetworksReloading() ){
    					clearInterval(interval);
    					
    					if( isSubmittable() ){
// console.log("submit after waiting for submit button");
							$("#relatedGenes").submit();
						} else {
// console.log("try again after waiting for submit button");
							$("#findBtn").click();
						}
    				}
    			}, 100);
    		} else if( uploadingNetwork() ) {
    			$("#loading_line").css("visibility", "visible");
    			// console.log("find button validation waiting for networks to reload");
    			    			
    			    			var interval = setInterval(function(){  			
    			    				if( !uploadingNetwork() ){
    			    					clearInterval(interval);
    			    					
    			    					if( isSubmittable() ){
    			// console.log("submit after waiting for submit button");
    										$("#relatedGenes").submit();
    									} else {
    			// console.log("try again after waiting for submit button");
    										$("#findBtn").click();
    									}
    			    				}
    			    			}, 100);
    		} else if( noNetworksSelected() ) {
// console.log("no networks selected for find button");
    			
    			show_tooltip('<p>Please enable at least one network in the advanced options, or <a href="#" class="action_link default_networks_link also_submit">use the default networks</a>.</p>', "no_networks_selected");
    			
    		} else {
// console.log("something else is wrong for submitting the find button");
    		}
    		
    		
    		
    	}
    	
    	
    	
    });
	
});