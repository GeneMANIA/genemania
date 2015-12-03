'use strict';

app.factory('cy',
[ 'cyStylesheet',
function( cyStylesheet ){

  return new Promise(function( resolve ){

    var loadCy = function(){

      var cy = window.cy = cytoscape({
        container: document.getElementById('cy'),

        style: cyStylesheet(),

        boxSelectionEnabled: false,
        autounselectify: true,
        motionBlur: true,
        motionBlurOpacity: 0.075,
        maxZoom: 10,
        minZoom: 0.1,
        pixelRatio: 'auto'
      });

      var edgeIdsToSelector = function( edgeIds ){
        return edgeIds.map(function(id){ return '#' + id; }).join(', ')
      };

      var hoverTimeout, hoverEle, hoverIsHold;

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

        .on('hoverover', '[?gene]', function(){
          result.rateLimitedHighlight({ genes: [ this.data('idInt') ] })
        })

        .on('hoverout', '[?gene]', _.debounce( function(){
          result.rateLimitedUnhighlight({ genes: [ this.data('idInt') ] })
        }, debounceRate ) )

        .on('hoverover', '[?attr]', function(){
          result.rateLimitedHighlight({ attrs: [ this.data('id') ] })
        })

        .on('hoverout', '[?attr]', _.debounce( function(){
          result.rateLimitedUnhighlight({ attrs: [ this.data('id') ] })
        }, debounceRate ) )

        .on('mouseover taphold', 'node', function(e){
          var ele = this;

          hoverTimeout = setTimeout(function(){
            if( !ele.same(hoverEle) ){
              ele.trigger('hoverover');
              hoverEle = ele;
              hoverIsHold = e.type === 'taphold';
            }
          }, 150);
        })

        .on('mouseout', 'node', function(){
          if( this.grabbed() ){ return; }

          clearTimeout( hoverTimeout );

          this.trigger('hoverout');

          hoverEle = null;
        })

        .on('tapend', 'node', function(){
          if( this.same(hoverEle) && !hoverIsHold ){ return; }

          clearTimeout( hoverTimeout );

          this.trigger('hoverout');

          hoverEle = null;
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

      resolve( cy );
    };

    switch( document.readyState ){
      case 'complete':
        loadCy();
        break;

      default:
        window.addEventListener('load', loadCy);
    }

  }); // promise

}]);
