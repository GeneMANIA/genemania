CytowebUtil.checkVersionFor = function(name){
		var version = "" + $("html").attr("webversion");
	var dbVersion = "" + $("html").attr("dbversion");
	var newVersion;
	var newDbVersion;
	
	var error = false;
	
	$.ajax({
		async: false,
		type: "GET",
		data: {},
		dataType: "json",
		url: absoluteUrl("json/version"),
		error: function(request, status, error){
			warning();
			error = true;
		},
		success: function(data, status, request){
			newVersion = "" + data.webappVersion;
			newDbVersion = "" + data.dbVersion;
			
		}
	});
	
	if( error ){
		return false;
	}
	
	function warning(){
		$('<p>GeneMANIA has been updated since you performed your search.  To make sure that your ' + name + ' is consistent ' +
				'with results from the new version of GeneMANIA, please resubmit your search and try to generate the ' +
				name + ' again.</p>').dialog({
			title: "The " + name + " can not be generated",
			buttons: {
				"OK, resubmit the search for me.": function(){ $(this).dialog("close"); $("#reloader").show(); $("#resubmit_form").submit(); },
				"Nevermind, I will resubmit myself later.": function(){ $(this).dialog("close"); }
			},
			modal: true,
			closeOnEscape: true,
	    	resizable: false,
	    	width: 500,
	    	minHeight: 0
		});
	}
	

	// ok to print if versions match or local debug version
	if( version.search("antdebug") < 0 && ( newVersion != version || newDbVersion != dbVersion ) ){
//			console.log("version: " + version);
//			console.log("new version: " + newVersion);
//			console.log("dbversion: " + dbVersion);
//			console.log("new dbversion: " + newDbVersion);
//			console.log("different versions -- can't print");
		
		warning();
		
		return false;
	} else {
//			console.log("ok to make report");
		
		return true;
	}
};