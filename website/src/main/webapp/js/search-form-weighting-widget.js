$(function(){
	
	if( $("#phrase_and_go").size() == 0 && $("#query_line").size() == 0 ){
		return; // ignore this js file if no query area
	}
    
	/***************************************************************************
	 * radio buttons for weighting
	 **************************************************************************/
	$(".network_weighting_group input").change(function(){
		var id = $(this).attr("id");
		var label = $("label[for=" + id + "]");
		
		track("Network weighting", "Change", label.text());
	});
});
