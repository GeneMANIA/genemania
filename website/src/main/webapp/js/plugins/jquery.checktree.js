/**
    Project: CheckTree jQuery Plugin
    Version: 0.21
    Project Website: http://static.geewax.org/checktree/
    Author: JJ Geewax <jj@geewax.org>
    
    License:
        The CheckTree jQuery plugin is currently available for use in all personal or 
        commercial projects under both MIT and GPL licenses. This means that you can choose 
        the license that best suits your project, and use it accordingly.
*/
(function(jQuery) {

jQuery.fn.modifyCheckTree = function(settings) {
    settings = jQuery.extend({
        expandAll: false,
        expandTopLevel: false,
        expandNone: false,
        checkAll: false,
        checkNone: false
    }, settings);
    
    if(settings.expandAll) {
        jQuery(this).children("li").find(".arrow").each(function() {
            if( ! jQuery(this).hasClass("expanded") ) {
                jQuery(this).click();
            }
        });
    } else {
        if(settings.expandNone || settings.expandTopLevel) {
            // must be children first, then parent (reverse is a specific case of that scenario)
            jQuery(this).children("li").find(".arrow").reverse().each(function() {
                if( jQuery(this).hasClass("expanded") ) {
                    jQuery(this).click();
                }
            });
        }
        
        if(settings.expandTopLevel) {
            jQuery(this).children("li").find(".arrow:first").each(function() {
                if( ! jQuery(this).hasClass("expanded") ) {
                    jQuery(this).click();
                }
            });
        }
    }

    if(settings.checkAll) {
    	// On Firefox, sometimes children("li") returns zero elements after rebuilding the tree:
    	// Using a pseudo-class instead of "li" seems to work (don't ask me why!!!):
    	// *****************************************************************************************
    	//jQuery(this).children("li").find(".checkbox:first").each(function() {
    	jQuery(this).children(".checktree_top_level").find(".checkbox:first").each(function() {
            if( ! jQuery(this).hasClass("checked") ) {
                jQuery(this).click();
            }
        });
    } else if(settings.checkNone) {
    	// On Firefox, sometimes children("li") returns zero elements after rebuilding the tree:
    	// Using a pseudo-class instead of "li" seems to work (don't ask me why!!!):
    	// *****************************************************************************************
    	//jQuery(this).children("li").find(".checkbox:first").each(function() {
    	jQuery(this).children(".checktree_top_level").find(".checkbox:first").each(function() {
    		if (! jQuery(this).find("input:first").attr("disabled")) {
	    		if( jQuery(this).hasClass("checked") ) {
	                jQuery(this).click();
	            } else if ( jQuery(this).hasClass("half_checked") ) {
	                jQuery(this).click().click();
	            }
    		}
        });
    }
};


jQuery.fn.checkTree = function(settings) {


    settings = jQuery.extend({
        /* Callbacks
            The callbacks should be functions that take one argument. The checkbox tree
            will return the jQuery wrapped LI element of the item that was checked/expanded.
        */
        onExpand: null,
        onCollapse: null,
        onCheck: null,
        onUnCheck: null,
        onHalfCheck: null,
        onLabelHoverOver: null,
        onLabelHoverOut: null,
        onSelect: null,
        onUnSelect: null,
        
        checkedText: "&nbsp;",
        uncheckedText: "&nbsp;",
        halfCheckedText: "&nbsp",
        
        expandedText: "&nbsp",
        collapsedText: "&nbsp;",
        
        /* Valid choices: 'expand', 'check' */
        labelAction: "expand",
        
        // Debug (currently does nothing)
        debug: false
    }, settings);

    function updateNamedCheckMarks(li) {
        var name = li.children(":checkbox").attr("name");
        var checked = li.find(".checkbox").hasClass("checked");
        
        if( ! name ) {   
            return;
        }
       

        li.parents("ul").find(":checkbox[name=" + name +"]").each(function(){
            jQuery(this).prev(".checkbox:first").each(function(){
                if( checked ) {
                    if( ! jQuery(this).hasClass("checked") ) {
                        jQuery(this).click();
                    }
                } else {
                    if( jQuery(this).hasClass("checked") ) {
                        jQuery(this).click();
                    } else if ( jQuery(this).hasClass("half_checked") ) {
                        jQuery(this).click().click();
                    }
                }
            });
        });
        
    }

    // update view of arrows
    function updateCheckTreeArrow(li, type) {
        li.find(".arrow:first").each(function() {
            if( type == "expanded" || ( ! type && jQuery(this).hasClass("expanded") ) ) {
                jQuery(this).html(settings.expandedText);
            } else if ( type == "collapsed" || ( ! type && jQuery(this).hasClass("collapsed") ) ) {
                jQuery(this).html(settings.collapsedText);
            } else {
                jQuery(this).html("&nbsp;");
            }
        });
    }
    
    // update check box views
    function updateCheckTreeCheck(li, type, change) {
    	
    	if( li.size() == 0 ){
    		return;
    	}
    	
//    	console.log(li);
//    	console.log(type);
    	
    	function update_parent(check){
    		var parent = check.parents("li:first").parents("li:first");
    		var sibs = check.parents("ul:first").find(".checkbox");
    		var checked_sibs = sibs.filter(".checked");
    		
    		if( checked_sibs.size() == sibs.size() ){
    			updateCheckTreeCheck(parent, "checked");
    		} else if( checked_sibs.size() == 0 ){
    			updateCheckTreeCheck(parent, "unchecked");
    		} else {
    			updateCheckTreeCheck(parent, "half_checked");
    		}
    	}
    	
        li.find(".checkbox:first").each(function() {
            if( type == "checked" || ( ! type && jQuery(this).hasClass("checked") ) ) {
            	jQuery(this).siblings(":checkbox").attr("checked", "checked");
            	jQuery(this).removeClass("half_checked").addClass("checked");
                jQuery(this).html(settings.checkedText);
                
                update_parent( jQuery(this) );
                
                if( change ){
                	jQuery(this).trigger("change");
                	if (settings.onCheck) settings.onCheck(li);
                }
            } else if ( type == "half_checked" || ( ! type && jQuery(this).hasClass("half_checked") ) ) {
            	jQuery(this).siblings(":checkbox").attr("checked", null);
            	jQuery(this).removeClass("checked").addClass("half_checked");
                jQuery(this).html(settings.halfCheckedText);
                
                update_parent( jQuery(this) );
                
                if( change ){
                	jQuery(this).trigger("change");
                	if (settings.onHalfCheck) settings.onHalfCheck(li);
                }
            } else {
            	jQuery(this).siblings(":checkbox").attr("checked", null);
            	jQuery(this).removeClass("checked").removeClass("half_checked");
                jQuery(this).html(settings.uncheckedText);
                
                update_parent( jQuery(this) );
                
                if( change ){
                	jQuery(this).trigger("change");
                	if (settings.onUnCheck) settings.onUnCheck(li);
                }
            }
        });
    }

    var $tree = this;
    
    $tree.find("li")
        // Hide all of the sub-trees
        .find("ul")
            .hide()
        .end()
        
        // Hide all checkbox inputs
        .find(":checkbox")
        	.hide()
        .end()
        
        
        .find(".label")
            // Clicking the labels should expand the children
            .click(function() {
                var action = settings.labelAction;
                switch(settings.labelAction) {
                    case 'expand':
                        jQuery(this).siblings(".arrow").click();
                        break;
                    case 'check':
                        jQuery(this).siblings(".checkbox").click();
                        break;
                    case 'select':
                    	if( jQuery(this).hasClass("selected") ) {
                    		jQuery(this).removeClass("selected").removeClass("select_hover");
                    		if(settings.onSelect != null) {
                    			settings.onUnSelect( jQuery(this).parent() );
                        	}
                    	} else {
                    		jQuery(this).addClass("selected").removeClass("select_hover");
                    		if(settings.onSelect != null) {
                    			settings.onSelect( jQuery(this).parent() );
                        	}
                    	}
                    	break;
                }
            })
        .mouseover(function(){
        	if( settings.labelAction == "select" ) {
        		jQuery(this).addClass("select_hover");
        	}
        	if(settings.onLabelHoverOver != null) {
    			settings.onLabelHoverOver( jQuery(this).parent() );
        	}
        })
        .mouseout(function(){
        	if( settings.labelAction == "select" ) {
        		jQuery(this).removeClass("select_hover");
        	}
        	if(settings.onLabelHoverOut != null) {
    			settings.onLabelHoverOut( jQuery(this).parent() );
        	}
        })
        .end()
        
        .each(function() {
            // Create the image for the arrow (to expand and collapse the hidden trees)
            var $arrow = jQuery('<div class="arrow">&nbsp;</div>');
            
            // If it has children:
            if (jQuery(this).is(":has(ul)")) {
                $arrow.addClass("collapsed"); // Should start collapsed
                
                // When you click the image, toggle the child list
                $arrow.click(function() {
                    jQuery(this).siblings("ul").toggle();
                    
                    if (jQuery(this).hasClass("collapsed")) {
                        //toggled = settings.expandedarrow;
                        jQuery(this)
                            .addClass("expanded")
                            .removeClass("collapsed")
                        ;
                        if (settings.onExpand) settings.onExpand(jQuery(this).parent());
                        updateCheckTreeArrow( jQuery(this).parent(), "expanded" );
                    }
                    else {
                        //toggled = settings.collapsedarrow;
                        jQuery(this)
                            .addClass("collapsed")
                            .removeClass("expanded")
                        ;
                        if (settings.onCollapse) settings.onCollapse(jQuery(this).parent());
                        updateCheckTreeArrow( jQuery(this).parent(), "collapsed" );
                    }
                });
            }
            
            // Create the image for the checkbox next to the label
            var $checkbox = jQuery('<div class="checkbox">&nbsp;</div>');
            
            // Don't create check box if there is no input
            if ( ! jQuery(this).is(":has(input)") ) {
                $checkbox = jQuery('<div class="checkbox_spacer">&nbsp;</div>');
            }
            
            // When you click the checkbox, it should do the checking/unchecking
            $checkbox.click(function() {
                
                if( $(this).hasClass("checkbox_spacer") ) {
                    return;
                }
                
                // Make the current class checked
                if( jQuery(this).hasClass("half_checked") ){
                	updateCheckTreeCheck(jQuery(this).parent(), "checked", true);
                } else if( jQuery(this).hasClass("checked") ){
                	updateCheckTreeCheck(jQuery(this).parent(), "unchecked", true);
                } else {
                	updateCheckTreeCheck(jQuery(this).parent(), "checked", true);
                }
                
                // Check/uncheck children depending on our status.
                if (jQuery(this).hasClass("checked")) {
                    // Fire the check callback for this parent
//                    updateCheckTreeCheck(jQuery(this).parent(), "checked");
//                    updateNamedCheckMarks(jQuery(this).parent());
//                    if (settings.onCheck) settings.onCheck(jQuery(this).parent());
                    
                    
                    jQuery(this).siblings("ul").find(".checkbox").not(".checked")
                        .each(function() {
                            
                            updateCheckTreeCheck(jQuery(this).parent(), "checked");
                            updateNamedCheckMarks(jQuery(this).parent());
//                            if (settings.onCheck) settings.onCheck(jQuery(this).parent());
                            
                        })
                    ;

                    // Moved check action to here, so it's not called once per input:
                    // #################################################################
					// Tell our parent checkbox that we've changed
                	//jQuery(this).parents("ul").siblings(".checkbox").change();
//                    if (settings.onCheck) settings.onCheck(jQuery(this).parent());
                    // #################################################################
                }
                else {
                    // Fire the uncheck callback for this parent
//                    updateCheckTreeCheck(jQuery(this).parent(), "unchecked");
//                    updateNamedCheckMarks(jQuery(this).parent());
                    //if (settings.onUnCheck) settings.onUnCheck(jQuery(this).parent());
                    
                    jQuery(this).siblings("ul").find(".checkbox").filter(".checked")
                        .each(function() {
                            
                            updateCheckTreeCheck(jQuery(this).parent(), "unchecked");
                            updateNamedCheckMarks(jQuery(this).parent());
                            //if (settings.onUnCheck) settings.onUnCheck(jQuery(this).parent());
                            
                        })

                    ;

                    // Moved uncheck action to here, so it's not called once per input:
                    // #################################################################
					// Tell our parent checkbox that we've changed
                	//jQuery(this).parents("ul").siblings(".checkbox").change();
//                    if (settings.onUnCheck) settings.onUnCheck(jQuery(this).parent());
                    // #################################################################
                }
                // Tell our parent checkbox that we've changed
                //jQuery(this).parents("ul").siblings(":checkbox").change();
                if (settings.onCheck) settings.onCheck(jQuery(this).parent());
            });
            
            // Prepend the arrow and checkbox images to the front of the LI
            jQuery(this)
                .prepend($checkbox)
                .prepend($arrow)    
            ;
            
            updateCheckTreeCheck( jQuery(this) );
            updateCheckTreeArrow( jQuery(this) );
            
            
        });
        

    return $tree;
};
})(jQuery);