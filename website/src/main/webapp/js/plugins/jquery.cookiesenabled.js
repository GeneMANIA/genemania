(function(jQuery) {
	
	$.cookiesenabled = function(){
		$.cookie("cookiesenabled", "true");
		
		if( $.cookie("cookiesenabled") == "true" ){
			$.cookie("cookiesenabled", null);
			return true;
		} else {
			return false;
		}
	};
	
})(jQuery);