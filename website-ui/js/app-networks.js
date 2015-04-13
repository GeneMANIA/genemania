app.factory('$$networks', 
['$http', 'util', 
function( $http, util ){

  var cache;
  var strcmp = util.strcmp;
  var postprocessNetwork = config.networks.postprocess;

  var $$networks = window.$$networks = function(){
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
            var aName = a.name.toLowerCase();
            var bName = b.name.toLowerCase();

            if( aName < bName ){
              return -1;
            } else if( aName > bName ){
              return 1;
            }

            return 0;
          });

          for( var i = 0; i < netGrs.length; i++ ){
            var gr = netGrs[i];
            var nets = gr.interactionNetworks;

            if( nets ){ for( var j = 0; j < nets.length; j++ ){
              var net = nets[j];

              postprocessNetwork( net );
            } }
          }
        }

        return cache = orgNetGrs;
      })
    ;
  };

  return $$networks;

}]);