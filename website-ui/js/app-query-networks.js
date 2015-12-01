'use strict';

app.factory('Query_networks',
[ 'util', '$$networks',
function( util, $$networks ){ return function( Query ){

  var q = Query;
  var qfn = q.prototype;

  //
  // NETWORKS

  qfn.toggleNetworksToMatchParams = function( params, pub ){
    var query = this;

    for( var i = 0; i < query.networks.length; i++ ){
      var net = query.networks[i];

      query.toggleNetworkSelection( net.id, false, false );
    }

    for( var i = 0; i < params.networkIds.length; i++ ){
      var netId = params.networkIds[i];
      var net = this.getNetwork( netId );

      query.toggleNetworkSelection( net.id, true, false );
    }

    if( pub || pub === undefined ){
      PubSub.publish( 'query.toggleNetworksToMatchParams', {
        query: query,
        params: params
      } );
    }
  };

  qfn.updateNetworkGroupSelection = function( group ){
    var selCount = group.selectedCount;
    var netCount = group.interactionNetworks ? group.interactionNetworks.length : 0;

    if( selCount === 0 ){
      group.selected = false;
    } else if( selCount === netCount ){
      group.selected = true;
    } else {
      group.selected = 'semi';
    }
  };

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
    this.updateNetworkGroupSelection( net.group );

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
      var setter = _.find( this.setNetworkOptions, { name: nets } );

      if( !setter ){ return; } // can't set w/o setter
      setter = setter.setter; // we only want the function

      for( var i = 0; i < this.networks.length; i++ ){
        var network = this.networks[i];

        if( setter( network ) ){
          this.selectNetwork( network, false );
        } else {
          this.unselectNetwork( network, false );
        }
      }

      for( var i = 0; i < this.attributeGroups.length; i++ ){
        var attrGr = this.attributeGroups[i];

        this.toggleAttributeGroupSelection( attrGr, setter( attrGr ), false );
      }
    }

    //this.showingNetworkCheckOptions = false; // because we set

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

  qfn.sortNetworksBy = function( factor, autoOpen ){
    var self = this;

    if( autoOpen === undefined ){
      autoOpen = true;
    }

    factor = _.find(self.networkSortFactors, function(f){
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

      if( self.attributeGroups ){
        try{
          self.attributeGroups.sort( factor.sorter );
        } catch(err){
          // if attr sorting fails, we don't care since the sorting doesn't apply then
        }
      }

      var grsExpd = this.networkGroups.filter(function( gr ){
        return gr.expanded;
      }).length !== 0;

      if( autoOpen && !grsExpd ){
        self.toggleNetworkGroupExpansion( this.networkGroups[0], true );
      }

      //this.showingNetworkSortOptions = false; // because we've set it

      PubSub.publish('query.sortNetworksBy', {
        factor: factor,
        query: this
      });
    }
  };

  qfn.addNetwork = function( file ){
    var self = this;

    var readFile = function(){
      return new Promise(function( resolve, reject ){
        var reader = new FileReader();

        reader.addEventListener('loadend', function(){
          resolve({
            name: file.name,
            contents: reader.result
          });
        });

        reader.addEventListener('error', function(){
          reject('File could not be read');
        });

        reader.readAsText( file );
      });
    };

    self.addingNetwork = true;

    PubSub.publish('query.addingNetwork', this);

    return readFile().then(function( file ){
      return $$networks.add({
        organismId: self.organism.id,
        file: file.contents,
        fileName: file.name
      });
    }).then(function( net ){
      config.networks.postprocess( net );

      var gr = self.getUploadNetworkGroup();

      // add to group
      if( !gr.interactionNetworks ){
        gr.interactionNetworks = [];
      }

      net.group = gr;
      gr.interactionNetworks.push( net );

      // add to master list
      self.networks.push( net );

      // add to id map
      self.networksById[ net.id ] = net;

      // check
      self.toggleNetworkSelection( net, net.defaultSelected, false );

      self.addingNetwork = false;

      PubSub.publish('query.addNetwork', this);
    });
  };

  qfn.getUploadNetworkGroup = function(){
    return this.networkGroups.filter(function( gr ){
      return gr.code === 'uploaded';
    })[0];
  };

  qfn.removeNetwork = function( net ){
    var self = this;

    net.removing = true;
    PubSub.publish('query.removingNetwork', this);

    return $$networks.remove({
      organismId: self.organism.id,
      networkId: net.id
    }).then(function(){
      var removeFromList = function( list ){
        for( var i = 0; i < list.length; i++ ){
          if( list[i].id === net.id ){
            list.splice( i, 1 );
            break;
          }
        }
      }

      // uncheck
      self.toggleNetworkSelection( net, false, false );

      // remove from master list
      removeFromList( self.networks );

      // remove from id map
      self.networksById[ net.id ] = null;

      // remove from group
      var gr = self.getUploadNetworkGroup();

      removeFromList( gr.interactionNetworks );

      PubSub.publish('query.removeNetwork', this);
    });
  };


} } ]);
