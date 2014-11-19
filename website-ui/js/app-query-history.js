app.factory('Query_history', 
[ 'util', 'Result', 'io', 'cy', 
function( util, Result, io, cy ){ return function( Query ){
  
  var q = Query;
  var qfn = q.prototype;
  var copy = util.copy;

  //
  // NAVIGATING THE QUERY HISTORY

  qfn.store = function(){
    var query = this;
    var ioq = io('queries');

    // store query data params in clientside datastore

    //return util.delayPromise(1000).then(function(){
    return Promise.resolve().then(function(){
      return ioq.read();
    }).then(function( qJson ){
      var history = qJson.history = qJson.history || [];
      var bb = cy.elements().boundingBox();
      var l = Math.max( bb.w, bb.h );
      var idealL = 800;
      var scale = idealL / l;
      var genes = query.genesText.split('\n');

      for( var i = genes.length - 1; i >= 0; i-- ){
        if( genes[i].match(/^\s*$/) ){
          genes.splice( i, 1 );
        }
      }

      history.unshift({
        params: query.params(),
        image: cy.png({ scale: scale, full: true }),
        timestamp: Date.now(),
        genes: genes,
        version: copy( query.version ),
        organismIcon: query.organism.icon
      });

      return ioq.write();
    }).then(function(){
      PubSub.publish('query.store', this);
    });

  };

  qfn.succeed = function( historyEntry ){
    var hEnt = historyEntry;

    var newQuery = Query.current = new Query({
      params: hEnt.params,
      version: hEnt.version
    });

    var result = newQuery.result = new Result({
      query: newQuery,
      store: false
    });

    qfn.splashed = true;

    PubSub.publish('query.succeed', newQuery);
    PubSub.publish('query.search', newQuery);
  };

  qfn.clearHistory = function(){
    return io('queries').delete().then(function(){
      PubSub.publish('query.clearHistory', this);
    });
  };

  // search using this query, thereby superseding the previous query (i.e. this is current)
  qfn.search = function(){
    var query = this;
    var result = query.result = new Result({
      query: query
    });

    var initSplash = !qfn.splashed;

    qfn.splashed = true;

    PubSub.publish('query.search', this);

    if( initSplash ){
      this.collapseHistory();
    }
  };

  qfn.searchOrCancel = function(){
    var query = this;

    if( query.result && query.result.cancellable() ){
      query.result.cancel();
    } else {
      query.search();
    }
  };

  // get search params as new json obj
  qfn.params = function(){
    var attrGrIds = [];
    for( var i = 0; i < this.attributeGroups.length; i++ ){
      var attrGr = this.attributeGroups[i];

      if( attrGr.selected ){
        attrGrIds.push( attrGr.id );
      }
    }

    var netIds = [];
    for( var i = 0; i < this.networks.length; i++ ){
      var net = this.networks[i];

      if( net.selected ){
        netIds.push( net.id );
      }
    }

    return {
      organismId: this.organism.id,
      genesText: this.genesText,
      attributeGroupIds: attrGrIds,
      networkIds: netIds,
      weighting: this.weighting.value || config.networks.defaultWeighting.value
    };
  };

  qfn.toggleHistoryExpansion = function(){
    if( this.historyExpanded ){
      this.collapseHistory();
    } else {
      this.expandHistory();
    }

    PubSub.publish('query.toggleHistoryExpansion', this);
  };

  qfn.expandHistory = function(){
    qfn.historyExpanded = true;

    PubSub.publish('query.toggleHistoryExpansion', this);
  };

  qfn.collapseHistory = function(){
    qfn.historyExpanded = false;

    PubSub.publish('query.collapseHistory', this);
  };
  

} } ]);

