app.factory('Query_genes', 
[ 'util',
function( util ){ return function( Query ){

  var q = Query;
  var qfn = q.prototype;
  
  //
  // GENES

  qfn.addGenes = function(){};
  qfn.removeGenes = function(){};
  qfn.removeAllGenes = function(){};

  qfn.setGenes = function( genes ){
    var self = this;

    self.settingGenes = true;

    self.genesText = _.isArray( genes ) ? genes.join('\n') : genes;

    PubSub.publish('query.setGenes', self);
    self.validateGenesFromText();
  };

  qfn.describeGeneLine = function( lineIndex ){
    if( lineIndex != null ){
      this.geneLineIndex = lineIndex;
    }

    // console.log('describeGeneLine', lineIndex);
    this.geneLineDescr = this.geneValidations ? this.geneValidations[ this.geneLineIndex ] : null;

    PubSub.publish('query.describeGeneLine', self);
  };

  // internal helper function for setGenesFromText()
  qfn.validateGenesFromText = _.debounce( function(){
    this.validateGenes();

  }, config.query.genesValidationDelay, {
    leading: false,
    trailing: true
  });

  // ui sets genes from text box => validate
  qfn.setGenesFromText = function(){
    var self = this;

    self.settingGenes = true;

    PubSub.publish('query.setGenesText', self);
    self.validateGenesFromText();
  };

  qfn.setExampleGenes = function(){
    var self = this;

    self.settingGenes = true;

    //$('#query-genes-textarea').css('white-space', 'normal'); // ios8 hack

    self.genesText = self.organism.defaultGenes.map(function( g ){
      return g.symbol;
    }).join('\n');

    PubSub.publish('query.setGenesTextFromCode', self);
    self.validateGenesFromText();
  };

  qfn.updateGenesArea = function(){
    $('#query-genes-textarea').trigger('autosize.resize');
  };

  // PubSub.promise('query.ready').then(function(){
  //   $textarea.autosize({
  //     callback: function(){
  //       var textarea = document.getElementById('query-genes-textarea');
  //       var genesVal = document.getElementById('query-genes-validation');

  //       genesVal.style.height = textarea.style.height;
  //     }
  //   });
  // });

  // validate genes directly
  qfn.validateGenes = function(){
    var self = this;
    var txt = this.genesText;
    var p;
    var prev = self.prevValidateGenes;

    self.settingGenes = false;
    self.validatingGenes = true;
    PubSub.publish('query.validateGenesStart', self);

    if( prev ){
      prev.cancel('Cancelling stale gene validation query');
    }

    if( txt && !txt.match(/^\s+$/) ){
      p = $$genes.validate({
        organism: this.organism.id,
        genes: txt
      })
        .cancellable()

        .then(function( t ){
          self.validatingGenes = false;
          var geneValns = self.geneValidations = t.genes;
          self.invalidGenesCount = 0;

          var geneSpcks = '';
          for( var i = 0; i < geneValns.length; i++ ){
            var geneValn = geneValns[i];
            var last = i === geneValns.length - 1;
            var spck = '';

            if( geneValn.type === 'INVALID' ){
              self.invalidGenesCount++;

              while( spck.length < geneValn.name.length ){
                spck += '_';
              }
            }

            geneSpcks += spck + ( last ? '' : '\n' );
          }
          self.geneSpellchecks = geneSpcks;

          PubSub.publish('query.validateGenes', self);
        })
      ;
    } else {
      p = Promise.resolve().cancellable().then(function(){
        self.validatingGenes = false;
        self.geneValidations = [];
        self.geneSpellchecks = '';
        self.invalidGenesCount = 0;
        
        PubSub.publish('query.validateGenes', self);
      });
    }

    p = p.then(function(){
      self.describeGeneLine();
    });

    return self.prevValidateGenes = p;
  };

  qfn.expandGenes = function(){
    this.genesExpanded = true;

    PubSub.publish('query.expandGenes', this);
  };

  qfn.collapseGenes = function(){
    this.genesExpanded = false;

    PubSub.publish('query.collapseGenes', this);
  };
  

} } ]);

