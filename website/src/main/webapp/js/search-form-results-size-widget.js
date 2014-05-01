$(function(){
	
	if( $("#phrase_and_go").size() == 0 && $("#query_line").size() == 0 ){
		return; // ignore this js file if no query area
	}
    

	/***************************************************************************
	 * Number of gene results
	 **************************************************************************/
	$("#threshold").change(function(){
		track("Number of gene results", "Change", $(this).val());
	});
});
