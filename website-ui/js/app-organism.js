app.factory('$$organisms', 
['$http',
function( $http ){

  var $$organisms = function(){
    return $http.get( config.service.baseUrl + 'organisms' )
      .then(function( res ){
        return res.data;
      })
    ;
  };

  return $$organisms;

}]);