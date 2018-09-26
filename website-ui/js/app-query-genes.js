'use strict';

app.factory('Query_genes',
[ 'util',
function( util ){ return function( Query ){

  var q = Query;
  var qfn = q.prototype;

  //
  // GENES

  qfn.addGenes = function( genes ){
    var self = this;

    genes = _.isArray( genes ) ? genes : [ genes ];

    self.settingGenes = true;

    self.genesText += '\n' + genes.join('\n');

    PubSub.publish('query.setGenes', self);
    PubSub.publish('query.addGenes', genes);
    self.validateGenesFromText();
  };

  qfn.removeGenes = function( genes ){
    var self = this;

    genes = _.isArray(genes) ? genes : [genes];

    self.settingGenes = true;

    var lines = self.genesText.split('\n');

    var geneToBeRemoved = {};
    for( var i = 0; i < genes.length; i++ ){
      var gene = genes[i].toLowerCase();

      geneToBeRemoved[gene] = true;
    }

    var lineMatches = function( line ){
      return !!geneToBeRemoved[line];
    };

    for( var i = 0; i < lines.length; i++ ){
      var line = lines[i].trim().toLowerCase();

      if( lineMatches(line) ){
        lines.splice( i, 1 );
        i--;
        continue;
      }
    }

    self.genesText = lines.join('\n');

    PubSub.publish('query.setGenes', self);
    PubSub.publish('query.removeGenes', genes);
    self.validateGenesFromText();
  };

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

  qfn.openGenesArea = function(){
    return new Promise(function(resolve){
      var $ta = $('#query-genes-textarea').focus();

      setTimeout(function(){
        $ta.trigger('click');

        resolve();
      }, 20);
    });
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

    var uiTimeout = setTimeout(function(){
      self.validatingGenesUi = true;
      PubSub.publish('query.validateGenesUiStart', self);
    }, 250);

    if( txt && !txt.match(/^\s+$/) ){
      p = $$genes.validate({
        organism: this.organism.id,
        genes: txt
      })

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
      p = Promise.resolve().then(function(){
        self.validatingGenes = false;
        self.geneValidations = [];
        self.geneSpellchecks = '';
        self.invalidGenesCount = 0;

        PubSub.publish('query.validateGenes', self);
      });
    }

    p = p.then(function(){
      self.describeGeneLine();

      clearTimeout( uiTimeout );

      self.validatingGenesUi = false;
      PubSub.publish('query.validateGenesUiEnd', self);
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
