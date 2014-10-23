app.factory('Query', 
[ '$$organisms', '$$networks', '$$attributes', 'util', '$$genes', 'Result',
function( $$organisms, $$networks, $$attributes, util, $$genes, Result ){
  var copy = util.copy;
  var strcmp = util.strcmp;

  var netSetOpts = ['all', 'none', 'default'];

  var netSortFactors = [
    {
      name: 'first author',
      sorter: function(a, b){
        var aAuth = a.metadata.firstAuthor.toLowerCase();
        var bAuth = b.metadata.firstAuthor.toLowerCase();

        return strcmp(aAuth, bAuth);
      }
    },

    {
      name: 'last author',
      sorter: function(a, b){
        var aAuth = a.metadata.lastAuthor.toLowerCase();
        var bAuth = b.metadata.lastAuthor.toLowerCase();

        return strcmp(aAuth, bAuth);
      }
    },

    {
      name: 'size',
      sorter: function(a, b){
        var aSize = a.metadata.interactionCount;
        var bSize = b.metadata.interactionCount;

        return bSize - aSize; // bigger first
      }
    },

    {
      name: 'date',
      sorter: function(a, b){
        var aYr = parseInt( a.metadata.yearPublished, 10 );
        var bYr = parseInt( b.metadata.yearPublished, 10 );

        return bYr - aYr; // newer first
      }
    }
  ];

  var organisms;
  var networkGroups;
  var attributeGroups;

  // when all resources are pulled in, the query is ready
  Promise.all([
    
    $$organisms().then(function( orgs ){
      organisms = orgs;
    }),

    $$networks().then(function( nets ){
      networkGroups = nets;
    }),

    $$attributes().then(function( attrs ){
      attributeGroups = attrs;
    })

  ]).then(function(){
    // current query (only one at a time)
    q.current = new q();
    qfn.ready = true;

    console.log('Query ready');
    PubSub.publish('query.ready', q.current);
  });

  PubSub.subscribe('ready', function(){
    qfn.appReady = true;
  });

  function Query( opts ){
    if( !(this instanceof Query) ){
      return new Query( opts );
    }

    // set defaults
    var self = this;

    self.organisms = copy( organisms );
    self.organism = _.find( self.organisms, function( o ){ // default org is human
      return o.id === 4;
    } ) || self.organisms[0]; // fallback on first org

    self.networkSortFactors = netSortFactors;
    self.setNetworkOptions = netSetOpts;
    self.weighting = config.networks.defaultWeighting;

    updateQParamsFromOrg( self );

    self.sortNetworksBy('first author');
  };
  var q = Query;
  var qfn = q.prototype;

  function updateQParamsFromOrg( self ){
    self.networkGroups = copy( networkGroups[ self.organism.id ] );
    self.organism.networkGroups = self.networkGroups;
    self.networkGroupsById = {};

    self.networks = [];
    self.networksById = {};
    for( var i = 0; i < self.networkGroups.length; i++ ){
      var group = self.networkGroups[i];
      var nets = group.interactionNetworks;
      var selCount = 0;

      group.expanded = false;

      self.networkGroupsById[ group.id ] = group;

      if( nets ){ for( var j = 0; j < nets.length; j++ ){
        var net = nets[j];

        self.networks.push( net );
        self.networksById[ net.id ] = net;

        net.group = group;
        net.selected = net.defaultSelected;
        net.expanded = false;

        if( net.selected ){
          selCount++;
        }

      } }

      group.selectedCount = selCount;

      updateNetworkGroupSelection( group );
      
    }

    self.attributeGroups = copy( attributeGroups[ self.organism.id ] );
  }

  // ref some stuff into query
  qfn.weightings = config.networks.weightings; // flat list of weighting types
  qfn.weightingGroups = config.networks.weightingGroups; // categorised groups of weightings used in ui
  qfn.$$search = $$search;

  // 
  // EXPANDING AND COLLAPSING THE QUERY INTERFACE

  qfn.expanded = true;

  qfn.collapse = function(){
    qfn.expanded = false;
  };

  qfn.expand = function(){
    qfn.expanded = true;
  };

  qfn.toggleExpand = function(){
    qfn.expanded = !qfn.expanded;
  };

  //
  // NAVIGATING THE QUERY HISTORY

  // get the next query in the history
  qfn.next = function(){};

  // get the previous query in the history
  qfn.prev = function(){};

  // make this query the current/active one
  qfn.activate = function(){};

  // search using this query, thereby superseding the previous query (i.e. this is current)
  qfn.search = function(){
    var query = this;
    var result = query.result = new Result({
      query: query
    });

    qfn.splashed = true;

    PubSub.publish('query.search', this);
  };

  qfn.searchOrCancel = function(){
    var query = this;

    if( query.result && query.result.cancellable() ){
      query.result.cancel();
    } else {
      query.search();
    }
  };
  

  //
  // ORGANISM

  qfn.setOrganism = function( org ){ 
    this.organism = org;

    updateQParamsFromOrg( this );

    PubSub.publish('query.setOrganism', self);

    this.validateGenes(); // new org => new genes validation
  };


  //
  // GENES

  qfn.addGenes = function(){};
  qfn.removeGenes = function(){};
  qfn.setGenes = function(){};
  qfn.removeAllGenes = function(){};

  qfn.describeGeneLine = function(){
    // TODO
  };

  // internal helper function for setGenesFromText()
  qfn.validateGenesFromText = _.debounce( function(){
    this.validateGenes();

  }, config.query.genesValidationDelay, {
    leading: false,
    trailing: true
  });

  // ui sets genes from text box => validate
  qfn.setGenesFromText = function(){
    var self = this;

    self.settingGenes = true;

    PubSub.publish('query.setGenesText', self);
    self.validateGenesFromText();
  };

  qfn.setExampleGenes = function(){
    var self = this;

    self.settingGenes = true;

    //$('#query-genes-textarea').css('white-space', 'normal'); // ios8 hack

    self.genesText = self.organism.defaultGenes.map(function( g ){
      return g.symbol;
    }).join('\n');

    PubSub.publish('query.setGenesTextFromCode', self);
    self.validateGenesFromText();
  };

  qfn.updateGenesArea = function(){
    $('#query-genes-textarea').trigger('autosize.resize');
  };

  // PubSub.promise('query.ready').then(function(){
  //   $textarea.autosize({
  //     callback: function(){
  //       var textarea = document.getElementById('query-genes-textarea');
  //       var genesVal = document.getElementById('query-genes-validation');

  //       genesVal.style.height = textarea.style.height;
  //     }
  //   });
  // });

  // validate genes directly
  qfn.validateGenes = function(){
    var self = this;
    var txt = this.genesText;
    var p;
    var prev = self.prevValidateGenes;

    self.settingGenes = false;
    self.validatingGenes = true;
    PubSub.publish('query.validateGenesStart', self);

    if( prev ){
      prev.cancel('Cancelling stale gene validation query');
    }

    if( txt && !txt.match(/^\s+$/) ){
      p = $$genes.validate({
        organism: this.organism.id,
        genes: txt
      })
        .cancellable()

        .then(function( t ){
          self.validatingGenes = false;
          var geneValns = self.geneValidations = t.genes;

          var geneSpcks = '';
          for( var i = 0; i < geneValns.length; i++ ){
            var geneValn = geneValns[i];
            var last = i === geneValns.length - 1;
            var spck = '';

            if( geneValn.type === 'INVALID' ){
              while( spck.length < geneValn.name.length ){
                spck += '_';
              }
            }

            geneSpcks += spck + ( last ? '' : '\n' );
          }
          self.geneSpellchecks = geneSpcks;

          PubSub.publish('query.validateGenes', self);
        })
      ;
    } else {
      p = Promise.resolve().cancellable().then(function(){
        self.validatingGenes = false;
        self.geneValidations = [];
        self.geneSpellchecks = '';
        
        PubSub.publish('query.validateGenes', self);
      });
    }

    return self.prevValidateGenes = p;
  };

  qfn.expandGenes = function(){
    this.genesExpanded = true;

    PubSub.publish('query.expandGenes', this);
  };

  qfn.collapseGenes = function(){
    this.genesExpanded = false;

    PubSub.publish('query.collapseGenes', this);
  };

  // weighting
  qfn.setWeighting = function( w ){
    this.weighting = w;
  };

  qfn.toggleEditWeighting = function(){
    this.editingWeighting = this.editingWeighting ? false : true;
  };


  //
  // NETWORKS

  function updateNetworkGroupSelection( group ){
    var selCount = group.selectedCount;
    var netCount = group.interactionNetworks ? group.interactionNetworks.length : 0;

    if( selCount === 0 ){
      group.selected = false;
    } else if( selCount === netCount ){
      group.selected = true;
    } else {
      group.selected = 'semi';
    }
  }

  qfn.getNetwork = function( idOrNet ){
    if( $.isPlainObject( idOrNet ) ){
      var net = idOrNet;
      return net;
    } else {
      var id = idOrNet;
      return this.networksById[ id ];
    }
  };

  qfn.getNetworkGroup = function( idOrGr ){
    if( $.isPlainObject( idOrGr ) ){
      var gr = idOrGr;
      return gr;
    } else {
      var id = idOrGr;
      return this.networkGroupsById[ id ];
    }
  };

  qfn.toggleNetworkSelection = function( net, sel, pub ){
    net = this.getNetwork( net );
    sel = sel === undefined ? !net.selected : sel; // toggle if unspecified selection state

    if( net.selected === sel ){ return; } // update unnecessary

    net.selected = sel;
    net.group.selectedCount += sel ? 1 : -1;
    updateNetworkGroupSelection( net.group );

    if( pub || pub === undefined ){
      pub = { network: net, query: this, selected: sel };
      PubSub.publish( sel ? 'query.selectNetwork' : 'query.unselectNetwork', pub );
      PubSub.publish( 'query.toggleNetworkSelection', pub );
    }
  };
  qfn.selectNetwork = function( net, pub ){ this.toggleNetworkSelection(net, true, pub); };
  qfn.unselectNetwork = function( net, pub ){ this.toggleNetworkSelection(net, false, pub); };

  qfn.toggleNetworkGroupSelection = function( group, sel ){
    group = this.getNetworkGroup( group );

    if( sel === undefined ){ // toggle if unspecified selection state
      sel = !group.selected || group.selected === 'semi' ? true : false;
    }

    var nets = group.interactionNetworks;
    for( var i = 0; i < nets.length; i++ ){
      var net = nets[i];

      this.toggleNetworkSelection( net.id, sel );
    }

    var pub = { query: this, group: group, selected: sel };
    PubSub.publish( sel ? 'query.selectNetworkGroup' : 'query.unselectNetworkGroup', pub );
    PubSub.publish( 'query.toggleNetworkGroupSelection', pub );
  };
  qfn.selectNetworkGroup = function( gr ){ this.toggleNetworkGroupSelection(gr, true); };
  qfn.unselectNetworkGroup = function( gr ){ this.toggleNetworkGroupSelection(gr, false); };

  qfn.toggleNetworkExpansion = function( net, exp ){
    net = this.getNetwork( net );
    exp = exp === undefined ? !net.expanded : exp; // toggle if unspecified

    if( net.expanded === exp ){ return net.expanded; } // update unnecessary

    net.expanded = exp;

    var pub = { network: net, query: this, expanded: exp };
    PubSub.publish( exp ? 'query.expandNetwork' : 'query.collapseNetwork', pub );
    PubSub.publish( 'query.toggleNetworkExpansion', pub );

    return net.expanded;
  };
  qfn.expandNetwork = function( net ){ return this.toggleNetworkExpansion(net, true); };
  qfn.collapseNetwork = function( net ){ return this.toggleNetworkExpansion(net, false); };

  qfn.toggleNetworkGroupExpansion = function( group, exp ){
    group = this.getNetworkGroup( group );
    exp = exp === undefined ? !group.expanded : exp; // toggle if unspecified

    group.expanded = exp;

    var pub = { group: group, query: this, expanded: exp };
    PubSub.publish( exp ? 'query.expandNetworkGroup' : 'query.collapseNetworkGroup', pub );
    PubSub.publish( 'query.toggleNetworkGroupExpansion', pub );

    return group.expanded;
  };

  // for an array of network objects { id, selected }, set selected
  qfn.setNetworks = function( nets ){
    if( _.isArray(nets) ){
      for( var i = 0; i < nets.length; i++ ){
        var net = nets[i];

        net.selected ? this.selectNetwork( net.id, false ) : this.unselectNetwork( net.id, false );
      }
    } else if( _.isString(nets) ){
      for( var i = 0; i < this.networks.length; i++ ){
        var network = this.networks[i];

        if( nets === 'all' || (nets === 'default' && network.defaultSelected) ){
          this.selectNetwork( network, false );
        } else {
          this.unselectNetwork( network, false );
        }
      }
    }

    this.showingNetworkCheckOptions = false; // because we set

    PubSub.publish( 'query.setNetworks', {
      query: this
    } );
  };

  qfn.toggleNetworkCheckOptions = function(){
    this.showingNetworkCheckOptions = this.showingNetworkCheckOptions ? false : true;

    PubSub.publish('query.toggleNetworkCheckOptions', {
      shown: this.showingNetworkCheckOptions,
      query: this
    });
  };

  qfn.toggleNetworkSortOptions = function(){
    this.showingNetworkSortOptions = this.showingNetworkSortOptions ? false : true;

    PubSub.publish('query.toggleNetworkSortOptions', {
      shown: this.showingNetworkSortOptions,
      query: this
    });
  };

  qfn.sortNetworksBy = function( factor ){
    factor = _.find(netSortFactors, function(f){
      return f.name === factor || f === factor;
    });

    if( factor ){

      this.selectedNetworkSortFactor = factor;

      for( var i = 0; i < this.networkGroups.length; i++ ){
        var gr = this.networkGroups[i];
        var nets = gr.interactionNetworks;

        if( nets ){
          nets.sort( factor.sorter );
        }
      }

      this.showingNetworkSortOptions = false; // because we've set it

      PubSub.publish('query.sortNetworksBy', {
        factor: factor,
        query: this
      });
    }
  };


  //
  // MAX RETURN PARAMS

  // results genes size
  qfn.setMaxGenes = function( max ){
    this.maxGenes = max;
  };

  // results attrs size
  qfn.setMaxAttrs = function( max ){
    this.maxAttrs = max;
  };


  return q;

} ]);


