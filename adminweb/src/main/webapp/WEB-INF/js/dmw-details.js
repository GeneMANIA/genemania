
var dmw = function(my, $) {
//		my.setupTabChangeCallbacks = function(node) {
//			$('a[data-toggle="tab"]').on('shown', function (e) {
//				console.log("tab change received");
//				my.setupFileUpload(node);
//				my.setupForm(node);
//			});
//		}
	
	my.formatNodeDetails = function(node) {
	    switch (node.data.type) {
    		case my.NETWORK_NODE:
    			return my.formatNetworkDetails(node);
    		case  my.GROUP_FOLDER_NODE:
    			return my.formatGroupDetails(node);
    		case my.IDENTIFIERS_NODE:
    			return my.formatIdentifiersDetails(node);
    		case my.IDENTIFIERS_FOLDER_NODE:
    			return my.formatIdentifierFolderDetails(node);
    		case my.ATTRIBUTES_FOLDER_NODE:
    			return my.formatAttributeDetails(node);
    		case my.FUNCTIONS_FOLDER_NODE:
    			return my.formatFunctionsFolderDetails(node);
    		case my.FUNCTIONS_NODE:
	    		return my.formatFunctionsDetails(node);
	    	case my.ORGANISM_NODE:
	    		return my.formatOrganismDetails(node);
	    	default:
	    		console.log("unknown node type: " + node.data.type);
	    		return "";
		}
	}

	my.formatNetworkDetails = function(node) {
	    var nodeData = node.data;
		
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
		

		my.makePubmedLink(nodeData);
		
		nodeData.suggestedName = my.suggestNetworkName(node);
		nodeData.suggestedDescription = my.suggestNetworkDescription(nodeData);
		
		d = $.mustache(my.network_details_template, nodeData);

		return d;
	}

	my.formatIdentifiersDetails = function(node) {
	    var nodeData = node.data;

		link = my.makeFileDownloadLink(nodeData.fileId, nodeData.filename);
		nodeData.link = link;
		return $.mustache(my.identifier_details_template, nodeData);
	}

	my.formatIdentifierFolderDetails = function(node) {
	    var nodeData = node.data;
		return my.identifier_folder_details_template;
	}
	
	my.formatOrganismDetails = function(node) {
		return $.mustache(my.organism_details_template, node.data);
	}
	
	my.formatAttributeDetails = function(node) {
		return $.mustache(my.attribute_details_template, node.data);
	}

	my.formatFunctionsFolderDetails = function(node) {
		return $.mustache(my.functions_folder_details_template, node.data);
	}
	
	my.formatFunctionsDetails = function(node) {
	    var nodeData = node.data;
		nodeData.fileDownloadLink = my.makeFileDownloadLink(nodeData.fileId, nodeData.filename);
		return $.mustache(my.functions_details_template, nodeData);   
	}
	
	my.formatGroupDetails = function(node) {
		return $.mustache(my.group_details_template, node.data);
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
	
	my.suggestNetworkName = function(node) {
	    var nodeData = node.data;
	    var newTitle = "";
	    console.log("in suggest name with %o", nodeData);
		if (node.title != "") {
			newTitle = node.title;
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

	my.loadFileSnippet = function(node) {
	    if (node.data.type === my.IDENTIFIERS_NODE) {
            if (node.data.fileId) {
                link = "preview/file?&id=" + node.data.fileId;
                $('#file_snippet').dataTable({
                    "serverSide": true,
                    "ajax": link
                });
            }
        }
	}
	
	return my;
}(dmw || {}, $);