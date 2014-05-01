;(function($){

	// much faster than native jquery $(ele).eq(index)
	// about 100x faster which improves dom manipulation performance significantly
	
	$.fn.quickeq = function(index){
		
		var set = $(this);
		var elem = $([1]);
		var size = set.size();
		
		if( index < 0 ){
			index = size - 1 + index;
		} 
		
		if( index < 0 || index >= size ){
			throw "quickeq can not go out of bounds with index " + index + " on array size " + size;
		}
		
		(elem.context = elem[0] = set[index]);
		
		return elem;
	}
	
})(jQuery); 