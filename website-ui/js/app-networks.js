'use strict';

app.factory('$$networks',
['$http', 'util', '$$resources',
function( $http, util, $$resources ){

  var cache;
  var strcmp = util.strcmp;
  var postprocessNetwork = config.networks.postprocess;

  var $$networks = window.$$networks = function(){
    if( cache ){ return Promise.resolve(cache); }

    return $$resources().then(function( resources ){
      return resources.networkGroups;
    }).then(function( orgNetGrs ){

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
    });
  };

  $$networks.add = function( opts ){
    return $$user.read().then(function( user ){
      return util.nativePromise( $.ajax({
        url: config.service.baseUrl + 'upload_network',
        type: "POST",
        data: { organism_id: opts.organismId, file: opts.file, file_name: opts.fileName, session_id: user.localId },
        dataType: "json"
      }) );
    }).then(function( res ){
      if( res.error ){
        return Promise.reject( res.error );
      } else {
        return res.network;
      }
    });
  };

  $$networks.testAdd = function(){
    return util.nativePromise(
      $.get('http://localhost:8888/genemania/website/src/main/webapp/test/Networks/TxtNetworks/Human-small.txt'
    )).then(function( file ){
      return $$networks.add({
        organismId: 4,
        file: file,
        fileName: 'Human-small.txt'
      });
    }).then(function( res ){
      console.log(res);

      return res;
    });
  };

  $$networks.remove = function( opts ){
    return $$user.read().then(function( user ){
      return util.nativePromise( $.ajax({
        url: config.service.baseUrl + 'delete_network',
        type: "POST",
        data: { organism_id: opts.organismId, network_id: opts.networkId, session_id: user.localId },
        dataType: "json"
      }) );
    }).then(function( res ){
      if( res.error ){
        return Promise.reject( res.error );
      } else {
        return res.network;
      }
    });
  };

  return $$networks;

}]);
