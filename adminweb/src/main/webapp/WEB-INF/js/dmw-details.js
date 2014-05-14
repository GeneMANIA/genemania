
var dmw = function(my, $) {
//		my.setupTabChangeCallbacks = function(node) {
//			$('a[data-toggle="tab"]').on('shown', function (e) {
//				console.log("tab change received");
//				my.setupFileUpload(node);
//				my.setupForm(node);
//			});
//		}
	
	my.formatNodeDetails = function(nodeData) {
		if (nodeData.type == my.NETWORK_NODE) {
			return my.formatNetworkDetails(nodeData);
		} 
		else if (nodeData.type == my.GROUP_FOLDER_NODE) {
			return my.formatGroupDetails(nodeData);
		}
		else if (nodeData.type == my.IDENTIFIERS_NODE) {
			return my.formatIdentifiersDetails(nodeData);
		}
		else if (nodeData.type == my.IDENTIFIERS_FOLDER_NODE) {
			return my.formatIdentifierFolderDetails(nodeData);
		}
		else if (nodeData.type == my.ATTRIBUTES_FOLDER_NODE) {
			return my.formatAttributeDetails(nodeData);
		}
		else if (nodeData.type == my.FUNCTIONS_FOLDER_NODE) {
			return my.formatFunctionsFolderDetails(nodeData);
		}
		else if (nodeData.type == my.FUNCTIONS_NODE) {
			return my.formatFunctionsDetails(nodeData);
		}
		else if (nodeData.type == my.ORGANISM_NODE) {
			return my.formatOrganismDetails(nodeData);
		}
		else {
			console.log("unknown node type: " + nodeData.type);
			return "";
		}
	}

	my.formatNetworkDetails = function(nodeData) {
		
		if (nodeData.defaultSelected) {
			nodeData.isDefaultChecked = "checked";
		}
		else {
			nodeData.isDefaultChecked = "";
		}
		if (nodeData.restrictedLicense) {
			nodeData.isRestrictedLicenseChecked = "checked";
		}
		else {
			nodeData.isRestrictedLicenseChecked = "";
		}
		if (nodeData.enabled) {
			nodeData.isEnabledChecked = "checked";
		}
		else {
			nodeData.isEnabledChecked = "";
		}
		
		nodeData.fileDownloadLink = my.makeFileDownloadLink(nodeData.fileId, nodeData.filename);
		nodeData.metadatafileDownloadLink = my.makeFileDownloadLink(nodeData.metadataFileId, nodeData.metadataFilename);

		// sample linkout for metadata
		if (nodeData.metadataProcessingDetails && nodeData.metadataProcessingDetails.sampleAccession) {
			console.log("linkout: sample from metadata");
			nodeData.sampleLinkoutDescription = nodeData.metadataProcessingDetails.sampleName;
			nodeData.sampleLinkoutLabel = nodeData.linkoutLabel;
			if (nodeData.linkoutUrl) {
			    nodeData.sampleLinkoutUrl = nodeData.linkoutUrl.replace('{1}', nodeData.metadataProcessingDetails.sampleAccession);
			}
		}
		else if (nodeData.processingDetails && nodeData.processingDetails.sampleAccession) {
			console.log("linkout: sample from data");
			nodeData.sampleLinkoutDescription = nodeData.processingDetails.sampleName;
			nodeData.sampleLinkoutLabel = nodeData.linkoutLabel;
			if (nodeData.linkoutUrl) {
			    nodeData.sampleLinkoutUrl = nodeData.linkoutUrl.replace('{1}', nodeData.processingDetails.sampleAccession);
		    }
		}
		else {
			console.log("linkout: nada");
		}
		
		console.log("node data %o", nodeData);
		
		my.makePubmedLink(nodeData);
		
		nodeData.suggestedName = my.suggestNetworkName(nodeData);
		nodeData.suggestedDescription = my.suggestNetworkDescription(nodeData);
		
		d = $.mustache(my.network_details_template, nodeData);

		return d;
	}

	my.formatIdentifiersDetails = function(nodeData) {
		link = my.makeFileDownloadLink(nodeData.fileId, nodeData.filename);
		nodeData.link = link;
		return $.mustache(my.identifier_details_template, nodeData);
	}

	my.formatIdentifierFolderDetails = function(nodeData) {
		return my.identifier_folder_details_template;
	}
	
	my.formatOrganismDetails = function(nodeData) {
		console.log("organism node %o", nodeData);
		return $.mustache(my.organism_details_template, nodeData);
	}
	
	my.formatAttributeDetails = function(nodeData) {
		return $.mustache(my.attribute_details_template, nodeData); 
	}

	my.formatFunctionsFolderDetails = function(nodeData) {
		return $.mustache(my.functions_folder_details_template, nodeData);   
	}
	
	my.formatFunctionsDetails = function(nodeData) {
		nodeData.fileDownloadLink = my.makeFileDownloadLink(nodeData.fileId, nodeData.filename);
		return $.mustache(my.functions_details_template, nodeData);   
	}
	
	my.formatGroupDetails = function(nodeData) {		
		return $.mustache(my.group_details_template, nodeData);
	}
	
	my.makeFileDownloadLink = function(id, label) {
		template = '<a href="{{link}}">{{label}}</a>';
		link = "download/file?&id=" + id;
		return $.mustache(template, {link: link, label:label});			
	}
	
	my.makePubmedLink = function(nodeData) {
		if (nodeData.pubmedId == '' || nodeData.pubmedId == 0) {
			nodeData.displayPubmedLink = "none";
			nodeData.pubmedLink = "#";
			nodeData.pubmedDisplayId = '';
		}
		else {
			nodeData.displayPubmedLink = "inline";
			nodeData.pubmedLink = "http://www.ncbi.nlm.nih.gov/pubmed/" + nodeData.pubmedId;
			nodeData.pubmedDisplayId = nodeData.pubmedId;
		}
	}
	
	my.suggestNetworkName = function(nodeData) {
		if (nodeData.title != "") {
			newTitle = nodeData.title;
		} 
		else if (nodeData.extra) {
			extra = nodeData.extra;
			newTitle = extra.faln + "-" + extra.laln + "-" + extra.year;
		}
		else {
			newTitle = nodeData.filename;
		}

		return newTitle;		
	}
	
	my.suggestNetworkDescription = function(nodeData) {
		if (nodeData.description != null && nodeData.description != "") {
			newDescription = nodeData.description;
		}
		else if (nodeData.extra) {
			newDescription = nodeData.extra.title;
		}
		else {
			newDescription = "";
		}
		return newDescription;
	}
	
	return my;
}(dmw || {}, $);