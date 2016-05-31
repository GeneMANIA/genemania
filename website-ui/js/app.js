'use strict';

var log = window.log = {
  ga: function( category, action, label ){
    if( DEBUG ){
      console.log('Log (`' + category + '`, `' + action + '`' + ( label != null ? ', `' + label + '`' : '' ) + ')');
    }

    var evt = {
      eventCategory: category,
      eventAction: action
    };

    if( _.isNumber( label ) && Math.round( label ) === label ){
      evt.eventValue = label;
    } else {
      evt.eventLabel = label;
    }

    setTimeout(function(){
      try {
        ga('send', 'event', evt);
      } catch( err ){}
    }, 0);
  },

  action: function( name, value ){
    this.ga( 'User actions', name, value );
  },

  query: function( name, value ){
    this.ga( 'Query parameters', name, value );
  }
};

// if templates not built, then define a dummy module
(function(){
  var templates;

  try {
    templates = angular.module('templates');
  } catch (e) {
    templates = angular.module('templates', []);
  }
})();

var app = window.app = angular.module('app', ['templates', 'pasvaz.bindonce', 'pragmatic-angular']);

PubSub.promise = function( topic ){
  return new Promise(function( resolve ){
    PubSub.subscribe(topic, function( arg ){
      resolve( arg );
    });
  });
};

(function(){
  // when all pieces of the ui are ready, then the app overall is ready
  var readyPromise = Promise.all([
    PubSub.promise('query.ready'),

    new Promise(function( resolve ){
      window.addEventListener('load', function(){ console.log('Window ready'); resolve(); });
    })
  ])
    .then(function(){
      console.log('GeneMANIA app ready');
      PubSub.publish('ready'); // all app ready
    })
  ;
})();

// because angularjs depends on this and it's not reliable
window.scrollTo = window.scrollTo || function(){};

app.factory('updateScope', [ '$timeout', '$rootScope', function( $timeout, $rootScope ){
  var lastUpdate;

  return function(){
    if( lastUpdate ){ return; clearTimeout(lastUpdate); }

    lastUpdate = setTimeout(function(){ lastUpdate = null; $rootScope.$apply(); }, 16);
  }

  function updateScope(){
    if( lastUpdate ){ $timeout.cancel(lastUpdate); }

    lastUpdate = $timeout(function(){ lastUpdate = null; }, 16);
  }

  return updateScope;
} ]);

app.controller('SplashCtrl', ['$scope', 'updateScope', 'Query', function( $scope, updateScope, Query ){

  PubSub.promise('query.ready').then(function(){
    $scope.ready = true;
    $scope.stats = Query.current.stats;

    updateScope();
  });

  PubSub.promise('query.search').then(function(){
    $scope.splashed = true;

    updateScope();
  });

} ]);
