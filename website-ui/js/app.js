// if templates not built, then define a dummy module
(function(){
  var templates;

  try {
    templates = angular.module('templates');
  } catch (e) {
    templates = angular.module('templates', []);
  }
})();

var app = angular.module('app', ['templates', 'pasvaz.bindonce']);

(function(){
  // promise from subscription
  function subscribe( topic ){
    return new Promise(function( resolve ){
      PubSub.subscribe(topic, function(){
        resolve();
      });
    });
  }

  // when all pieces of the ui are ready, then the app overall is ready
  Promise.all([
    subscribe('query.ready')
  ])
    .then(function(){
      console.log('app ready')
      PubSub.publish('ready'); // all app ready
    })
  ;
})();

app.controller('Test', 
[ '$$networks', '$$organisms', '$$attributes', '$$email', '$$features', '$$genes', 'Query',
function( $$networks, $$organisms, $$attributes, $$email, $$features, $$genes, Query ){

  console.log('running test ctrl');

  window.$$networks = $$networks;
  window.$$organisms = $$organisms;
  window.$$attributes = $$attributes;
  window.$$email = $$email;
  window.$$features = $$features;
  window.$$genes = $$genes;
} ]);