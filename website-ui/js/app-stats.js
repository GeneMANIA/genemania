'use strict';

app.factory('$$stats',
['$http', 'util', '$$resources',
function( $http, util, $$resources ){

  var $$stats = window.$$stats = function( opts ){
    return $$resources()
      .then(function( res ){
        var stats = res.stats;

        stats.networksFormatted = numeral( stats.networks ).format('000,000,000,000');
        stats.interactionsFormatted = numeral( stats.interactions ).format('000,000,000,000');
        stats.genesFormatted = numeral( stats.genes ).format('000,000,000,000');
        stats.organismsFormatted = numeral( stats.organisms ).format('000,000,000,000');

        return stats;
      })
    ;
  };

  return $$stats;

}]);
