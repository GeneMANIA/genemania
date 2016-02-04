'use strict';

app.factory('$$resources',
['$http', 'util', '$$user',
function( $http, util, $$user ){

  var cache;

  var $$resources = window.$$resources = function(){
    if( cache ){ return cache; }

    return cache = $$user.read().then(function( user ){
      return util.nativePromise(
        $http.get(config.service.baseUrl + 'resources?session_id=' + user.localId)
      ).then(function( res ){
        return res.data;
      });
    });
  };

  return $$resources;

}]);
