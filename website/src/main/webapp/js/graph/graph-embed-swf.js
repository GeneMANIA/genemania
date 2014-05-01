


CytowebUtil._embedSWF = function() {
    //Major version of Flash required
    var requiredMajorVersion = MIN_FLASH_VERSION;
    //Minor version of Flash required
    var requiredMinorVersion = MIN_FLASH_MINOR_VERSION;
    //Minor version of Flash required
    var requiredRevision = 0;

    var containerId = this.containerId;

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
            "src", this.flashInstallerPath,
            "FlashVars", "MMredirectURL="+MMredirectURL+'&MMplayerType='+MMPlayerType+'&MMdoctitle='+MMdoctitle+"",
            "width", "100%",
            "height", "100%",
            "align", "middle",
            "id", this.id,
            "quality", "high",
            "bgcolor", "#ffffff",
            "name", this.id,
            "allowScriptAccess","sameDomain",
            "type", "application/x-shockwave-flash",
            "pluginspage", "http://www.adobe.com/go/getflashplayer"
        );
        
        if (onCytoscapeWebLoaded) { onCytoscapeWebLoaded(true); progress("cytolite"); }
    } else if (hasRequestedVersion) {
        var optionKeys = ["resourceBundleUrl"];
        var flashVars = "";
        if (this.options) {
            for (var i in optionKeys) {
                if (Object.hasOwnProperty.call(optionKeys, i)) {
                    var key = optionKeys[i];
                    if (this.options[key] !== undefined) {
                        flashVars += key + "=" + this.options[key] + "&";
                    }
                }
            }
            flashVars += "id=" + this.id;
        }

        // if we've detected an acceptable version
        // embed the Flash Content SWF when all tests are passed
        AC_FL_RunContent(
                "src", this.swfPath,
                "width", "100%",
                "height", "100%",
                "align", "middle",
                "id", this.id,
                "quality", "high",
                "bgcolor", "#ffffff",
                "name", this.id,
                "allowScriptAccess", "always",
                "type", "application/x-shockwave-flash",
                "pluginspage", "http://www.adobe.com/go/getflashplayer",
                "wmode", "opaque", // DO NOT set it to "transparent", because it may crash FireFox and IE on Windows!
                "flashVars", flashVars
        );
    } else { // flash is too old or we can't detect the plugin
        // Insert non-flash content:
        document.getElementById(containerId).innerHTML = this.flashAlternateContent;
    	
        if (onCytoscapeWebLoaded) { onCytoscapeWebLoaded(true); progress("cytolite"); }
    }
    return this;
};
