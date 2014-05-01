$(function(){	
	
    /**********************************************
    disable native language spellcheck
    otherwise, when we add one, there could be
    two underlines at once
    
    NOTE: affects only items with "widget" css
    class
    **********************************************/			
    // firefox fix
    $(".widget").attr("spellcheck", false);
    
    
});