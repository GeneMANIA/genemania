_edgesCache = null;

CytowebUtil.refresh = function() {
	// Create a Cytoscape Lite instance:
	// ---------------------------------------------------------------------------------------------
	var options = {
			swfPath: absoluteUrl("swf/CytoscapeWeb_1.0.2"),
			flashInstallerPath: absoluteUrl("swf/playerProductInstall"),
			flashAlternateContent: '<div class="ui-state-error ui-corner-all"><p>GeneMANIA requires the Adobe Flash Player to use all of its features.</p>' +
			                       '<p><a href="http://get.adobe.com/flashplayer/"><img width="160" height="41" border="0" alt="Get Adobe Flash Player" src="http://www.adobe.com/macromedia/style_guide/images/160x41_Get_Flash_Player.jpg"></a></p></div>'
	};
	_vis = new org.cytoscapeweb.Visualization("graphBox", options);
	_vis.embedSWF = this._embedSWF;
	
	_vis.shapeMapper = CytowebUtil.shapeMapper;
	_vis.imageMapper = CytowebUtil.imageMapper;
	
	// Overwrite callback functions of interest:
	// ---------------------------------------------------------------------------------------------
	_vis.ready(function() {
		CytowebUtil.recomputeLayout();

		// Update the page:
		_vis.swf().focus(); // So ctrl-click, etc. work without having to click the CW area first!

		_edgesCache = CytowebUtil.edges();
		
		CytowebUtil.updateNetworksTab();
    	if (onCytoscapeWebLoaded) { onCytoscapeWebLoaded(); }
    	
    	// Context menu items:
	    _vis.addContextMenuItem("About Cytoscape Web...", function(evt) {
    		window.open("http://cytoscapeweb.cytoscape.org/");	
    	});

    	// Workaround to prevent the cursor from disappearing when CytoWeb is using a custom cursor
    	// and the mouse is over an HTML container that overlaps the Flash area:
    	$(".over_flash, .qtip").live("mouseover", function() {
			if (_vis) { _vis.customCursorsEnabled(false); }
		}).live("mouseout", function() {
			if (_vis) { _vis.customCursorsEnabled(true); }
		});
		
		// enable query genes grey colour by default
        $("#go_tab .content table tr.query .add_button").children(":first").click();
        
        // no tooltips open at start
        $("#menu_close_tooltips").addClass("ui-state-disabled");
        
        progress("cytolite");
        $("body").trigger("cytoweb"); // added to help the QUnit test recognize when the results page is loaded
        //console.log(_vis.xgmml());
		
    });

	_vis.onEdgeTooltip = function(data) {
		var w = Math.round(data.weight*1000)/10;
		w = w < 0.1 ? "&lt; 0.1" : w;
		return "<b>weight:</b> " + w;
	}
	
	CytowebUtil._addListeners();
    
    // Draw the network:
    // ---------------------------------------------------------------------------------------------
	
	CytowebUtil.loadNetwork();
};


CytowebUtil.loadNetwork = function(){
	var selected_networks = [];
	var selected_attrgroups = [];
	
	$("#selected_networks").children().filter("[network]").each(function(){
		var id = parseInt( $(this).attr("network") );
		selected_networks.push( id );
	});
		
	$("#selected_networks").children().filter("[attrgroup]").each(function(){
		var id = parseInt( $(this).attr("attrgroup") );
		selected_attrgroups.push( id );
	});
	
	//console.log(selected_networks);
	$.ajax({
	    type: "POST",
	    url: absoluteUrl("json/visualization?" + Math.random()),
	    dataType:"json",
	    data: {
			networks: selected_networks,
			organism: $("[name=organism]").val(),
			genes: $("[name=genes]").val(),
		 	weighting: $("[name=weighting]:checked").val(),
			threshold: $("[name=threshold]").val(),
			attrThreshold: $("[name=attrThreshold]").val(),
			attrgroups: selected_attrgroups
		},
	    success: function (response) {
			CytowebUtil.OPTIONS.network = response;
//				console.log("response");
//				console.log(response);
			
			_vis.draw(CytowebUtil.OPTIONS);
	    },
	    error: function (xhr, ajaxOptions, thrownError){
	    	//console.log('Error loading network : ' + thrownError);
	    },
	    cache: false
	});
};
	




CytowebUtil.error = function(error){
	
	var msg = "An exception occurred in Cytoscape Web and was caught by the GeneMANIA web application.  It is logged below.\n";
	
	msg += "\n====\nID\n" + error.id;
	
	msg += "\n====\nName\n" + error.name;
	
	msg += "\n====\nMessage\n" + error.msg;
	
	msg += "\n====\nStack trace\n" + error.stackTrace;
	
	msg += "\n\n\n======== Debug info ========\n\n";
	
	var i = 0;
	$("#debug_info_values").children().each(function(){
		
		if( i % 2 == 0){
			msg += "====\n";
		}
		
		msg += $(this).text() + "\n";
		i++;
	});
	
	$("#cytoweb_error").show();
	
	$.ajax({
		type: "POST",
		url: absoluteUrl("json/mail"),
		data: { 
			subject: "GeneMANIA exception: " + error.name + " : " + error.msg,
			message: msg
		},
		success: function(){
			$("#cytoweb_error .sending").hide();
			$("#cytoweb_error .sent").show();
		},
		error: function(){
			$("#cytoweb_error .sending").hide();
			$("#cytoweb_error .error").show();
		}
	});
};


