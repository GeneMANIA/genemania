app.directive('onTap', ['$parse', function ($parse) {
  return function (scope, ele, attr){

    var tapHandler = $parse(attr.onTap);

    ele[0].addEventListener('click', function (evt){
      scope.$apply(function() {
        tapHandler(scope, { $event: evt });
      });
    });

  };
}]);

app.directive('onTapScroll', ['$parse', function ($parse) {
  return function (scope, $ele, attr){

    var tapHandler = $parse(attr.onTapScroll);

    $ele[0].addEventListener('click', function (evt){
      var parentSel = attr.onTapScrollParent;
      var $parent = $( parentSel );
      var condition = tapHandler(scope, { $event: evt });

      if( !condition ){
        return;
      }

      var scrollDiff = $ele.offset().top - $parent.offset().top;
      var scrollTop = $parent.scrollTop() + scrollDiff;

      // $ele.velocity("scroll", {
      //   container: $parent,
      //   duration: config.query.networkScrollDuration
      // });

      if( condition ){
        $parent.animate({
            scrollTop: scrollTop
        }, config.query.networkScrollDuration);
      }
    });

  };
}]);

app.directive('onTapPreventAll', ['$parse', function ($parse) {
  return function (scope, ele, attr){

    var tapHandler = $parse(attr.onTap);

    ele.on('touchstart touchend touchmove mousedown mouseup click', function (evt){
      evt.preventDefault();
      evt.stopPropagation();
    });

  };
}]);

$(function(){
  FastClick.attach(document); // so touch devices get click quick

  // make tap event alias
  // document.addEventListener('click', function(e){ 
  //   var el = e.target;

  //   if (window.CustomEvent) {
  //     var event = new CustomEvent('tap', e);
  //   } else {
  //     var event = document.createEvent('CustomEvent');
  //     event.initCustomEvent('tap', true, true, e);
  //   }

  //   el.dispatchEvent(event);
  // });
});