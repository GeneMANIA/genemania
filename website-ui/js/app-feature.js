app.factory('$$features', 
['$http',
function( $http ){

  var $$features = function(){
    return $http.get( config.service.baseUrl + 'feature_groups' )
      .then(function( res ){
        return res.data;
      })
    ;
  };

  return $$features;

}]);