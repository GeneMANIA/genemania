app.factory('Result_layouts',
[ 'util',
function( util ){ return function( Result ){

  var r = Result;
  var rfn = r.prototype;

  var sortByWeight = function(a, b){
    return b.data('weight') - a.data('weight');
  };

  rfn.layoutPrepost = function(){
    var self = this;

    return new Promise(function(resolve){

      if( self.networksExpanded ){
        var container = cy.container();

        cy.one('layoutstop', function(){
          container.classList.remove('cy-layouting-shift');

          resolve();
        });

        container.classList.add('cy-layouting-shift');
      } else {
        cy.one('layoutstop', function(){
          resolve();
        });
      }
    });
  };

  rfn.circleLayout = function(options){
    options = $.extend({
      animate: true
    }, options);

    var p = this.layoutPrepost();

    cy.layout({
      name: 'concentric',
      animate: options.animate,
      animationDuration: 500,
      concentric: function(){
        return (this.data('query') ? 100 : 0) + this.data('score');
      },
      levelWidth: function(){
        return 1;
      },
      sort: sortByWeight
    });

    return p;
  };

  rfn.forceLayout = function(options){
    options = $.extend({
      randomize: true,
      animate: true,
      maxSimulationTime: 2000
    }, options);

    var p = this.layoutPrepost();

    var layoutEles = cy.elements().stdFilter(function( ele ){
      return ele.isNode() || ele.data('group') !== 'coexp';
    });

    layoutEles.layout({
      name: 'cola',
      animate: options.animate,
      randomize: options.randomize,
      maxSimulationTime: options.maxSimulationTime,
      edgeLength: function( e ){ return layoutEles.length / 8 / e.data('weight'); } // as w => inf, l => 0
    });

    return p;
  };

  rfn.linearLayout = function(options){
    options = $.extend({
      animate: true
    }, options);

    var p = this.layoutPrepost();

    cy.layout({
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
      padding: 50
    });

    return p;
  };



} } ]);
