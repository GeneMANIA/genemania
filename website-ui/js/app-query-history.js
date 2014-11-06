app.factory('Query_history', 
[ 'util', 'Result',
function( util, Result ){ return function( Query ){
  
  var q = Query;
  var qfn = q.prototype;

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

