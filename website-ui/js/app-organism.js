app.factory('$$organisms', 
['$http',
function( $http ){

  var $$organisms = function(){
    return $http.get( config.service.baseUrl + 'organisms', {
      cache: true
    } )
      .then(function( res ){
        return res.data;
      })
    ;
  };

  return $$organisms;

}]);