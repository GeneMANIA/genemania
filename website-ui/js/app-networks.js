app.factory('$$networks', 
['$http', 'util',
function( $http, util ){

  var cache;
  var strcmp = util.strcmp;

  function postprocessNetwork( net ){
    var meta = net.metadata;
    var authors = meta.authors.split(/\s*,\s*/);
    var sAuthors;

    if( authors.length === 0 ){
      sAuthors = '';
    } else if( authors.length < 2 ){
      sAuthors = authors[0];
    } else {
      sAuthors = authors[0] + ' et al';
    }

    meta.shortAuthors = sAuthors;
    meta.firstAuthor = authors[0];
    meta.lastAuthor = authors[ authors.length - 1 ];

    var mappedSourceName = config.networks.sourceName[ meta.source ];
    meta.sourceName = mappedSourceName ? mappedSourceName : 'unknown';

    meta.interactionCountFormatted = numeral( meta.interactionCount ).format('0,0');

    var tags = net.tags;
    for( var i = 0; i < tags.length; i++ ){
      var tag = tags[i];

      tag.nameFormatted = tag.name.toLowerCase();
    }

    tags.sort(function(a, b){
      var aName = a.nameFormatted;
      var bName = b.nameFormatted;

      return strcmp( aName, bName );
    });
  }

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