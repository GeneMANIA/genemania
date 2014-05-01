/*
  This file is part of Cytoscape Web.
  Copyright (c) 2009, The Cytoscape Consortium (www.cytoscape.org)

  The Cytoscape Consortium is:
    - Agilent Technologies
    - Institut Pasteur
    - Institute for Systems Biology
    - Memorial Sloan-Kettering Cancer Center
    - National Center for Integrative Biomedical Informatics
    - Unilever
    - University of California San Diego
    - University of California San Francisco
    - University of Toronto

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
*/

// ===[ namespaces ]================================================================================

// Create namespaces if not already defined:
(function () {
    "use strict";
    
    if (typeof(window['org']) === 'undefined') {
    	window['org'] = {};
    }
    if (typeof(window.org['cytoscapeweb']) === 'undefined') {
    	window.org['cytoscapeweb'] = {};
    }
    if (typeof(window.org.cytoscapeweb['demo']) === 'undefined') {
    	window.org.cytoscapeweb['demo'] = {};
    }

    // ===[ Exporter ]==============================================================================

    // Create a global map to store all instances:
    window._cytoscapeWebExporterInstances = { index: 0 };
    	
    org.cytoscapeweb.demo.Exporter = function (containerId, options) {
        this.containerId = containerId;

        if (!options) { options = {}; }
        this.options = options;

        // Part of the embed or object tag id:
        this.idToken = options.idToken ? options.idToken : "exporter";
        // The .swf path, including its name, but without the file extension:
        this.swfPath = options.swfPath ? options.swfPath : "Exporter";
        // The path of the .swf file that updates the Flash player version:
        this.flashInstallerPath = options.flashInstallerPath ? options.flashInstallerPath : "playerProductInstall";
        // Alternate content to be displayed in case user does not have Flash installed:
        this.flashAlternateContent = options.flashAlternateContent ? options.flashAlternateContent : 'This content requires the Adobe Flash Player. ' +
                                                                                                     '<a href=http://www.adobe.com/go/getflash/>Get Flash</a>';
        _cytoscapeWebExporterInstances.index++;

        this.id = this.idToken + _cytoscapeWebExporterInstances.index;
        _cytoscapeWebExporterInstances[this.id] = this;
        _embedSWF(this.containerId, this.id, this.swfPath, this.flashInstallerPath);
    };

    org.cytoscapeweb.demo.Exporter.prototype = {
    	
    	swf: function () {
    		return _swf(this.id);
    	},
    		
        _onReady: function () {
    		if (this.swf().fileName) {
    			this.swf().fileName(this.options.fileName());
    		}
    		if (this.swf().base64) {
    			this.swf().base64(this.options.base64);
    		}
            this.options.ready();
        },
        
        _onClick: function () {
        	return this.options.data();
        }
    };
    
    // ===[ Importer ]==============================================================================
    
    // Create a global map to store all instances:
    window._cytoscapeWebImporterInstances = { index: 0 };
    	
    org.cytoscapeweb.demo.Importer = function (containerId, options) {
        this.containerId = containerId;

        if (!options) { options = {}; }
        this.options = options;

        // Part of the embed or object tag id:
        this.idToken = options.idToken ? options.idToken : "importer";
        // The .swf path, including its name, but without the file extension:
        this.swfPath = options.swfPath ? options.swfPath : "Importer";
        // The path of the .swf file that updates the Flash player version:
        this.flashInstallerPath = options.flashInstallerPath ? options.flashInstallerPath : "playerProductInstall";
        // Alternate content to be displayed in case user does not have Flash installed:
        this.flashAlternateContent = options.flashAlternateContent ? options.flashAlternateContent : 'This content requires the Adobe Flash Player. ' +
                                                                                                     '<a href=http://www.adobe.com/go/getflash/>Get Flash</a>';
        _cytoscapeWebImporterInstances.index++;

        this.id = this.idToken + _cytoscapeWebImporterInstances.index;
        _cytoscapeWebImporterInstances[this.id] = this;
        _embedSWF(this.containerId, this.id, this.swfPath, this.flashInstallerPath);
    };
    
    org.cytoscapeweb.demo.Importer.prototype = {
    	swf: function () {
			return _swf(this.id);
		},
    		
    	_onReady: function () {
			if (this.swf().typeFilter) {
				this.swf().typeFilter(this.options.typeFilter());
			}
			if (this.swf().typeDescription) {
				this.swf().typeDescription(this.options.typeDescription());
			}
            this.options.ready();
        },
        
		_onStart: function(metadata){
        	// when data starts opening
        	if (this.options.binary) {
        		this.swf().binary(this.options.binary(metadata));
        	}
        	if (this.options.start) {
        		this.options.start(metadata);
        	}
		},
		
		_onComplete: function(data){
			// when data finishes loading
			this.options.data(data);
		},
		
		_onCancel: function(){
			// when the user closes the dialog
			if (this.options.cancel) {
				this.options.cancel();
			}
		},
		
		_onError: function(msg){
			// when the file can not be read
			if (this.options.error) {
				this.options.error(msg);
			}
		}
    };
    
    // ===[ Functions ]=============================================================================
    
    function _swf (id) {
        if (navigator.appName.indexOf("Microsoft") !== -1) {
            return window[id];
        } else {
            return document[id];
        }
    }

    function _embedSWF (containerId, id, swfPath, flashInstallerPath) {
        //Major version of Flash required
        var requiredMajorVersion = 10;
        //Minor version of Flash required
        var requiredMinorVersion = 0;
        //Minor version of Flash required
        var requiredRevision = 0;

        // Let's redefine the default AC_OETags function, because we don't necessarily want
        // to replace the whole HTML page with the swf object:
        AC_Generateobj = function (objAttrs, params, embedAttrs) {
            var str = '';
            var i;
            if (isIE && isWin && !isOpera) {
                str += '<object ';
                for (i in objAttrs) {
                    if (Object.hasOwnProperty.call(objAttrs, i)) {
                        str += i + '="' + objAttrs[i] + '" ';
                    }
                }
                str += '>';
                for (i in params) {
                    if (Object.hasOwnProperty.call(params, i)) {
                        str += '<param name="' + i + '" value="' + params[i] + '" /> ';
                    }
                }
                str += '</object>';
            } else {
                str += '<embed ';
                for (i in embedAttrs) {
                    if (Object.hasOwnProperty.call(embedAttrs, i)) {
                        str += i + '="' + embedAttrs[i] + '" ';
                    }
                }
                str += '> </embed>';
            }
            // Replace only the indicated DOM element:
            document.getElementById(containerId).innerHTML = str;
        };

        // Version check for the Flash Player that has the ability to start Player Product Install (6.0r65)
        var hasProductInstall = DetectFlashVer(6, 0, 65);

        // Version check based upon the values defined in globals
        var hasRequestedVersion = DetectFlashVer(requiredMajorVersion, requiredMinorVersion, requiredRevision);

        if (hasProductInstall && !hasRequestedVersion) {
            // DO NOT MODIFY THE FOLLOWING FOUR LINES
            // Location visited after installation is complete if installation is required
            var MMPlayerType = (isIE === true) ? "ActiveX" : "PlugIn";
            var MMredirectURL = window.location;
            document.title = document.title.slice(0, 47) + " - Flash Player Installation";
            var MMdoctitle = document.title;

            AC_FL_RunContent(
                "src", flashInstallerPath,
                "FlashVars", "MMredirectURL="+MMredirectURL+'&MMplayerType='+MMPlayerType+'&MMdoctitle='+MMdoctitle+"",
                "width", "100%",
                "height", "100%",
                "align", "middle",
                "id", id,
                "quality", "high",
                "bgcolor", "#ffffff",
                "name", id,
                "allowScriptAccess","sameDomain",
                "type", "application/x-shockwave-flash",
                "pluginspage", "http://www.adobe.com/go/getflashplayer"
            );
        } else if (hasRequestedVersion) {
            // if we've detected an acceptable version
            // embed the Flash Content SWF when all tests are passed
            AC_FL_RunContent(
                    "src", swfPath,
                    "class", "swf",
                    "width", "100%",
                    "height", "100%",
                    "align", "middle",
                    "id", id,
                    "quality", "high",
                    "name", id,
                    "allowScriptAccess", "always",
                    "type", "application/x-shockwave-flash",
                    "pluginspage", "http://www.adobe.com/go/getflashplayer",
                    "wmode", "transparent",
                    "flashVars", "id="+id
            );
        } else { // flash is too old or we can't detect the plugin
            // Insert non-flash content:
            document.getElementById(containerId).innerHTML = 'This content requires the Adobe Flash Player. '
               	+ '<a href=http://www.adobe.com/go/getflash/>Get Flash</a>';
        }
        return this;
    }
})();
