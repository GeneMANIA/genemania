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

app.directive('onCtrlEnter', ['$parse', function ($parse) {
  return function (scope, $ele, attr){

    var handler = $parse(attr.onCtrlEnter);

    $ele[0].addEventListener('keydown', function(evt){
      if( (evt.keyCode == 10 || evt.keyCode == 13) && (evt.ctrlKey || evt.metaKey || evt.altKey) ){
        evt.preventDefault();
        handler(scope, { $event: evt });
      }
    });

  };
}]);

app.directive('onLineSelect', ['$parse', function ($parse) {
  return function (scope, $ele, attr){
    var handler = $parse(attr.onLineSelect);
    var ele = $ele[0];

    var next = function(evt){
      var chi = ele.selectionStart; // character i
      var li = 0; // line i
      var text = ele.value;

      for( var i = 0; i < chi; i++ ){
        if( text[i] === '\n' ){
          li++;
        }
      }
      console.log(i, li);

      handler(scope, { $event: evt });
    };

    ele.addEventListener('change', next);
    ele.addEventListener('selectstart', next);
    ele.addEventListener('keydown', next);
    ele.addEventListener('keypress', next);
    ele.addEventListener('keyup', next);
    ele.addEventListener('click', next);
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