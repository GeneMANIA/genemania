app.factory('query', 
[ 'bootstrapper', '$$organisms', 
function( $$organisms ){

  console.log('query init');

  var pOrgs = $$organisms();
  bootstrapper( pOrgs );

  var q = function(){

  };

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

  // current query (only one at a time)
  q.current = new q();

  // get a new query that contains the defaults
  q.defaults = function( opts ){

  };

  // get the next query in the history
  qfn.next = function(){};

  // get the previous query in the history
  qfn.prev = function(){};

  // genes
  qfn.addGenes = function(){};
  qfn.removeGenes = function(){};
  qfn.setGenes = function(){};
  qfn.removeAllGenes = function(){};

  // weighting
  qfn.setWeighting = function(){};

  // networks
  qfn.enableNetwork = function(){};
  qfn.disableNetwork = function(){};
  qfn.setNetworks = function(){};

  // results size
  qfn.setMaxGenes = function(){};
  qfn.setMaxAttrs = function(){};

  // search using this query, thereby superseding the previous query (i.e. this is current)
  qfn.search = function(){};

  return q;

} ]);


app.controller('QueryCtrl',
[ '$scope', 'query',
function( $scope, query ){

  $scope.foo = 'bar';

} ]);