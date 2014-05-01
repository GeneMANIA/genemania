
function setError(error, target, msgField, ellipsis) {
	var type = error.type;
	var msg = error.msg;
	var details = error.details;
	if (details == "") { details = null; }
	
	clearError(target, msgField);
	target.addClass("input_"+type);	
	
	if (msgField && msg) {
		msgField.show();
		msgField.addClass(type+"_msg");
		msgField.html('<span class="ellipsis_text">'+msg+'  </span>'); // Do NOT remove extra spaces!!! The ThreeDots plugin needs it.
		if (false && ellipsis) { // disable three dots for now since it doesn't seem to work the 1st time
			msgField.ThreeDots({ max_rows: 1, 
				                 alt_text_e: false, 
				                 alt_text_t: false, 
				                 whole_word: false });
		}
		msgField.show();
	}
}

function clearError(target, msgField) {
	// remove class and warning msg and hide any tool tip shown on target
	
	if (msgField) {
		msgField.hide();
		msgField.html('');
		msgField.removeAttr("warning");
		msgField.removeClass("warning_msg");
		msgField.removeAttr("error");
		msgField.removeClass("error_msg");
		msgField.removeAttr("threedots");
	}
	target.removeClass("input_error");
	target.removeClass("input_warning");
}

/** Remove error highlights. */
function clearErrors() {
	clearGeneError();
	clearError( $("#gene_open") );
	clearError( $("#networks_section"), $("#networks_section_error") );
}
	



var _lastValidationOrg = "";
var _lastValidationGeneSet = "";
var _validating = 0;
var _validating_active_count = 0;
var _between_edit_and_validated = false;
