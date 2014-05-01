$(function(){

	if( $("#results_page").size() == 0 ){
		return; // ignore this js file if on front page
	}

	$.menu_popup = {};
	
	// Store the created panels:
	$.menu_popup.panels = { /* menu_obj -> panel_id */ }
    
    $.menu_popup.defaults = {
        hiddenToolTip: "Use the toggle button above to restore the panel when hidden",
        title: "",
        content: "",
        resizable: false,
        minHeight: 80,
        minWidth: 120,
        maxHeight: 520,
        maxWidth: 400,
        onResize: undefined, // function
        onStartResize: undefined, // function
        onEndResize: undefined // function
    };
	
	$.fn.menuPopup = function(id, options) {
		options = $.extend({}, $.menu_popup.defaults, options);
		var menu_item = $(this);
		var popup = $("#"+id);

		if (popup.length === 0) {
			popup = $('<div id="'+id+'" class="menu_popup outline over_flash">' +
					      '<div class="ui-widget-header title ui-corner-top">' +
					          '<div class="floating normal_width button close ui-state-default ui-corner-all"><a href="#"><span class="ui-icon ui-icon-close"></span></a></div>' +
					      '<label>'+options.title+'</label></div>' +
					      '<div class="content"></div>' +
					  '</div>');
			
			$("body").append(popup);
			
			popup.find(".content").append(options.content);
			
			popup.find(".close a").click(function(){
	        	menu_item.click();
	        	return false;
	        });
			
			popup.draggable({
//		        handle: '.title',
		        cursor: 'auto',
		        scroll: false,
		        //containment: 'window'
		        stop: function(){

		            var x1 = popup.offset().left;
		            var x2 = x1 + popup.width();
		            var y1 = popup.offset().top;
		            var y2 = y1 + popup.height();
		            
		            var maxX = $("html").width();
		            var maxY = $("html").height();
		            
		            if(0 < x1 && x2 < maxX && 0 < y1 && y2 < maxY) {
		                // in bounds
		                $.tooltip.hide(toolTipOptions);
		            } else {
		                // out of bounds
		                $.tooltip.show($.extend({
		                    message: options.hiddenToolTip,
		                    object: menu_item,
		                    arrowAlign: "left",
		                    positionAtCursor: false,
		                    delayAfterHover: 200,
		                    showLength: 10000
		                }, toolTipOptions));
		            }
		        }
		    });
		    
			popup.bind("mouseover", function(){
				$(this).css("z-index", 99999);
			}).bind("mouseout", function(){
				$(this).css("z-index", "");
			});
			
			if (options.resizable) {
				popup.resizable({ 
					mimWidth: options.minWidth, 
					minHeight: options.minHeight,
					maxWidth: options.maxWidth, 
					maxHeight: options.maxHeight,
					start: function(evt, ui) {
					    if (options.onStartResize) {
					    	options.onStartResize(evt, ui);
					    }
				    },
					resize: function(evt, ui) {
				    	var content = $(this).find(".content");
				    	var h = $(this).height() - $(this).find(".ui-widget-header").height();
				    	content.css("height", h);
				    	
					    if (options.onResize) {
					    	options.onResize(evt, ui);
					    }
				    },
					end: function(evt, ui) {
					    if (options.onEndResize) {
					    	options.onEndResize(evt, ui);
					    }
				    }
               });
			}
		}
		
		popup.show().each(function() {
			$(this).css( "top", menu_item.offset().top + menu_item.outerHeight() );
			$(this).css( "left", menu_item.offset().left );
		})
	};
    
});