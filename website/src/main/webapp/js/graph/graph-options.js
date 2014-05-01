
//Mapping network groups to edge colors:
CytowebUtil._edgeColorMapper = {
		attrName: "code",
		entries: [
	          { attrValue: "coexp"  	, value: "#d0b7d5" }, //Co-expression
	          { attrValue: "coloc"  	, value: "#a0b3dc" }, //Co-localization
	          { attrValue: "gi"     	, value: "#90e190" }, //Genetic interactions
	          { attrValue: "path"   	, value: "#9bd8de" }, //Pathway
	          { attrValue: "pi"     	, value: "#eaa2a2" }, //Physical interactions
	          { attrValue: "predict"	, value: "#f6c384" }, //Predicted
	          { attrValue: "spd"    	, value: "#dad4a2" }, //Shared protein domains
	          { attrValue: "spd_attr"	, value: "#D0D0D0" }, 
	          { attrValue: "reg"  		, value: "#D0D0D0" }, //Regulatory
	          { attrValue: "reg_attr"  	, value: "#D0D0D0" }, //Regulatory
	          { attrValue: "user"   	, value: "#f0ec86" }, //User-defined
		]
};

CytowebUtil.DEF_EDGE_OPACITY = 0.85;
CytowebUtil.AUX_EDGE_OPACITY = 0.4;
CytowebUtil.DEF_MERGED_EDGE_OPACITY = 0.6;

CytowebUtil.VISUAL_STYLE_OPACITY = {
	defaultValue: CytowebUtil.DEF_EDGE_OPACITY, 
       discreteMapper: {
		   attrName: "networkGroupCode",
		   entries: [
		          { attrValue: "coexp", value: CytowebUtil.AUX_EDGE_OPACITY },
		          { attrValue: "coloc", value: CytowebUtil.AUX_EDGE_OPACITY }
			]
       }
};

CytowebUtil.shapeMapper = function(data){
	if( data.attribute ){
		return "DIAMOND";
	}
	
	return null;
};

CytowebUtil.nodeColorMapper = {
	attrName: "attribute",
	entries: [ { attrValue: true, value: "#808080" } ]
};

CytowebUtil.nodeLabelGlowColorMapper = {
	attrName: "attribute",
	entries: [ { attrValue: true, value: "#808080" } ]
};

CytowebUtil.imageMapper = function(data){
	if( data.queryGene ){
		return absoluteUrl("img/etc/node_stripe.png");
	}
}

CytowebUtil.VISUAL_STYLE = {
		global: {
			backgroundColor: "#ffffff",
			selectionFillColor: "#aaaaaa",
			selectionLineColor: "#333333",
			selectionFillOpacity: 0.25,
			selectionLineOpacity: 0.5,
			selectionLineWidth: 1
		},
		nodes: {
			shape: {
				defaultValue: "ELLIPSE",
				customMapper: { functionName: "shapeMapper" }
			},
			image: {
				customMapper: { functionName: "imageMapper" }
			},
			color: { defaultValue: "#808080", discreteMapper: CytowebUtil.nodeColorMapper },
			opacity: 1,
			size: { defaultValue: 30, continuousMapper: { attrName: "score", minValue: 20, maxValue: 45 } },
			borderColor: "#777777",
			borderWidth: 1,
			label: { passthroughMapper: { attrName: "symbol" } },
			labelFontColor: "#ffffff",
			labelFontWeight: "bold",
			labelGlowColor: { defaultValue: "#808080", discreteMapper: CytowebUtil.nodeLabelGlowColorMapper },
            labelGlowOpacity: 1,
            labelGlowBlur: 4,
			labelGlowStrength: 32,
            labelHorizontalAnchor: "center",
            labelVerticalAnchor: "middle",
			selectionBorderColor: "#ffff77",
			selectionBorderWidth: 3,
			selectionGlowColor: "#ffff33",
			selectionGlowOpacity: 0,
			hoverBorderColor: "#aae6ff",
			hoverBorderWidth: 3,
			hoverGlowColor: "#aae6ff",
			hoverGlowOpacity: 0
		},
		edges: {
			color: {
				defaultValue: "#999999",
		        discreteMapper: CytowebUtil._edgeColorMapper
			},
			width: { defaultValue: 1.5, continuousMapper: { attrName: "weight", minValue: 1.5, maxValue: 5 } },
			mergeWidth: { defaultValue: 1, continuousMapper: { attrName: "weight", minValue: 2, maxValue: 5 } },
			opacity: CytowebUtil.VISUAL_STYLE_OPACITY,
			mergeOpacity: CytowebUtil.DEF_MERGED_EDGE_OPACITY,
			curvature: 16,
			selectionGlowColor: "#ffff33",
			selectionGlowOpacity: 0
		}
};

CytowebUtil.OPTIONS = {
		layout: { name: "Preset", options: { fitToScreen: false } },
		panZoomControlVisible: true,
		edgesMerged: false,
		nodeLabelsVisible: true,
		edgeLabelsVisible: false,
		nodeTooltipsEnabled: false,
		edgeTooltipsEnabled: false,
		visualStyle: CytowebUtil.VISUAL_STYLE
};

CytowebUtil.SCROLL_DELAY = 500;