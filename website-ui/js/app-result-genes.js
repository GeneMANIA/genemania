app.controller('GenesCtrl',
[ '$scope', 'updateScope', 'cy',
function( $scope, updateScope, cy ){

  var r = Result;
  var rfn = r.prototype;


  // toggling the sidebar

  rfn.toggleGenesExpansion = function(){
    if( this.genesExpanded ){
      this.collapseGenes();
    } else {
      this.expandGenes();
    }

    PubSub.publish('result.toggleGenesExpansion', this);
  };

  rfn.expandGenes = function(){
    rfn.genesExpanded = true;

    PubSub.publish('result.expandGenes', this);
  };

  rfn.collapseGenes = function(){
    rfn.genesExpanded = false;

    PubSub.publish('result.collapseGenes', this);
  };


  // toggling the individual genes

  rfn.toggleGeneExpansion = function( rGene ){
    rGene.expanded = !rGene.expanded;

    PubSub.publish('result.toggleGeneExpansion', {
      result: this,
      gene: rGene
    });
  };

  function init(){
    $scope.query = Query.current;
    $scope.result = $scope.query.result;

    updateScope();
  }

  PubSub.subscribe('result.searched', init);

} ]);