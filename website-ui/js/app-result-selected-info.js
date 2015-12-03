'use strict';

app.factory('Result_selectedinfo',
[ 'util', 'cy',
function( util, ngCy ){ return function( Result ){

  var r = Result;
  var rfn = r.prototype;



  // toggling the sidebar

  rfn.closeSelectedInfo = function(opts){
    opts = $.extend({
      publish: true
    }, opts);

    this.selectedInfoOpen = false;

    if( opts.publish ){
      PubSub.publish('result.closeSelectedInfo', this);
    }
  };

  rfn.openSelectedInfo = function(opts){
    opts = $.extend({
      publish: true
    }, opts);

    this.selectedResultGene = opts.rGene;
    this.selectedResultAttribute = opts.rAttr;
    this.selectedResultInteraction = opts.rIntn;
    this.selectedInfoOpen = true;

    if( opts.publish ){
      PubSub.publish('result.openSelectedInfo', this);
    }
  };

  rfn.selectGene = function( idOrObj ){
    var rGene = typeof idOrObj === typeof {} ? idOrObj : this.resultGenesById[ idOrObj ];

    this.openSelectedInfo({
      rGene: rGene
    });
  };

  rfn.selectAttribute = function( idOrObj ){
    var rAttr = typeof idOrObj === typeof {} ? idOrObj : this.resultAttributesById[ idOrObj ];

    this.openSelectedInfo({
      rAttr: rAttr
    });
  };

  rfn.selectInteraction = function( idOrObj ){
    var rIntn = typeof idOrObj === typeof {} ? idOrObj : this.resultInteractionsById[ idOrObj ];

    this.openSelectedInfo({
      rIntn: rIntn
    });
  };

} } ]);

app.controller('SelectedInfoCtrl',
[ '$scope', 'updateScope', 'cy',
function( $scope, updateScope, ngCy ){

  function init(){
    $scope.query = Query.current;
    $scope.result = $scope.query.result;
    $scope.rGene = $scope.result.selectedResultGene;
    $scope.rAttr = $scope.result.selectedResultAttribute;
    $scope.rIntn = $scope.result.selectedResultInteraction;

    if( $scope.rIntn ){
      $scope.rNet = $scope.rIntn.resultNetwork;
      $scope.net = $scope.rNet.network;
    } else {
      $scope.rNet = null;
      $scope.net = null;
    }

    updateScope();
  }

  PubSub.subscribe('result.searched', init);

  PubSub.subscribe('result.openSelectedInfo', init);
  PubSub.subscribe('result.closeSelectedInfo', init);
  PubSub.subscribe('result.updateHighlights', init);

} ]);
