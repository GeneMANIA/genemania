'use strict';

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

      if( util.isSmallScreen() && this.query.expanded ){
        this.query.toggleExpand();
      }
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
    var id = rNet.network ? rNet.network.id : rNet.attribute.id;
    var netEnabled = this.getFilterNetworksCache()[ id ] = rNet.enabled = !rNet.enabled;
    var rGr = rNet.resultNetworkGroup || rNet.resultAttributeGroup;
    var rNets = rGr.resultNetworks || rGr.resultAttributes;
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

    this.filterNetworksFromEnables();

    var pub  = {
      result: this,
      network: rNet
    };

    PubSub.publish('result.toggleNetwork', pub);
    PubSub.publish(netEnabled ? 'result.enableNetwork' : 'result.disableNetwork', pub);
  };

  rfn.toggleNetworkGroup = function( rGr ){
    var en = !rGr.enabled || rGr.enabled === 'semi' ? true : false;

    rGr.enabled = en;

    var rNets = rGr.resultNetworks || rGr.resultAttributes;
    for( var i = 0; i < rNets.length; i++ ){
      var rNet = rNets[i];
      var id = rNet.network ? rNet.network.id : rNet.attribute.id;

      this.getFilterNetworksCache()[ id ] = rNet.enabled = rGr.enabled;
    }

    this.filterNetworksFromEnables();

    var pub = {
      result: this,
      networkGroup: rGr
    };

    PubSub.publish('result.toggleNetworkGroup', pub);
    PubSub.publish(en ? 'result.enableNetworkGroup' : 'result.disableNetworkGroup', pub);
  };

  rfn.getFilterNetworksCache = function(){
    if( !this.netEnabled ){
      this.updateFilterNetworksCache();
    }

    return this.netEnabled;
  };

  rfn.updateFilterNetworksCache = function(){
    var netEnabled = this.netEnabled = {};

    var rGrs = this.resultAllGroups;
    for( var i = 0; i < rGrs.length; i++ ){
      var rGr = rGrs[i];
      var rNets = rGr.resultNetworks || rGr.resultAttributes;

      for( var j = 0; j < rNets.length; j++ ){
        var rNet = rNets[j];
        var id = rNet.network ? rNet.network.id : rNet.attribute.id;
        var strId = '' + id;

        netEnabled[ strId ] = rNet.enabled;
      }
    }

    return netEnabled;
  };

  rfn.filterNetworksFromEnables = function( opts ){
    opts = opts || {};

    var invalCache = opts.invalidateCache || !this.netEnabled;
    var netEnabled = this.netEnabled;

    if( invalCache ){
      netEnabled = this.updateFilterNetworksCache();
    }

    var edges = cy.edges();
    var nonfilteredEdges = edges.stdFilter(function( edge ){
      var strId = '' + ( edge.data('attr') ? edge.data('attributeId') : edge.data('networkId') );

      return netEnabled[ strId ];
    });
    var filteredEdges = edges.not( nonfilteredEdges );

    var attrNodes = cy.nodes('[?attr]');
    var nonfilteredNodes = attrNodes.stdFilter(function( node ){
      var strId = '' + node.id();

      return netEnabled[ strId ];
    });
    var filteredNodes = attrNodes.not( nonfilteredNodes );

    cy.batch(function(){
      edges.removeClass('filtered');
      filteredEdges.addClass('filtered');

      attrNodes.removeClass('filtered');
      filteredNodes.addClass('filtered');
    });

    PubSub.publish('result.filterNetworksFromEnables', {
      result: this
    });
  };

  rfn.toggleNetworkGroupExpansion = function( rGr ){
    var en = rGr.expanded = !rGr.expanded;

    var pub = {
      result: this,
      networkGroup: rGr
    };

    PubSub.publish('result.toggleNetworkGroupExpansion', pub);
    PubSub.publish(en ? 'result.expandNetworkGroup' : 'result.collapseNetworkGroup', pub);
  };

  rfn.toggleNetworkExpansion = function( rNet ){
    var en = rNet.expanded = !rNet.expanded;

    var pub = {
      result: this,
      network: rNet
    };

    PubSub.publish('result.toggleNetworkExpansion', pub);
    PubSub.publish(en ? 'result.expandNetwork' : 'result.collapseNetwork', pub);
  };

  rfn.toggleNetworkListEnables = function(){
    this.showNetworkListEnables = !this.showNetworkListEnables;
  };

  rfn.enableNetworksByEnum = function( e ){

    var update = function( self ){
      switch( e ){
        case 'all':
          self.enabled = true;
          break;
        case 'none':
        default:
          self.enabled = false;
      }
    };

    for( var i = 0; i < this.resultAllGroups.length; i++ ){
      var gr = this.resultAllGroups[i];

      update(gr);

      if( gr.children ){ for( var j = 0; j < gr.children.length; j++ ){
        var ch = gr.children[j];

        update(ch);
      } }
    }

    this.filterNetworksFromEnables({ invalidateCache: true });

    PubSub.publish('result.enableNetworksByEnum', {
      result: this
    });

  };

  rfn.toggleNetworkListExpands = function(){
    this.showNetworkListExpands = !this.showNetworkListExpands;
  };

  rfn.expandNetworksByEnum = function( e ){

    var update = function( self ){
      switch( e ){
        case 'all':
          self.expanded = true;
          break;
        case 'top':
          self.expanded = self.children != null;
          break;
        case 'none':
        default:
          self.expanded = false;
      }
    };

    for( var i = 0; i < this.resultAllGroups.length; i++ ){
      var gr = this.resultAllGroups[i];

      update(gr);

      if( gr.children ){ for( var j = 0; j < gr.children.length; j++ ){
        var ch = gr.children[j];

        update(ch);
      } }
    }

    PubSub.publish('result.expandNetworksByEnum', {
      result: this
    });

  };


} } ]);


app.controller('NetworksCtrl',
[ '$scope', 'updateScope', 'cy',
function( $scope, updateScope, ngCy ){

  function init(){
    $scope.query = Query.current;
    $scope.result = $scope.query.result;

    updateScope();
  }

  PubSub.subscribe('result.searched', init);
  PubSub.subscribe('result.updateHighlights', init);

} ]);
