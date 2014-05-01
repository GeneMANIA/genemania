var _layout;

$(function(){    

	if( $("#results_page").size() == 0 ){
		return; // ignore this js file if on front page
	}

	$("#relatedGenes").bind("mouseenter", function(){
		$(this).addClass("active");
	}).bind("mouseleave", function(){
		$(this).removeClass("active", ANI_SPD);
	});
	
    /**********************************************
    side bar layout
    **********************************************/
    
    var MIN_GRAPH_WIDTH = parseInt( $("#cytoscape_lite").css("min-width") );
    var MIN_SIDE_BAR_WIDTH = parseInt( $("#side_bar").css("min-width") );
    var SPACING = 12;
    
    var resizing = false;
    var layout = $("body").layout({
        name: "defaultLayout",
        
        defaults: {
            size: "auto",
            resizable: false,
            fxName: "hide",
            fxSpeed: 0,
            contentIgnoreSelector: ".qtip"
        },
        
        north: {
            paneSelector: "#header"
        },
        
        center: {
            paneSelector: "#cytoscape_lite"
        },
        
        east: {
            size: MIN_SIDE_BAR_WIDTH,
            minSize: MIN_SIDE_BAR_WIDTH,
            maxSize: get_max_side_size(),
            paneSelector: "#side_bar",
            resizable: true,
            resizerTip: "",
            spacing_open: SPACING,
            onresize_start: function(){
        		track("Sidebar", "Resize");
            },
            onresize_end: function(){
                $("#overlay").remove();
            }
        },
        
        south: {
            paneSelector: "#footer",
            closable: false
        }
    });
    _layout = layout;

    
    $("[resizer=east]").bind("mousedown", function(){
    	$("body").append('<div id="overlay"></div>');
    }).bind("mouseup", function(){
    	$("#overlay").remove();
    });
    
    $("#overlay").live("mouseup mousedown click", function(){
    	$("#overlay").remove();
    });
    
    
    function get_max_side_size(){
        var max = $(window).width() - MIN_GRAPH_WIDTH - 4*SPACING;
        var min = (layout == null ? null : layout.options.east.minSize);
        
        if( min != null && max < min ){
        	return min;
        }
        
        return max;
    }
    
    $(window).bind("resize", function(event){
		layout.options.east.maxSize = get_max_side_size();
        
        // workaround for small window size hides sidebar
        if( !$("#side_bar").is(":visible") && !$("#side_bar_toggle_closed").is(":visible") ){
        	layout.show("east");
        }
        
        // fix resizer location if the layout plugin loses it
        $("[resizer=east]").each(function(){
        	var right = parseInt( $(this).offset.right );
        	var should_be_right = $("#side_bar").width()
        	
        	if( right != should_be_right ){
        		$(this).css( "right",  should_be_right );
        	}
        });
    });
    
    // overlap fixes
    // plugin sets all to index 2
    $("#header").css("z-index", 4);
    $("#cytoscape_lite").css("z-index", 3);
    $("#side_bar").css("z-index", 2);
    $("#footer").css("z-index", 3);
    
    
    
    // tabs
    $(".tabs").tabs({
        show: function(){
            $(window).trigger("resize"); // trigger resize for tab contents fitting
        }
    });
    
    $("#side_bar .ui-tabs-nav a").bind("click", function(){
    	var name = $(this).text() + " tab";
    	track(name, "Open");
    });
    
    $(window).bind("load", function(){
    	$(window).trigger("resize");
    });
    
    
    /**********************************************
    side bar toggle open, closed
    **********************************************/
    
    $("#side_bar_toggle_open").click(function(){
        layout.hide("east");
        $("#side_bar_toggle_closed").show();
        $(this).mouseout(); // fixes bug on older browsers where mouseout isn't sent after animation
        track("Sidebar", "Close");
    });
    
    $("#side_bar_toggle_closed").click(function(){
        $(this).hide();
        layout.show("east");
        $(this).mouseout(); // fixes bug on older browsers where mouseout isn't sent after animation
        track("Sidebar", "Open");
    }).hide();
    
    /**********************************************
    fix sizes of side bar contents on window resize
    **********************************************/
    
    // resize tab contents on contents resize
    $(window).bind("resize", function(){
        
       fix();
       setTimeout(function(){
            fix();
       }, 100); // 100 ms is empirically good 
       
        function fix(){
           $(".tabs").add(".menu").each(function(){
                var totalHeight = $(this).innerHeight();
                var tabHeaderHeight = $(this).find("ul:first").outerHeight({margin: true});
                
                $(this).find(".tab, .menu_area").each(function(){
                    var headerHeight = 0;
                    
                    $(this).children(".header").each(function(){
                    	if ($(this).css("display") != "none") {
                    		headerHeight += $(this).outerHeight({margin: true});
                    	}
                    });
                     
                    $(this).find(".content").height( totalHeight - ( headerHeight + tabHeaderHeight ) );
                });
            });
            
            $("#side_bar").each(function(){
                var max = $("html").width() - parseInt( $("#cytoscape_lite").css("min-width") ) - 50;
                var min = $("#side_bar").resizable("option", "minWidth");
                
                if( max <  min) {
                    max = min;
                }
                
                $(this).resizable("option", "maxWidth", max); // - 50 for unknown spacing for resizer
                if( $(this).width() > max ) {
                    $(this).width( max );
                }
            });
        }   
       
    }).resize(); // fire once for initial size
    
    /**********************************************
    loader
    **********************************************/
    
    // hide loader when both cytolite and page loaded
    
    var cytowebLoaded = false;
    var cytowebError = false;
    var pageLoaded = false;
    
    $(window).load(function(){
        pageLoaded = true;
        checkLoader();
    });
    
    onCytoscapeWebLoaded = function(error){
    	cytowebLoaded = true;
    	cytowebError = error;
    	var done = checkLoader();
    	if (done) {
    		// Trigger resize to recalculate the tab contents height:
    		$(window).resize();
    	}
    }
    
    function checkLoader() {
    	if( cytowebLoaded && pageLoaded ) {
            if (cytowebError) {
            	$("#cytoscape_lite .ui-tabs-nav").hide();
            	$("#networks_tab .content .checkbox").hide();
            	$("#networks_filter_menu").hide();
            	$("#genes_tab .label").unbind("click").unbind("mouseover");
            	$("#side_bar .ui-tabs-nav li").eq(2).hide();
            	$("#cytoscape_lite .ui-menu-nav").hide();
            }
            return true;
    	}
    	return false;
    }
      
    CytowebUtil.refresh();
    
    progress("layout");
});
