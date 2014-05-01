;(function($){
    
	var options;
	var map = {};
	
    $.i18n = function(opts){
    	var defaults = {
            url: null,
            success: function(){}
        };
        
    	if( typeof(opts) == "string" ){ 
    		var message = opts;
    		
    		var ret = "" + map[message.toLowerCase()];
    		
    		for(var i = 1; i < arguments.length; i++){
    			var arg = arguments[i];

    			while( ret.match("\\{" + (i - 1) + "\\}") ){
    				ret = ret.replace("{" + (i - 1) + "}", arg);	
    			}
    		}
    		
    		return ret;
    	} else {
    		options = $.extend(defaults, opts);
    		
    		$.ajax({
    			url: options.url,
    			cache: false, // never cache the file to avoid sync issues
    			async: false,
    			success: function(data){
    				
    				$.each(data, function(i, v){
    					map[ i.toLowerCase() ] = v;
    				});
    				
    				
    				typeof options.success == typeof function(){} && options.success();
    			}
    		});    		
    	}
    }
   
    
})(jQuery);  