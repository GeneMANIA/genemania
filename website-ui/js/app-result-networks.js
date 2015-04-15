app.factory('Result_networks', 
[ 'util',
function( util ){ return function( Result ){
  
  var r = Result;
  var rfn = r.prototype;


  // toggling the sidebar

  rfn.toggleNetworksExpansion = function(){
    if( this.networksExpanded ){
      this.collapseNetworks();
    } else {
      this.expandNetworks();
    }

    PubSub.publish('result.toggleNetworksExpansion', this);
  };

  rfn.expandNetworks = function(){
    rfn.networksExpanded = true;

    PubSub.publish('result.expandNetworks', this);
  };

  rfn.collapseNetworks = function(){
    rfn.networksExpanded = false;

    PubSub.publish('result.collapseNetworks', this);
  };

  // toggling the individual nets

  rfn.toggleNetwork = function( rNet ){     
    var netEnabled = rNet.enabled = !rNet.enabled;
    var rGr = rNet.resultNetworkGroup;
    var rNets = rGr.resultNetworks;
    var anyEnabled = netEnabled;
    var allEnabled = netEnabled;

    for( var i = 0; i < rNets.length; i++ ){
      var iEnabled = rNets[i].enabled;

      anyEnabled = anyEnabled || iEnabled;
      allEnabled = allEnabled && iEnabled;
    }

    if( allEnabled ){
      rGr.enabled = true;
    } else if( anyEnabled ){
      rGr.enabled = 'semi';
    } else {
      rGr.enabled = false;
    }

    this.filterNetworksFromEnables({ invalidateCache: true });
    PubSub.publish('result.toggleNetwork', {
      result: this,
      network: rNet
    });
  };

  rfn.toggleNetworkGroup = function( rGr ){
    var en = !rGr.enabled || rGr.enabled === 'semi' ? true : false;

    rGr.enabled = en;

    var rNets = rGr.resultNetworks;
    for( var i = 0; i < rNets.length; i++ ){
      var rNet = rNets[i];

      rNet.enabled = rGr.enabled;
    }

    this.filterNetworksFromEnables({ invalidateCache: true });
    PubSub.publish('result.toggleNetworkGroup', {
      result: this,
      networkGroup: rGr
    });
  };

  rfn.filterNetworksFromEnables = function( opts ){
    opts = opts || {};

    var invalCache = opts.invalidateCache || !this.netEnabled;
    var netEnabled = this.netEnabled;

    if( invalCache ){
      netEnabled = this.netEnabled = {};

      var rGrs = this.resultNetworkGroups;
      for( var i = 0; i < rGrs.length; i++ ){
        var rGr = rGrs[i];
        var rNets = rGr.resultNetworks;

        for( var j = 0; j < rNets.length; j++ ){
          var rNet = rNets[j];
          var id = rNet.network.id;
          var strId = '' + id;

          netEnabled[ strId ] = rNet.enabled;
        }
      }
    }

    var edges = cy.edges();
    var nonfilteredEdges = edges.stdFilter(function( edge ){
      var strId = '' + edge.data('networkId');

      return netEnabled[ strId ];
    });
    var filteredEdges = edges.not( nonfilteredEdges );

    cy.batch(function(){
      edges.removeClass('filtered');
      filteredEdges.addClass('filtered');
    });

    PubSub.publish('result.filterNetworksFromEnables', {
      result: this
    });
  };

  rfn.toggleNetworkGroupExpansion = function( rGr ){
    rGr.expanded = !rGr.expanded;

    PubSub.publish('result.toggleNetworkGroupExpansion', {
      result: this,
      networkGroup: rGr
    });
  };

  rfn.toggleNetworkExpansion = function( rNet ){
    rNet.expanded = !rNet.expanded;

    PubSub.publish('result.toggleNetworkGroupExpansion', {
      result: this,
      network: rNet
    });
  };
  

} } ]);


app.controller('NetworksCtrl',
[ '$scope', 'updateScope', 'cy',
function( $scope, updateScope, cy ){

  function init(){
    $scope.query = Query.current;
    $scope.result = $scope.query.result;

    updateScope();
  }

  PubSub.subscribe('result.searched', init);

} ]);