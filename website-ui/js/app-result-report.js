app.factory('Result_report', 
[ 'util',
function( util ){ return function( Result ){
  
  var r = Result;
  var rfn = r.prototype;


  rfn.report = function(){
    var docDefinition = {
      content: 'Hello, world!',
      defaultStyle: {
        font: 'latin'
      }
    };
    
    pdfMake.createPdf( docDefinition ).open();
    
    console.log('report')
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
