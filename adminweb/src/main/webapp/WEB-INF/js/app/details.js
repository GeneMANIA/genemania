
define(['jquery', 'app/constants', 'app/templates', 'mustache', 'datatables'],
    function($, constants, templates, mustache, datatables) {
//		my.setupTabChangeCallbacks = function(node) {
//			$('a[data-toggle="tab"]').on('shown', function (e) {
//				console.log("tab change received");
//				my.setupFileUpload(node);
//				my.setupForm(node);
//			});
//		}

	details = {};

	details.formatNodeDetails = function(node) {
	    switch (node.data.type) {
    		case constants.NETWORK_NODE:
    			return details.formatNetworkDetails(node);
    		case  constants.GROUP_FOLDER_NODE:
    			return details.formatGroupDetails(node);
    		case constants.IDENTIFIERS_NODE:
    			return details.formatIdentifiersDetails(node);
    		case constants.IDENTIFIERS_FOLDER_NODE:
    			return details.formatIdentifierFolderDetails(node);
    		case constants.ATTRIBUTES_FOLDER_NODE:
    			return details.formatAttributeDetails(node);
    		case constants.FUNCTIONS_FOLDER_NODE:
    			return details.formatFunctionsFolderDetails(node);
    		case constants.FUNCTIONS_NODE:
	    		return details.formatFunctionsDetails(node);
	    	case constants.ORGANISM_NODE:
	    		return details.formatOrganismDetails(node);
	    	default:
	    		console.log("unknown node type: " + node.data.type);
	    		return "";
		}
	}

	details.formatNetworkDetails = function(node) {
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
		
		nodeData.fileDownloadLink = details.makeFileDownloadLink(nodeData.fileId, nodeData.filename);
		nodeData.metadatafileDownloadLink = details.makeFileDownloadLink(nodeData.metadataFileId, nodeData.metadataFilename);

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
		

		details.makePubmedLink(nodeData);
		
		nodeData.suggestedName = details.suggestNetworkName(node);
		nodeData.suggestedDescription = details.suggestNetworkDescription(nodeData);
		
		d = mustache.render(templates.network, nodeData);

		return d;
	}

	details.formatIdentifiersDetails = function(node) {
	    console.log("identifiers node %o", node);
	    var nodeData = node.data;

		link = details.makeFileDownloadLink(nodeData.fileId, nodeData.filename);
		nodeData.link = link;
		return mustache.render(templates.identifier, nodeData);
	}

	details.formatIdentifierFolderDetails = function(node) {
	    var nodeData = node.data;
		return templates.identifier_folder;
	}
	
	details.formatOrganismDetails = function(node) {
	    node.data.title = node.title;
		return mustache.render(templates.organism, node.data);
	}
	
	details.formatAttributeDetails = function(node) {
		return mustache.render(templates.attribute, node.data);
	}

	details.formatFunctionsFolderDetails = function(node) {
		return mustache.render(templates.functions_folder, node.data);
	}
	
	details.formatFunctionsDetails = function(node) {
	    var nodeData = node.data;
		nodeData.fileDownloadLink = details.makeFileDownloadLink(nodeData.fileId, nodeData.filename);

		// mark the appropriate usage as selected. TODO currently
		// this just sets up some template vars, but can probably
		// do this in a couple lines of jquery instead. need to reorganize
		// templating to be able to run a function afterwards
        node.data.isUsageBPChecked = node.data.isUsageMFChecked = node.data.isUsageCCChecked = node.data.isUsageEnrichmentChecked = "";
        switch(node.data.usage) {
            case "BP":
                node.data.isUsageBPChecked = "checked";
                break;
            case "MF":
                node.data.isUsageMFChecked = "checked";
                break;
            case "CC":
                node.data.isUsageCCChecked = "checked";
                break;
            case "ENRICHMENT":
                node.data.isUsageEnrichmentChecked = "checked";
                break;
        }


		return mustache.render(templates.functions, nodeData);
	}
	
	details.formatGroupDetails = function(node) {
		return mustache.render(templates.group, node.data);
	}
	
	details.makeFileDownloadLink = function(id, label) {
		template = '<a href="{{link}}">{{label}}</a>';
		link = "download/file?&id=" + id;
		return mustache.render(template, {link: link, label:label});
	}
	
	details.makePubmedLink = function(nodeData) {
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

	// if a node has no title, try computing one
	// first using author information, then falling
	// back to the filename
	details.suggestNetworkName = function(node) {
	    var nodeData = node.data;
	    var newTitle = "Unnamed";
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
	
	details.suggestNetworkDescription = function(nodeData) {
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

	details.loadFileSnippet = function(node) {
	    if (node.data.type === constants.IDENTIFIERS_NODE) {
            if (node.data.fileId) {
                link = "preview/file?&id=" + node.data.fileId;
                $('#file_snippet').dataTable({
                    "serverSide": true,
                    "ajax": link
                });
            }
        }
	}
	
	return details;
});