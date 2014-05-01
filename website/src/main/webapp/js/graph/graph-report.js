CytowebUtil.generateReport = function(token, params) {
	
	if( !CytowebUtil.checkVersionFor("report") ){
		return;
	}
			
	$("#print_form [name=golegend]").remove();
	$("#print_form").append('<textarea name="golegend">' + $("#go_tab > .header").html() + '</textarea>');
	
	var legend = $('<div></div>');
	var names = [];
	var colors = {};
	$("#networks_widget .checktree_top_level").each(function(){
		var name = $(this).find(".label:first .name").text();
		var color = $(this).find(".bar:first").css("background-color");
		
		names.push(name);
		colors[name] = color;
	});
	
	names.sort();
	$.each(names, function(i, name){
		var color = colors[name];
		legend.append('<div class="entry"> <span class="colour" style="border-left-color: '+ color +'"></span> <span class="name">'+ name +'</span> </div>');
	});
	
	$("#print_form [name=netlegend]").remove();
	$("#print_form").append('<textarea name="netlegend">' + legend.html() + '</textarea>');
	
	var svg = _vis.svg();
	$("#print_form [name=svg]").remove();
	$("#print_form").append('<input type="hidden" name="svg" value=\'' + svg + '\' />');
	
	$("#print_form").submit();

	
};
