/* (c) 2009
   AUTHORS: Max Franz
   LICENSE: TODO
*/

;(function($){
   
    $.fn.menu = function(options) {  
        
        var defaults = {
            menuTitleClass: "ui-menu-title",
            menuItemClass: "ui-menu-item",
            selectedClass: "ui-state-active",
            togglableClass: "ui-menu-togglable",
            menuBarClass: "ui-menu-bar",
            topMenuClass: "ui-top-menu",
            subMenuClass: "ui-sub-menu",
            menuItemFirstClass: "ui-menu-item-first",
            menuItemLastClass: "ui-menu-item-last",
            menuItemParentClass: "ui-menu-item-parent",
            parentIconClass: "ui-menu-parent-icon",
            checkableClass: "ui-menu-checkable",
            checkIconClass: "ui-menu-check-icon",
            checkedClass: "ui-menu-checked",
            addArrow: true,
            titleArrowText: "<small>&nbsp;&#9660;</small>",
            menuZIndex: 9999,
            menuOpenDelay: 200, // in ms
            onMenuItemSelect: function(li){},
            onMenuItemDeselect: function(li){},
            onMenuItemOpen: function(li){},
            onMenuItemClose: function(li){},
            onMenuItemClick: function(li){},
            onMenuItemCheck: function(li){},
            onMenuItemUnCheck: function(li){}
        };
        
        var options = $.extend(defaults, options); 
        
        var timeout;
        
        function openMenu(li) {
            // add selected style
            li.addClass(options.selectedClass);
            
            // clicking one menu item (toggle or otherwise) closes siblings
            li.siblings("li").each(function(){
                if( $(this).hasClass(options.selectedClass) && ! $(this).hasClass(options.togglableClass) ) {
                    $(this).click();
                }
            });
            
            li.find("." + options.parentIconClass).addClass(options.selectedClass);
            li.children("ul").find("." + options.parentIconClass).removeClass(options.selectedClass);
            
            li.find("." + options.checkIconClass).addClass(options.selectedClass);
            li.children("ul").find("." + options.checkIconClass).removeClass(options.selectedClass);
        
            (! options.onMenuItemSelect) || options.onMenuItemSelect(li);
            
            // opening sub menu is delayed
            timeout = setTimeout(function(){
                var children = false;
                
                // open sub menus
                li.children("ul").show().each(function(){
                    
                    var maxWidth = 0;
                    
                    $(this).css("width", "100%");
                    
                    $(this).children("li").each(function(){
                        $(this).css("display", "block").css("width", "auto");
                        maxWidth = Math.max( maxWidth, $(this).outerWidth() );
                    }).width(maxWidth);
                    
                    $(this).css("height", $(this).height());
                    
                    if( ! $(this).parent().hasClass(options.menuTitleClass) ) {
                        $(this).css( "left", Math.ceil( $(this).parent().outerWidth() ) );
                        $(this).css( "top", Math.ceil( $(this).parent().position().top ) );
                    } else {
                        $(this).css( "top", Math.ceil( $(this).parent().outerHeight() + $(this).parent().position().top ) );
                        $(this).css( "left", Math.ceil( $(this).parent().offset().left ) );
                    }
                    
                    // icon offsets
                    $(this).children("li").children("." + options.parentIconClass + ", ." + options.checkIconClass).each(function(){
                        var offsetY = ( ( $(this).closest("li").height() - $(this).height() ) / 2 );
                        
                        if( parseInt( $(this).css("margin-top") ) == 0 ) {
                            $(this).css( "margin-top", offsetY );
                        }
                        
                        if( $(this).hasClass(options.parentIconClass) ) {
                            var offsetX = $(this).closest("li").width() - $(this).width();
                            $(this).css("margin-left", offsetX);
                        }
                    });
                    
                    children = true;
                });
                
                if(children) {
                    (! options.onMenuItemOpen) || options.onMenuItemOpen(li);
                }
            }, (li.hasClass(options.menuTitleClass)) ? (0) : (options.menuOpenDelay) );
        }
        
        function closeMenu(li) {
            clearTimeout(timeout);
        
            var children = li.children("ul").length > 0;
        
            li.children("ul").hide();
            
            li.removeClass(options.selectedClass);
            
            li.find("." + options.parentIconClass).removeClass(options.selectedClass);
            li.find("." + options.checkIconClass).removeClass(options.selectedClass);
            
            if( li.hasClass(options.togglableClass) ) {
                li.siblings("li").each(function(){
                    if( $(this).hasClass(options.selectedClass) && ! $(this).hasClass(options.togglableClass) ) {
                        $(this).click();
                    }
                });
            }
            
            (! options.onMenuItemDeselect) || options.onMenuItemDeselect(li);
            if(children) {
                (! options.onMenuItemClose) || options.onMenuItemClose(li);
            }
        }
        
        return this.each(function() {
            var ul = $(this).children("ul:first");
        
            // add style classes
            $(this).addClass("ui-corner-all outline ui-menu ui-widget ui-widget-content");
            ul.addClass("ui-menu-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-top");
            ul.addClass(options.menuBarClass);
            ul.children("li").addClass("ui-state-default ui-corner-all");
            ul.find("ul").each(function(){                
                $(this).children("li:first").addClass("ui-corner-top").addClass(options.menuItemFirstClass);
                $(this).children("li:last").addClass("ui-corner-bottom").addClass(options.menuItemLastClass);
                
                $(this).children("li").each(function(){
                    if( $(this).children("ul").length > 0 ) {
                        $(this).addClass(options.menuItemParentClass);
                        $(this).prepend("<div style=\"position: absolute\" class=\"" + options.parentIconClass + "\"></div>");
                    }
                    
                    if( $(this).hasClass(options.checkableClass) ) {
                        $(this).prepend("<div style=\"position: absolute\" class=\"" + options.checkIconClass + "\"></div>");
                        
                        $(this).click(function(){
                            var check = $(this).children("." + options.checkIconClass);
                            
                            check.toggleClass(options.checkedClass);
                            
                            if( check.hasClass(options.checkedClass) ) {
                                (! options.onMenuItemCheck) || options.onMenuItemCheck( $(this) );
                            } else {
                                (! options.onMenuItemCheck) || options.onMenuItemUncheck( $(this) );
                            }
                        });
                    }
                });
            });
            
            // only top level are menu items
            ul.find("li").addClass(options.menuItemClass);
            ul.children("li").removeClass(options.menuItemClass).addClass(options.menuTitleClass);
            ul.children("li").each(function(){
                if( $(this).children("ul").length > 0 ) {
                    $(this).find("label:first").append(options.titleArrowText);
                } else {
                    $(this).addClass(options.togglableClass);
                }
            });
            
            // differentiate top level and sub menus
            ul.find("ul").each(function(){
                if( $(this).parent().hasClass(options.menuTitleClass) ) {
                    $(this).addClass(options.topMenuClass);
                } else {
                    $(this).addClass(options.subMenuClass);
                }
            });
            
            // remove corner from top level menus
            ul.find("." + options.topMenuClass).children("." + options.menuItemFirstClass);
            
            // set position and index
            ul.find("ul").css("position", "absolute").css("z-index", options.menuZIndex).hide();

            ul.children("li").toggle(function(){
                openMenu( $(this) );
            }, function(){
                closeMenu( $(this) );
            });
            
            ul.find("." + options.menuItemClass).mouseenter(function(){
                openMenu( $(this) );
            }).mouseleave(function(){
                closeMenu( $(this) );
            });
            
            ul.find("li").click(function(){
                // don't send parent clicks
                if( $(this).find("." + options.menuItemClass + "." + options.selectedClass).length > 0 ) {
                    return;
                }
            
                // if it's not selected, we've clicked it, not the user
                // BUT always send toggles
                if( $(this).hasClass(options.selectedClass) || $(this).hasClass(options.togglableClass) ) {
                    (! options.onMenuItemClick) || options.onMenuItemClick( $(this) );
                }
            });
            
            function closeAll() {
                ul.find("." + options.selectedClass).reverse().each(function(){
                    if( ! $(this).hasClass(options.togglableClass) ) {
                    	closeMenu( $(this) );
                    }
                });
            }
            
            $("html").click(function(){
                closeAll();
            });
        });  
    };  
})(jQuery);  