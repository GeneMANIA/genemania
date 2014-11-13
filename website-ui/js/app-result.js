app.factory('Result', 
[ '$$search', 'cy', 'cyStylesheet',
function( $$search, cy, cyStylesheet ){

  var Result = window.Result = function( opts ){
    if( !(this instanceof Result) ){
      return new Result( opts );
    }

    var self = this;

    if( opts.query ){
      this.query = opts.query;
    } else {
      console.error('A result must have a query specified');
    }

    this.search();
  };
  var r = Result;
  var rfn = Result.prototype;

  rfn.search = function(){
    var q = this.query;
    var self = this;

    self.searching = true;

    PubSub.publish('result.search', self);

    return self.searchPromise = Promise.resolve().cancellable().then(function(){
      var netIds = [];
      for( var i = 0; i < q.networks.length; i++ ){
        var net = q.networks[i];

        if( net.selected ){
          netIds.push( net.id );
        }
      }

      var attrGrIds = [];
      for( var i = 0; i < q.attributeGroups.length; i++ ){
        var gr = q.attributeGroups[i];

        if( gr.selected ){
          attrGrIds.push( gr.id );
        }
      }

      return $$search({
        organism: q.organism.id,
        genes: q.genesText,
        weighting: q.weighting.value,
        networks: netIds,
        attrGroups: attrGrIds
      });
    }).then(function( searchResult ){
      for( var i in searchResult ){
        self[i] = searchResult[i];
      }

      self.loadGraph();

      self.searching = false;
      self.searchPromise = null;

      q.store();

      PubSub.publish('result.searched', self);
    }).catch(function( err ){
      self.searching = false;
      self.searchPromise = null;

      throw err;
    });

  };

  rfn.cancel = function(){
    var self = this;

    if( self.searchPromise ){
      PubSub.publish('result.cancel', self);
      self.searchPromise.cancel('A search was cancelled');
    }
  };

  rfn.cancellable = function(){
    return this.searchPromise != null;
  };

  rfn.loadGraph = function(){
    var self = this;
    var eles = [];
    var id2AttrEle = {};

    cy.startBatch();
    cy.elements().remove();

    // gene nodes
    for( var i = 0; i < self.resultGenes.length; i++ ){
      var rGene = self.resultGenes[i];
      var gene = rGene.gene;
      var ele;

      eles.push( ele = {
        group: 'nodes',
        data: {
          id: '' + gene.id,
          idInt: gene.id,
          name: rGene.typedName || gene.symbol,
          score: rGene.score,
          query: rGene.queryGene
        }
      } );

      // attribute nodes & edges
      var rAttrs = rGene.resultAttributes;
      for( var j = 0; j < rAttrs.length; j++ ){
        var rAttr = rAttrs[j];
        var attr = rAttr.attribute;
        var attrEle = id2AttrEle[ attr.id ]; 
        var attrEdge;

        if( !attrEle ){
          eles.push( attrEle = {
            group: 'nodes',
            data: {
              id: '' + attr.id,
              idInt: attr.id,
              name: attr.name,
              attr: true
            }
          } );
        }

        eles.push( attrEdge = {
          group: 'edges',
          data: {
            source: '' + gene.id,
            target: '' + attr.id,
            group: 'attr'
          }
        } );
      }
    }

    // interaction edges
    for( var i = 0; i < self.resultNetworkGroups.length; i++ ){
      var rGr = self.resultNetworkGroups[i];
      var gr = rGr.networkGroup;
      var rNets = rGr.resultNetworks;

      for( var j = 0; j < rNets.length; j++ ){
        var rNet = rNets[j];
        var rIntns = rNet.resultInteractions;

        for( var k = 0; k < rIntns.length; k++ ){
          var rIntn = rIntns[k];
          var ele;

          eles.push( ele = {
            group: 'edges',
            data: {
              source: '' + rIntn.fromGene.gene.id,
              target: '' + rIntn.toGene.gene.id,
              weight: rIntn.interaction.weight,
              group: gr.code
            }
          } );
        }
      }
    }

    cy.add( eles );

    // normalise scores
    var qNodes = cy.$('node[?query]');
    var nqNodes = cy.$('node[!query]');
    var maxScore = nqNodes.max(function( n ){ return n.data('score'); }).value;
    qNodes.data('score', maxScore);

    // generate the stylesheet for the graph
    var stylesheet = cyStylesheet(cy);
    cy.style().fromJson( stylesheet );

    cy.endBatch(); // will trigger new stylesheet etc
    
    cy.elements().stdFilter(function( ele ){
      return ele.isNode() || ele.data('group') !== 'coexp';
    }).layout({ name: 'cola' });

    // cy.layout({
    //   name: 'concentric',
      
    //   concentric: function(){
    //     return this.data('score');
    //   },

    //   levelWidth: function( nodes ){
    //     return 0.25;
    //   },
    // });
  };

  return r;

} ]);