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
      console.log('app ready')
      PubSub.publish('ready'); // all app ready
    })
  ;
})();

app.controller('Test', 
[ '$$networks', '$$organisms', '$$attributes', '$$email', '$$features', '$$genes', 'Query', '$scope',
function( $$networks, $$organisms, $$attributes, $$email, $$features, $$genes, Query, $scope ){

  console.log('running test ctrl');

  window.$$networks = $$networks;
  window.$$organisms = $$organisms;
  window.$$attributes = $$attributes;
  window.$$email = $$email;
  window.$$features = $$features;
  window.$$genes = $$genes;

  $scope.foo = 'init';

  $scope.bar = function(){
    $scope.foo = 'bar';
  };
} ]);