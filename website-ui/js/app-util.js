app.factory('util', [ function(){
  return {
    copy: function ( obj ){
      return JSON.parse( JSON.stringify(obj) );
    }
  };
} ]);