
CytowebUtil._addListeners = function() {
    
    var node_hover_timeout;

    function handle_click(evt){
        if(!CytowebUtil._shift_down){
        	
        	if( evt.target.group == "edges" && evt.target.data.attributeId != null ){
        		evt.target = _vis.node(evt.target.data.source);
    			evt.group = "nodes";
    			
    			_vis.deselect();
    			setTimeout(function(){
    				_vis.select([ evt.target.data.id ]);
    			}, 10);
    			
        	}
        	
            CytowebUtil._showTooltip(evt);
        }
    }

    _vis.addListener("select", "nodes", function(evt) {
    	setTimeout(function(){
    		var nodesArray = evt.target;
	    	var already_selected = false;
	    	$.each(nodesArray, function(i, node) {
	    		if (!$("#gene"+node.data.id + " .label").hasClass("selected")) {
	    			// hilight node
	    			$("#gene"+node.data.id + " .label").addClass("selected");
	    			already_selected = true;
	    			
	    	    	// Expand the gene row content:
	    	    	if(false && $("#gene"+node.data.id + " .arrow").hasClass("collapsed")) {
	    				$("#gene"+node.data.id + " .arrow").click();
	    			}
	    		}
	    	});
	    	// scroll to only first gene
	    	if (false && already_selected) {
				var first_node = $("#genes_widget").find(".label.selected").parent();
				$("#genes_tab .content").scrollTo($(first_node), CytowebUtil.SCROLL_DELAY);
	    	}
	    	CytowebUtil._onSelectNodesChanged();
    	}, 100);
    	
    })
    .addListener("deselect", "nodes", function(evt) {
    	setTimeout(function(){
	    	var nodesArray = evt.target;
	    	$.each(nodesArray, function(i, node) {
	    		$("#gene"+node.data.id + " .label").removeClass("selected");
	    	});
	    	CytowebUtil._onSelectNodesChanged();
    	}, 100);
    })
    .addListener("select", "edges", function(evt) {
    	setTimeout(function(){
    		CytowebUtil._onSelectEdgesChanged();
    	}, 100);
    })
    .addListener("deselect", "edges", function(evt) {
    	setTimeout(function(){
    		CytowebUtil._onSelectEdgesChanged();
    	}, 100);
    })
    .addListener("click", "nodes", function(evt) {
        handle_click(evt);
        track("Node", "Click");
    })
    .addListener("dblclick", "nodes", function(evt) {
        handle_click(evt);
    })
    .addListener("click", "edges", function(evt) {
        handle_click(evt);
        track("Edge", "Click");
    })
    .addListener("dblclick", "edges", function(evt) {
        handle_click(evt);
    })
    .addListener("layout", function(evt) {
    	
    })
	.addListener("error", function(error) {
		//console.log('Error drawing network: ' + evt.value.msg);
		CytowebUtil.error(error.value);
	})
	.addListener("click", function(evt) {
    	CytowebUtil.body_click();
    });
};