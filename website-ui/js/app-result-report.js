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
    
    var docDefinition = {
      content: [
        { text: 'GeneMANIA report', style: 'h1' },
        
        {
          image: cy.png({
            // bg: 'red',
            maxHeight: 1000,
            full: true
          }),
          width: 400,
          style: 'figure'
        },
        
        { text: 'Search parameters', style: 'h2' },
        
        {
          table: {
            headerRows: 0,
            widths: [ 'auto', '*' ],

            body: [
              [ { text: 'Organism', style: 'tableh' }, params.organism.alias ],
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
        
        { text: 'Genes', style: 'h2' },
        
        {
          table: {
            headerRows: 1,
            widths: ['auto', '*', 'auto'],
            body: [
              [
                { text: 'Gene', style: 'tableh' },
                { text: 'Description', style: 'tableh' },
                { text: 'Rank', style: 'tableh' },
              ]
            ].concat( res.resultGenes.map(function( rGene ){
              return [
                rGene.name,
                rGene.gene.node.geneData.description,
                '' + rGene.rank
              ];
            }) )
          },
          layout: 'lightHorizontalLines'
        }
      ],
      
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
        font: 'latin'
      },
      
      styles: {
        h1: {
          fontSize: 36,
          margin: [ 0, 18, 0, 9 ]
        },
        
        h2: {
          fontSize: 24,
          margin: [ 0, 12, 0, 6 ]
        },
        
        tableh: {
          bold: true
        },
        
        figure: {
          alignment: 'center'
        }
      }
    };
    
    pdfMake.createPdf( docDefinition ).open();
  };
  

} } ]);

(function(){
  pdfMake.fonts = {
   latin: {
     normal: 'lmroman10-regular.ttf',
     bold: 'lmroman10-bold.ttf'
   }
 };
})();
