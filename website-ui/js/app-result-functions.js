app.factory('Result_functions',
[ 'util',
function( util ){ return function( Result ){

  var r = Result;
  var rfn = r.prototype;

  rfn.toggleFunctionColoring = function( fn ){
    if( fn.colored ){
      this.removeFunctionColoring( fn );
    } else {
      this.addFunctionColoring( fn );
    }
  };

  rfn.addFunctionColoring = function( fn ){
    if( this.functionColors.length === 0 ){ return; }

    fn.colored = true;
    fn.color = this.functionColors.shift();

    this.coloringFunctions.push( fn );

    this.updateFunctionStylesheet();

    PubSub.publish('result.addFunctionColoring', {
      function: fn,
      result: this
    });
  };

  rfn.removeFunctionColoring = function( fn ){
    var cfns = this.coloringFunctions;

    for( var i = 0; i < cfns.length; i++ ){
      var cfn = cfns[i];

      if( cfn.id === fn.id ){
        fn.colored = false;
        this.functionColors.push( fn.color );
        cfns.splice(i, 1);

        break;
      }
    }

    this.updateFunctionStylesheet();

    PubSub.publish('result.removeFunctionColoring', {
      function: fn,
      result: this
    });
  };

  rfn.updateFunctionStylesheet = function(){
    var stylesheet = cyStylesheet(cy);
    var style = cy.style().fromJson( stylesheet );

    var cfns = this.coloringFunctions;

    for( var i = 0; i < cfns.length; i++ ){
      var cfn = cfns[i];
      var selector = '.fn' + cfn.id;
      var css = {};
      var p = i + 1;

      css['pie-'+p+'-background-opacity'] = 0;
      css['pie-'+p+'-background-size'] = 100/cfns.length;
      css['pie-'+p+'-background-color'] = cfn.color;

      style.selector('node').css( css );

      css['pie-'+p+'-background-opacity'] = 0.4;
      css['pie-'+p+'-background-size'] = 100/cfns.length;
      css['pie-'+p+'-background-color'] = cfn.color;

      style.selector( selector ).css( css );
    }

    style.update();
  };


} } ]);


app.controller('FunctionsCtrl',
[ '$scope', 'updateScope', 'cy',
function( $scope, updateScope, cy ){

  function init(){
    $scope.query = Query.current;
    $scope.result = $scope.query.result;
    $scope.addedFunctions = $scope.result.coloringFunctions;
    $scope.functions = $scope.result.resultOntologyCategories;

    updateScope();
  }

  PubSub.subscribe('result.searched', init);
  PubSub.subscribe('result.addFunctionColoring', updateScope);
  PubSub.subscribe('result.removeFunctionColoring', updateScope);

} ]);
