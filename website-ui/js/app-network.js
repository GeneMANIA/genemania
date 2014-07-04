app.factory('$$networks', 
['$http',
function( $http ){

  var $$networks = function(){
    return $http.get( config.service.baseUrl + 'network_groups/1' )
      .then(function( json ){
        return json;
      })
    ;
  };

  return $$networks;

}]);