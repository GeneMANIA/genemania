$(function(){
   // debug box
    $("#debug_info").dialog({
    	autoOpen: false,
    	title: "Debug information",
    	width: 400,
    	height: 400
    });
    
    $("#debug_info .tabs").tabs();
    
    var timeout;
    var count = 0;
    var times = 3;
    var time = 1000;
    
   
    function toggle_debug(){
    	count = 0;
    	clearTimeout(timeout);
    	
    	if( $("#debug_info").is(":visible") ){
			$("#debug_info").dialog("close");
		} else {
			$("#debug_info").dialog("open");
		}
    }
	
	$("html").bind("keydown", function(e){
		if( e.which == 192 && e.shiftKey ){ // backtab with shift`
			toggle_debug();			
		} else if( e.which == 192 ){
			count++;

			if( count == 1 ){
				clearTimeout(timeout);
				timeout = setTimeout(function(){
					count = 0;
				}, time);
			} else if( count >= times ){
				toggle_debug();
			}
		}
	});
	
	$("#debug_info_networks button").click(function(){
		var networks = $("#debug_info_networks textarea").val().split("\n");
		var ids = [];
		
		$.each(networks, function(i, network){
			var id = parseInt(network);
			
			if( !isNaN(id) ){
				ids.push(id);
			}
		});
		
		if( $(".query_network_checkbox[value=" + ids[0] + "]").size() == 0 ){
			$(this).after(' <span class="done_message">Networks set</span>');
			return;
		}
		
		$(".query_network_checkbox").attr("checked", null);
		
		$.each(ids, function(i, id){
			$(".query_network_checkbox[value=" + id + "]").attr("checked", true);
    	});
	    	
	 
    	updateParentChecks();
    	refreshAllGroupCounts();
    	
    	$(this).siblings(".done_message").remove();
		$(this).after(' <span class="done_message">Networks set</span>');
		var input = $(this);
		setTimeout(function(){
			input.siblings(".done_message").fadeOut(500);
		}, 2000);
	});
});