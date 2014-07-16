app.factory('Query', 
[ '$$organisms', '$$networks', '$$attributes', 'util', '$$genes',
function( $$organisms, $$networks, $$attributes, util, $$genes ){
  var copy = util.copy;

  console.log('query init');

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
    self.organism = _.find( self.organisms, function( o ){
      return o.taxonomyId === 9606;
    } ) || self.organisms[0];

    self.networkGroups = copy( networkGroups[ self.organism.id ] );

    self.networks = [];
    self.networksById = {};
    for( var i = 0; i < self.networkGroups.length; i++ ){
      var group = self.networkGroups[i];
      var nets = group.interactionNetworks;

      if( nets ){ for( var j = 0; j < nets.length; j++ ){
        var net = nets[j];

        net.selected = net.defaultSelected;
        net.expanded = false;

        self.networks.push( net );
        self.networksById[ net.id ] = net;
      } }
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

  // get the next query in the history
  qfn.next = function(){};

  // get the previous query in the history
  qfn.prev = function(){};

  // genes
  qfn.addGenes = function(){};
  qfn.removeGenes = function(){};
  qfn.setGenes = function(){};
  qfn.removeAllGenes = function(){};

  qfn.setGenesFromText = _.debounce( function(){
    var txt = this.genesText;

    if( txt && !txt.match(/^\s+$/) ){
      $$genes.validate({
        organism: this.organism.id,
        genes: txt
      })
        .then(function( t ){
          console.log( 'val', t );
        })
      ;
    }
  }, config.query.genesValidationDelay, {
    leading: false,
    trailing: true
  });

  qfn.expandGenes = function(){
    this.genesExpanded = true;

    PubSub.publish('query.expand_genes', this);
  };

  qfn.collapseGenes = function(){
    this.genesExpanded = false;

    PubSub.publish('query.collapse_genes', this);
  };

  // weighting
  qfn.setWeighting = function( w ){
    this.weighting = w;
  };

  qfn.enableNetwork = function( id ){
    var net = this.networksById[ id ];
    net.enabled = true;

    PubSub.publish( 'query.enable_network', net );
    PubSub.publish( 'query.toggle_network', net );
  };

  qfn.disableNetwork = function( id ){
    this.networksById[ id ].enabled = false;

    PubSub.publish( 'query.disable_network', net );
    PubSub.publish( 'query.toggle_network', net );
  };

  // for an array of network objects { id, enabled }, set enabled
  qfn.setNetworks = function( nets ){
    for( var i = 0; i < nets.length; i++ ){
      var net = nets[i];

      net.enabled ? this.enableNetwork( net ) : this.disableNetwork( net );
    }
  };

  // results genes size
  qfn.setMaxGenes = function( max ){
    this.maxGenes = max;
  };

  // results attrs size
  qfn.setMaxAttrs = function( max ){
    this.maxAttrs = max;
  };

  // search using this query, thereby superseding the previous query (i.e. this is current)
  qfn.search = function(){};

  return q;

} ]);


app.controller('QueryCtrl',
[ '$scope', 'Query',
function( $scope, Query ){

  // initialise once whole app is ready
  function init(){
    window.query = $scope.query = Query.current;

    $scope.$apply();
  }

  PubSub.subscribe('ready', init);
  PubSub.subscribe('query.search_result', init);

} ]);