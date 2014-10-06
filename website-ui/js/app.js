// if templates not built, then define a dummy module
(function(){
  var templates;

  try {
    templates = angular.module('templates');
  } catch (e) {
    templates = angular.module('templates', []);
  }
})();

var app = angular.module('app', ['templates', 'pasvaz.bindonce', 'pragmatic-angular']);

PubSub.promise = function( topic ){
  return new Promise(function( resolve ){
    PubSub.subscribe(topic, function( arg ){
      resolve( arg );
    });
  });
};

(function(){
  // when all pieces of the ui are ready, then the app overall is ready
  Promise.all([
    PubSub.promise('query.ready')
  ])
    .then(function(){
      console.log('GeneMANIA app ready')
      PubSub.publish('ready'); // all app ready
    })
  ;
})();

// because angularjs depends on this and it's not reliable
window.scrollTo = window.scrollTo || function(){};

app.controller('SplashCtrl', ['$scope', '$timeout', function( $scope, $timeout ){

  function applyScope(){
    $timeout(function(){}, 0);
  }

  PubSub.promise('query.ready').then(function(){
    $scope.ready = true;

    applyScope();
  });

  PubSub.promise('query.search').then(function(){
    $scope.splashed = true;

    applyScope();
  });

} ]);