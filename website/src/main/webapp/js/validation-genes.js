function validateGeneSymbols() {
	
	if( !validMaxGeneList() ){
//console.log("too many genes to validate");
		_between_edit_and_validated = false;
		$("#gene_area").trigger("validationcomplete");
		return; // don't validate on huge lists
	}
	
	var msg = "";
	var type = "warning";
	var code = 0;
	var org = $('#species_select').val();
	var geneSet = $("#gene_area").val();
	var errMsg = "";
	var details = "";
	var val_result;
		
	var counting_sem;
	function get_sem(){
		_validating_active_count++;
		counting_sem = ++_validating % 1000000;
		return counting_sem;
	}
	
	function release_sem(){
		_validating_active_count--;
	}
	
	get_sem();
//console.log("--\nvalidating gene symbols");
//console.log("semaphor id: "+counting_sem);
//console.log("number of active validation threads: "+_validating_active_count);
	
	function latest_sem(){
//console.log("sem : " + counting_sem);
		return counting_sem == _validating;
	}

	
	// don't validate if the gene list is the same
	if (_lastValidationGeneSet.toLowerCase() == geneSet.toLowerCase() && _lastValidationOrg.toLowerCase() == org.toLowerCase()
	&& validMinGeneList() // need to show error even if the same
	) {
		if( latest_sem() ){
			_between_edit_and_validated = false;
		}
		release_sem();
//console.log("validation thread died before starting; same as last");
//console.log( geneSet );
//console.log( org );
//console.log("with last");
//console.log( _lastValidationGeneSet );
//console.log( _lastValidationOrg );
		
		removeLoadingIconsForGenes();
		updateErrorMessage();
		
		$("#gene_area").trigger("validationcomplete");
		return;
	}
	
	_lastValidationGeneSet = geneSet;
	_lastValidationOrg = org;
	
	// min/max validation 
	if (! validMinGeneList()) {
		showGeneError("error", "Please enter at least one gene symbol.", undefined, false);
			
		if( latest_sem() ){
			_between_edit_and_validated = false;
		}
		
		release_sem();
//console.log("empty die early: "+_validating_active_count);
		
		removeLoadingIconsForGenes();
		
		// We don't need to call the server side if there is no gene!
		$("#gene_area").trigger("validationcomplete");
		return;
	}
	
	function ajax_handler(data){
		if( !latest_sem() ){
			release_sem();
			_between_edit_and_validated = false;
			removeLoadingIconsForGenes();
			$("#gene_area").trigger("validationcomplete");
			return; // if we're not the last validation thread created, just die
		}
		
		var errMsg = "";
		details = "";
		var MAX_GENES = 10;
		
		removeLoadingIconsForGenes();
		setIcons(data);
		updateErrorMessage();
		
		release_sem();
//console.log("releasing semaphore with new active thread count of "+_validating_active_count);
		
		_between_edit_and_validated = false;
		$("#gene_area").trigger("validationcomplete");
		
	}
	
	$.ajax({
		cache: false,
		success: function(data, status, request){
//			console.log(data);
			ajax_handler(data);
		},
		dataType: "json",
		data: { organism: org, genes: geneSet },
		type: "POST",
		url: absoluteUrl("json/gene_validation")
	});
}


function maxNumberOfGenes(){
	return get_number_of_icons();
}

function numberOfGenes(){
	return $("#gene_area").val().split("\n").length;
}

function clearGeneError(){
	clearError( $("#gene_text"), $("#gene_error") );
	clearError( $("#gene_open") );
	
		
	$(".qtip[qtipfor=gene_selection]").remove();
}

function showGeneError(type, msg, details, show_tooltip) {
	clearGeneError();

	// update tooltips for both gene inputs
	setError({ type: type, msg: msg, details: details }, $("#gene_text"), $("#gene_error"), true);
	setError({ type: type }, $("#gene_open"));


	if( show_tooltip == undefined || show_tooltip ){
		var prev_qtip = $(".qtip[qtipfor=gene_selection]");
		var had_prev_qtip = prev_qtip.size() > 0;
		prev_qtip.remove();

		$("#gene_area").qtip({
			content: {
				title: { 
					text: msg
				},
				text: details
			},
			show: {
				delay: 0,
				when: { event: "showtooltip", target: $("#gene_area") },
				effect: { type: "fade", length: 0 },
				ready: true // Show the tooltip when ready
			},
			hide: {
				delay: 0,
				effect: { type: "fade", length: 0 },
				when: { event: "hidetooltip", target: $("#gene_area") }, // Hide when clicking anywhere else
				fixed: true // Make it fixed so it can be hovered over
			},
			style: {
			   border: { width: 1, radius: 8 },
			   width: { min: 360, max: 360 },
			   screen: true,
			   padding: 8, 
			   textAlign: 'left',
			   name: 'cream',
			   tip: true      // Give it a speech bubble tip with automatic corner detection
			},
			position: {
				type: "absolute",
				adjust: {
					y: -12,
					screen: true,
					scroll: true
				},
				corner: {
					target: 'leftTop',
					tooltip: 'rightTop'
				}
			}
		});
		
		var qtip = $("body").children(".qtip:last");
	
		qtip.hide();
		qtip.attr("qtipfor", "gene_selection").addClass("gene_error_tooltip");
		
		// fix alt tab bug
		qtip.qtip("api").beforeShow = function(){
			if( !$("#gene_area").is(":visible") ){
				return false;
			}
		};
		
		if( $("#gene_area").is(":visible") ){
			$("#gene_area").trigger("showtooltip");
		}
		
		$("#gene_area").bind("adjusttooltipposition", function(){
			$(".qtip[qtipfor=gene_selection]").each(function(){
				$(this).qtip("api").updatePosition();
			});
		});
	}
	

}

function updateGeneCount(){
	var count;
	
	if( $("#gene_area").val() == "" || $("#gene_area").val().match(/^\s*$/g) ){
		count = 0;
	} else {
		count = numberOfGenes();
	}
	
	$("#gene_count").html(count);
}

var _geneAreaLineHeightCached = undefined;
function geneAreaLineHeight(){
	
	if( _geneAreaLineHeightCached == undefined ){
		var div = $("<div>hello&nbsp;</div>").css({
			"font" : $("#gene_area").css("font"),
			"position" : "absolute",
			"left" : 0,
			"top" : 0,
			"font-size" : $("#gene_area").css("font-size")
		});
		
		if( $.browser.msie && parseFloat($.browser.version) <= 8 ){
        	// ie8 does this for garbage and returns 1px
        } else {
        	div.css({
        		"line-height": $("#gene_area").css("line-height")
        	});
        }
		
		$("body").append(div);
		
		
		var lineHeight = parseFloat( div.css("height") );
		
		if( isNaN(lineHeight) ){
			lineHeight = div.height();
		}
		
		div.remove();
		_geneAreaLineHeightCached = lineHeight;
	}
	
	return _geneAreaLineHeightCached;
}

