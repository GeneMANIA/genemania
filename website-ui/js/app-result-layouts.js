app.factory('Result_layouts',
[ 'util',
function( util ){ return function( Result ){

  var r = Result;
  var rfn = r.prototype;
  
  var defaultPadding = 50;

  var sortByWeight = function(a, b){
    return b.data('score') - a.data('score');
  };

  rfn.layoutDelay = function( layout ){
    var self = this;
    var container = cy.container();
    
    if( self.layoutPromise ){
      self.layoutPromise.cancel();
    }

    if( self.cyLayout ){
      cy.elements().stop( true ); // because https://github.com/cytoscape/cytoscape.js/issues/983
      
      self.cyLayout.stop();
      self.cyLayout = null;
    }
    
    setTimeout(function(){
      self.cyLayout = layout;
      
      if( self.networksExpanded ){
        container.classList.add('cy-layouting-shift');
      }
      
      if( self.query.historyExpanded ){
        container.classList.add('cy-layouting-shift-history');
      }
      
      layout.run();
    }, 100);
  };

  rfn.layoutPrepost = function( layout ){
    var self = this;

    // TODO layout can still get bad size
    // race condition: classes removed after added from prev layout

    return self.layoutPromise = new Promise(function(resolve){      
      layout.one('layoutstop', function(){
        resolve();
      });
    }).then(function(){
      var cl = container.classList;
      
      cl.remove('cy-layouting-shift');
      cl.remove('cy-layouting-shift-history');
    }).cancellable();
  };

  rfn.circleLayout = function(options){
    options = $.extend({
      animate: true
    }, options);

    var l = cy.makeLayout({
      name: 'concentric',
      animate: options.animate,
      animationDuration: 500,
      concentric: function(){
        var isQuery = this.data('query');
        var score = this.data('score') || 0;
        
        return (isQuery ? 100 : 0) + score;
      },
      levelWidth: function(){
        return 1;
      },
      sort: sortByWeight,
      padding: defaultPadding
    });

    var p = this.layoutPrepost( l );

    this.layoutDelay(l);

    return p;
  };

  rfn.forceLayout = function(options){
    options = $.extend({
      randomize: true,
      animate: true,
      maxSimulationTime: 2000,
      padding: defaultPadding
    }, options);

    var layoutEles = cy.elements().stdFilter(function( ele ){
      return ele.isNode() || !ele.hasClass('filtered');
    });
    
    // var layoutElesWoCoexp = layoutEles.stdFilter(function( ele ){
    //   return ele.data('group') !== 'coexp';
    // });
    // 
    // if( layoutElesWoCoexp.edges().length === 0 ){
    //   // then keep coexp edges b/c we need some edges
    // } else {
    //   layoutEles = layoutElesWoCoexp;
    // }

    var avgW = 0;
    for( var i = 0; i < layoutEles.length; i++ ){
      avgW += layoutEles.data('weight');
    }
    avgW /= layoutEles.length;
    
    var minLength = 10;
    var maxLength = 9999999;

    var edgeLength = function( e ){
      function length(e){
        var w = e.data('weight');
        
        if( w == null ){
          w = avgW;
        }
        
        var l = layoutEles.length / 8 / w; // as w => inf, l => 0
        // TODO revise equation
        
        return l;
      }
      
      if( e.data('group') === 'coexp' ){
        return 10 * length(e);
      }
      
      var l = length(e);
      
      if( l < minLength ){
        return minLength;
      } else if( l > maxLength ){
        return maxLength;
      } else {
        return l;
      }
    };

    var l = layoutEles.makeLayout({
      name: 'cola',
      animate: options.animate,
      randomize: options.randomize,
      maxSimulationTime: options.maxSimulationTime,
      edgeLength: edgeLength,
      padding: defaultPadding 
    });
    
    var p = this.layoutPrepost( l );

    this.layoutDelay(l);
    
    return p;
  };

  rfn.linearLayout = function(options){
    options = $.extend({
      animate: true
    }, options);

    var l = cy.makeLayout({
      name: 'grid',
      animate: options.animate,
      animationDuration: 500,
      columns: 1,
      position: function(n){
        return {
          col: n.data('query') ? 0 : 1
        }
      },
      sort: sortByWeight,
      padding: defaultPadding
    });
    
    var p = this.layoutPrepost( l );

    this.layoutDelay(l);

    return p;
  };

  rfn.fitGraph = function( options ){
    options = $.extend({
      animate: true
    }, options);

    var l = cy.makeLayout({
      name: 'preset',
      fit: !options.animate,
      padding: defaultPadding
    });
    
    if( options.animate ){
      cy.animate({
        fit: {
          eles: cy.elements(),
          padding: defaultPadding
        }
      }, {
        duration: 500
      });
    }

    var p = this.layoutPrepost( l );

    this.layoutDelay(l);

    return p;
  };


} } ]);
