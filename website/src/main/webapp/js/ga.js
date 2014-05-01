/*
 * Google Analytics - Asynchronous Tracking 
 * http://code.google.com/apis/analytics/docs/tracking/asyncTracking.html
 */
var _gaq = _gaq || [];
_gaq.push(['_setAccount', '${GA_ACCT_NR}']);
_gaq.push(['_trackPageview']);
_gaq.push(['_setVar', window.navigator.userAgent]);

(function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    (document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(ga);
})();