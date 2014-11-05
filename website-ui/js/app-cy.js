app.factory('cy', 
[
function(  ){

  var cy = window.cy = cytoscape({
    container: document.getElementById('cy'),

    motionBlur: true
  });

  return cy;

}]);