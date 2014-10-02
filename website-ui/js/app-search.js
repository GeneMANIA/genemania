app.factory('$$search', 
['$http', 'util',
function( $http, util ){

  // organism : id of org
  // genes : newline separated list of genes
  // weighting : network weighting scheme
  // geneThreshold : max number of genes to return
  // attrThreshold : max number of attributes to return
  // networks : array of network ids to use in search
  // attrGroups : array of attribute group ids to use in search
  var $$search = window.$$search = function( opts ){
    return util.nativePromise( $http.post(config.service.baseUrl + 'search_results', opts)
      .then(function( res ){
        return res.data;
      })
    );
  };

  return $$search;

}]);