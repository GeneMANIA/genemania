'use strict';

(function(){
  var defns = [
    {
      id: 'query-network-groups',
      handler: function(){
        var vp = document.getElementById('query-qtip-viewport');

        return [
          { name: 'max-height', value: vp.clientHeight - 70 + 'px' }
        ];
      },
      after: qtipFix
    },

    {
      id: 'query-organism-select',
      handler: function(){
        var vp = document.getElementById('query-qtip-viewport');

        return [
          { name: 'max-height', value: vp.clientHeight - 30 + 'px' }
        ];
      },
      after: qtipFix
    },

    {
      id: 'query-adv-opts',
      handler: function(){
        var vp = document.getElementById('query-qtip-viewport');

        return [
          { name: 'max-height', value: vp.clientHeight - 50 + 'px' }
        ];
      },
      after: qtipFix
    },

    {
      id: 'functions-add-list',
      handler: function(){
        var vp = document.body;

        return [
          { name: 'max-height', value: vp.clientHeight - 60 + 'px' }
        ];
      },
      after: qtipFix
    },

    {
      id: 'graph-more-qtip-content',
      handler: function(){
        var vp = document.body;

        return [
          { name: 'max-height', value: vp.clientHeight - 60 + 'px' }
        ];
      },
      after: qtipFix
    },

    {
      selector: [
        '.query-genes-expander-expanded',
        '.query-genes-expander-expanded .query-genes-validation',
        //'.query-genes-expander-expanded .query-genes-textarea',
        '.query-genes-validation'
      ].join(', '),
      getMatchingEles: false,
      handler: setGenesExpH
    },

    {
      selector: '.query-genes-expander-instr',
      getMatchingEles: false,
      handler: function(){
        var h = getGenesExpH();

        return [
          { name: 'top', value: h + 'px' }
        ];
      }
    }
  ];

  function getQueryQtipViewportHeight(){

  }

  function setGenesExpH(){
    var h = getGenesExpH();

    return [
      { name: 'height', value: h + 'px' },
      { name: 'min-height', value: h + 'px' }
    ];
  }

  function getGenesExpH(){
    var h = document.getElementById('query-genes-textarea').clientHeight;
    var vpH = document.getElementById('query-qtip-viewport').clientHeight;
    var min = 100;
    var max = vpH - 40;

    //console.log( '---', min, max, h );

    if( h < min ){
      h = min;
    }

    if( h > max ){
      h = max;
    }

    return h;
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

  responsive.defines( defns );

  // fix: on mobile 100% viewport height isn't accurate and can cause overflow
  window.addEventListener('resize', function(){
    var h = window.innerHeight + 'px';

    document.body.style.height = h;
    document.documentElement.style.height = h;
    window.scrollTo(0, 0);
  });

  PubSub.subscribe( 'query.search', function(){
    responsive.restyle();
  } );
})();
