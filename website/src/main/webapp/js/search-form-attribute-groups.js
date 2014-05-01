function loadAttributeGroups(){
	var organism = $("#species_select").val();
	
	$(".attribute_group[organism=" + organism + "]").show();
	$(".attribute_group[organism!=" + organism + "]").hide();
	
	if( $(".attribute_group:visible").size() == 0 ){
		$("#no_attr_groups").show();
	} else {
		$("#no_attr_groups").hide();
	}
}