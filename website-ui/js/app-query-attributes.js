app.factory('Query_attributes', 
[ 'util',
function( util ){ return function( Query ){
  
  var q = Query;
  var qfn = q.prototype;

  //
  // ATTRIBUTES

  qfn.toggleAttributesToMatchQuery = function( q2, pub ){
    var q1 = this;

    for( var i = 0; i < q2.attributeGroups.length; i++ ){
      var attrGr = q2.attributeGroups[i];

      q1.toggleAttributeGroupSelection( attrGr.id, attrGr.selected, false );
    }

    if( pub || pub === undefined ){
      PubSub.publish( 'query.toggleAttributesToMatchQuery', {
        query: q1,
        otherQuery: q2
      } );
    }
  };

  qfn.updateAttributeGroupsSelection = function(){
    var selCount = this.selectedAttributeGroupCount;
    var grsCount = this.attributeGroups.length;

    if( selCount === 0 ){
      this.attributeGroupsSelected = false;
    } else if( selCount === grsCount ){
      this.attributeGroupsSelected = true;
    } else {
      this.attributeGroupsSelected = 'semi';
    }
  };

  qfn.getAttributeGroup = function( idOrGr ){
    if( $.isPlainObject( idOrGr ) ){
      var gr = idOrGr;
      return gr;
    } else {
      var id = idOrGr;
      return this.attributeGroupsById[ id ];
    }
  };

  qfn.toggleAttributeGroupSelection = function( gr, sel, pub ){
    gr = this.getAttributeGroup( gr );
    sel = sel === undefined ? !gr.selected : sel; // toggle if unspecified selection state

    if( gr.selected === sel ){ return; } // update unnecessary

    gr.selected = sel;
    this.selectedAttributeGroupCount += sel ? 1 : -1;
    this.updateAttributeGroupsSelection();

    if( pub || pub === undefined ){
      pub = { attributeGroup: gr, query: this, selected: sel };
      PubSub.publish( sel ? 'query.selectAttributeGroup' : 'query.unselectAttributeGroup', pub );
      PubSub.publish( 'query.toggleAttributeGroupSelection', pub );
    }
  };
  qfn.selectAttributeGroup = function( gr, pub ){ this.toggleAttributeGroupSelection(gr, true, pub); };
  qfn.unselectAttributeGroup = function( gr, pub ){ this.toggleAttributeGroupSelection(gr, false, pub); };

  qfn.toggleAttributeGroupExpansion = function( gr, exp ){
    gr = this.getAttributeGroup( gr );
    exp = exp === undefined ? !gr.expanded : exp; // toggle if unspecified

    if( gr.expanded === exp ){ return; } // update unnecessary

    gr.expanded = exp;

    var pub = { attributeGroup: gr, query: this, expanded: exp };
    PubSub.publish( exp ? 'query.expandAttributeGroup' : 'query.collapseAttributeGroup', pub );
    PubSub.publish( 'query.toggleAttributeGroupExpansion', pub );
  };
  qfn.expandAttributeGroup = function( gr ){ return this.toggleAttributeGroupExpansion(gr, true); };
  qfn.collapseAttributeGroup = function( gr ){ return this.toggleAttributeGroupExpansion(gr, false); };

  qfn.toggleAttributeGroupsExpansion = function( exp ){
    exp = exp === undefined ? !this.attributeGroupsExpanded : exp; // toggle if unspecified

    this.attributeGroupsExpanded = exp;

    var pub = { query: this, expanded: exp };
    PubSub.publish( exp ? 'query.expandAttributeGroups' : 'query.collapseAttributeGroups', pub );
    PubSub.publish( 'query.toggleAttributeGroupsExpansion', pub );
  };
  qfn.expandAttributeGroups = function(){ return this.toggleAttributeGroupsExpansion(true); };
  qfn.collapseAttributeGroups = function(){ return this.toggleAttributeGroupsExpansion(false); };

  qfn.toggleAttributeGroupsSelection = function( sel ){
    if( sel === undefined ){ // toggle if unspecified selection state
      sel = !this.attributeGroupsSelected || this.attributeGroupsSelected === 'semi' ? true : false;
    }

    var grs = this.attributeGroups;
    for( var i = 0; i < grs.length; i++ ){
      var gr = grs[i];

      this.toggleAttributeGroupSelection( gr.id, sel );
    }

    var pub = { query: this, selected: sel };
    PubSub.publish( sel ? 'query.selectAttributeGroups' : 'query.unselectAttributeGroups', pub );
    PubSub.publish( 'query.toggleAttributeGroupsSelection', pub );
  };
  qfn.selectAttributeGroups = function(){ this.toggleAttributeGroupsSelection(true); };
  qfn.unselectAttributeGroups = function(){ this.toggleAttributeGroupsSelection(false); };

  // for an array of attrgr objects { id, selected }, set selected
  qfn.setAttributeGroups = function( grs ){
    if( _.isArray(grs) ){
      for( var i = 0; i < grs.length; i++ ){
        var gr = grs[i];

        gr.selected ? this.selectAttributeGroup( gr.id, false ) : this.unselectAttributeGroup( gr.id, false );
      }
    } else if( _.isString(grs) ){
      var setter = _.find( this.setAttributeGroupOptions, { name: grs } );

      if( !setter ){ return; } // can't set w/o setter
      setter = setter.setter; // we only want the function
 
      for( var i = 0; i < this.attributeGroups.length; i++ ){
        var gr = this.attributeGroups[i];

        if( setter( gr ) ){
          this.selectAttributeGroup( gr, false );
        } else {
          this.unselectAttributeGroup( gr, false );
        }
      }
    }

    PubSub.publish( 'query.setAttributeGroups', {
      query: this
    } );
  };
  

} } ]);
