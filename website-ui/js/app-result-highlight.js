'use strict';

app.factory('Result_highlight',
[ 'util',
function( util ){ return function( Result ){

  var r = Result;
  var rfn = r.prototype;

  rfn.rateLimitedHighlightFn = function( fn, immediate ){

    if( immediate ){
      clearTimeout( this.rlhTimeout );

      rlhTimeout = null;

      fn();
    } else {
      clearTimeout( this.rlhTimeout );

      var rlhTimeout = this.rlhTimeout = setTimeout(function(){
        rlhTimeout = null;

        fn();
      }, 150);
    }

  };

  rfn.rateLimitedHighlight = function(opts){
    var self = this;

    self.rateLimitedHighlightFn(function(){
      self.highlight( opts );
    });
  };

  rfn.rateLimitedUnhighlight = function(opts){
    var self = this;

    self.rateLimitedHighlightFn(function(){
      self.unhighlight( opts );
    }, true);
  };

  rfn.highlight = function(opts){
    opts = opts || {};

    var self = this;
    var hl = this.getHighlights();

    function addToIdList( optIds, list, eachId ){
      hl[ list ] = []; // start empty

      for( var i = 0; i < optIds.length; i++ ){
        var id = optIds[i];

        hl[ list ].push( id );

        if( eachId ){
          eachId( id );
        }
      }
    }

    if( opts.genes ){
      addToIdList( opts.genes, 'genes' );
    }

    if( opts.interactions ){
      addToIdList( opts.interactions, 'interactions' );
    }

    if( opts.attrs ){
      addToIdList( opts.attrs, 'attrs' );
    }

    if( opts.attributeGroups ){
      addToIdList( opts.attributeGroups, 'attributeGroups' );
    }

    if( opts.networkGroups ){
      addToIdList( opts.networkGroups, 'networkGroups' );
    }

    if( opts.networks ){
      addToIdList( opts.networks, 'networks' );
    }

    this.updateHighlights();
  };

  rfn.unhighlight = function(opts){
    opts = opts || {};

    var self = this;
    var hl = this.getHighlights();

    function removeFromIdList( optIds, list, eachId ){
      var removeId = {};

      if( optIds === '*' ){
        if( eachId ){ for( var i = 0; i < hl[ list ].length; i++ ){
          eachId( hl[ list ][i] );
        } }

        hl[ list ] = [];
      } else {

        for( var i = 0; i < optIds.length; i++ ){
          id = optIds[i];

          removeId[ id ] = true;

          if( eachId ){
            eachId( id );
          }
        }

        hl[ list ] = hl[ list ].filter(function(id){
          return !removeId[ id ];
        });
      }

    }

    if( opts.genes ){
      removeFromIdList( opts.genes, 'genes' );
    }

    if( opts.interactions ){
      removeFromIdList( opts.interactions, 'interactions' );
    }

    if( opts.attrs ){
      removeFromIdList( opts.attrs, 'attrs' );
    }

    if( opts.attributeGroups ){
      removeFromIdList( opts.attributeGroups, 'attributeGroups', function(id){
        self.resultAttributeGroups.filter(function( rGr ){
          return rGr.attributeGroup.id === id;
        })[0].highlighted = false;
      } );
    }

    if( opts.networkGroups ){
      removeFromIdList( opts.networkGroups, 'networkGroups', function(id){
        self.resultNetworkGroups.filter(function( rGr ){
          return rGr.networkGroup.id === id;
        })[0].highlighted = false;
      } );
    }

    if( opts.networks ){
      removeFromIdList( opts.networks, 'networks', function(id){
        self.resultNetworksById[ id ].highlighted = false;
      } );
    }

    this.updateHighlights();
  };

  rfn.getHighlights = function(){
    return this.highlights = this.highlights || {
      genes: [],
      interactions: [],
      attrs: [],
      attributeGroups: [],
      networkGroups: [],
      networks: []
    };
  };

  rfn.updateHighlights = function(){
    var self = this;
    var hl = this.getHighlights();

    var toHl = cy.collection();
    var toUnhl = cy.collection();

    var highlight = function( eles ){
      toHl = toHl.add( eles );
      toUnhl = toUnhl.not( eles );
    };

    var unhighlight = function( eles ){
      toHl = toHl.not( eles );
      toUnhl = toUnhl.add( eles );
    };

    var normlight = function( eles ){
      toUnhl = toUnhl.not( eles );
    };

    cy.batch(function(){

      hl.active = false;

      var initted = false;
      function initAllUnhighlighted(){
        if( initted ){ return; }

        unhighlight( cy.elements() );

        initted = true;

        hl.active = true;
      }

      //
      // networks

      if( hl.genes.length > 0 ){
        initAllUnhighlighted();

        var nodes = cy.nodes( hl.genes.map(function(id){ return '#' + id; }).join(', ') );

        highlight( nodes );
        normlight( nodes.neighborhood() );
      }

      //
      // interactions

      if( hl.interactions.length > 0 ){
        initAllUnhighlighted();

        var edges = cy.edges( hl.interactions.map(function(id){ return '[rIntnId = ' + id + ']'; }).join(', ') );

        highlight( edges );
        normlight( edges.connectedNodes() );
      }

      //
      // attrs

      if( hl.attrs.length > 0 ){
        initAllUnhighlighted();

        var nodes = cy.nodes( hl.attrs.map(function(id){ return '#' + id; }).join(', ') );

        highlight( nodes );
        normlight( nodes.neighborhood() );
      }

      //
      // network groups

      if( hl.networkGroups.length > 0 ){
        initAllUnhighlighted();

        var edges = cy.edges( hl.networkGroups.map(function(id){ return '[networkGroupId = ' + id + ']'; }).join(', ') );

        highlight( edges );
        normlight( edges.connectedNodes() );

      }

      var hasId = {};
      for( var i = 0; i < hl.networkGroups.length; i++ ){
        var id = hl.networkGroups[i];

        hasId[ id ] = true;
      }

      var rGrs = self.resultNetworkGroups;
      for( var i = 0; i < rGrs.length; i++ ){
        var rGr = rGrs[i];

        rGr.highlighted = hasId[ rGr.networkGroup.id ];
      }

      //
      // attr groups

      if( hl.attributeGroups.length > 0 ){
        initAllUnhighlighted();

        var edges = cy.edges( hl.attributeGroups.map(function(id){ return '[attributeGroupId = ' + id + ']'; }).join(', ') );

        highlight( edges );
        normlight( edges.connectedNodes() );

      }

      var hasId = {};
      for( var i = 0; i < hl.attributeGroups.length; i++ ){
        var id = hl.attributeGroups[i];

        hasId[ id ] = true;
      }

      var rGrs = self.resultAttributeGroups;
      for( var i = 0; i < rGrs.length; i++ ){
        var rGr = rGrs[i];

        rGr.highlighted = hasId[ rGr.attributeGroup.id ];
      }

      //
      // networks

      if( hl.networks.length > 0 ){
        initAllUnhighlighted();

        var edges = cy.edges( hl.networks.map(function(id){ return '[networkId = ' + id + ']'; }).join(', ') );

        highlight( edges );
        normlight( edges.connectedNodes() );
      }

      var hasId = {};
      for( var i = 0; i < hl.networks.length; i++ ){
        var id = hl.networks[i];

        hasId[ id ] = true;
      }

      var rNets = self.resultNetworks;
      for( var i = 0; i < rNets.length; i++ ){
        var rNet = rNets[i];

        rNet.highlighted = hasId[ rNet.network.id ];
      }

      //
      // apply highlights
      var eles = cy.elements();

      eles.not( toHl ).removeClass('highlighted');
      eles.not( toUnhl ).removeClass('unhighlighted');

      toHl.removeClass('unhighlighted').addClass('highlighted');
      toUnhl.removeClass('highlighted').addClass('unhighlighted');

    });

    PubSub.publish('result.updateHighlights');
  };

} } ]);
