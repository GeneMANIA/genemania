app.factory('$$attributes', 
['$http', 'util',
function( $http, util ){

  var cache;

  var $$attributes = window.$$attributes = function(){
    if( cache ){ return Promise.resolve(cache); }

    return util.nativePromise( $http.get(config.service.baseUrl + 'attribute_groups') )
      .then(function( res ){
        return res.data;
      })

      .then(function( attrs ){
        return cache = attrs;
      })
    ;
  };

  return $$attributes;

}]);