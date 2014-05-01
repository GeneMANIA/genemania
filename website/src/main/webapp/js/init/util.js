$(function(){

	/**********************************************
	 * Loader
	 **********************************************/
	
    $.fn.showLoader = function(options) {
	    var options = $.extend({
	        message: "Loading..."
	    }, options);

	    var loader = $('<div class="loader">' +
                           '<div>' +
                               '<div class="icon"></div>' +
                               '<div class="message">' + (options.message || '') + '</div>' +
                           '</div>' +
                       '</div>');
        $(this).append(loader);
    };
    
    $.fn.hideLoader = function() {
        $(this).find(".loader").fadeOut(ANI_SPD, function(){
            $(this).remove();
        });
    };
 

});


function rgb2hex(rgb) {
    rgb = rgb.match(/^rgb\((\d+),\s*(\d+),\s*(\d+)\)$/);
    function hex(x) {
        return parseInt(x).toString(16);
    }
    return "#" + hex(rgb[1]) + hex(rgb[2]) + hex(rgb[3]);
}

if( !window.console || !console || !console.log ){
	console = { log: function(){} };
}

function absoluteUrl(relativeUrl){
	return $("html").attr("contextpath") + "/" + relativeUrl;
}

function track(object, action, label, value){
	// don't track while progress bar is going
	if( !$("#progress").is(":visible") ){
//		console.log("track (" + object + ", " + action + ", " + label + ", " + value + ")");
		
		_gaq.push(['_trackEvent', object, action, label, value]);
	}
}