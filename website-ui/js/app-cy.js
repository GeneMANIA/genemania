app.factory('cy',
[
function(  ){

  var cy = window.cy = cytoscape({
    container: document.getElementById('cy'),

    boxSelectionEnabled: false,
    autounselectify: true,
    motionBlur: true
  });

  // interacting with the graph should close the genes box
  cy.on('tapstart', function(){
    var qg = document.getElementById('query-genes-textarea');

    if( qg && document.activeElement === qg ){
      qg.blur();
    }
  });

  cy.on('tap', '[?gene]', function(){
    query.result.selectGene( this.data('id') );
  });

  cy.on('tap', function(e){
    if( e.cyTarget === cy ){
      query.result.closeSelectedInfo();
    }
  });

  return cy;

}]);
