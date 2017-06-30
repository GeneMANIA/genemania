// allows more dynamic & expressive responsive styling than css can do alone

(function(){

  // defitions of elements to update
  // { id|selector, handler, useStyleAttr }
  var defns = [
  ];

  var stylesheet = document.getElementById('responsive.js-stylesheet');

  if( !stylesheet ){
    stylesheet = document.createElement('style');
    stylesheet.id = 'responsive.js-stylesheet';
    document.documentElement.appendChild( stylesheet ); // so it's always last
  }

  var handleDefns = _.debounce( function( e ){
    var css = '';
    var afters = [];

    for( var i = 0; i < defns.length; i++ ){
      var defn = defns[i];
      var eles = null;
      if( defn.getMatchingEles == null || defn.getMatchingEles ){
        eles = defn.id ? [ document.getElementById( defn.id ) ] : document.querySelectorAll( defn.selector );
      }

      var props;

      try{
        props = defn.handler( eles, e );
      } catch(err){
        console.error('responsive.js detected an error in a style block definition');
        console.error( err );
        console.error( defn );
      }

      var selector = defn.id ? ('#' + defn.id) : defn.selector;
      var propsStr = props.map(function( prop ){
        return prop.name + ': ' + prop.value + '; ';
      }).join('\n');

      if( defn.useStyleAttr ){ // apply directly
        for( var j = 0; j < eles.length; j++ ){
          eles[j].setAttribute('style', propsStr);
        }
      } else { // use stylesheet
        css += selector + ' { ' + propsStr + ' } \n';
      }

      if( defn.after ){
        afters.push({
          eles: eles,
          fn: defn.after
        });
      }
    }

    stylesheet.textContent = css;

    // call all the afters on next tick
    setTimeout(function(){
      for( var i = 0; i < afters.length; i++ ){
        afters[i].fn( afters[i].eles, e );
      }
    }, 1);
  }, 150 );

  // when responsiveness should be applied
  window.addEventListener( 'resize', handleDefns );
  window.addEventListener( 'orientationchanged', handleDefns );

  // these cause issues on edge/ie
  //window.addEventListener( 'load', handleDefns );
  //window.addEventListener( 'DOMContentLoaded', handleDefns );

  var responsive = window.responsive = {
    define: function( d ){
      defns.push( d );
    },

    defines: function( ds ){
      Array.prototype.push.apply( defns, ds );
    },

    restyle: function(){
      handleDefns();
    }
  };

})();
