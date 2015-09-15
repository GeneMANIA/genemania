document.addEventListener('DOMContentLoaded', function(){
  var script = document.createElement('script');
  script.src = 'http://' + window.location.hostname + ':35729/livereload.js';

  document.head.insertBefore( script, document.head.firstChild );
});
