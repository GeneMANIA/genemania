app.factory('$$networks', 
['$http',
function( $http ){

  var $$networks = function(){
    return $http.get( config.service.baseUrl + 'network_groups' )
      .then(function( res ){
        return res.data;
      })
    ;
  };

  return $$networks;

}]);