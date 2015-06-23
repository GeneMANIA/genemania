app.factory('Result_highlight',
[ 'util',
function( util ){ return function( Result ){

  var r = Result;
  var rfn = r.prototype;
  
  rfn.highlight = function(opts){
    opts = opts || {};
    
    var hl = this.getHighlights();
    
    if( opts.genes ){
      for( var i = 0; i < opts.genes.length; i++ ){
        geneId = opts.genes[i];
        
        hl.genes.push( geneId );
      }
    }
    
    if( opts.networkGroup ){
      
    }
    
    if( opts.network ){
      
    }
    
    this.updateHighlights();
  };

  rfn.unhighlight = function(opts){
    opts = opts || {};
    
    var hl = this.getHighlights();
    
    if( opts.genes === '*' ){
      hl.genes = [];
      
    } else if( opts.genes ){
      var removeId = {};
      
      for( var i = 0; i < opts.genes.length; i++ ){
        geneId = opts.genes[i];
        
        removeId[ geneId ] = true;
      }
      
      hl.genes = hl.genes.filter(function(id){
        return !removeId[ id ];
      });
    }
    
    if( opts.networkGroup ){
      
    }
    
    if( opts.network ){
      
    }
    
    this.updateHighlights();
  };
  
  rfn.getHighlights = function(){
    return this.highlights = this.highlights || {
      genes: [],
      networkGroup: [],
      network: [],
      attribute: []
    };
  };
  
  rfn.updateHighlights = function(){
    var hl = this.getHighlights();
    
    cy.batch(function(){
      
      cy.elements().addClass('unhighlighted').removeClass('highlighted');
      
      if( hl.genes.length === 0 ){
        cy.elements().removeClass('unhighlighted');
      } else {
        cy.nodes( hl.genes.map(function(id){ return '#' + id; }).join(', ') )
          .removeClass('unhighlighted')
          .addClass('highlighted')
          .neighborhood().removeClass('unhighlighted')
        ;
      }
      
      
      
    });
  };

} } ]);
