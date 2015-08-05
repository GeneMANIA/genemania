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
    var cfns = this.coloringFunctions;
    var nodes = cy.nodes();
    
    cy.batch(function(){ 
      //debugger;

      for( var j = 0; j < nodes.length; j++ ){
        var node = nodes[j];
        var css = node.data('css');
        
        for( var i = 0; i < 16; i++ ){
          var cfn = cfns[i];
          var p = i + 1;
          
          if( cfn ){
            var cls = 'fn' + cfn.id;
          
            css['pie_'+p+'_background_size'] = 100/cfns.length;
            css['pie_'+p+'_background_color'] = cfn.color;
            css['pie_'+p+'_background_opacity'] = node.hasClass(cls) ? 0.4 : 0;
          } else {
            css['pie_'+p+'_background_size'] = 0;
            css['pie_'+p+'_background_color'] = '#000';
            css['pie_'+p+'_background_opacity'] = 0;
          }
        }
      
        node.data( 'css', css );
      }
      
    });

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
