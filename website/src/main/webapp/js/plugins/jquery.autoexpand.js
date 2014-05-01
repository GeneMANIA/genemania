(function($){

    $.fn.autoexpand = function(options) {  
        var defaults = {
            minLines: undefined,
            maxLines: undefined,
            timeBetweenSizeChanges: 100, // once one event is fired to change the text box size, other events are ignored for this No. of ms, and then the text box is resized
            canShrink: true
        };
    
        options = $.extend({}, defaults, options);
        
        return $(this).each(function(){
            
            var target = $(this);
            var timeout = undefined;

            var div = $("<div>&nbsp;</div>").css({
                "font" : target.css("font"),
                "font-size" : target.css("font-size"),
                "position" : "absolute",
                "left" : 0,
                "top" : 0
            });
            
            if( $.browser.msie && parseFloat($.browser.version) <= 8 ){
            	// ie8 does this for garbage and returns 1px
            } else {
            	div.css({
            		"line-height": target.css("line-height")
            	});
            }
            
            $("body").append(div);
            var lineHeight = parseFloat( div.css("height") );
            
            if( $.browser.msie || isNaN(lineHeight) ){
            	lineHeight = div.height();
            }
      
            div.remove();
            
            var clone = $(this).clone(false);
            
            $("body").append(clone);
            
            $(clone).attr("id", "").css({
                "position" : "absolute",
                "width" : target.width() || target.css("width"),
                "height" : lineHeight,
                "overflow-x" : target.css("overflow-x"),
                "overflow-y" : "scroll",
                "font" : target.css("font"),
                "white-space": target.css("white-space"),
                "font-size" : target.css("font-size")
            });
            
            if( $.browser.msie && parseFloat($.browser.version) <= 8 ){
            	// ie8 does this for garbage and returns 1px
            } else {
            	$(clone).css({
            		"line-height": target.css("line-height")
            	});
            }
            
            clone.css({
                "left" : -2 * clone.width(),
                "top" : -2 * clone.height()
            });
            
            var widthWithoutScroll = 2 * clone.width() - clone.attr("scrollWidth");
            var widthWithScroll = clone.width();
            
            function scrollMax(obj) {
                var prevScroll = 0;
                obj.scrollTop(1);
                while( obj.scrollTop() != prevScroll ) {
                    prevScroll = obj.scrollTop();
                    obj.scrollTop(prevScroll * 2);
                }
                
                if(obj.scrollTop() == 1) {
                    obj.scrollTop(0);
                }
            }
            
            function getCloneHeight() {
                return clone.scrollTop() + clone.innerHeight();
            }
           
            function fix() {           
                
                if( target.css("overflow-y") == "scroll" ) {
                    clone.width( widthWithScroll );
                } else {
                    clone.width( widthWithoutScroll );
                }
    
                clone.val( target.val() );
                
                
                // safari 3 fix
                // TODO remove on safari 4 main release
                if( $.browser.safari && parseInt( $.browser.version ) <= 525 ) {
                    var text = target.val();
                    
                    if( text.charAt( text.length - 1 ) == '\n' ) {
                        text = text.substr( 0, text.length - 1 );
                        clone.val( text );
                    }
                }
                
                var minHeight = (options.minLines * lineHeight) || parseFloat( target.css("min-height") ) || lineHeight;
                var maxHeight = (options.maxLines * lineHeight) || parseFloat( target.css("max-height") ) || target.height();
                
                // use scroll top, since scroll height can be buggy in some browsers
                // increase until can't be bigger
                
                scrollMax(clone);
                var newHeight = getCloneHeight();
                var oldHeight = target.height();
                
                if( ! options.canShrink && newHeight < target.height() ) {
                    newHeight = target.height();
                }
                
                if( newHeight < minHeight ) {
                    target.css("overflow-y", "hidden");
                    target.css( "height", minHeight );
                } else if( newHeight > maxHeight ) {
                    if( target.css("overflow-y") != "scroll" ) {
                        target.css("overflow-y", "scroll");
                    }
                    if( target.height() < maxHeight ) {
                        target.css("overflow-y", "hidden");
                        target.css("height", maxHeight);
                    }
                } else {
                    target.css("overflow-y", "hidden");
                    target.css( "height", newHeight );
                }

                if (oldHeight != newHeight && options.onResize) {
                	options.onResize();
                }
            }
            
            $(this).bind("click mouseup blur keyup input keydown keypress focus paste change", function(e){

//            	console.log(e.type);
//            	console.log($(this).val());
            	
            	clearTimeout(timeout);
            	timeout = null;
            	
                if( ! timeout ) {
//                	console.log("fix");
//                	console.log($(this).val());
                    fix();
                    
                    timeout = setTimeout(function(){
                        fix();
                        timeout = undefined;
                    }, options.timeBetweenSizeChanges);
                }
            });
            
            fix(); // initial fix for current text
        });
    }

})(jQuery); 