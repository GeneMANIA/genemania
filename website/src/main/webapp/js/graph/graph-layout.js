CytowebUtil.recomputeLayout = function() {
//console.log("recompute layout");
	
	if(_vis) {
		$("#graph").showLoader({ message: "Resetting layout..." });

		var layout = { name: "ForceDirected", options: { weightAttr: "weight", restLength: 25, seed: 7 } };

		var layoutListener = function() {
			_vis.removeListener("layout", layoutListener);
			$("#graph").hideLoader();
		};
		
		_vis.addListener("layout", layoutListener);
		_vis.layout(layout);
		
	}
	
	
};