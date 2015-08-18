'use strict';

app.factory('Result_layouts',
[ 'util',
function( util ){ return function( Result ){

  var r = Result;
  var rfn = r.prototype;

  var defaultPadding = 50;

  var sortByWeight = function(a, b){
    return b.data('score') - a.data('score');
  };

  rfn.layoutResizeCyPre = function(){
    var self = this;
    var container = cy.container();

    if( self.networksExpanded ){
      container.classList.add('cy-layouting-shift');
    }

    if( self.query.historyExpanded ){
      container.classList.add('cy-layouting-shift-history');
    }
  };

  rfn.layoutResizeCyPost = function(){
    var self = this;
    var container = cy.container();

    if( self.networksExpanded ){
      container.classList.remove('cy-layouting-shift');
    }

    if( self.query.historyExpanded ){
      container.classList.remove('cy-layouting-shift-history');
    }
  };

  rfn.layoutDelay = function( layout, options ){
    var self = this;
    var container = cy.container();

    if( self.cyLayout ){
      cy.elements().stop( true ); // because https://github.com/cytoscape/cytoscape.js/issues/983

      self.cyLayout.stop();
      self.cyLayout = null;
    }

    setTimeout(function(){
      self.cyLayout = layout;

      if( self.networksExpanded && options.resizeCy ){
        container.classList.add('cy-layouting-shift');
      }

      if( self.query.historyExpanded && options.resizeCy ){
        container.classList.add('cy-layouting-shift-history');
      }

      layout.run();
    }, 0);
  };

  rfn.layoutPrepost = function( layout, options ){
    var self = this;
    var container = cy.container();

    if( self.layoutPromise ){
      self.layoutPromise.cancel();
    }

    return self.layoutPromise = new Promise(function(resolve){
      layout.one('layoutstop', function(){
        resolve();
      });
    }).then(function(){
      var cl = container.classList;

      if( options.resizeCy ){
        cl.remove('cy-layouting-shift');
        cl.remove('cy-layouting-shift-history');
      }
    }).cancellable();
  };

  rfn.circleLayout = function(options){
    options = $.extend({
      animate: true,
      resizeCy: true
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

    var p = this.layoutPrepost( l, options );

    this.layoutDelay(l, options);

    return p;
  };

  rfn.forceLayout = function(options){
    options = $.extend({
      fit: true,
      randomize: true,
      animate: true,
      nodeSpacing: 15,
      lengthFactor: 75,
      maxSimulationTime: 2000,
      padding: defaultPadding,
      resizeCy: true
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
    var minW = Infinity;
    var maxW = -Infinity;

    var layoutEdges = layoutEles.edges();

    for( var i = 0; i < layoutEdges.length; i++ ){
      var ele = layoutEdges[i];
      var w = ele.data('weight');

      avgW += w;

      minW = Math.min( w, minW );
      maxW = Math.max( w, maxW );
    }

    avgW /= layoutEdges.length;

    var norm = function( w ){
      return (w - minW) / (maxW - minW) * 9 + 1; // ranges on (1, 10)
    };

    for( var i = 0; i < layoutEdges.length; i++ ){
      var ele = layoutEdges[i];
      var w = ele.data('weight');

      ele.data( 'normWeight', norm(w) );
    }

    var avgWNorm = norm( avgW );
    var minLength = 0;
    var maxLength = 200;

    var edgeLength = function( e ){
      function length(e){
        var w = e.data('weight');

        if( w == null ){
          w = avgW;
        }

        // as w => inf, l => 0
        var l = options.lengthFactor / w;

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
      fit: options.fit,
      animate: options.animate,
      randomize: options.randomize,
      maxSimulationTime: options.maxSimulationTime,
      edgeLength: edgeLength,
      padding: defaultPadding
    });

    var p = this.layoutPrepost( l, options );

    this.layoutDelay(l, options);

    return p;
  };

  rfn.linearLayout = function(options){
    options = $.extend({
      animate: true,
      resizeCy: true
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

    var p = this.layoutPrepost( l, options );

    this.layoutDelay(l, options);

    return p;
  };

  rfn.fitGraph = function(options){
    options = $.extend({
      duration: 500,
      resizeCy: true
    }, options);

    var self = this;
    var container = cy.container();

    if( self.networksExpanded && options.resizeCy ){
      container.classList.add('cy-layouting-shift');
    }

    if( self.query.historyExpanded && options.resizeCy ){
      container.classList.add('cy-layouting-shift-history');
    }

    return new Promise(function( resolve ){
      if( options.resizeCy ){
        cy.resize();
      }

      cy.animate({
        fit: {
          eles: cy.elements(),
          padding: defaultPadding
        }
      }, {
        duration: options.duration,
        complete: function(){
          var cl = container.classList;

          if( options.resizeCy ){
            cl.remove('cy-layouting-shift');
            cl.remove('cy-layouting-shift-history');
          }

          resolve();
        }
      });
    });

  };


} } ]);
