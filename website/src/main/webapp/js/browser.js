$(function(){
	
	if( $("#phrase_and_go").size() == 0 && $("#query_line").size() == 0 ){
		return; // ignore this js file if no query area
	}
	
	$.browserupdate({
		siteName: "GeneMANIA",
		browsers: {
			chrome: {
				name: "Chrome",
				version: 15,
				url: "http://www.google.com/chrome"
			},
			firefox: {
				name: "Firefox",
				version: 8,
				url: "http://firefox.com"
			},
			safari: {
				name: "Safari",
				version: 5.1,
				url: "http://www.apple.com/safari/download/"
			},
			msie: {
				name: "Internet Explorer",
				version: 9,
				url: "http://windows.microsoft.com/en-US/internet-explorer/products/ie/home"
			}
		}
	});
	
});