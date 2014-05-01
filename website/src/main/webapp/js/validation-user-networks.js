$(function(){
	if( $("#phrase_and_go").size() == 0 && $("#query_line").size() == 0 ){
		return; // ignore this js file if no query area
	}
	
	if( $("#user_network_exception").size() > 0 ){
		
		var title = "We couldn't complete your query";
		var details = "<p>You deleted some of your uploaded networks while we were processing your query.  Please resend your query by clicking here.</p><p>Please note that your networks are lost when you clear your browser cookies.</p>";
		
		$("#findBtn").qtip({
			content: {
				title: { 
					text: title
				},
				text: details
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
			   width: { min: 360, max: 360 },
			   screen: true,
			   padding: 8, 
			   textAlign: 'left',
			   name: 'cream',
			   tip: true      // Give it a speech bubble tip with automatic corner detection
			},
			position: {
				type: "absolute",
				adjust: {
					screen: true,
					scroll: true
				},
				corner: {
					target: 'topMiddle',
					tooltip: 'bottomRight'
				}
			}
		});
	}
});