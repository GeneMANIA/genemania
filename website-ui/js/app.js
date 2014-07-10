// if templates not built, then define a dummy module
var templates;
try {
  templates = angular.module('templates');
} catch (e) {
  templates = angular.module('templates', []);
}

var app = angular.module('app', ['templates', 'pasvaz.bindonce']);

app.controller('Test', 
[ '$$networks', '$$organisms', '$$attributes', '$$email', '$$features', '$$genes',
function( $$networks, $$organisms, $$attributes, $$email, $$features, $$genes ){

  window.$$networks = $$networks;
  window.$$organisms = $$organisms;
  window.$$attributes = $$attributes;
  window.$$email = $$email;
  window.$$features = $$features;
  window.$$genes = $$genes;
} ]);