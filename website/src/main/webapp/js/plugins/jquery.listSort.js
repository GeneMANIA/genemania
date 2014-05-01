(function($){  
    $.fn.listSort = function(options) {  
        
        var defaults = {
            value: "label",
            descending: true,
            attribute: undefined
        };
        
        var options = $.extend(defaults, options); 
        
        function sortList(ul, child, descending, attribute) {
            
            var items = ul.children("li");
    
            items.sort(function(x, y){
                var ret;
                var xVal, yVal;

                if( attribute != null ){
                	xVal = $(x).find(child).attr( attribute );
                	yVal = $(y).find(child).attr( attribute );
                } else {
                	xVal = $(x).find(child).text() || "";
                    yVal = $(y).find(child).text() || "";
                }
                
                // use integer values if possible
                var xFloat = parseFloat(xVal);
                var yFloat = parseFloat(yVal);
                
                if( isFinite(xFloat) && isFinite(yFloat) ) {
                    xVal = xFloat;
                    yVal = yFloat;
                } else {               
                    xVal = $.trim(xVal).toLowerCase();
                    yVal = $.trim(yVal).toLowerCase();
                }
                
                if( xVal < yVal ) {
                    ret = -1;
                } else if ( xVal > yVal ) {
                    ret =  1;
                } else {
                    ret = 0;
                }
                
                if( ! descending ) {
                    return ret;
                } else {
                    return -1 * ret;
                }
            });
            
            $.each(items, function(i, li){
                ul.append(li);
            });
            
            items.children("ul").each(function(){
                sortList( $(this), child, descending, attribute);
            });
    
        }
        
        return this.each(function() {  
            sortList( $(this), options.value, options.descending, options.attribute );
        });  
    };  
})(jQuery);  