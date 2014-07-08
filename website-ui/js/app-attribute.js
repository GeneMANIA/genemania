app.factory('$$attributes', 
['$http',
function( $http ){

  var $$attributes = function(){
    return $http.get( config.service.baseUrl + 'attribute_groups' )
      .then(function( res ){
        return res.data;
      })
    ;
  };

  return $$attributes;

}]);