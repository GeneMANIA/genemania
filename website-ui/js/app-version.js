app.factory('$$version', 
['$http', 'util',
function( $http, util ){

  var $$version = window.$$version = function( opts ){
    return util.nativePromise( $http.get( config.service.baseUrl + 'version', opts )
      .then(function( res ){
        return res.data;
      })
      // .then(function(t){
      //   return util.timeoutPromise(t, 3000);
      // })
    );
  };

  return $$version;

}]);