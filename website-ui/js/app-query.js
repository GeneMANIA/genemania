app.factory('Query', 
[ '$$organisms', '$$networks', '$$attributes', 'util', '$$genes',
function( $$organisms, $$networks, $$attributes, util, $$genes ){
  var copy = util.copy;

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

    PubSub.publish('query.ready', q.current);
  });

  function Query( opts ){
    // set defaults
    var self = this;

    self.organisms = copy( organisms );
    self.organism = _.find( self.organisms, function( o ){ // default org is human
      return o.id === 4;
    } ) || self.organisms[0]; // fallback on first org

    self.networkGroups = copy( networkGroups[ self.organism.id ] );
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
  };
  var q = Query;

  // flat list of weighting types
  var wg = q.weightings = [
    { name: 'Automatically selected weighting method', value: 'AUTOMATIC_SELECT' },
    { name: 'Assigned based on query genes', value: 'AUTOMATIC' },
    { name: 'Biological process based', value: 'BP' },
    { name: 'Molecular function based', value: 'MF' },
    { name: 'Cellular component based', value: 'CC' },
    { name: 'Equal by network', value: 'AVERAGE' },
    { name: 'Equal by data type', value: 'AVERAGE_CATEGORY' }
  ];

  // allow getting weight by const/val
  for( var i = 0; i < q.weightings.length; i++ ){
    var w = q.weightings[i];

    q.weightings[ w.value ] = w;
  }

  // categorised groups of weightings used in ui
  q.weightingGroups = [
    { name: 'Query-dependent weighting', weightings: [ wg.AUTOMATIC_SELECT, wg.AUTOMATIC ] },
    { name: 'Gene Ontology (GO) weighting', weightings: [ wg.BP, wg.MF, wg.CC ] },
    { name: 'Equal weighting', weightings: [ wg.AVERAGE, wg.AVERAGE_CATEGORY ] }
  ];

  var qfn = q.prototype;


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
  qfn.search = function(){};
  

  //
  // ORGANISM

  qfn.setOrganism = function( org ){ 
    this.organism = org;

    PubSub.publish('query.setOrganism', self);

    this.validateGenes(); // new org => new genes validation
  };


  //
  // GENES

  qfn.addGenes = function(){};
  qfn.removeGenes = function(){};
  qfn.setGenes = function(){};
  qfn.removeAllGenes = function(){};


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

  var $textarea;
  var $genesVal;
  $(function(){
    $textarea = $('#query-genes-textarea');
    $genesVal = $('#query-genes-validation');

    $textarea.autosize({
      callback: function(){
        $genesVal[0].style.height = $textarea[0].style.height;
      }
    });
  });

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
          self.geneValidations = t.genes;

          PubSub.publish('query.validateGenes', self);
        })
      ;
    } else {
      p = Promise.resolve().cancellable().then(function(){
        self.validatingGenes = false;
        self.geneValidations = [];
        
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

  qfn.toggleNetworkSelection = function( net, sel ){
    net = this.getNetwork( net );
    sel = sel === undefined ? !net.selected : sel; // toggle if unspecified selection state

    if( net.selected === sel ){ return; } // update unnecessary

    net.selected = sel;
    net.group.selectedCount += sel ? 1 : -1;
    updateNetworkGroupSelection( net.group );

    var pub = { network: net, query: this, selected: sel };
    PubSub.publish( sel ? 'query.selectNetwork' : 'query.unselectNetwork', pub );
    PubSub.publish( 'query.toggleNetworkSelection', pub );
  };
  qfn.selectNetwork = function( net ){ this.toggleNetworkSelection(net, true); };
  qfn.unselectNetwork = function( net ){ this.toggleNetworkSelection(net, false); };

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

    if( net.expanded === exp ){ return; } // update unnecessary

    net.expanded = exp;

    var pub = { network: net, query: this, expanded: exp };
    PubSub.publish( exp ? 'query.expandNetwork' : 'query.collapseNetwork', pub );
    PubSub.publish( 'query.toggleNetworkExpansion', pub );
  };
  qfn.expandNetwork = function( net ){ this.toggleNetworkExpansion(net, true); };
  qfn.collapseNetwork = function( net ){ this.toggleNetworkExpansion(net, false); };

  qfn.toggleNetworkGroupExpansion = function( group, exp ){
    group = this.getNetworkGroup( group );
    exp = exp === undefined ? !group.expanded : exp; // toggle if unspecified

    group.expanded = exp;

    var pub = { group: group, query: this, expanded: exp };
    PubSub.publish( exp ? 'query.expandNetworkGroup' : 'query.collapseNetworkGroup', pub );
    PubSub.publish( 'query.toggleNetworkGroupExpansion', pub );
  };

  // for an array of network objects { id, selected }, set selected
  qfn.setNetworks = function( nets ){
    for( var i = 0; i < nets.length; i++ ){
      var net = nets[i];

      net.selected ? this.selectNetwork( net.id ) : this.unselectNetwork( net.id );
    }
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
[ '$scope', 'Query', '$timeout',
function( $scope, Query, $timeout ){

  var lastUpdate;
  function updateScope(){
    lastUpdate && $timeout.cancel(lastUpdate);
    lastUpdate = $timeout(function(){}, 0);
  }

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

  PubSub.subscribe('query.toggleNetworkGroupExpansion', updateScope);
  PubSub.subscribe('query.toggleNetworkExpansion', updateScope);
  PubSub.subscribe('query.toggleNetworkGroupSelection', updateScope);
  PubSub.subscribe('query.toggleNetworkSelection', updateScope);
  PubSub.subscribe('query.toggleNetworkCheckOptions', updateScope);
  PubSub.subscribe('query.toggleNetworkSortOptions', updateScope);
  

} ]);