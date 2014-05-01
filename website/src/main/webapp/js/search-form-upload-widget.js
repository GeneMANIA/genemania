$(function(){
	
	if( $("#phrase_and_go").size() == 0 && $("#query_line").size() == 0 ){
		return; // ignore this js file if no query area
	}
    
    var networkHelpDialog = $("#uploadHelpDialog").dialog({
    	autoOpen: false,
    	modal: true,
    	closeOnEscape: true,
    	resizable: false,
    	width: 400,
    	position: ["center", "center"],
    	buttons: {
			"OK, bring me back to GeneMANIA.": function(){ networkHelpDialog.dialog("close"); }
		}
    });
    
	$("#uploadHelpBtn").click(function() {
		networkHelpDialog.dialog("open");
		return false;
	});
	
    /* upload button */

    _uploading = false;
    function process_upload(data, status, request, error){
    	if(error == null){
    		error = data.error;
    	}
//    	console.log(data);
    	showUploadCompleted(data, error);
    	$("#uploadArea").removeClass("disabled");
    	
    	if( data.network != null ){
    		if( error ){
    			track("Upload", "Error", error);
    		} else {
    			track("Upload", "Success");
    		}
    	} else {
    		track("Upload", "Error", "No network was received from the server");
    	}
    	
    	_uploading = false;
    	$("body").trigger("uploadcomplete");
    }
    
    function upload(file, fileName){
    	_uploading = true;
    	
    	var organismId = $("#species_select").val();
    	
    	$("#uploadArea").addClass("disabled");
    	showUploadProgress(fileName);
    	
    	$.ajax({
    		url: absoluteUrl("json/upload_network"),
    		type: "POST",
    		data: { organism_id: organismId, file: file, file_name: fileName },
    		dataType: "json",
    		success: function(data, status, request){
//    			console.log(data);
//    			console.log(status);
//    			console.log(request);
    			
    			process_upload(data, status, request, null);
    		},
    		error: function(request, status, error){
//    			console.log(error);
//    			console.log(status);
//    			console.log(request);
    			
    			process_upload({ error: "no data" }, status, request, error);
    		}
    	});
    }
    jsUpload = function(file, name){
    	track("Upload", "Start", "File size (bytes)", file.length);
    	upload(file, name);
    }
    
    if( FlashDetect.versionAtLeast(MIN_FLASH_VERSION) && $.cookiesenabled() ) {
    	// Use the flash upload component
    	var options = {
                swfPath: absoluteUrl("swf/Importer"),
                flashInstallerPath: absoluteUrl("swf/playerProductInstall"),
                data: function(data){
//    			console.log(data);
    				
    				track("Upload", "Start", "File size (bytes)", data.bytes.length);
    		
    				var str = "";
    				for(var i = 0; i < data.bytes.length; i++){
//    					console.log("character");
    					
    					var ch = String.fromCharCode( data.bytes[i] );
    					str += ch;
//    					
//    					console.log("character");
//    					console.log(ch);
    				}
//    				console.log(str);
    			
        			upload(str, data.metadata.name);
				},
	            ready: function(){
	            	// when the flash component is loaded
	            },
	            typeFilter: function(){
	                return "*";
	            },
	            binary: function(metadata){
	            	return true; // to return data.string and not data.bytes
	            } 
            };
            
        new org.cytoscapeweb.demo.Importer("uploadOverlay", options);
    } else {
    	$("#uploadOverlay").hide();
    }
    
	$("#uploadBtn").bind("click", function(){
		$('<div><p>You need to have <a href="http://get.adobe.com/flashplayer/">Flash</a> installed and cookies enabled ' +
				'to use the upload feature.  Please <a href="http://get.adobe.com/flashplayer/">install Flash</a>, enable cookies, ' +
				'and then reload GeneMANIA.</p> ' +
				'<p>You will need <a href="http://get.adobe.com/flashplayer/">Flash</a> installed if you want to view ' +
				'GeneMANIA\'s visualization, anyway.  So, it is advised that you install <a href="http://get.adobe.com/flashplayer/">Flash</a>. ' +
		'</div>').dialog({
			title: "You need Flash and cookies to upload",
			buttons: {
				"OK, return me to GeneMANIA": function(){ $(this).dialog("close"); }
			},
			modal: true,
			closeOnEscape: true,
	    	resizable: false,
	    	width: 300,
	    	minHeight: 0
		});
    });
});
