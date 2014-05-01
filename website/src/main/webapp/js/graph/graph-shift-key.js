
CytowebUtil._shift_down = false;

$(function(){
	$("body").bind("keydown", function(e){
	    CytowebUtil._shift_down = e.shiftKey;
	}).bind("keyup", function(e){
	    CytowebUtil._shift_down = e.shiftKey;
	});
});