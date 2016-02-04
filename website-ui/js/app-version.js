'use strict';

app.factory('$$version',
['$http', 'util', '$$resources',
function( $http, util, $$resources ){

  var $$version = window.$$version = function( opts ){
    return $$resources()
      .then(function( res ){
        return res.versions;
      })
    ;
  };

  return $$version;

}]);
