app.factory('Result', 
[ '$$search', 'cy',
function( $$search, cy ){

  var Result = function( opts ){
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
      return $$search({
        organism: q.organism.id,
        genes: q.genesText
      });
    }).then(function( searchResult ){
      for( var i in searchResult ){
        self[i] = searchResult[i];
      }

      self.loadGraph();

      self.searching = false;
      self.searchPromise = null;

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

    cy.elements().remove();

    // nodes
    for( var i = 0; i < self.resultGenes.length; i++ ){
      var rGene = self.resultGenes[i];
      var gene = rGene.gene;

      eles.push({
        group: 'nodes',
        data: {
          id: '' + gene.id,
          idInt: gene.id,
          symbol: rGene.typedName || gene.symbol,
          score: rGene.score
        }
      });
    }

    // edges
    for( var i = 0; i < self.resultNetworkGroups.length; i++ ){
      var rGr = self.resultNetworkGroups[i];
      var gr = rGr.networkGroup;
      var rNets = rGr.resultNetworks;

      for( var j = 0; j < rNets.length; j++ ){
        var rNet = rNets[j];
        var rIntns = rNet.resultInteractions;

        for( var k = 0; k < rIntns.length; k++ ){
          var rIntn = rIntns[k];

          eles.push({
            group: 'edges',
            data: {
              source: '' + rIntn.fromGene.gene.id,
              target: '' + rIntn.toGene.gene.id,
              weight: rIntn.interaction.weight,
              group: gr.code
            }
          });
        }
      }
    }

    cy.add( eles );
    
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