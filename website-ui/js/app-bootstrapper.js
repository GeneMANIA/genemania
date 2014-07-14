app.factory('bootstrapper', [ function(){
  var promises = []; // to be fulfilled prior to bootstrap
  var $injector = angular.injector(['ng', 'app']);

  return function( promise ){
    promises.push( promise );
  };
} ]);

// $(function(){
//   angular.bootstrap(document, ['app']);
// });
