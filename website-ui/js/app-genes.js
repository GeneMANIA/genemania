'use strict';

app.factory('$$genes', 
['$http', 'util',
function( $http, util ){

  var $$genes = window.$$genes = {};

  // organism : id of organism
  // genes : newline separated string of genes
  $$genes.validate = function( opts ){
    return util.nativePromise( $http.post( config.service.baseUrl + 'gene_validation', opts )
      .then(function( res ){
        return res.data;
      })
      // .then(function(t){
      //   return util.timeoutPromise(t, 3000);
      // })
    );
  };

  return $$genes;

}]);
