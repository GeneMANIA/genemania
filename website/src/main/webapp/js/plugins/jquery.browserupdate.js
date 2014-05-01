(function($){
	
	var defaults = {
		title: "Your browser needs to be updated",
		siteName: "this site",
		outOfDateText: "Your browser is out-of-date and needs to be updated to work on {name} properly.",
		updateText: "Update {name}.",
		unsupportedText: "Your browser is not supported by {name}.  Please upgrade to one of the supported browsers below.",
		alternativeText: "These browsers are also supported by {name}.",
		recommendText: "<strong>Internet Explorer is slower and buggier than other browsers.  We recommend you use one of these alternative browsers with {name}.</strong>",
		browsers: {
		}
	};
	
	function version(str){
		str = "" + str;
		var parts = str.split(".");
		var version = {};
		
		version.major = parseInt(parts[0]);
		if( parts.length > 1 ){
			version.minor = parseInt(parts[1]);
		}
		
		version.olderThan = function(needed){
			if( needed.major > version.major ){
				return true;
			} else if( needed.major == version.major ){
				if( needed.minor > version.minor ){
					return true;
				}
			}
			
			return false;
		};
		
		return version;
	}
	
	$.browserupdate = function(opts){
		var options = $.extend(true, {}, defaults, opts);
		
		var browser = options.browsers[ $.browser.name ];
		
		var $root = $('<div class="ui-browserupdate-notification"></div>');
		$("body").append($root);
		
		var $bg = $('<div class="ui-browserupdate-background"></div>');
		$root.append($bg);
		
		var $msg = $('<div class="ui-browserupdate-message"></div>');
		$root.append($msg);
		
		$msg.append('<h2>' + options.title + '</h2>');
		
		if( browser == null ){
			$msg.append('<p class="ui-browserupdate-unsupported">' + options.unsupportedText.replace("{name}", options.siteName) + '</p>');
		} else {
			var current = version($.browser.version);
			var needed = version(browser.version);
			
			if( current.olderThan(needed) ) {
				$msg.append('<p class="ui-browserupdate-out-of-date">' + options.outOfDateText.replace("{name}", options.siteName) + '</p>');
				
				$msg.append('<a href="' + browser.url + '">\
							<div class="ui-browserupdate-icon '+ $.browser.name +'"></div>\
							<span class="ui-browserupdate-update-message">' + options.updateText.replace("{name}", browser.name) + '</span>\
							<span class="ui-browserupdate-versions">Current version: <em>' + $.browser.version + '</em></span>\
							<span class="ui-browserupdate-versions">Minimum required version: <em>' + browser.version + '</em></span>\
						</a>');
				
				if( $.browser.msie ){
					$msg.append('<p class="ui-browserupdate-alternative">' + options.recommendText.replace("{name}", options.siteName) + '</p>');
				} else {
					$msg.append('<p class="ui-browserupdate-alternative">' + options.alternativeText.replace("{name}", options.siteName) + '</p>');
				}
				
			} else {
				$root.hide();
			}
			
			$.each(options.browsers, function(name, browser){
				if( name == $.browser.name || name == "msie" ){
					return;
				}
				
				$msg.append('<a href="'+ browser.url +'" target="_blank">\
								<div class="ui-browserupdate-icon '+ name +'"><span class="ui-browserupdate-name">'+ browser.name +'</span></div>\
							</a>');
			});
		}
		
		
		
		$msg.css({
			position: "absolute",
			left: "50%",
			top: "50%"
		});
		
		$msg.css({
			marginLeft: -1*( parseInt($msg.outerWidth())/2 ),
			marginTop: -1*( parseInt($msg.outerHeight())/2 )
		});
		
		
	};
	
})(jQuery);