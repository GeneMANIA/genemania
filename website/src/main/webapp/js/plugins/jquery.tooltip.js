/* (c) 2009
   AUTHORS: Max Franz
   LICENSE: TODO
*/

;(function($){
    
	var visibleTooltips = {};
	
    $.tooltip = new Object();
    
    $.tooltip.defaults = {
        toolTipId: "tool_tip",
        toolTipClass: "tool_tip",
        toolTipTopArrowId: "tool_tip_top",
        toolTipBottomArrowId: "tool_tip_bottom",
        toolTipPointerClass: "tool_tip_pointer", // must have width set in css
        toolTipTextId: "tool_tip_text",
        toolTipDirectionUp: "points_to_top",
        toolTipDirectionDown: "points_to_bottom",
        toolTipAttr: "tooltip",
        showEvents: new Array("mouseover"), // list of target events to show tool tip
        hideEvents: new Array("mouseout", "mousedown"), // list of target events to hide tool tip
        minToolTipWidth: 200, // min width of entire tool tip bubble; used if target is smaller than minToolTipWidth and text overflows, otherwise, fits to text or target size
        delayAfterHover: 500, // ms delay from mouse over to showing tool tip; do not show tool tip on negative value
        minArrowMargin: 7, // min margin (left or right) from edge of tool tip bubble for the tool tip arrow
        opacity: 1,
        anchor: "bottom", // top | bottom
        left: null, // where arrow points NOT BOUNDING BOX LEFT
        top: null, // where arrow points NOT BOUNDING BOX TOP
        positionAtCursor: false, // overrides left and top, tooltip at cursor position after timeout
        object: null, // target object OVERRIDE
        message: null, // message to print OVERRIDE
        arrowAlign: "cursor", // OVERRIDE: one of "left", "right", "center", or "cursor",
        fadeInSpeed: 0,
        fadeOutSpeed: 0,
        showLength: null // how long to show before auto hide
    };
    
    $.tooltip.timeoutList = []; // timeoutList[toolTipId] == timeout for toolTipId 
    $.tooltip.autohideTimeoutList = []; // autohideTimeoutList[toolTipId] == autohide timeout for toolTipId 
    
    $.tooltip.cursor = {x:0, y:0};

    $.tooltip.cursor.getPosition = function(e) {
        e = e || window.event;
        var cursor = {x:0, y:0};
        if (e.pageX || e.pageY) {
            cursor.x = e.pageX;
            cursor.y = e.pageY;
        } 
        else {
            var de = document.documentElement;
            var b = document.body;
            cursor.x = e.clientX + 
                (de.scrollLeft || b.scrollLeft) - (de.clientLeft || 0);
            cursor.y = e.clientY + 
                (de.scrollTop || b.scrollTop) - (de.clientTop || 0);
        }
        return cursor;
    }
    
    $(document).ready(function(){
        $("html").mousemove(function(e){
            $.tooltip.cursor.x = $.tooltip.cursor.getPosition(e).x;
            $.tooltip.cursor.y = $.tooltip.cursor.getPosition(e).y;
        });
    });
    
    // attribute used to see if tool tips are enabled
    $.tooltip.attr = "tooltipsEnabled";
    
    // TODO replace show hide with this
    $.tooltip.show = function(options) {
    
        options = $.extend({}, $.tooltip.defaults, options);
    
        if( options.toolTipSpeed < 0 ) {
            return;
        }
    
        clearTimeout( $.tooltip.autohideTimeoutList[options.toolTipId] );
    
        if( $("#" + options.toolTipId).length <= 0 ) {  
            $("body").append("<div id=\"" + options.toolTipId + "\" class=\"" + options.toolTipClass + "\">"
                    + "<div class=\"" + options.toolTipTopArrowId + "\"> <div class='" + options.toolTipPointerClass + "'></div> </div>"
                    + "<div class=\"" + options.toolTipTextId + "\">&nbsp;</div>"
                    + "<div class=\"" + options.toolTipBottomArrowId + "\"> <div class='" + options.toolTipPointerClass + "'></div> </div>"
                    + "</div>");
        }
        
        var toolTip = $("#" + options.toolTipId);       
        var toolTipTop = $("#" + options.toolTipId + " ." + options.toolTipTopArrowId);
        var toolTipBottom = $("#" + options.toolTipId + " ." + options.toolTipBottomArrowId);
        var toolTipText = $("#" + options.toolTipId + " ." + options.toolTipTextId);
        
        // show offscreen so dimensions calculations always work
        if( jQuery.browser.opera ) {
            // opera uses 16 bit int for page dimensions
            // -2^15 <= pageX, pageY <= 2^15
            toolTip.css("left", -30000).css("top", -30000).show(); 
        } else {
            // decent browsers use at least a 32 bit int for page dimensions
            // -2^31 <= pageX, pageY <= 2^31
            toolTip.css("left", -20000000).css("top", -20000000).show();
        }
        
        $.tooltip.timeoutList[options.toolTipId] = setTimeout(function(){
        
            var target = options.object;
            var message = options.message;
            var screenWidth = $("html").width();
            var screenHeight = $("html").height();
            var offsetLeft = 0;
            var width = options.minToolTipWidth;
            var offsetTop = 0;
            var offsetBottom = 0;
            var enabled = true;
            var imgWidth = parseInt( $("." + options.toolTipTopArrowId).css("width") );
            var targetShown = target.is(":visible") && target.css("display") != "none";
            var absMinWidth = imgWidth + options.minArrowMargin * 2;
            var cursorX = $.tooltip.cursor.x;
            var cursorY = $.tooltip.cursor.y;
            var useTargetWidth;
            
            if( target ) {
                offsetLeft = target.offset().left;
                
                if( target.outerWidth() && target.outerWidth() > options.minToolTipWidth ) {
                    width = target.outerWidth();
                    useTargetWidth = true;
                }
                
                offsetTop = target.offset().top + target.outerHeight();
                offsetBottom = target.offset().top;
                message = message || target.attr(options.toolTipAttr);
                enabled = target.tooltipsEnabled();
            }
            
            // fix these or they could throw off the auto width calc
            toolTipTop.css("margin-left", 0);
            toolTipBottom.css("margin-left", 0);
            
            // set text and fix width in case the text is smaller than min
            toolTipText.html( message ); 
            toolTip.css("width", "auto");
            if( toolTip.width() > width ) {
                toolTip.css("width", width);
            } else {
                if( toolTip.width() < absMinWidth ) {
                    toolTip.css("width", absMinWidth);
                }
                
                width = toolTip.width() + 1; // + 1 to fix in case width is a decimal
                toolTip.css("width", width);
                useTargetWidth = false;
                
                offsetLeft = Math.min(cursorX - imgWidth/2 - options.minArrowMargin, target.offset().left + target.width() - width);
                offsetLeft = Math.max(offsetLeft, target.offset().left);
            }
            
            if( message == null || message == "" ){
            	return;
            }
                        
            // override position using options
            if( options.left != 0 && ! options.left ) {
                // value is undefined, not zero
            } else {
                offsetLeft = options.left - imgWidth/2 - options.minArrowMargin;
            }
            
            if( options.top != 0 && ! options.top ) {
                // value is undefined, not zero
            } else {
                offsetTop = options.top;
            }
           
            // correct x bounds
            if(screenWidth < offsetLeft + width) {
                offsetLeft -= offsetLeft + width - screenWidth;
            }
            
            if(options.positionAtCursor) {
                offsetTop = cursorY;
                offsetLeft = cursorX - imgWidth/2 - options.minArrowMargin;
            }
            
            toolTip.css("top",  offsetTop).css("left", offsetLeft);
            
            // do cursor calculations in timeout; otherwise, it's out of date
            var offset = Math.max(cursorX - offsetLeft, 0); // must be > 0
            var left = offset;
            var right = width - offset;
            var minMargin = options.minArrowMargin;
            var height = toolTip.height();
            
            if( options.arrowAlign ) {
                switch( options.arrowAlign.toLowerCase() ){
                case "left":
                    left = 0;
                    break;
                case "right":
                    left = width;
                    break;
                case "center":
                    left = width / 2;
                    break;
                case "cursor":
                    break;
                }
            }
            
            function adjustMargin(arrow) {
                arrow.css( "margin-left", Math.max( Math.min(left - imgWidth/2, width - imgWidth - minMargin), minMargin) );
            }
            
            adjustMargin(toolTipTop);
            adjustMargin(toolTipBottom);
            
            // on small targets, align the arrow with the cursor rather than the lhs of the box
            var cursorArrowDiff = cursorX - toolTipTop.offset().left - imgWidth/2;
            if(options.arrowAlign == "cursor" && ! useTargetWidth && Math.round(cursorArrowDiff) != 0 && target.outerWidth() <= imgWidth + minMargin) {
                toolTip.css("left", offsetLeft- imgWidth/2 - minMargin + target.outerWidth()/2);
            }

            if(options.anchor == "top" || screenHeight < offsetTop + height) {
                toolTip.css( "top", offsetBottom - toolTip.height() );
                toolTipTop.css("visibility", "hidden");
                toolTipBottom.css("visibility", "visible");
                toolTipText.addClass(options.toolTipDirectionDown).removeClass(options.toolTipDirectionUp);
            } else {
                toolTipTop.css("visibility", "visible");
                toolTipBottom.css("visibility", "hidden");
                toolTipText.addClass(options.toolTipDirectionUp).removeClass(options.toolTipDirectionDown);
            }

            if( enabled && (! target || targetShown) ) {
                toolTip.show();
                currentTooltip = toolTip;
                
                if(options.opacity != 1.0) {
                    toolTip.fadeTo(options.fadeInSpeed, options.opacity);
                }
                
                if(options.showLength) {
                    $.tooltip.autohideTimeoutList[options.toolTipId] = setTimeout(function(){
                        $.tooltip.hide(options);
                    }, options.showLength);
                }
            }
        }, options.delayAfterHover);
    };
    
    $.tooltip.hide = function(options) {
        options = $.extend({}, $.tooltip.defaults, options);

        clearTimeout( $.tooltip.timeoutList[options.toolTipId] );
        clearTimeout( $.tooltip.autohideTimeoutList[options.toolTipId] );
    
        $("#" + options.toolTipId).fadeOut(options.fadeOutSpeed);
        currentTooltip = null;
    };
    
    $.fn.disableTooltips = function() {
        return this.each(function() {
            $(this).attr($.tooltip.attr, "false");
        });
    };
    
    $.fn.enableTooltips = function() {
        return this.each(function() {
            $(this).attr($.tooltip.attr, "true");
        });
    };
    
    // tool tip enabling is recursive (e.g. if parent has tool tips enabled, then the
    // children do)
    
    // HOWEVER, children can override parents
    
    // body[enabled] > #foo > #bar[disabled]
    // here #bar is disabled but #foo is enabled by body  
    
    $.fn.tooltipsEnabled = function() {
        if( "true" == $(this).attr($.tooltip.attr) ) {
            return true; // true if self true
        } else if ( "true" == $(this).closest("[" + $.tooltip.attr + "]").attr($.tooltip.attr) ) {
            return true; // true if nearest set parent true
        } else {
            return false;
        }
    };
    
    $.fn.hideTooltip = function(options) {
	    options = $.extend({}, $.tooltip.defaults, options);
	    options.object = $(this);
	    if ( options.toolTipId == visibleTooltips[$(this).attr("id")] ) {
	        $.tooltip.hide(options);
	        visibleTooltips[$(this).attr("id")] = null;
        }
    };
    
    $.fn.showTooltip = function(options) {
        options = $.extend({}, $.tooltip.defaults, options);
        visibleTooltips[$(this).attr("id")] = options.toolTipId;
        options.object = $(this);
        $.tooltip.show(options);
    };
    
    $.fn.tooltip = function(options) {  
        
        options = $.extend({}, $.tooltip.defaults, options);
        
        return this.each(function() { 
            $(this).enableTooltips();
            	
        	for(i in options.showEvents) {
        		var event = options.showEvents[i];
        		
        		$(this).find("[" + options.toolTipAttr + "]").live(event, function(){
        			// If the same HTML element already has a displayed tooltip, does not show another one: 
                    if (visibleTooltips[$(this).attr("id")] == null) {
                    	$(this).showTooltip(options);
                    }
        		});
        	}
        	
        	for(i in options.hideEvents) {
        		var event = options.hideEvents[i];
        		
        		$(this).find("[" + options.toolTipAttr + "]").live(event, function(){
        			$(this).hideTooltip(options);
        		});
        	}

        });  
    };  
})(jQuery);  