'use strict';

app.factory('cy',
[ 'cyStylesheet',
function( cyStylesheet ){

  var cy = window.cy = cytoscape({
    container: document.getElementById('cy'),

    style: cyStylesheet(),

    boxSelectionEnabled: false,
    autounselectify: true,
    motionBlur: true,
    motionBlurOpacity: cytoscape.is.khtmlEtc() ? 0.05 : 0.1
  });

  // interacting with the graph should close the genes box
  cy
    .on('tapstart', function(){
      var qg = document.getElementById('query-genes-textarea');

      if( qg && document.activeElement === qg ){
        qg.blur();
      }
    })

    .on('tap', '[?gene]', function(){
      query.result.selectGene( this.data('id') );
    })

    .on('tap', '[?attr]', function(){
      query.result.selectAttribute( this.data('id') );
    })

    .on('tap', 'edge[?attr]', function(){
      var attrNode = this.connectedNodes('[?attr]');

      query.result.selectAttribute( attrNode.data('id') );
    })

    .on('tap', 'edge[?intn]', function(){
      query.result.selectInteraction( this.data('rIntnId') );
    })

    .on('tap', function(e){
      if( e.cyTarget === cy ){
        query.result.closeSelectedInfo();
      }
    })
  ;

  return cy;

}]);
