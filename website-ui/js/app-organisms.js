app.factory('$$organisms', 
['$http', 'util',
function( $http, util ){

  var id2icon = {
    '1': 'bio-plant', // arabidopsis
    '2': 'bio-worm', // c elegans
    '3': 'bio-fly', // fruit fly
    '4': 'bio-human', // human
    '5': 'bio-mouse', // mouse
    '6': 'bio-cells', // yeast
    '7': 'bio-rat', // rat
    '8': 'bio-fish', // zebrafish
  };

  var cache;

  var $$organisms = function(){
    if( cache ){ return Promise.resolve(cache); }

    return util.nativePromise( $http.get(config.service.baseUrl + 'organisms') )
      .then(function( res ){
        return res.data;
      })

      .then(function( orgs ){ // add icons & sort

        for( var i = 0; i < orgs.length; i++ ){
          var org = orgs[i];

          org.icon = id2icon[ org.id ];
        }

        return orgs.sort(function(a, b){
          if( a.alias < b.alias ){
            return -1;
          } else if( b.alias < a.alias ){
            return 1;
          } 

          return 0;
        });
      })

      .then(function(orgs){
        return cache = orgs;
      })
    ;
  };

  return $$organisms;

}]);