// allows more dynamic & expressive responsive styling than css can do alone

(function(){

  // defitions of elements to update
  // { id|selector, handler, useStyleAttr }
  var defns = [
    {
      id: 'query-network-groups',
      handler: function( eles, e ){
        var vp = document.getElementById('query-qtip-viewport');

        return [ 
          { name: 'height', value: vp.clientHeight - 70 + 'px' }
        ];
      },
      after: qtipFix
    },

    {
      id: 'query-organism-select',
      handler: function( eles, e ){
        var vp = document.getElementById('query-qtip-viewport');

        return [ 
          { name: 'max-height', value: vp.clientHeight - 30 + 'px' }
        ];
      },
      after: qtipFix
    }
  ];

  function getQueryQtipViewportHeight(){

  }

  function qtipFix( eles, e ){
    for( var i = 0; i < eles.length; i++ ){
      var ele = eles[i];
      var $qtip = $(ele).parents('.qtip:first');

      if( $qtip.length > 0 && $qtip.is(':visible') ){
        $qtip.qtip('api').reposition(); // ask the qtip to rerender since it's buggy
      }
    }
  }

  var stylesheet = document.createElement('style');
  stylesheet.id = 'responsive.js-stylesheet';
  document.head.appendChild( stylesheet );

  var handleDefns = _.debounce( function( e ){
    var css = '';

    for( var i = 0; i < defns.length; i++ ){
      var defn = defns[i];
      var eles = defn.id ? [ document.getElementById( defn.id ) ] : document.querySelectorAll( defn.selector );
      var props = defn.handler( eles, e );
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

      setTimeout(function(){ // defer
        defn.after && defn.after( eles, e );
      }, 0);
    }

    stylesheet.textContent = css;
  }, 150 );

  // when responsiveness should be applied
  window.addEventListener( 'resize', handleDefns );
  window.addEventListener( 'orientationchanged', handleDefns );
  window.addEventListener( 'load', handleDefns );
  PubSub.subscribe( 'query.search', handleDefns );

})();