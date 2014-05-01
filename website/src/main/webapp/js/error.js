$(function(){
	if( $("#error_page").size() == 0 ){
		return;
	}
	
	$("body").bind("keydown", function(e){
		if( e.which == 192 && e.shiftKey ){ // backtab ` or ~
			
			$(".error_details").toggle();
			
		}
	});
	
	if( $("#error_page").hasClass("email") ){
	
		var msg = "An exception was caught by the GeneMANIA web application.  It is logged below.\n\n";
		
		var i = 0;
		$(".error_details").children().each(function(){
			
			if( i % 2 == 0){
				msg += "====\n";
			}
			
			msg += $(this).text() + "\n";
			i++;
		});
		
		$.ajax({
			type: "POST",
			url: absoluteUrl("json/mail"),
			data: { 
				subject: "GeneMANIA exception: " + $(".error_details").attr("exception"),
				message: msg
			},
			success: function(){
				//console.log("sent email");
				$("#loading_message").attr("class", "").addClass("sent");
			},
			error: function(){
				//console.log("could not send mail");
				$("#loading_message").attr("class", "").addClass("error");
			}
		});
		//console.log("error js");
	
	}
});