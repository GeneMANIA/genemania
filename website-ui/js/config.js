'use strict';

var config = {
  debug: !!window.DEBUG || !!window.location.href.match(/\?debug/),

  service: {
    baseUrl: window.DEBUG_URL || 'http://server4.baderlab.med.utoronto.ca:8080/genemania/json/',
    baseUrlProd: tomcatContextPath() + 'json/' // assuming that the ui is deployed in tomcat
  },

  query: {
    genesValidationDelay: 250,
    networkScrollDuration: 150,
  },

  attributes: {
    color: '#d0d0d0'
  },

  functionColors: [ "#c26661", "#639bc7", "#e9ae3d", "#907cc7", "#6fc17c", "#d28bcc", "#8dcbc3" ],

  networks: {
    colors: [
      { code: 'coexp', color: '#d0b7d5' },
      { code: 'coloc', color: '#a0b3dc' },
      { code: 'gi', color: '#90e190' },
      { code: 'path', color: '#9bd8de' },
      { code: 'pi', color: '#eaa2a2' },
      { code: 'predict', color: '#f6c384' },
      { code: 'spd', color: '#dad4a2' },
      { code: 'spd_attr', color: '#D0D0D0' },
      { code: 'reg', color: '#D0D0D0' },
      { code: 'reg_attr', color: '#D0D0D0' },
      { code: 'user', color: '#f0ec86' },
      { code: 'other', color: '#bbbbbb' }
    ],

    sourceName: {
      'GEO': 'GEO',
      'PATHWAYCOMMONS': 'Pathway Commons',
      'BIOGRID': 'BioGRID',
      'I2D': 'I2D',
      'INTERPRO': 'InterPro',
      'DROID': 'DroID',
      'MOUSEFUNC': 'MouseFunc',
      'PFAM': 'Pfam',
      'SUPPLEMENTARY_MATERIAL': 'supplementary material',
      'COLLABORATOR': 'collaborator',
      'MICROCOSM': 'MicroCOSM',
      'IREF': 'iRefIndex',
      'NONE': 'unknown'
    },

    weightings: [
      { name: 'Automatically selected weighting method', value: 'AUTOMATIC_SELECT' },
      { name: 'Assigned based on query genes', value: 'AUTOMATIC' },
      { name: 'Biological process based', value: 'BP' },
      { name: 'Molecular function based', value: 'MF' },
      { name: 'Cellular component based', value: 'CC' },
      { name: 'Equal by network', value: 'AVERAGE' },
      { name: 'Equal by data type', value: 'AVERAGE_CATEGORY' }
    ],

    setters: [
      {
        name: 'none',
        setter: function( net ){
          return false;
        }
      },

      {
        name: 'default',
        setter: function( net ){
          return net.defaultSelected;
        }
      },

      {
        name: 'all',
        setter: function( net ){
          return true;
        }
      }
    ]
  },

  organisms: {
    icons: {
      '1': 'bio-plant', // arabidopsis
      '2': 'bio-worm', // c elegans
      '3': 'bio-fly', // fruit fly
      '4': 'bio-human', // human
      '5': 'bio-mouse', // mouse
      '6': 'bio-yeast', // yeast
      '7': 'bio-rat', // rat
      '8': 'bio-fish', // zebrafish
      '9': 'bio-cells', // e coli
    }
  }
};

(function(){
  var strcmp = function( str1, str2 ){
    return ( ( str1 == str2 ) ? 0 : ( ( str1 > str2 ) ? 1 : -1 ) );
  };

  (function(){
    var wg = config.networks.weightings;

    for( var i = 0; i < wg.length; i++ ){
      wg[ wg[i].value ] = wg[i]; // allow access by value
    }

    config.networks.weightingGroups = [
      { name: 'Query-dependent weighting', weightings: [ wg.AUTOMATIC_SELECT, wg.AUTOMATIC ] },
      { name: 'Gene Ontology (GO) weighting', weightings: [ wg.BP, wg.MF, wg.CC ] },
      { name: 'Equal weighting', weightings: [ wg.AVERAGE, wg.AVERAGE_CATEGORY ] }
    ];

    config.networks.defaultWeighting = wg.AUTOMATIC_SELECT;
  })();

  (function(){
    config.networks.sorters = [
      {
        name: 'first author',
        sorter: function(a, b){
          var aAuth = a.metadata.firstAuthor.toLowerCase();
          var bAuth = b.metadata.firstAuthor.toLowerCase();

          return strcmp(aAuth, bAuth);
        }
      },

      {
        name: 'last author',
        sorter: function(a, b){
          var aAuth = a.metadata.lastAuthor.toLowerCase();
          var bAuth = b.metadata.lastAuthor.toLowerCase();

          return strcmp(aAuth, bAuth);
        }
      },

      {
        name: 'size',
        sorter: function(a, b){
          var aSize = a.metadata.interactionCount;
          var bSize = b.metadata.interactionCount;

          return bSize - aSize; // bigger first
        }
      },

      {
        name: 'date',
        sorter: function(a, b){
          var aYr = parseInt( a.metadata.yearPublished, 10 );
          var bYr = parseInt( b.metadata.yearPublished, 10 );

          return bYr - aYr; // newer first
        }
      }
    ];

    var colorsByCode = config.networks.colorsByCode = {};
    var colors = config.networks.colors;
    for( var i = 0; i < colors.length; i++ ){
      var spec = colors[i];
      var color = spec.color;
      var code = spec.code;

      colorsByCode[ code ] = color;
    }
  })();


  (function(){
    config.networks.postprocess = function( net ){
      var meta = net.metadata;
      var authors = meta.authors.split(/\s*,\s*/);
      var sAuthors;

      if( authors.length === 0 ){
        sAuthors = '';
      } else if( authors.length < 2 ){
        sAuthors = authors[0];
      } else {
        sAuthors = authors[0] + ' et al';
      }

      meta.shortAuthors = sAuthors;
      meta.firstAuthor = authors[0];
      meta.lastAuthor = authors[ authors.length - 1 ];

      meta.networkType = meta.networkType[0].toUpperCase() + meta.networkType.substr(1);

      var mappedSourceName = config.networks.sourceName[ meta.source ];

      if( meta.networkType === 'Uploaded' ){
        mappedSourceName = 'a file';

        net.uploaded = true;
      }

      meta.sourceName = mappedSourceName ? mappedSourceName : 'unknown';

      meta.interactionCountFormatted = numeral( meta.interactionCount ).format('0,0');

      var tags = net.tags;
      for( var i = 0; i < tags.length; i++ ){
        var tag = tags[i];

        tag.nameFormatted = tag.name.toLowerCase();
      }

      tags.sort(function(a, b){
        var aName = a.nameFormatted;
        var bName = b.nameFormatted;

        return strcmp( aName, bName );
      });
    };

    if( config.debug ){
      Promise.longStackTraces(); // enable long stack traces in bluebird for debugging
    }

    if( !config.debug ){
      config.service.baseUrl = config.service.baseUrlProd;
    }

    console.log('Init app with debug=' + config.debug);
  })();
})();
