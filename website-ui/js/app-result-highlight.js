app.factory('Result_highlight',
[ 'util',
function( util ){ return function( Result ){

  var r = Result;
  var rfn = r.prototype;
  
  rfn.rateLimitedHighlightFn = _.debounce( function(fn){
    fn();
  }, 150 );
  
  rfn.rateLimitedHighlight = function(opts){
    var self = this; 
    
    self.rateLimitedHighlightFn(function(){
      self.highlight( opts );
    });
  };
  
  rfn.rateLimitedUnhighlight = function(opts){
    var self = this;
    
    // self.rateLimitedHighlightFn(function(){
      self.unhighlight( opts );
    // });
  };
  
  rfn.highlight = function(opts){
    opts = opts || {};
    
    var self = this;
    var hl = this.getHighlights();
    
    function addToIdList( optIds, list, eachId ){
      hl[ list ] = []; // start empty
      
      for( var i = 0; i < optIds.length; i++ ){
        id = optIds[i];
        
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
    
    if( opts.attributes ){
      addToIdList( opts.attributes, 'attributes' );
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
    
    if( opts.attributes ){
      removeFromIdList( opts.attributes, 'attributes' );
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
      attributes: [],
      networkGroups: [],
      networks: []
    };
  };
  
  rfn.updateHighlights = function(){
    var self = this;
    var hl = this.getHighlights();
    
    cy.batch(function(){
      
      // start clear
      cy.elements().removeClass('unhighlighted').removeClass('highlighted');
      hl.active = false;
      
      var initted = false;
      function initAllUnhighlighted(){
        if( initted ){ return; }
        
        cy.elements().addClass('unhighlighted').removeClass('highlighted');
        
        initted = true;
        
        hl.active = true;
      }
      
      //
      // networks
      
      if( hl.genes.length > 0 ){
        initAllUnhighlighted();
        
        cy.nodes( hl.genes.map(function(id){ return '#' + id; }).join(', ') )
          .removeClass('unhighlighted')
          .addClass('highlighted')
          .neighborhood().removeClass('unhighlighted')
        ;
      }
      
      //
      // interactions
      
      if( hl.interactions.length > 0 ){
        initAllUnhighlighted();
        
        cy.edges( hl.interactions.map(function(id){ return '[rIntnId = ' + id + ']'; }).join(', ') )
          .removeClass('unhighlighted')
          .addClass('highlighted')
          .connectedNodes().removeClass('unhighlighted')
        ;
      }
      
      //
      // attrs
      
      if( hl.attributes.length > 0 ){
        initAllUnhighlighted();
        
        cy.nodes( hl.attributes.map(function(id){ return '#' + id; }).join(', ') )
          .removeClass('unhighlighted')
          .addClass('highlighted')
          .neighborhood().removeClass('unhighlighted')
        ;
      }
      
      //
      // network groups
      
      if( hl.networkGroups.length > 0 ){
        initAllUnhighlighted();
        
        cy.edges( hl.networkGroups.map(function(id){ return '[networkGroupId = ' + id + ']'; }).join(', ') )
          .removeClass('unhighlighted')
          .addClass('highlighted')
          .connectedNodes().removeClass('unhighlighted')
        ;
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
      // networks
      
      if( hl.networks.length > 0 ){
        initAllUnhighlighted();
        
        cy.edges( hl.networks.map(function(id){ return '[networkId = ' + id + ']'; }).join(', ') )
          .removeClass('unhighlighted')
          .addClass('highlighted')
          .connectedNodes().removeClass('unhighlighted')
        ;
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

    });
    
    PubSub.publish('result.updateHighlights');
  };

} } ]);
