'use strict';

app.factory('Result_report', 
[ 'util',
function( util ){ return function( Result ){

  var r = Result;
  var rfn = r.prototype;


  rfn.report = function(){
    var res = this;
    var params = res.parameters;
    var qy = res.query;

    var binify = function( arr, binner ){
      var idToBin = {};
      var bins = [];

      var getBin = function( id ){
        if( !idToBin[id] ){
          var bin = idToBin[id] = {
            id: id,
            els: []
          };

          bins.push( bin );
        }

        return idToBin[id];
      };

      arr.forEach(function( el ){
        var id = binner( el ).toLowerCase();
        var bin = getBin( id );

        bin.els.push( el );
      });

      return bins.sort(function( a, b ){
        a = a.id.toLowerCase();
        b = b.id.toLowerCase();

        if( a < b ){
          return -1;
        } else if( a > b ){
          return 1;
        } else {
          return 0;
        }
      });
    };

    var make1pxPng = function( color ){
      var canvas = document.createElement('canvas');
      var cxt = canvas.getContext('2d');

      canvas.width = 1;
      canvas.height = 1;

      cxt.fillStyle = color;
      cxt.fillRect( 0, 0, 1, 1 );

      return canvas.toDataURL("image/png");
    };

    var legendLayout = function(){
      return {
        hLineWidth: function(i, node) {
          0;
        },
        vLineWidth: function(i, node) {
          0;
        },
        hLineColor: function(i, node) {
          'black';
        },
        vLineColor: function(i, node) {
          'black';
        },
        paddingLeft: function(i, node) { return 0; },
        paddingRight: function(i, node) { return 0; },
        paddingTop: function(i, node) { return 0; },
        paddingBottom: function(i, node) { return 0; }
      };
    };

    var date = new Date();

    var docDefinition = {
      content: [
        { text: 'GeneMANIA report', style: 'h1' },

        {
          text: [
            'Created on : ' + moment(date).format('D MMMM YYYY HH:mm:ss') + '\n',
            'Last database update : ' + qy.version.dbVersion + '\n',
            'Application version : ' + qy.version.webappVersion
          ],
          style: 'subtitle'
        },

        {
          image: cy.jpg({
            maxHeight: 1000,
            full: true
          }),
          fit: [500, 400],
          style: 'figure'
        },

        {
          columns: [

            {
              width: '50%',
              table: {
                headerRows: 1,
                widths: [ 'auto', 'auto' ],
                body: [
                  [ { text: 'Networks', colSpan: 2, style: 'tableh' }, {} ]
                ].concat( result.resultAllGroups.map(function( rGr ){
                  return [
                    { image: make1pxPng( rGr.color ), width: 6, height: 6, margin: [0, 4, 0, 4] },
                    { text: rGr.ele.name, style: 'legend' }
                  ];
                }) )
              },
              layout: 'noBorders'
            },

            {
              width: '50%',
              table: {
                headerRows: 1,
                widths: [ 'auto', 'auto' ],
                body: [
                  [ { text: 'Functions', colSpan: 2, style: 'tableh' }, {} ]
                ].concat( (
                  result.coloringFunctions.length === 0
                    ? [[ { text: 'N/A', colSpan: 2, style: 'legend' }, {} ]]
                    : result.coloringFunctions.map(function( cfn ){
                        return [
                          { image: make1pxPng( cfn.color ), width: 6, height: 6, margin: [0, 4, 0, 4] },
                          { text: cfn.ontologyCategory.description, style: 'legend' }
                        ];
                      })
                ) )
              },
              layout: 'noBorders'
            }

          ]
        },

        { text: 'Search parameters', style: 'h2', pageBreak: 'before' },

        {
          table: {
            headerRows: 0,
            widths: [ 'auto', '*' ],

            body: [
              [ { text: 'Organism', style: 'tableh' }, params.organism.alias + ' (' + params.organism.description + ')' ],
              [ { text: 'Genes', style: 'tableh' }, params.genes.map(function(g){ return g.symbol; }).join(' , ') ],
              [
                { text: 'Network weighting', style: 'tableh' },
                config.networks.weightings.filter(function(w){ return w.value === params.weighting; })[0].name
              ],
              [
                { text: 'Networks', style: 'tableh' },

                binify(
                  params.networks.concat( params.attributeGroups )
                    .map(function(n){ return n.name; })
                    .sort(function(a, b){
                      a = a.toLowerCase();
                      b = b.toLowerCase();

                      if( a < b ){
                        return -1;
                      } else if( a > b ){
                        return 1;
                      }

                      return 0;
                    }),

                  function( name ){
                    return name[0].toLowerCase();
                  }
                ).map(function( netBin ){
                  return {
                    table: {
                      headerRows: 1,
                      widths: ['*'],
                      body: [
                        [ { text: netBin.id.toUpperCase(), style: 'tableh' } ],
                        [ netBin.els.join(' , ') ]
                      ]
                    },
                    layout: 'lightHorizontalLines'
                  };
                })
              ]
            ]
          },
          layout: 'noBorders'
        },

        { text: 'Genes', style: 'h2', pageBreak: 'before' },

        {
          table: {
            headerRows: 1,
            widths: ['auto', '*', 'auto'],
            body: [
              [
                { text: 'Gene', style: 'tableh' },
                { text: 'Description', style: 'tableh' },
                { text: 'Rank', style: 'tablehnum' },
              ]
            ].concat( res.resultGenes.map(function( rGene ){
              return [
                rGene.name,
                rGene.gene.node.geneData.description,
                { text: '' + ( rGene.rank === 0 ? 'N/A' : rGene.rank ), style: 'tablenum' }
              ];
            }) )
          },
          layout: 'lightHorizontalLines'
        },

        { text: 'Networks', style: 'h2', pageBreak: 'before' },

      ].concat( res.resultAllGroups.map(function( rGr ){
        return {
          table: {
            headerRows: 1,
            widths: ['*', 40],
            body: [ [
              { text: rGr.ele.name, style: 'tableh' },
              { text: rGr.displayWeight, style: 'tablenum' }
            ] ].concat( rGr.children.map(function( rNet ){
              var net = rNet.ele;
              var meta = net.metadata;

              return [
                {
                  table: {
                    headerRows: 1,
                    widths: ['*'],
                    body: [
                      [ net.name ]
                    ]
                      .concat( meta && meta.title ? [[
                        {
                          text: [
                            { text: meta.title + ' ' + meta.shortAuthors + ' (' + meta.yearPublished + '). ', style: 'netdetails' },
                            { text: meta.publicationName, style: 'netdetailspubname' }
                          ]
                        }
                      ]] : [] )

                      .concat( meta && meta.networkType ? [[{ text: meta.networkType + ' with ' + meta.interactionCountFormatted + ' interactions from ' + meta.sourceName, style: 'netdetails' }]] : [] )
                  },
                  layout: 'noBorders'
                },
                { text: rNet.displayWeight, style: 'tablenum' }
              ];
            }) )
          },
          layout: 'lightHorizontalLines'
        };
      }) ).concat([

        // { text: 'Functions', style: 'h2', pageBreak: 'before' },
        //
        // {
        //   table: {
        //     headerRows: 1,
        //     widths: ['*', 60, 60],
        //     body: [
        //       [
        //         { text: 'Function', style: 'tableh' },
        //         { text: 'FDR', style: 'tableh' },
        //         { text: 'Coverage', style: 'tableh' }
        //       ]
        //     ].concat( res.resultOntologyCategories.map(function( rOCat ){
        //       return [
        //         rOCat.ontologyCategory.description,
        //         { text: rOCat.qValueFormatted },
        //         { text: rOCat.coverageFormatted }
        //       ];
        //     }) )
        //   },
        //   layout: 'lightHorizontalLines'
        // },
        //
        // { text: 'Interactions', style: 'h2', pageBreak: 'before' },
        //
        // {
        //   table: {
        //     headerRows: 1,
        //     widths: ['auto', 'auto', 'auto', 'auto', '*'],
        //     body: [
        //       [
        //         { text: 'Gene 1', style: 'tableh' },
        //         { text: 'Gene 2', style: 'tableh' },
        //         { text: 'Weight', style: 'tableh' },
        //         { text: 'Network group', style: 'tableh' },
        //         { text: 'Network', style: 'tableh' }
        //       ]
        //     ].concat( res.resultInteractions.map(function( rIntn ){
        //       return [
        //         rIntn.fromGene.name,
        //         rIntn.toGene.name,
        //         '' + rIntn.weight,
        //         rIntn.resultNetwork.resultNetworkGroup.networkGroup.name,
        //         rIntn.resultNetwork.network.name
        //       ];
        //     }) )
        //   },
        //   layout: 'lightHorizontalLines'
        // }

      ]),

      footer: function( currentPage, pageCount ){
        return {
          text: currentPage.toString() + ' of ' + pageCount,
          alignment: 'center'
        }
      },

      header: function( currentPage, pageCount ){
        return ''; //{ text: 'simple text', alignment: (currentPage % 2) ? 'left' : 'right' };
      },

      defaultStyle: {
        font: 'latin',
        fontSize: 12
      },

      styles: {
        h1: {
          fontSize: 36,
          margin: [ 0, 18, 0, 4 ]
        },

        subtitle: {
          fontSize: 8,
          margin: [ 0, 0, 0, 8 ]
        },

        h2: {
          fontSize: 24,
          margin: [ 0, 12, 0, 6 ]
        },

        tableh: {
          bold: true
        },

        tablehnum: {
          bold: true,
          alignment: 'right'
        },

        tablenum: {
          alignment: 'right'
        },

        figure: {
          alignment: 'center'
        },

        legend: {
          fontSize: 8
        },

        netdetails: {
          fontSize: 8
        },

        netdetailspubname: {
          fontSize: 8,
          italics: true
        },

        ref: {
          italics: true
        }
      }
    };

    lazyLib().then(function( libs ){
      var pdfMake = libs.pdfMake;

      pdfMake.createPdf( docDefinition ).open();
    });

  };


} } ]);
