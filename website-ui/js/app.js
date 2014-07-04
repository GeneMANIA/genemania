// if templates not built, then define a dummy module
var templates;
try {
  templates = angular.module('templates');
} catch (e) {
  templates = angular.module('templates', []);
}

var app = angular.module('app', ['templates', 'pasvaz.bindonce']);

app.controller('Test', [ '$$networks', function( $$networks ){
  window.$$networks = $$networks;
} ]);