app.factory('Result',
[ '$$search', 'cy', 'cyStylesheet', 'util', 'Result_genes', 'Result_networks', 'Result_layouts', 'Result_selectedinfo', 'Result_functions', 'Result_highlight', 'Result_report',
function( $$search, cy, cyStylesheet, util, Result_genes, Result_networks, Result_layouts, Result_selectedinfo, Result_functions, Result_highlight, Result_report ){

  var rmods = [ Result_genes, Result_networks, Result_layouts, Result_selectedinfo, Result_functions, Result_highlight, Result_report ];

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

    if( rfn.networksExpanded === undefined && !util.isSmallScreen() ){
      rfn.networksExpanded = true;
    }

    if( rfn.genesExpanded === undefined && !util.isSmallScreen() ){
      rfn.genesExpanded = true;
    }

    this.search({
      store: opts.store || opts.store === undefined ? true : false
    });
  };
  var r = Result;
  var rfn = Result.prototype;

  rfn.search = function( opts ){
    var q = this.query;
    var self = this;

    self.searching = true;

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

      self.updateNetworkData();
      self.updateGenesData();
      self.updateFunctionsData();

      self.loadGraph().then(function(){
        if( opts.store ){
          q.store();
        }
      });

      self.searching = false;
      self.searchPromise = null;

      PubSub.publish('result.searched', self);
    }).catch(function( err ){
      self.searching = false;
      self.searchPromise = null;

      throw err;
    });
    
    PubSub.publish('result.search', self);

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

  rfn.updateFunctionsData = function(){
    var fns = this.resultOntologyCategories;

    for( var i = 0; i < fns.length; i++ ){
      var fn = fns[i];

      fn.qValueFormatted = new Number( fn.qValue ).toExponential( 2 );
    }

    this.coloringFunctions = [];

    this.functionColors = config.functionColors.slice();
  };

  rfn.updateGenesData = function(){
    var self = this;

    var m = self.resultGenesById = {};

    for( var i = 0; i < self.resultGenes.length; i++ ){
      var rg = self.resultGenes[i];

      rg.name = rg.gene.name = rg.typedName || rg.gene.symbol;

      m[ '' + rg.gene.id ] = rg;
    }
  };

  rfn.makeResultInteractionId = function(){
    this.lastRIntnId = this.lastRIntnId || 1;

    return ++this.lastRIntnId;
  };

  rfn.updateNetworkData = function(){
    var self = this;
    var rGrs = self.resultNetworkGroups;
    var rAttrGrs = self.resultAttributeGroups;
    var rAllGrs = self.resultAllGroups = rAttrGrs.concat( rGrs );
    var rAttrsById = self.resultAttributesById = {};
    var rIntnsById = self.resultInteractionsById = {};
    var rNetsById = self.resultNetworksById = {};
    self.resultNetworks = [];

    var sortByWeight = function(a, b){
      return b.weight - a.weight;
    };

    var makeDisplayWeight = function( weight ){
      return numeral( weight ).format('0.00%');
    };

    // process the (real) networks
    for( var i = 0; i < rGrs.length; i++ ){
      var rGr = rGrs[i];
      var rNets = rGr.resultNetworks;
      var color = config.networks.colorsByCode[ rGr.networkGroup.code ];

      for( var j = 0; j < rNets.length; j++ ){
        var rNet = rNets[j];

        rNet.color = color;
        rNet.displayWeight = makeDisplayWeight( rNet.weight );
        rNet.enabled = true;
        rNet.expanded = false;
        rNet.resultNetworkGroup = rGr;
        
        rNetsById[ rNet.network.id ] = rNet;
        self.resultNetworks.push( rNet );

        var rIntns = rNet.resultInteractions;
        for( var k = 0; k < rIntns.length; k++ ){
          var rIntn = rIntns[k];

          rIntn.weight = rIntn.interaction.weight;
          rIntn.displayWeight = numeral( rIntn.weight ).format('0.00%');

          rIntn.absoluteWeight = rIntn.interaction.weight * rNet.weight;
          rIntn.absoluteDisplayWeight = numeral( rIntn.absoluteWeight ).format('0.00%');

          rIntn.id = self.makeResultInteractionId();
          rIntn.resultNetwork = rNet;

          rIntn.fromGene.name = rIntn.fromGene.gene.name = rIntn.fromGene.typedName || rIntn.fromGene.gene.symbol;
          rIntn.toGene.name = rIntn.toGene.gene.name = rIntn.toGene.typedName || rIntn.toGene.gene.symbol;

          rIntnsById[ rIntn.id ] = rIntn;
        }

        config.networks.postprocess( rNet.network );
      }

      rGr.isResultNetworkGroup = true;
      rGr.color = color;
      rGr.displayWeight = makeDisplayWeight( rGr.weight );
      rGr.enabled = true;
      rGr.expanded = false;

      rNets.sort( sortByWeight );
    }

    // process the attributes
    for( var i = 0; i < rAttrGrs.length; i++ ){
      var rGr = rAttrGrs[i];
      var rAttrs = rGr.resultAttributes;
      var color = config.attributes.color;

      for( var j = 0; j < rAttrs.length; j++ ){
        var rAttr = rAttrs[j];

        rAttr.color = color;
        rAttr.displayWeight = makeDisplayWeight( rAttr.weight );
        rAttr.enabled = true;
        rAttr.expanded = false;
        rAttr.resultAttributeGroup = rGr;

        rAttrsById[ rAttr.attribute.id ] = rAttr;
      }

      rGr.isResultAttributeGroup = true;
      rGr.color = color;
      rGr.displayWeight = makeDisplayWeight( rGr.weight );
      rGr.enabled = true;
      rGr.expanded = false;

      rAttrs.sort( sortByWeight );
    }

    rAllGrs.sort( sortByWeight );
  };

  rfn.loadGraph = function(){
    var self = this;
    var eles = [];
    var id2AttrEle = {};
    var rank = 0;

    cy.startBatch();
    cy.elements().remove();

    // gene nodes
    for( var i = 0; i < self.resultGenes.length; i++ ){
      var rGene = self.resultGenes[i];
      var gene = rGene.gene;
      var ele;

      rank = rGene.queryGene ? 0 : rank + 1;

      eles.push( ele = {
        group: 'nodes',
        data: {
          id: '' + gene.id,
          idInt: gene.id,
          name: (rGene.typedName || gene.symbol) + (rank !== 0 ? ' (' + rank + ')' : ''),
          score: rGene.score,
          query: rGene.queryGene,
          gene: true
        },
        classes: rGene.resultOntologyCategories.map(function(fn){
          return 'fn' + fn.id;
        }).join(' ')
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
            group: 'attr',
            attr: true
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
              group: gr.code,
              networkId: rNet.network.id,
              networkGroupId: rGr.networkGroup.id,
              intn: true,
              rIntnId: rIntn.id
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

    var $list = $('#network-list');
    var container = cy.container();

    return self.forceLayout({
      animate: false
    });

  };

  // inject the individual query submodules
  for( var i = 0; i < rmods.length; i++ ){
    rmods[i]( r );
  }

  return r;

} ]);

app.controller('ResultCtrl',
[ '$scope', 'updateScope', 'cy',
function( $scope, updateScope, cy ){

  function init(){
    $scope.query = Query.current;
    $scope.result = $scope.query.result;

    updateScope();
  }

  PubSub.subscribe('result.searched', init);

} ]);
