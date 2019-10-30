'use strict';

app.factory('Result',
[ '$$search', '$$user', 'cy', 'cyStylesheet', 'util', 'Result_genes', 'Result_networks', 'Result_layouts', 'Result_selectedinfo', 'Result_functions', 'Result_highlight', 'Result_report', 'Result_save', 'Result_shortcuts',
function( $$search, $$user, ngCy, cyStylesheet, util, Result_genes, Result_networks, Result_layouts, Result_selectedinfo, Result_functions, Result_highlight, Result_report, Result_save, Result_shortcuts ){

  var rmods = [ Result_genes, Result_networks, Result_layouts, Result_selectedinfo, Result_functions, Result_highlight, Result_report, Result_save, Result_shortcuts ];

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
      store: opts.store || opts.store === undefined ? true : false,
      positions: opts.positions
    });

    this.bindShortcutKeys();
  };
  var r = Result;
  var rfn = Result.prototype;

  rfn.search = function( opts ){
    var q = this.query;
    var self = this;

    self.searching = true;

    return self.searchPromise = Promise.resolve().then(function(){
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

      self.loadGraph({ positions: opts.positions }).then(function(){
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

      rg.name = rg.gene.name = rg.gene.symbol;

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

    var maxWeight = _.max( rAllGrs.map(function( rGr ){ return rGr.weight; }) );

    // process the (real) networks
    for( var i = 0; i < rGrs.length; i++ ){
      var rGr = rGrs[i];
      var rNets = rGr.resultNetworks;
      var color = config.networks.colorsByCode[ rGr.networkGroup.code ];

      for( var j = 0; j < rNets.length; j++ ){
        var rNet = rNets[j];

        rNet.color = color;
        rNet.relativeWeight = rNet.weight / maxWeight;
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
      rGr.relativeWeight = rGr.weight / maxWeight;
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
        rAttr.relativeWeight = rAttr.weight / maxWeight;
        rAttr.displayWeight = makeDisplayWeight( rAttr.weight );
        rAttr.enabled = true;
        rAttr.expanded = false;
        rAttr.resultAttributeGroup = rGr;
        rAttr.ele = rAttr.attribute;

        rAttrsById[ rAttr.attribute.id ] = rAttr;
      }

      rGr.isResultAttributeGroup = true;
      rGr.color = color;
      rGr.relativeWeight = rGr.weight / maxWeight;
      rGr.displayWeight = makeDisplayWeight( rGr.weight );
      rGr.enabled = true;
      rGr.expanded = false;
      rGr.children = rAttrs;
      rGr.ele = rGr.attributeGroup;

      rAttrs.sort( sortByWeight );
    }

    // process the interactions
    var intnMaxAbsW = _.max( rInteractions.map(function( rIntn ){ return rIntn.absoluteWeight; }) );
    for( var i = 0; i < rInteractions.length; i++ ){
      var rIntn = rInteractions[i];

      rIntn.absoluteWeightPercent = rIntn.absoluteWeight / intnMaxAbsW;
    }

    rAllGrs.sort( sortByWeight );
  };

  rfn.loadGraph = function( opts ){
    var self = this;
    var eles = [];
    var id2AttrEle = {};

    var pos = {};
    if( opts.positions ){
      for( var i = 0; i < opts.positions.length; i++ ){
        var p = opts.positions[i];

        pos[ p.id ] = p.position;
      }
    }

    cy.startBatch();
    var oldEles = cy.elements();
    var oldElesById = {};
    var someOldEles = false;

    for( var i = 0; i < oldEles.length; i++){
      oldElesById[ oldEles[i].id() ] = oldEles[i];
    }

    var initNodePos = function(){
      return { x: cy.width()/2, y: cy.height()/2 };
    };

    var copy = function(o){
      return $.extend({}, o);
    };

    var getNodePos = function( id ){
      var oldEle = oldElesById[ id ];

      return pos[ id ] ? pos[ id ] : oldEle ? copy( oldEle.position() ) : initNodePos();
    };

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
        position: getNodePos( gene.id ),
        //locked: !!oldEle,
        data: {
          oldEle: !!oldEle,
          id: '' + gene.id,
          idInt: gene.id,
          name: rGene.name,
          nameDescr: rGene.name + '\n' + rGene.gene.node.geneData.description.replace('[Source', '\n[Source'),
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
        var oldEle = oldElesById[ attr.id ];

        if( !attrEle ){
          eles.push( attrEle = {
            group: 'nodes',
            position: getNodePos( attr.id ),
            data: {
              id: '' + attr.id,
              idInt: attr.id,
              name: attr.name,
              nameDescr: attr.name,
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
            attributeGroupId: self.resultAttributesById[ rAttr.attribute.id ].resultAttributeGroup.attributeGroup.id,
            attributeId: attr.id
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
              absoluteWeight: rIntn.absoluteWeight,
              absoluteWeightPercent: rIntn.absoluteWeightPercent,
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

    // get average connected scores for query nodes
    var qNodes = nodes.filter('[?query]');

    for( var i = 0; i < qNodes.length; i++ ){
      var node = qNodes[i];
      var avgConndScore = 0;
      var totalWeights = 0;

      var edges = node.connectedEdges().forEach(function( edge ){
        var otherNode = edge.connectedNodes().not( node );

        if( otherNode.data('query') ){
          return;
        }

        avgConndScore += otherNode.data('score') * edge.data('weight');
        totalWeights += edge.data('weight');
      });

      avgConndScore /= totalWeights;

      if( totalWeights === 0 ){
        avgConndScore = 0;
      }

      node.data('avgConndScore', avgConndScore);
    }

    var newEles = cy.elements();

    newEles.addClass('hidden');

    cy.endBatch(); // will trigger new stylesheet etc

    var $list = $('#network-list');
    var container = cy.container();


    var delay = function( l ){
      return new Promise(function( resolve ){
        setTimeout(resolve, l);
      });
    };

    if( !opts.positions ){

      self.layoutResizeCyPre();

      cy.pon('layoutready').then(function(){
        newEles.removeClass('hidden');
      });

      return self.forceLayout({
        animate: false,
        fit: true,
        randomize: true,
        resizeCy: false
      }).then(function(){
        self.layoutResizeCyPost();
      });

    } else {
      self.layoutResizeCyPre();

      newEles.removeClass('hidden');

      return self.fitGraph({
        duration: 0,
        resizeCy: false
      }).then(function(){
        self.layoutResizeCyPost();
      });
    }

  };

  // inject the individual query submodules
  for( var i = 0; i < rmods.length; i++ ){
    rmods[i]( r );
  }

  return r;

} ]);

app.controller('ResultCtrl',
[ '$scope', 'updateScope', 'cy',
function( $scope, updateScope, ngCy ){

  function init(){
    $scope.query = Query.current;
    $scope.result = $scope.query.result;

    updateScope();
  }

  PubSub.subscribe('result.searched', init);
  PubSub.subscribe('result.error', updateScope);
  PubSub.subscribe('result.layoutUndone', updateScope);

} ]);
