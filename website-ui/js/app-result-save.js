'use strict';

app.factory('Result_save',
[ 'util',
function( util ){ return function( Result ){

  var r = Result;
  var rfn = r.prototype;

  function download( filename, content, plain ){
    var pom = document.createElement('a');

    if( plain ){
      content = 'data:text/plain;charset=utf-8,' + encodeURIComponent(content);
      //content = 'data:text/plain;charset=utf-8;base64,' + btoa(content);
    }

    pom.setAttribute('href', content);
    pom.setAttribute('download', filename);
    pom.setAttribute('target', '_blank');

    if( document.createEvent ){
      var event = document.createEvent('MouseEvents');
      event.initEvent('click', true, true);
      pom.dispatchEvent(event);
    } else {
      pom.click();
    }
  }

  rfn.saveImage = function(){
    var png = cy.jpg({
      full: true,
      maxHeight: 4000
    });

    download( 'genemania-network.jpg', png );
  };

  rfn.saveText = function(){
    var txt = '';
    var qu = this.query;
    var re = this;

    txt += '# Organism: ' + qu.organism.name + '\n';
    txt += '# Application version: ' + qu.version.webappVersion + '\n';
    txt += '# Database version: ' + qu.version.dbVersion + '\n';
    txt += '# Network generated on: ' + moment().format('D MMMM YYYY') + '\n';
    txt += '# Author: GeneMANIA (genemania.org)\n';
    txt += '# Notes: Network weight reflects the data source relevance for predicting the function of interest\n';

    txt += [
      'Entity 1',
      'Entity 2',
      'Weight',
      'Network group',
      'Network'
    ].join('\t') + '\n';

    txt += re.resultInteractions.map(function( rInt ){
      return [
        rInt.fromGene.name,
        rInt.toGene.name,
        rInt.weight,
        rInt.resultNetwork.resultNetworkGroup.networkGroup.name,
        rInt.resultNetwork.network.name
      ].join('\t');
    }).join('\n') + '\n';

    re.resultGenes.forEach(function( rGene ){
      txt += rGene.resultAttributes.map(function( rAttr ){
        rAttr = re.resultAttributesById[ rAttr.attribute.id ];

        return [
          rGene.name,
          rAttr.attribute.description,
          0,
          'Attributes',
          rAttr.resultAttributeGroup.attributeGroup.name
        ].join('\t');
      }).join('\n');
    });

    download( 'genemania-network.txt', txt, true );
  };

  rfn.saveNetworks = function(){
    var re = this;
    var txt = '';

    txt += [
      'Group',
      'Name',
      'Weight',
      'Reference',
      'PubMed',
      'Source',
      'Note'
    ].join('\t') + '\n';

    txt += re.resultAllGroups.map(function( rGr ){

      return rGr.children.map(function( rEle ){
        var meta = rEle.ele.metadata;

        return [
          rGr.ele.name,
          rEle.ele.name,
          rEle.weight,
          meta && meta.title ? (meta.title + ' ' + meta.shortAuthors + '. (' + meta.yearPublished + '). ' + meta.publicationName) : '',
          meta ? meta.url : '',
          meta ? meta.soruceName : '',
          meta ? meta.comment : ''
        ].join('\t');
      }).join('\n');

    }).join('\n');

    download( 'genemania-networks.txt', txt, true );
  };

  rfn.saveAttributes = function(){
    var re = this;
    var txt = '';

    txt += [
      'Attribute',
      'Gene'
    ].join('\t') + '\n';

    txt += re.resultGenes.map(function( rGene ){

      return rGene.resultAttributes.map(function( rAttr ){

        return [
          rAttr.attribute.name,
          rGene.name
        ].join('\t');
      }).join('\n');

    }).join('\n');

    download( 'genemania-attributes.txt', txt, true );
  };

  rfn.saveGenes = function(){
    var re = this;
    var txt = '';

    txt += [
      'Symbol',
      'Description',
      'Score',
      'Functions',
      'Links'
    ].join('\t') + '\n';

    txt += re.resultGenes.map(function( rGene ){

      return [
        rGene.name,
        rGene.gene.node.description,
        rGene.score,
        rGene.resultOntologyCategories.map(function( rOCat ){
          return rOCat.ontologyCategory.description;
        }).join(', '),
        rGene.links.map(function(l){ return l.url; }).join(', ')
      ].join('\t');

    }).join('\n');

    download( 'genemania-genes.txt', txt, true );
  };

  rfn.saveFunctions = function(){
    var re = this;
    var txt = '';

    txt += [
      'Function',
      'FDR',
      'Genes in network',
      'Genes in genome'
    ].join('\t') + '\n';

    txt += re.resultOntologyCategories.map(function( rOCat ){

      return [
        rOCat.ontologyCategory.description,
        rOCat.qValue,
        rOCat.numAnnotatedInSample,
        rOCat.numAnnotatedInTotal
      ].join('\t');

    }).join('\n');

    download( 'genemania-functions.txt', txt, true );
  };

  rfn.saveInteractions = function(){
    var re = this;
    var txt = '';

    txt += [
      'Gene 1',
      'Gene 2',
      'Weight',
      'Network group',
      'Network'
    ].join('\t') + '\n';

    txt += re.resultNetworkGroups.map(function( rGr ){

      return rGr.resultNetworks.map(function( rNet ){

        return rNet.resultInteractions.map(function( rInt ){
          return [
            rInt.fromGene.name,
            rInt.toGene.name,
            rInt.weight,
            rGr.networkGroup.name,
            rNet.network.name
          ].join('\t') + '\n';
        }).join('');

      }).join('');

    }).join('');

    download( 'genemania-interactions.txt', txt, true );
  };

  rfn.saveParamsText = function(){
    var re = this;
    var qu = re.query;
    var txt = '';

    txt += 'Organism\t' + qu.organism.name + '\n\n';

    txt += 'Genes\t' + qu.genesText.split('\n').map(function(g){
      return g.trim();
    }).join('\t') + '\n\n';

    txt += 'Networks\n';
    txt += qu.networkGroups.map(function( gr ){
      return gr.name + '\t' + (gr.interactionNetworks || []).filter(function( net ){
        return net.selected;
      }).map(function( net ){
        return net.name;
      }).join('\t');
    }) + '\n\n';

    txt += 'Network weighting\t' + config.networks.weightings[ qu.weighting ].name + '\n';
    txt += 'Network weighting code\t' + qu.weighting + '\n\n';

    txt += 'Attribute groups\t' + qu.attributeGroups.map(function( aGr ){
      return aGr.name;
    }).join('\t') + '\n\n';

    txt += 'Number of gene results\t' + qu.maxGenes + '\n\n';

    txt += 'Number of attribute results\t' + qu.maxAttrs + '\n\n';

    txt += 'Version\t' + qu.version.dbVersion + '\n\n';

    download( 'genemania-parameters.txt', txt, true );
  };

  rfn.saveParamsJson = function(){
    var re = this;
    var qu = re.query;
    var obj = {};

    obj.version = qu.version.dbVersion;

    obj.organism = qu.organism.name;

    obj.genes = qu.genesText.split('\n').map(function(g){
      return g.trim();
    });

    obj.networks = {};

    qu.networkGroups.forEach(function( gr ){
      obj.networks[gr.name] = (gr.interactionNetworks || []).filter(function( net ){
        return net.selected;
      }).map(function( net ){
        return net.name;
      });
    });

    obj.attributeGroups = qu.attributeGroups.map(function( aGr ){
      return aGr.name;
    });

    obj.selectedWeighting = qu.weighting;
    obj.usedWeighting = re.weighting;

    obj.numberOfResultGenes = qu.maxGenes;

    obj.numberOfResultAttributes = qu.maxAttrs;

    download( 'genemania-parameters.json', JSON.stringify(obj, null, 2), true );
  };


} } ]);