app.controller('QueryCtrl',
[ '$scope', 'Query', 'updateScope',
function( $scope, Query, updateScope ){

  // initialise once whole app is ready
  function init(){
    window.query = $scope.query = Query.current;

    updateScope();
  }

  PubSub.subscribe('ready', init);
  PubSub.subscribe('query.searchResult', init);

  PubSub.subscribe('query.validateGenes', updateScope);
  PubSub.subscribe('query.validateGenesStart', updateScope);

  PubSub.subscribe('query.setGenesText', _.debounce(function(){
    updateScope();
  }, 50, {
    leading: true
  }));

  PubSub.subscribe('query.setGenesTextFromCode', _.debounce(function(){
    updateScope();
    $scope.query.updateGenesArea();
  }, 50, {
    leading: true
  }));

  PubSub.subscribe('query.toggleNetworkGroupExpansion', updateScope);
  PubSub.subscribe('query.toggleNetworkExpansion', updateScope);
  PubSub.subscribe('query.toggleNetworkGroupSelection', updateScope);
  PubSub.subscribe('query.toggleNetworkSelection', updateScope);
  PubSub.subscribe('query.toggleNetworkCheckOptions', updateScope);
  PubSub.subscribe('query.toggleNetworkSortOptions', updateScope);
  PubSub.subscribe('query.sortNetworksBy', updateScope);
  PubSub.subscribe('query.setNetworks', updateScope);

  PubSub.subscribe('ready', updateScope);

  PubSub.subscribe('result.searched', updateScope);
  PubSub.subscribe('result.cancel', updateScope);
  PubSub.subscribe('$$search.progress', updateScope);
  

} ]);