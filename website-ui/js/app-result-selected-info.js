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
    this.selectedResultInteractions = opts.rIntns ? opts.rIntns.sort(function( a, b ){
      return b.absoluteWeight - a.absoluteWeight;
    }) : null;
    this.selectedInteractionIds = ( opts.rIntns || [] ).map(function( rIntn ){
      return rIntn.id;
    });
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
    var rIntn1 = typeof idOrObj === typeof {} ? idOrObj : this.resultInteractionsById[ idOrObj ];
    var gid11 = rIntn1.fromGene.gene.id;
    var gid12 = rIntn1.toGene.gene.id;

    var rIntns = this.resultInteractions.filter(function( rIntn2 ){
      var gid21 = rIntn2.fromGene.gene.id;
      var gid22 = rIntn2.toGene.gene.id;

      return ( gid11 === gid21 && gid12 === gid22 ) || ( gid11 === gid22 && gid12 === gid21 );
    });

    this.openSelectedInfo({
      rIntn: rIntn1,
      rIntns: rIntns
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
    $scope.rIntns = $scope.result.selectedResultInteractions;
    $scope.intnIds = $scope.result.selectedInteractionIds;

    updateScope();
  }

  PubSub.subscribe('result.searched', init);

  PubSub.subscribe('result.openSelectedInfo', init);
  PubSub.subscribe('result.closeSelectedInfo', init);
  PubSub.subscribe('result.updateHighlights', init);

} ]);
