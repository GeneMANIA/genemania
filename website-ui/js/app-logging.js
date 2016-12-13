(function(){
  var log = window.log = {
    ga: function( category, action, label ){
      if( window.DEBUG ){
        console.log('Log (`' + category + '`, `' + action + '`' + ( label != null ? ', `' + label + '`' : '' ) + ')');
      }

      var evt = {
        eventCategory: category,
        eventAction: action
      };

      if( _.isNumber( label ) && Math.round( label ) === label ){
        evt.eventValue = label;
      } else if( label != null ){
        evt.eventLabel = '' + label;
      }

      setTimeout(function(){
        try {
          ga('send', 'event', evt);
        } catch( err ){}
      }, 0);
    },

    action: function( name, value ){
      this.ga( 'User actions', name, value );
    },

    query: function( name, value ){
      this.ga( 'Query parameters', name, value );
    }
  };

  var getNetworkName, getNetworkGroupName;

  var events = [
    { name: 'query.ready' },

    {
      name: 'query.search',
      handler: function( query ){
        log.action('query.search');

        log.query('organism', query.organism.alias);

        var genes = query.genesText.toLowerCase().split(/\s*\n\s*/);
        log.query('genesCount', genes.length);
        genes.slice( 0, 10 ).forEach(function( g ){ log.query('gene', g); });

        log.query('uploadedNetworksCount', query.networks.reduce(function( total, net ){
          return total + ( net.uploaded && net.selected ? 1 : 0 );
        }, 0));

        log.query('networksCount', query.networks.reduce(function( total, net ){
          return total + ( net.selected ? 1 : 0 );
        }, 0));

        log.query('defaultNetworks', query.networks.every(function( net ){
          return !net.defaultSelected || net.selected;
        }));

        log.query('resultGenesCount', query.maxGenes);

        log.query('resultAttributesCount', query.maxAttrs);

        log.query('weighting', query.weighting.name);
      }
    },

    { name: 'query.fromHistory' },

    { name: 'query.fromLink' },

    {
      name: 'query.addGenes',
      value: function( genes ){ return genes.length; }
    },

    {
      name: 'query.removeGenes',
      value: function( genes ){ return genes.length; }
    },

    {
      name: 'result.layout',
      value: function( name ){ return name; }
    },

    {
      name: 'result.enableNetworkGroup',
      value: ( getNetworkGroupName = function( op ){ return (op.networkGroup.networkGroup || op.networkGroup.attributeGroup).name; })
    },

    {
      name: 'result.disableNetworkGroup',
      value: getNetworkGroupName
    },

    {
      name: 'result.expandNetworkGroup',
      value: getNetworkGroupName
    },

    {
      name: 'result.collapseNetworkGroup',
      value: getNetworkGroupName
    },

    {
      name: 'result.enableNetwork',
      value: ( getNetworkName = function( op ){ return (op.network.network || op.network.attribute).name; } )
    },

    {
      name: 'result.disableNetwork',
      value: getNetworkName
    },

    {
      name: 'result.expandNetwork',
      value: getNetworkName
    },

    {
      name: 'result.collapseNetwork',
      value: getNetworkName
    },

    { name: 'result.report' },

    {
      name: 'result.saveImage',
      value: function( options ){ return options.plainLabels ? 'plain-labels' : 'as-shown'; }
    },

    { name: 'result.saveText' },

    { name: 'result.saveNetworks' },

    { name: 'result.saveAttributes' },

    { name: 'result.saveGenes' },

    { name: 'result.saveFunctions' },

    { name: 'result.saveInteractions' },

    { name: 'result.saveParamsText' },

    { name: 'result.saveParamsJson' },

    {
      name: 'result.addFunctionColoring',
      value: function( evt ){ return evt.function.ontologyCategory.description; }
    },

    {
      name: 'result.removeFunctionColoring',
      value: function( evt ){ return evt.function.ontologyCategory.description; }
    }
  ];

  _.each( events, function( e ){
    PubSub.subscribe( e.name, function(){
      try {
        var args = Array.prototype.slice.call( arguments, 1 );

        if( e.handler ){
          e.handler.apply( null, args );
          return;
        }

        var value;

        if( e.value ){
          value = e.value.apply( null, args );
        }

        log.action( e.name, value );
      } catch( err ){
        // if there is a problem in logging, don't propagate the err
      }
    } );
  } );

})();
