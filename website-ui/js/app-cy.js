app.factory('cy', 
[
function(  ){

  var cy = window.cy = cytoscape({
    container: document.getElementById('cy'),

    motionBlur: true
  });

  // interacting with the graph should close the genes box
  cy.on('tapstart', function(){
    var qg = document.getElementById('query-genes-textarea');

    if( qg && document.activeElement === qg ){
      qg.blur();
    }
  });

  return cy;

}]);