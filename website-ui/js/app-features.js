'use strict';

app.factory('$$features', 
['$http', 'util',
function( $http, util ){

  var cache;

  var $$features = window.$$features = function(){
    if( cache ){ return Promise.resolve(cache); }

    return util.nativePromise( $http.get( config.service.baseUrl + 'feature_groups') )
      .then(function( res ){
        return res.data;
      })

      .then(function(features){
        return cache = features;
      })
    ;
  };

  return $$features;

}]);
