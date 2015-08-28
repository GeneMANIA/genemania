'use strict';

app.factory('cy',
[ 'cyStylesheet',
function( cyStylesheet ){

  var cy = window.cy = cytoscape({
    container: document.getElementById('cy'),

    style: cyStylesheet(),

    boxSelectionEnabled: false,
    autounselectify: true,
    motionBlur: true,
    motionBlurOpacity: 0.075
  });

  var edgeIdsToSelector = function( edgeIds ){
    return edgeIds.map(function(id){ return '#' + id; }).join(', ')
  };

  var hoverTimeout;

  var debounceRate = 16;

  // interacting with the graph should close the genes box
  cy
    .on('tapstart', function(){
      var qg = document.getElementById('query-genes-textarea');

      if( qg && document.activeElement === qg ){
        qg.blur();
      }
    })

    .on('tap taphold', '[?gene]', function(){
      query.result.selectGene( this.data('id') );
    })

    .on('tap taphold', '[?attr]', function(){
      query.result.selectAttribute( this.data('id') );
    })

    .on('tap', 'edge[?attr]', function(){
      var attrNode = this.connectedNodes('[?attr]');

      query.result.selectAttribute( attrNode.data('id') );
    })

    .on('tap', 'edge[?intn]', function(){
      query.result.selectInteraction( this.data('rIntnId') );
    })

    .on('tap', 'edge[?metaintn]', function(){
      var edgeIds = this.data('edgeIds');

      cy.$( edgeIdsToSelector(edgeIds) ).removeClass('collapsed');
      this.addClass('collapsed');
    })

    .on('tap', function(e){
      if( e.cyTarget === cy ){
        query.result.closeSelectedInfo();

        // var metaedge = cy.$('[?metaintn].collapsed');
        //
        // if( metaedge.nonempty() ){
        //   metaedge.forEach(function( e ){
        //     e.removeClass('collapsed');
        //     cy.$( edgeIdsToSelector( e.data('edgeIds') ) ).addClass('collapsed');
        //   });
        // }
      }
    })

    .on('taphold hoverover', '[?gene]', function(){
      result.rateLimitedHighlight({ genes: [ this.data('idInt') ] })
    })

    .on('tapend drag hoverout', '[?gene]', _.debounce( function(){
      result.rateLimitedUnhighlight({ genes: [ this.data('idInt') ] })
    }, debounceRate ) )

    // .on('taphold', '[?intn]', function(){
    //   result.rateLimitedHighlight({ interactions: [ this.data('rIntnId') ] })
    // })
    //
    // .on('tapend free', '[?intn]', _.debounce( function(){
    //   result.rateLimitedUnhighlight({ interactions: [ this.data('rIntnId') ] })
    // }, debounceRate ) )

    .on('taphold hoverover', '[?attr]', function(){
      result.rateLimitedHighlight({ attrs: [ this.data('id') ] })
    })

    .on('tapend drag hoverout', '[?attr]', _.debounce( function(){
      result.rateLimitedUnhighlight({ attrs: [ this.data('id') ] })
    }, debounceRate ) )

    // .on('taphold', 'edge[?attr]', function(){
    //   var attrNode = this.connectedNodes('[?attr]');
    //
    //   result.rateLimitedHighlight({ attrs: [ attrNode.data('id') ] })
    // })
    //
    // .on('tapend', 'edge[?attr]', _.debounce( function(){
    //   var attrNode = this.connectedNodes('[?attr]');
    //
    //   result.rateLimitedUnhighlight({ attrs: [ attrNode.data('id') ] })
    // }, debounceRate ) )

    .on('mouseover', '[?gene], [?attr]', function(){
      var ele = this;

      hoverTimeout = setTimeout(function(){
        ele.trigger('hoverover');
      }, 500);
    })

    .on('mousedown', '[?gene], [?attr]', function(){
      clearTimeout( hoverTimeout );
    })

    .on('mouseout', '[?gene], [?attr]', function(){
      clearTimeout( hoverTimeout );

      this.trigger('hoverout');
    })
  ;

  var menuCommands = function( opts ){
    opts = opts || {};

    var getName = function(n){
      var id = n.data('idInt');

      return result.resultGenesById[ id ].name;
    };

    return [
      {
        content: '<sup><i class="fa fa-plus"></i></sup><i class="fa fa-search"></i>',
        select: function(){
          query.addGenes( getName(this) );
          query.search();
        },
        disabled: opts.queryGene
      },

      {
        content: '<sup><i class="fa fa-minus"></i></sup><i class="fa fa-search"></i>',
        select: function(){
          query.removeGenes( getName(this) );
          query.search();
        },
        disabled: !opts.queryGene
      },

      {
        content: '<sup><i class="fa fa-circle"></i></sup><i class="fa fa-search"></i>',
        select: function(){
          query.setGenes( getName(this) );
          query.search();
        }
      }
    ];
  };

  // cy.cxtmenu({
  //   selector: 'node[?query]',
  //   commands: menuCommands({ queryGene: true })
  // });
  //
  // cy.cxtmenu({
  //   selector: 'node[!query]',
  //   commands: menuCommands({ queryGene: false })
  // });

  return cy;

}]);
