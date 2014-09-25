var config = {
  service: {
    baseUrl: 'http://localhost:8080/genemania/json/'
  },

  query: {
    genesValidationDelay: 250,
    networkScrollDuration: 150,
  },

  networks: {
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
    ]
  },

  organisms: {
    icons: {
      '1': 'bio-plant', // arabidopsis
      '2': 'bio-worm', // c elegans
      '3': 'bio-fly', // fruit fly
      '4': 'bio-human', // human
      '5': 'bio-mouse', // mouse
      '6': 'bio-cells', // yeast
      '7': 'bio-rat', // rat
      '8': 'bio-fish', // zebrafish
    }
  }
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
})();