'use strict';

// simple api to read and write json "files"
app.factory('io', [ function(){

  var ios = {};

  function copy( obj ){
    return JSON.parse( JSON.stringify(obj) );
  }

  var io = window.io = function( ns, defaultJson ){
    if( ios[ns] ){
      return ios[ns];
    }

    if( !(this instanceof io) ){
      return new io( ns, defaultJson );
    }

    this.ns = ns;
    this.defaultJson = defaultJson || {};
    ios[ ns ] = this;
  };

  // read w/ cache s.t. multiple readers point to same obj
  // (so writes don't destroy info from other readers)
  io.prototype.read = function(){
    var self = this;

    return new Promise(function( resolve, reject ){
      if( self.json ){ resolve( self.json ); return; } // used cached val once read in

      localforage.getItem( self.ns, function( err, jsonStr ){
        if( err ){ reject( err ); }

        var json = jsonStr ? JSON.parse( jsonStr ) : copy(self.defaultJson);
        self.json = json; // cache read val

        resolve( json );
      } );
    }).catch(function(){ // reads can fail in private mode
      return self.json = {};
    });
  };

  io.prototype.write = function( value ){ // if value specified, then overwrites cached json
    var self = this;

    return new Promise(function( resolve, reject ){
      var json = value || self.json;

      if( json !== undefined ){
        localforage.setItem( self.ns, JSON.stringify( json ), function( err ){
          // if( err ){ reject(err); return; }
          // don't reject because writes can fail in private mode

          resolve( json );
        } );
      } else {
        reject('io::' + self.ns + ' has undefined json and so can not be written');
      }

    });
  };

  io.prototype.delete = function(){
    var self = this;

    return new Promise(function( resolve, reject ){
      delete self.json;

      localforage.removeItem( self.ns, function( err ){
        if( err ){ reject(err); return; }

        resolve();
      } );
    }).catch(function(){ // deletes can fail in private mode
      return self.json = {};
    });
  };

  return io;

} ]);
