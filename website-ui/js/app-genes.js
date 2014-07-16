app.factory('$$genes', 
['$http',
function( $http ){

  var $$genes = {};

  // organism : id of organism
  // genes : newline separated string of genes
  $$genes.validate = function( opts ){
    return $http.post( config.service.baseUrl + 'gene_validation', opts )
      .then(function( res ){
        return res.data;
      })
    ;
  };

  return $$genes;

}]);