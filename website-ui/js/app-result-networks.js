app.controller('NetworksCtrl',
[ '$scope', 'updateScope', 
function( $scope, updateScope ){

  function init(){
    $scope.query = Query.current;
    $scope.result = $scope.query.result;

    updateScope();
  }

  PubSub.subscribe('result.searched', init);

} ]);