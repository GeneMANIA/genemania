CytowebUtil._tooltip_timeout = null;

CytowebUtil._showTooltip = function(evt){
    $("#menu_close_tooltips").removeClass("ui-state-disabled");
    CytowebUtil._makeTooltip(evt);
};

CytowebUtil._makeTooltip = function(evt){
	//console.log("making tooltip");
	
    var outer_padding = 4;

    function sanitise(id){
    	
    	// replace : with \\: for jquery
    	
    	return id.replace(/\:/g, "-");
    	
    }
    
    var merged = evt.target.merged;
    
//    console.log(evt);
    var id;
    
    var prev_qtip;
    if( evt.group == "nodes" ){
    	id = sanitise(evt.target.data.id);
        prev_qtip = $(".qtip[nodeid=" + id + "]:visible");
    } else if( !merged ){  
        id = sanitise(evt.target.data.id);
    	prev_qtip = $(".qtip[edgeid=" + id + "]:visible");
    } else {
    	id = sanitise( evt.target.data.source[0] + "-" + evt.target.data.target[0] );
    	prev_qtip = $(".qtip[medgeid=" + id + "]:visible");
    }
    
     
    
    if( prev_qtip.size() > 0 ){
    	//console.log("show existing previous tooltip");
    	
        var title = prev_qtip.find(".qtip-title");
        var content = prev_qtip.find(".qtip-content");
        
        function flash(ele){
            var bg = ele.css("background-color");
            
            if( !ele.hasClass("highlighting") ){
            
                ele.addClass("highlighting");
            
                ele.animate({
                    backgroundColor: "#ffff88"
                }, ANI_SPD, function(){
                
                    setTimeout(function(){
                        ele.animate({
                            backgroundColor: bg
                        }, ANI_SPD, function(){
                            ele.removeClass("highlighting");
                        });
                    }, 1000);
                    
                });
            
            }
        }
        
        flash(title);
    
        return;
    }

    var source;
    var target;
    
    
    
    var x;
    var zoom = _vis.zoom();
    if( evt.group == "nodes" ){
        x = evt.target.x + $("#graph").offset().left - evt.target.size*zoom/2;
    } else {
    	source = _vis.node(evt.target.data.source);
        target = _vis.node(evt.target.data.target);
        
        
        x = evt.mouseX + $("#graph").offset().left;
    }

    
    var y;
    if( evt.group == "nodes" ){
        y = evt.target.y + $("#graph").offset().top;
    } else {
        y = evt.mouseY + $("#graph").offset().top;;
    }
    
    // The tooltip anchor:
    var maxH = 220;
    var maxW = 315;
    var screenHeight = $("html").height();
    var screenWidth = $("html").width();
    var top = (maxH + y + 2.2*outer_padding <= screenHeight);
    var left = (x <= maxW + 2.2*outer_padding);
    
    var position;
    
    if( left ){
        position = "left";
        
        if( evt.group == "nodes" ){
            x += evt.target.size*zoom;
        }
    } else {
        position = "right";
    }
    
    if( top ){
        position += "Top";
    } else {
        position += "Bottom";
    }
    
    var text = "";
    var title;

    if( evt.group == "nodes" ){
    	//console.log("making content for node");
    	
        title =  evt.target.data.symbol;
        
        if( evt.target.data.attribute ){
        	text = $("#networks_tab").find("li[attrid=" + evt.target.data.attributeId + "] .text:first").html();
        } else {
	        var score = "" + $("#genes_tab").find("li[id=gene" + evt.target.data.id + "]").not(".source_true").find(".score_text:first").text();
	        
	        if( score != "" ){
	            title += ' (rank: ' + score + ')';
	        }
	        
	        text = $("#genes_tab").find("li[id=gene" + evt.target.data.id + "] .text:first").html();
        }
    } else {
//    	console.log("making content for edge");
    	
        title = source.data.symbol + ' - ' + target.data.symbol;
  
        var edges = [];
        
        if( merged ){
            edges = evt.target.edges;
        } else {
            edges.push( evt.target );
        }
        //console.log("data for tooltip included edges:");

        for(var i in edges){
            var edge = edges[i];
            text += CytowebUtil._getLinkInfo(edge);
        }
        

    }
    
    // remove old ones
    $("body").children(".qtip").not(".ripped_out").remove();
    $("body").children(".qtip").not(":visible").remove();
     
    // Create the tooltip:
    $("body").qtip({
        content: {
            text: text,
            title: {
                text: '<span class="qtip-title-text">' + title + '</span> <div class="ui-state-default ui-corner-all minimise"> <span class="ui-icon"></span> </div> ',
                button: '<div class="ui-state-default ui-corner-all"> <span class="ui-icon ui-icon-close"></span> </div>'
            }
        },
        position: {
            target: false,
            type: "absolute",
            corner: {
                tooltip: position,
                target: "leftTop"
            },
            adjust: {
                mouse: false,
                x: x,
                y: y,
                scroll: false,
                resize: false
            }
        },
        show: {
            delay: 0,
            when: false,
            effect: { type: "fade", length: 0 },
            ready: true // Show the tooltip when ready
        },
        hide: {
            delay: 0,
            effect: { type: "fade", length: 0 },
            when: { event: "unfocus" }, // Hide when clicking anywhere else
            fixed: true // Make it fixed so it can be hovered over
        },
        style: {
           border: { width: 1, radius: 8 },
           width: {
                min: ( evt.group == "nodes" ? 0 : maxW ),
                max: ( maxW )
           },
           screen: true,
           padding: outer_padding, 
           textAlign: 'left',
           name: 'light', // Style it according to the preset 'cream' style,
           tip: true      // Give it a speech bubble tip with automatic corner detection
        }
    });
    
    $("body").children(".qtip:last").each(function(){
        var qtip = $(this);
        
        qtip.find(".qtip-button").bind("mousedown", function(){
            qtip.qtip("api").beforeHide = function(){

            }
        });
        
        function update_qtip_position(){
            if( !qtip.hasClass("ripped_out") ){
                qtip.qtip("api").updatePosition();
            }
        }
        
        function make_show_more(syn){
            var variance = 20; // px diff before hide
            var show_msg = "&#9658; Show more";
            var hide_msg = "&#9650; Show less";
        
            var syn_link = $('<a href="#" class="action_link">' + show_msg + '</a>');
            var old_syn_height = syn.height();
        
            syn.addClass("short");
                    
            if( old_syn_height - syn.height() > variance ){
                syn.after(syn_link);
                
                syn_link.click(function(){
                    if( syn.hasClass("short") ){
                        syn_link.html(hide_msg);
                    } else {
                        syn_link.html(show_msg);
                    }
                    syn.toggleClass("short");
                    update_qtip_position();
                    return false;
                });
            } else {
                syn.removeClass("short");
            }
        }
        
        if( evt.group == "nodes" ){
            var syn = qtip.find(".synonyms");
            make_show_more(syn);
            
            var descr = qtip.find(".description");
            make_show_more(descr);
            
//            var go_list = qtip.find(".go_list");
//            make_show_more(go_list);
            
            qtip.attr("nodeid", id);
        } else {
            qtip.find(".network").addClass("short").each(function(){
                var net = $(this);
                    
                net.find(".label").bind("click", function(){
                    net.toggleClass("short");
                    update_qtip_position();
                }).bind("mouseover", function(){
                    net.addClass("hover");
                }).bind("mouseout", function(){
                    net.removeClass("hover");
                });
                
                net.find(".per_cent_bar").bind("click", function(){
                    net.toggleClass("short");
                    update_qtip_position();
                }).bind("mouseover", function(){
                    net.addClass("hover");
                }).bind("mouseout", function(){
                    net.removeClass("hover");
                });
                
            });
            
            if( merged ){
            	qtip.attr("medgeid", id);
            } else {
            	qtip.attr("edgeid", id);
            }
            
        }
        
        qtip.find(".minimise").each(function(){
            var open_class = "ui-icon-arrowthickstop-1-n";
            var close_class = "ui-icon-arrowthickstop-1-s";
            var icon = $(this).find(".ui-icon");
            
            icon.addClass(open_class);
            $(this).addClass("ui-state-disabled");
        
            $(this).bind("click", function(){
                
                if( $(this).hasClass("ui-state-disabled") ){
                	return false;
                }
                
                if( icon.hasClass(open_class) ){
                    icon.removeClass(open_class);
                    icon.addClass(close_class);
                    qtip.find(".qtip-content").addClass("collapsed_min");
                } else {
                    icon.removeClass(close_class);
                    icon.addClass(open_class);
                    qtip.find(".qtip-content").removeClass("collapsed_min");
                }
            
                return false;
            }); 
        });
        
        function update_menu(){
            if( $(".qtip:visible").size() > 1 ){
                $("#menu_close_tooltips").removeClass("ui-state-disabled");
            } else {
                $("#menu_close_tooltips").addClass("ui-state-disabled");
            }
        }
        
        qtip.find(".qtip-button").bind("mousedown", function(){
            update_menu();
            removeHighlight();
        });
        
        qtip.qtip("api").beforeHide = function(){
            update_menu();
        }
        
        qtip.draggable({
            containment: "window",
            handle: ".qtip-title",
            cursor: "move",
            start: function(){
                qtip.addClass("ripped_out");
                qtip.find(".qtip-tip").hide();
                qtip.find(".qtip-title .minimise").removeClass("ui-state-disabled");
                
                qtip.qtip("api").beforeHide = function(){
                    if( qtip.hasClass("ripped_out") ){
                        return false;
                    }
                }
                
                qtip.qtip("api").beforePositionUpdate = function(){
                    if( qtip.hasClass("ripped_out") ){
                        return false;
                    }
                }
                
                track("Gene/edge tooltip", "Drag");
            }
        });
        
        function removeHighlight(){
        	qtip.removeClass("mouseover");
        	
        	if( qtip.hasClass("ui-draggable-dragging") ){
        		return; // don't remove highlight while moving qtips
        	}
        	
        	clearTimeout(CytowebUtil._qtipMouseoverTimeout);
        	CytowebUtil._qtipMouseoverTimeout = setTimeout(function(){
    			CytowebUtil.unhighlightGenesAndEdges();
        	}, 100);
        }
        
        function highlight(){
        	if( qtip.hasClass("ui-draggable-dragging") ){
        		return; // don't highlight while moving qtips
        	}
        	
        	qtip.addClass("mouseover");
        	clearTimeout(CytowebUtil._qtipMouseoverTimeout);
        	
        	var id = evt.target.data.id;
        	
        	CytowebUtil._qtipMouseoverTimeout = setTimeout(function(){
        		if( evt.group == "nodes" ){
            		CytowebUtil.highlightGene( id );
            	} else {
            		CytowebUtil.highlightEdge( id );
            	}
        	}, 500);
        	
        }
        
        qtip.find(".qtip-content").bind("mouseenter", function(){
        	highlight();
        });
        
        qtip.bind("mouseleave", function(){
        	removeHighlight();
        });
        
        qtip.qtip("api").updatePosition();
    });
    
};

