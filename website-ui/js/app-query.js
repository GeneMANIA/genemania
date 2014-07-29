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

  // get the next query in the history
  qfn.next = function(){};

  // get the previous query in the history
  qfn.prev = function(){};

  // make this query the current/active one
  qfn.activate = function(){};

  // organism
  qfn.setOrganism = function( org ){ 
    this.organism = org;

    PubSub.publish('query.set_organism', self);

    this.validateGenes(); // new org => new genes validation
  };

  // genes
  qfn.addGenes = function(){};
  qfn.removeGenes = function(){};
  qfn.setGenes = function(){};
  qfn.removeAllGenes = function(){};

  // ui sets genes from text box => validate
  qfn.setGenesFromText = _.debounce( function(){
    this.validateGenes();

  }, config.query.genesValidationDelay, {
    leading: false,
    trailing: true
  });

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

    self.validatingGenes = true;
    PubSub.publish('query.validate_genes_start', self);

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

          PubSub.publish('query.validate_genes', self);
        })
      ;
    } else {
      p = Promise.resolve().cancellable().then(function(){
        self.validatingGenes = false;
        self.geneValidations = [];
        
        PubSub.publish('query.validate_genes', self);
      });
    }

    return self.prevValidateGenes = p;
  };

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
[ '$scope', 'Query', '$timeout',
function( $scope, Query, $timeout ){

  function updateScope(){
    $timeout(function(){});
  }

  // initialise once whole app is ready
  function init(){
    window.query = $scope.query = Query.current;

    updateScope();
  }

  PubSub.subscribe('ready', init);
  PubSub.subscribe('query.search_result', init);

  PubSub.subscribe('query.validate_genes', updateScope);
  PubSub.subscribe('query.validate_genes_start', updateScope);


} ]);