app.factory('Query_history', 
[ 'util', 'Result', 'io', 'cy',
function( util, Result, io, cy ){ return function( Query ){
  
  var q = Query;
  var qfn = q.prototype;

  //
  // NAVIGATING THE QUERY HISTORY

  qfn.store = function(){
    var query = this;
    var ioq = io('queries');

    // store query data params in clientside datastore
    return util.delayPromise(1000).then(function(){
      return ioq.read();
    }).then(function( qJson ){
      var history = qJson.history = qJson.history || [];

      history.unshift({
        params: query.params(),
        image: cy.png({ scale: 0.25 }),
        timestamp: Date.now()
      });

      return ioq.write();
    }).then(function(){
      PubSub.publish('query.store', this);
    });

  };

  qfn.clearHistory = function(){
    return io('queries').delete().then(function(){
      PubSub.publish('query.clearHistory', this);
    });
  };

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
      weighting: this.weighting.value
    };
  };
  

} } ]);