CytowebUtil._qtipMouseoverTimeout = null;

CytowebUtil._getLinkInfo = function(e){
	//console.log("getting edge info");
	
    var info = '<ul class="network_list tooltip">';
    var c = e.color;
    var w = CytowebUtil._round_weight(e.data['weight']);
    
    var grId = e.data.networkGroupId;
    var grName = $("#networks_widget .checktree_network_group[netid="+grId+"] .name:first").text();
    
    info += '<li class="network_group"><div class="label">' +
            '<div class="per_cent_text"><span>Weight</span></div>' +
            '<div class="network_name">' + grName +'</div>' +
            '</div>' +
            //'<div class="per_cent_bar"> <div class="bar" style="background-color:'+c+';width:'+w+'%">&nbsp;</div> </div>' +
            '<ul style="display: block;">';
        
        var nn = e.data.networkIdToWeight;
        var weight_to_id = {};
        var weights = [];
        
        $.each(nn, function(id, w) {
            if( !weight_to_id[w] ){
            	weight_to_id[w] = [];
            }
            
			weight_to_id[w].push(id);
			weights.push( w );
        });
 
        weights.sort();
        
        for(var i = weights.length - 1; i >= 0; i--){
        	var weight = weights[i];
        	var ids = weight_to_id[weight];
        	
        	for(var j in ids){
        		var id = ids[j];
					var weight_rounded = CytowebUtil._round_weight( weight );
					var network = $("#networks_widget .checktree_network[netid="+id+"]");
					var n =  network.find(".name").text();
					var desc = network.find(".text:first").html();
				
				info += '<li class="network">' +
					'<div class="label">' +
						'<div class="per_cent_text"><span tooltip="Network weight">' + weight_rounded + '</span></div>' + 
						'<div class="network_name">' + n +'</div>' +
					'</div>' +
					'<div class="per_cent_bar"> <div class="bar" style="background-color:'+c+';width:'+weight_rounded+'%">&nbsp;</div> </div>' +
					'<div class="description">' + desc + '</div>' +
				'</li>';
    	}
    }
    				
    
    info += '</ul></li></ul>';
    
    return info;
};
