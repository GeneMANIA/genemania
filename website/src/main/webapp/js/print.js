$(function(){
	
	if( $("#print_page").size() <= 0 ){
		return;
	}
	
	if( $.browser.msie ){
		
		var timeout = undefined;
		var callback = function(){
		
			if( $(".graph_area object").size() == 0 ){
				setTimeout(callback, 10);
			} else {
				var svg = $(".graph_area object");
				var parent = $(".graph_area");
				
				var height = parent.height();
				var width = parent.height() / svg.height() * svg.width();
				
				svg.height(height);
				svg.width(width);
				svg.css({
					position: "relative",
					left: "50%",
					marginLeft: (-1/2 * width) + "px"
				});
			
				
			}
			
		};
		callback();
		
	}
	
	$("#transferred_go_legend .colouring").each(function(){
		var name = $(this).find(".annotation").text();
		var ocid = $(this).attr("ocid");
		var colour = $(this).attr("colour");
		
		var entry = $('<div class="entry" ocid="' + ocid + '" colour="' + colour + '">'+
					'<span class="colour"></span>' +
					'<span class="name">' + name + '</span>' +
				'</div>');
		
		$(".go.legend").append(entry);
		entry.find(".colour").css("border-color", colour);
	});
	
	$("#go_table th").unbind("click");
	
	$("#genes_widget .go .colour").html('');
	
	
	
	$("#genes_widget .go_list").each(function(){
		$(this).find(".colour").each(function(i){
			if( $(this).parent().hasClass("has") ){
				$(this).append('<span class="number">'+ (i+1) +'</span>');
			}
		});
	});
	
	$("#go_table td.annotation").each(function(i){
		$(this).prepend('<span class="colour"><span class="number"></span></span>');
	});
	
	$(".legend.go .entry").each(function(){
		var id = $(this).attr("ocid");
		var colour = $(this).attr("colour");
		
		$("#genes_widget .go[ocid=" + id + "].has .colour").css({
			"border-color": colour
		});
		
		$("#go_table .annotation[ocid=" + id + "] .colour").css({
			"border-color":  colour
		});
		
	});
	
	$(".print_selection .button").bind("click", function(){		
		window.print();
	});
	
	$(".print_selection input").bind("click", function(){
		
		var section = $(this).attr("section");
		var checked = $(this).is(":checked");
		
		if( checked ){
			$(".section[section=" + section + "]")
				.removeClass("do_not_print")
				.find("h2 input").attr("checked", "checked");
		} else {
			$(".section[section=" + section + "]")
				.addClass("do_not_print")
				.find("h2 input").removeAttr("checked");
		}
		
		adjust_breaks();
		
	});
	
	$("h2").append('<span class="print button"><label>Remove from print</label></span>');
	
	function adjust_breaks(){
		$("h2:visible:first").removeClass("break");
		$("h2:visible").not(":first").addClass("break");
	}
	adjust_breaks();
	
	$("h2 .print").bind("click", function(){
		var section = $(this).parents(".section").attr("section");
		
		$(".section[section=" + section + "]").addClass("do_not_print");
		
		$(".print_selection input[section=" + section + "]").removeAttr("checked");
	});
	
	$("h2").append('<span class="save button"><label>Save</label></span>');
	$(".print_selection .item").each(function(){
		var section = $(this).find("[section]").attr("section");
		
		$(this).prepend('<span class="save button" section="' + section + '"><label>Save</label></span>');
		$(this).find(".save").bind("click", function(){
			var export_fn = "export" + section.toUpperCase().charAt(0) + section.substr(1);
			
			CytowebUtil[export_fn](false);
		});
	});
	
	$("h2 .save").bind("click", function(){
		var section = $(this).parents(".section").attr("section");
		
		$(".print_selection .save[section=" + section + "]").click();
	});
	
	//sort genes
	$("#genes_widget").listSort({
        value: ".source_score_true",
        descending: true
    });
    $("#genes_widget").listSort({
    	value: ".score_text:first",
    	descending: true
    });
    
    //sort networks
    $("#networks_widget").listSort({
        value: ".per_cent_text:first",
        descending: true 
    });
	
    //sort go
    
    $.tablesorter.addParser({ 
        // set a unique id 
        id: 'exponent', 
        is: function(s) { 
            return ("" + s).match( /[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?/ ) != null;
        }, 
        format: function(s) { 
            return s;
        }, 
        type: 'numeric' 
    }); 
    
    $("#go_table").tablesorter({
        textExtraction: function(node){
            var ele = $(node);
            var val = "" + ele.attr("value");
            return val;
        }
    });
    
    $("#go_table .header.pval").click();
    
    $("#go_table th").unbind("click");
    
    setTimeout(function(){
    	$("#go_table td.annotation").each(function(i){
    		$(this).find(".number").append(i + 1);
    	});
    }, 50);
    
    // sort interactions
    
    $("#interactions_table").tablesorter({
    	sortList: [ [3,0], [2,1] ]
    });
    $("#interactions_table th").unbind("click");
});