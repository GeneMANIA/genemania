app.factory('Result_selectedinfo',
[ 'util', 'cy',
function( util, cy ){ return function( Result ){

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

} } ]);

app.controller('SelectedInfoCtrl',
[ '$scope', 'updateScope', 'cy',
function( $scope, updateScope, cy ){

  function init(){
    $scope.query = Query.current;
    $scope.result = $scope.query.result;
    $scope.rGene = $scope.result.selectedResultGene;

    updateScope();
  }

  PubSub.subscribe('result.searched', init);

  PubSub.subscribe('result.openSelectedInfo', init);
  PubSub.subscribe('result.closeSelectedInfo', init);

} ]);
