$(function(){
	if( $("#search_page").size() != 0 || $("#results_page").size() != 0 ){
		// load i18n
		$.i18n({
			url: absoluteUrl("json/i18n")
		});
	}
});