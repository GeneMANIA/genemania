'use strict';

app.factory('Result_layouts',
[ 'util',
function( util ){ return function( Result ){

  var r = Result;
  var rfn = r.prototype;

  var defaultPadding = 50;

  var getLayoutEles = function(){
    return cy.elements();
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

    cy.resize();
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

    cy.resize();
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

    options = options || {};

    if( !r.prelayoutPosns && !options.undo ){
      var posns = self.prelayoutPosns = {};
      var nodes = cy.nodes();

      for( var i = 0; i < nodes.length; i++ ){
        var node = nodes[i];
        var p = node.position();

        posns[ node.id() ] = { x: p.x, y: p.y };
      }

      self.layoutUndone = false;
    }

    if( self.layoutPromise ){
      self.layoutPromise.cancel();
    }

    cy.nodes().removeClass('with-descr');

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

    var l = getLayoutEles().makeLayout({
      name: 'concentric',
      animate: options.animate,
      animationDuration: 500,
      concentric: function(){
        var isQuery = this.data('query');
        var isAttr = this.data('attr');

        if( isQuery ){
          return 100 + this.data('score');
        } else if( isAttr ){
          return -100;
        } else {
          return this.data('score');
        }
      },
      levelWidth: function(){
        return 100;
      },
      padding: defaultPadding
    });

    var p = this.layoutPrepost( l, options );

    this.layoutDelay(l, options);

    return p;
  };

  rfn.forceLayout = function(options){
    var result = this;

    options = $.extend({
      fit: true,
      randomize: true,
      animate: true,
      nodeSpacing: 15,
      lengthFactor: 100,
      maxSimulationTime: 2000,
      padding: defaultPadding,
      resizeCy: true
    }, options);

    var layoutEles = cy.elements().stdFilter(function( ele ){
      return ele.isNode() || !ele.hasClass('filtered');
    });

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
    var maxLength = 150;

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

      var l = length(e);

      if( e.data('group') === 'coexp' ){
        l *= 10;
      }

      if( l < minLength ){
        return minLength;
      } else if( l > maxLength ){
        return maxLength;
      } else {
        return l;
      }
    };

    var layoutNodes = layoutEles.nodes();

    for( var i = 0; i < layoutNodes.length; i++ ){
      var node = layoutNodes[i];
      var pos = node.position();

      node.data( 'preForcePos', {
        x: pos.x,
        y: pos.y
      } );
    }

    var l = layoutEles.makeLayout({
      name: 'cose',
      nodeOverlap: 30,
      fit: options.fit,
      animate: false,
      randomize: options.randomize,
      maxSimulationTime: options.maxSimulationTime,
      idealEdgeLength: edgeLength,
      padding: defaultPadding
    });

    var p = this.layoutPrepost( l, options );

    this.layoutDelay(l, options);

    if( options.animate ){
      return p.then(function(){
        var id2pos = {};

        for( var i = 0; i < layoutNodes.length; i++ ){
          var node = layoutNodes[i];
          var pos1 = node.data('preForcePos');
          var pos = node.position();
          var pos2 = { x: pos.x, y: pos.y };

          node.position( pos1 );

          id2pos[ node.id() ] = pos2;
        }

        var presetLayout = layoutNodes.makeLayout({
          name: 'preset',
          positions: id2pos,
          fit: options.fit,
          animate: true,
          animationDuration: 500,
          padding: defaultPadding
        });

        result.layoutDelay( presetLayout, options );

        return presetLayout.promiseOn('layoutstop');
      });
    } else {
      return p;
    }
  };

  rfn.linearLayout = function(options){
    options = $.extend({
      animate: true,
      resizeCy: true
    }, options);

    var nodeSortVal = function( n ){
      if( n.data('query') ){
        return n.data('avgConndScore');
      } else {
        return 100 + n.data('score');
      }
    };

    var nodeSort = function( a, b ){
      return nodeSortVal(b) - nodeSortVal(a);
    };

    var hasAttrs = result.resultNetworkGroups.length > 0;

    var l = getLayoutEles().makeLayout({
      name: 'grid',
      avoidOverlap: true,
      avoidOverlapPadding: 2,
      condense: true,
      animate: options.animate,
      animationDuration: 500,
      cols: hasAttrs ? 3 : 2,
      position: function(n){
        if( !hasAttrs ){
          return {
            col: n.data('query') ? 0 : 1
          }
        } else {
          return {
            col: n.data('query') ? 0 : n.data('gene') ? 1 : 2
          };
        }
      },
      sort: nodeSort,
      padding: defaultPadding
    });

    var p = this.layoutPrepost( l, options );

    cy.nodes().addClass('with-descr');

    this.layoutDelay(l, options);

    return p;
  };

  rfn.undoLayout = function( options ){
    var self = this;

    options = $.extend({
      animate: true,
      resizeCy: true,
      fit: true,
      undo: true
    }, options);

    var l = getLayoutEles().makeLayout({
      name: 'preset',
      animate: options.animate,
      animationDuration: 500,
      positions: this.prelayoutPosns,
      padding: defaultPadding
    });

    self.layoutUndone = true;

    PubSub.publish('result.layoutUndone', {
      result: self
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
