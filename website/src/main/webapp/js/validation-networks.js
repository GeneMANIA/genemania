function validateNetworks() {
	
	//console.log("validate networks for organism " + $("#species_select").val());
	var msg;
	var networskError = ($("#networkTree input:checked").filter("[organism=" + $("#species_select").val() + "]").length == 0);
	if (networskError) {
		msg = "With no networks selected, the default networks will be used.";
		setError({ type: "warning", msg: msg }, $("#networks_section"), $("#networks_section_error"));
		$("#networks_section:visible");
	} else {
		clearError( $("#networks_section"), $("#networks_section_error") );
	}
	
}

function updateSubmitButton() {
	
}

function validateTree() {
	updateInputNetworks();
	validateNetworks();
}