'use strict';

app.factory('Result',
[ '$$search', '$$user', 'cy', 'cyStylesheet', 'util', 'Result_genes', 'Result_networks', 'Result_layouts', 'Result_selectedinfo', 'Result_functions', 'Result_highlight', 'Result_report', 'Result_save',
function( $$search, $$user, cy, cyStylesheet, util, Result_genes, Result_networks, Result_layouts, Result_selectedinfo, Result_functions, Result_highlight, Result_report, Result_save ){

  var rmods = [ Result_genes, Result_networks, Result_layouts, Result_selectedinfo, Result_functions, Result_highlight, Result_report, Result_save ];

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

    window.result = this;

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
        attrGroups: attrGrIds,
        geneThreshold: q.maxGenes,
        attrThreshold: q.maxAttrs
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
      self.error = true;

      PubSub.publish('result.error');

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
      fn.coverageFormatted = fn.numAnnotatedInSample + ' / ' + fn.numAnnotatedInTotal;
    }

    this.coloringFunctions = [];

    this.functionColors = config.functionColors.slice();
  };

  rfn.updateGenesData = function(){
    var self = this;

    var m = self.resultGenesById = {};
    var rankedRgenes = self.resultGenes.concat([]).sort(function( a, b ){
      return b.score - a.score;
    });

    var rank = 0;

    rankedRgenes.forEach(function( rGene ){
      if( !rGene.queryGene ){
        rank++;
      }

      rGene.rank = rank;
    });

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
    var rInteractions = self.resultInteractions = [];
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
        rNet.ele = rNet.network;

        if( rNet.network.metadata != null && rNet.network.metadata.yearPublished != null ){
          rNet.network.metadata.yearPublished = '' + parseInt( rNet.network.metadata.yearPublished ); // fix from server data
        }

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
          rInteractions.push( rIntn );
        }

        config.networks.postprocess( rNet.network );
      }

      rGr.isResultNetworkGroup = true;
      rGr.color = color;
      rGr.displayWeight = makeDisplayWeight( rGr.weight );
      rGr.enabled = true;
      rGr.expanded = false;
      rGr.children = rNets;
      rGr.ele = rGr.networkGroup;

      rNets.sort( sortByWeight );
    }

    // process the attributes
    for( var i = 0; i < rAttrGrs.length; i++ ){
      var rGr = rAttrGrs[i];
      var rAttrs = rGr.resultAttributes;
      var color = config.attributes.color;

      for( var j = 0; j < rAttrs.length; j++ ){
        var rAttr = rAttrs[j];

        rAttr.isResultAttribute = true;
        rAttr.color = color;
        rAttr.displayWeight = makeDisplayWeight( rAttr.weight );
        rAttr.enabled = true;
        rAttr.expanded = false;
        rAttr.resultAttributeGroup = rGr;
        rAttr.ele = rAttr.attribute;

        rAttrsById[ rAttr.attribute.id ] = rAttr;
      }

      rGr.isResultAttributeGroup = true;
      rGr.color = color;
      rGr.displayWeight = makeDisplayWeight( rGr.weight );
      rGr.enabled = true;
      rGr.expanded = false;
      rGr.children = rAttrs;
      rGr.ele = rGr.attributeGroup;

      rAttrs.sort( sortByWeight );
    }

    rAllGrs.sort( sortByWeight );
  };

  rfn.loadGraph = function(){
    var self = this;
    var eles = [];
    var id2AttrEle = {};

    cy.startBatch();
    var oldEles = cy.elements();
    var oldElesById = {};
    var someOldEles = false;

    for( var i = 0; i < oldEles.length; i++){
      oldElesById[ oldEles[i].id() ] = oldEles[i];
    }

    // gene nodes
    for( var i = 0; i < self.resultGenes.length; i++ ){
      var rGene = self.resultGenes[i];
      var gene = rGene.gene;
      var ele;
      var oldEle = oldElesById[ gene.id ];

      if( oldEle ){
        someOldEles = true;
      }

      var rank = rGene.rank;

      eles.push( ele = {
        group: 'nodes',
        position: oldEle ? oldEle.position() : { x: -9999, y: -9999 },
        //locked: !!oldEle,
        data: {
          oldEle: !!oldEle,
          id: '' + gene.id,
          idInt: gene.id,
          name: rGene.name,
          score: rGene.score,
          query: rGene.queryGene,
          gene: true,
          css: ( function(){
            var css = {};

            for( var p = 1; p <= 16; p++ ){
              css['pie_'+p+'_background_size'] = 0;
              css['pie_'+p+'_background_color'] = '#000';
              css['pie_'+p+'_background_opacity'] = 0;
            }

            return css;
          } )()
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
            // position: { x: -9999, y: -9999 },
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
            attr: true,
            attributeGroupId: self.resultAttributesById[ rAttr.attribute.id ].resultAttributeGroup.attributeGroup.id
          }
        } );
      }
    }

    // interaction edges
    for( var i = 0; i < self.resultNetworkGroups.length; i++ ){
      var rGr = self.resultNetworkGroups[i];
      var gr = rGr.networkGroup;
      var rNets = rGr.resultNetworks;
      var nodePairs = {}; // per network group
      var pairIds = [];

      for( var j = 0; j < rNets.length; j++ ){
        var rNet = rNets[j];
        var rIntns = rNet.resultInteractions;

        for( var k = 0; k < rIntns.length; k++ ){
          var rIntn = rIntns[k];
          var ele;
          var src = rIntn.fromGene.gene.id;
          var tgt = rIntn.toGene.gene.id;

          eles.push( ele = {
            group: 'edges',
            data: {
              id: 'intn-' + rIntn.id,
              source: src,
              target: tgt,
              weight: rIntn.interaction.weight,
              group: gr.code,
              networkId: rNet.network.id,
              networkGroupId: rGr.networkGroup.id,
              intn: true,
              rIntnId: rIntn.id
            }
          } );

          continue; // disable metaintn

          var pairId = src < tgt ? (src + '$' + tgt) : (tgt + '$' + src);

          var pair = nodePairs[pairId];
          if( !pair ){
            pair = nodePairs[pairId] = [];
            pairIds.push( pairId );
          }

          pair.push( ele );
        } // intns
      } // nets

      // create metainteractions to reduce total number of edges
      for( var j = 0; j < pairIds.length; j++ ){ break; // disabled
        var pairId = pairIds[j];
        var pairs = nodePairs[ pairId ];

        if( pairs.length > 1 ){
          var pairEle;

          eles.push( pairEle = {
            group: 'edges',
            data: {
              id: 'metaintn-' + pairId,
              source: pairs[0].data.source,
              target: pairs[0].data.target,
              weight: pairs.reduce(function( currWeight, ele ){
                return currWeight + ele.data.weight;
              }, 0),
              group: pairs[0].data.group,
              networkGroupId: pairs[0].data.networkGroupId,
              metaintn: true,
              edgeIds: pairs.map(function( ele ){
                return ele.data.id;
              })
            }
          } );

          pairs.forEach(function( ele ){
            ele.metaId = pairEle.data.id;
            ele.classes = 'collapsed metaintn-child';
          });
        }

      } // pair ids
    } // grs

    oldEles.remove();
    cy.add( eles );

    // normalise scores
    var nodes = cy.nodes();
    var qNodes = nodes.filter('[?query]');
    var nqNodes = nodes.filter('[!query]');
    var maxScore = nqNodes.max(function( n ){ return n.data('score'); }).value;

    for( var i = 0; i < nodes.length; i++ ){
      var n = nodes[i];
      var score = n.data('score');

      n.data( 'normScore', Math.min(score/maxScore, 1) );
    }

    cy.endBatch(); // will trigger new stylesheet etc

    var $list = $('#network-list');
    var container = cy.container();

    var lockedNodes = nodes.filter('[?oldEle][?query]').lock();

    // cy.pon('layoutready').then(function(){
    //   lockedNodes.unlock();
    // });

    self.layoutResizeCyPre();

    return self.forceLayout({
      animate: someOldEles,
      fit: !someOldEles,
      randomize: !someOldEles,
      resizeCy: false
    }).then(function(){
      lockedNodes.unlock();

      if( someOldEles ){

        var delay = function( l ){
          return new Promise(function( resolve ){
            setTimeout(resolve, l);
          });
        };

        return delay(25).then(function(){
          return self.fitGraph({
            duration: 250,
            resizeCy: false
          });
        }).then(function(){
          return delay(25);
        }).then(function(){
          return self.forceLayout({
            animate: true,
            fit: true,
            randomize: false,
            maxSimulationTime: 1000,
            resizeCy: false
          });
        });

      }
    }).then(function(){
      self.layoutResizeCyPost();
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
  PubSub.subscribe('result.error', updateScope);

} ]);
