CytowebUtil.goColor = {};
CytowebUtil.goEdgesColor = {};
CytowebUtil.goHighlight = {};
CytowebUtil.goEdgesHighlight = {};
CytowebUtil.goHighlightConnectedNodes = {};

CytowebUtil.updateGOColoursForGenes = function(){
	
    var cache = {};

    $("#genes_widget .go_list .go").add("body > .qtip .go_list .go").each(function(){
        var colour = $(this).find(".colour");
        var ocid = $(this).attr("ocid");
        var hasGo = $(this).hasClass("has");
        
        if( !hasGo ){
        	return; // skip go entries that the gene doesn't have
        }
        
        if( cache[ocid] == undefined ){
            var colouring = $("#go_tab .colouring[ocid=" + ocid + "]");
        
            if( colouring.size() > 0 ){
                var c = colouring.attr("colour");
            
                colour.css({
                    backgroundColor: c,
                    borderColor: c
                }).addClass("coloured");
                
                cache[ocid] = c;
                
            } else {
                colour.css({
                    backgroundColor: "",
                    borderColor: ""
                }).removeClass("coloured");
                
                cache[ocid] = false;
            }
        } else if( cache[ocid] ) {
            var c = cache[ocid];
            
            colour.css({
                backgroundColor: c,
                borderColor: c
            }).addClass("coloured");
            
        } else {
            colour.css({
                backgroundColor: "",
                borderColor: ""
            }).removeClass("coloured");
        }
        
    });
};

CytowebUtil.updateGO = function(draw){
    if(_vis) {
        var nodes = CytowebUtil.nodes();
        var edges = CytowebUtil.edges();
        CytowebUtil.goColor = {};
        CytowebUtil.goHighlight = {};
        CytowebUtil.goEdgesColor = {};
        CytowebUtil.goEdgesHighlight = {};
        
        if( draw == undefined ){
            draw = true;
        }
        
        $("#networks_tab .checktree_attr .bar").css("background-color", "");
        
        $("#go_tab .colouring").reverse().each(function(){
            var colouring = $(this);
            var colouring_ocid = $(this).find(".annotation").attr("ocid");
            var is_query_genes = $(this).hasClass("query");
            var tr = $("#go_tab tr[ocid="+ colouring_ocid +"]");
            var attr_id = parseInt( tr.attr("attrid") );
            var is_attr = !isNaN(attr_id);
            
            var colour = colouring.attr("colour");
            
            $.each(nodes, function(i, node) {
                var ocids = node.data.ocids;
                
                if( is_query_genes ){
                    if( node.data.queryGene == true || node.data.queryGene == "true" ){
                    	CytowebUtil.goColor[node.data.id] = colour;
                    }
                } else if( is_attr ){
                	if( node.data.attributeId == attr_id ){
                		CytowebUtil.goColor[node.data.id] = colour;
                		$("#networks_tab .checktree_attr[attrid="+ attr_id +"] .bar").css("background-color", colour);
                	}
                } else {
                    $.each(ocids, function(j, ocid){
                        if( ocid == "" || ocid != colouring_ocid ){
                            return;
                        }
                    
                        CytowebUtil.goColor[node.data.id] = colour;
                    });
                }
            });
            
            $.each(edges, function(i, edge){
            	if( is_attr ){
            		if( edge.data.attributeId == attr_id ){
            			CytowebUtil.goEdgesColor[edge.data.id] = colour;
            		}
            	}
            });
        });
        
        if( draw ){
		    CytowebUtil.updateBypass();
		}
	}
};

CytowebUtil._highlight = false;

CytowebUtil._highlightGO = function(go_id, is_query_genes){
    if(_vis) {
        
        var colouring = $("#go_tab .colouring[ocid=" + go_id + "]");
        var tr = $("#go_tab tr[ocid="+ go_id +"]");
        var colour;
        var attr_id = parseInt( tr.attr("attrid") );
        var is_attr = attr_id != null && !isNaN(attr_id);
        
        is_query_genes = ( is_query_genes ? true : false );
        CytowebUtil.goHighlight = {};
        
        if( is_query_genes ){
            colour = $("#go_tab").attr("querycolour");
        } else if( colouring.size() > 0 ){
            colour = colouring.attr("colour");
        } else {
            colour = $("#go_tab").attr("colour");
        }
        
        var nodes = CytowebUtil.nodes();
        var node_ids = [];
        for(var i in nodes){
            var node = nodes[i];
            
            var go_ids = node.data.ocids;
            var query = ( node.data.queryGene == "true" || node.data.queryGene == true );
            
            CytowebUtil.goHighlight[node.data.id] = false;
            
            if( is_query_genes ){
                if( query ){
                	CytowebUtil.goHighlight[node.data.id] = true;
            		CytowebUtil.goColor[node.data.id] = colour;	
                }
            } else if( is_attr ){
            	if( node.data.attributeId == attr_id ){
            		CytowebUtil.goHighlight[node.data.id] = true;
            		CytowebUtil.goColor[node.data.id] = colour;
            	}
            	
            	if( $.inArray(attr_id, node.data.attrids) >= 0 ){
            		CytowebUtil.goHighlight[node.data.id] = true;
            	}
            } else {
                for(var j in go_ids){
                    var id = go_ids[j];
                    
                    if( id == go_id ){
                    	CytowebUtil.goColor[node.data.id] = colour;
                    	CytowebUtil.goHighlight[node.data.id] = true;
                        break;
                    }
                }
            }
            
            if( CytowebUtil.goHighlight[node.data.id] ){
            	node_ids.push( node.data.id );
            }
        }
        
        var edges = CytowebUtil.edges();
    	$.each(edges, function(i, e){
    		if( is_attr ){
	    		if( e.data.attributeId == attr_id ){
	    			CytowebUtil.goEdgesColor[e.data.id] = colour;
	    			CytowebUtil.goEdgesHighlight[e.data.id] = true;
	    		} else {
	    			CytowebUtil.goEdgesHighlight[e.data.id] = false;
	    		}
    		} else {
    			var srcId = e.data.source;
    			var tgtId = e.data.target;
    			CytowebUtil.goEdgesHighlight[e.data.id] = CytowebUtil.goHighlight[srcId] && CytowebUtil.goHighlight[tgtId];
    		}
    	});
        
        CytowebUtil.updateBypass();
    }
};

CytowebUtil.highlightGO = function(go_id, is_query_genes){
    if( CytowebUtil._highlight ){
        CytowebUtil.updateGO(false);
    }
    
    CytowebUtil._highlight = true;
    
    CytowebUtil._highlightGO(go_id, is_query_genes);
};

CytowebUtil.unhighlightGO = function(go_id, is_query_genes){
	CytowebUtil._highlight = false;
    CytowebUtil.updateGO();
};