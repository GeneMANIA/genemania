'use strict';

app.factory('$$stats',
['$http', 'util',
function( $http, util ){

  var $$stats = window.$$stats = function( opts ){
    return util.nativePromise( $http.get( config.service.baseUrl + 'stats', opts )
      .then(function( res ){
        var stats = res.data;

        stats.networksFormatted = numeral( stats.networks ).format('000,000,000,000');
        stats.interactionsFormatted = numeral( stats.interactions ).format('000,000,000,000');
        stats.genesFormatted = numeral( stats.genes ).format('000,000,000,000');
        stats.organismsFormatted = numeral( stats.organisms ).format('000,000,000,000');

        return stats;
      })
    );
  };

  return $$stats;

}]);
