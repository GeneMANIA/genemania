app.factory('$$networks', 
['$http', 'util',
function( $http, util ){

  var cache;

  var $$networks = function(){
    if( cache ){ return Promise.resolve(cache); }

    return util.nativePromise( $http.get(config.service.baseUrl + 'network_groups') )
      .then(function( res ){
        return res.data;
      })

      .then(function( orgNetGrs ){

        for( var orgId in orgNetGrs ){
          var netGrs = orgNetGrs[ orgId ];

          // sort network groups by name
          netGrs.sort(function(a, b){
            if( a.name < b.name ){
              return -1;
            } else if( a.name > b.name ){
              return 1;
            }

            return 0;
          });
        }

        return cache = orgNetGrs;
      })
    ;
  };

  return $$networks;

}]);