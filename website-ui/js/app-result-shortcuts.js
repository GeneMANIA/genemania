'use strict';

app.factory('Result_shortcuts',
[ 'util',
function( util ){ return function( Result ){

  var rfn = Result.prototype;

  var shortcuts = [];

  var on = function( combo, callback ){
    shortcuts.push({
      combo: combo,
      callback: callback
    });
  };


  // pan

  let panAmount = 5;
  let panMult = 4;

  on('down', function(){ cy.panBy({ y: -panAmount * panMult }) });
  on('up', function(){ cy.panBy({ y: panAmount * panMult }) });
  on('left', function(){ cy.panBy({ x: panAmount * panMult }) });
  on('right', function(){ cy.panBy({ x: -panAmount * panMult }) });
  on('shift+down', function(){ cy.panBy({ y: -panAmount }) });
  on('shift+up', function(){ cy.panBy({ y: panAmount }) });
  on('shift+left', function(){ cy.panBy({ x: panAmount }) });
  on('shift+right', function(){ cy.panBy({ x: -panAmount }) });


  // zoom

  let zoomAmount = 0.05;
  let zoomMult = 4;

  let zoomBy = mult => {
    let z = cy.zoom();
    let w = cy.width() - (rfn.networksExpanded ? $('#network-list').width() : 0);
    let h = cy.height() - (rfn.historyExpanded ? $('#query-history').height() : 0);

    cy.zoom({
      level: z * mult,
      renderedPosition: { x: w/2, y: h/2 }
    });
  };

  on('=', function(){ zoomBy( (1 + zoomAmount * zoomMult) ) });
  on('-', function(){ zoomBy( 1/(1 + zoomAmount * zoomMult) ) });
  on('+', function(){ zoomBy( 1 + zoomAmount ) });
  on('_', function(){ zoomBy( 1/(1 + zoomAmount) ) });


  // layout

  on('f', function(){ shortcuts.result.fitGraph(); });
  on('j', function(){ shortcuts.result.circleLayout(); });
  on('k', function(){ shortcuts.result.linearLayout(); });
  on('l', function(){ shortcuts.result.forceLayout(); });

  rfn.bindShortcutKeys = function(){
    shortcuts.result = this;

    shortcuts.forEach(function( s ){
      Mousetrap.unbind( s.combo );
      Mousetrap.bind( s.combo, s.callback );
    });
  };

} } ]);
