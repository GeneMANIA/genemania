'use strict';

app.factory('$$user',
['$http', 'util', 'io',
function( $http, util, io ){

  var $$user = window.$$user = io('user', {
    localId: 'local-user-' + uuid.v4()
  });

  // make sure the default user data is saved
  $$user.read().then(function(){
    return $$user.write();
  });

  return $$user;

}]);
