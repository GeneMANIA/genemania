'use strict';

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

app.directive('onSelfTapPreventDefault', ['$parse', function ($parse) {
  return function (scope, $ele, attr){

    var tapHandler = $parse(attr.onTap);

    $ele.on('touchstart touchend touchmove mousedown mouseup click', function (evt){
      if( evt.target === $ele[0] ){
        evt.preventDefault();
      }
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

app.directive('onCtrlEnterBlurAnd', ['$parse', function ($parse) {
  return function (scope, $ele, attr){

    var handler = $parse(attr.onCtrlEnterBlurAnd);

    $ele[0].addEventListener('keydown', function(evt){
      if( (evt.keyCode == 10 || evt.keyCode == 13) && (evt.ctrlKey || evt.metaKey || evt.altKey) ){
        evt.preventDefault();
        handler(scope, { $event: evt });
        $ele.blur();
      }
    });

  };
}]);

app.directive('onEscBlur', ['$parse', function ($parse) {
  return function (scope, $ele, attr){

    var handler = $parse(attr.onCtrlEnter);

    $ele[0].addEventListener('keydown', function(evt){
      if( evt.keyCode == 27 ){
        $ele.blur();
      }
    });

  };
}]);

app.directive('onSelectFile', ['$parse', function ($parse) {
  return function (scope, $ele, attr){

    var handler = $parse(attr.onSelectFile);

    $ele[0].addEventListener('change', function(evt){
      var $file = $ele[0].files[0];

      if( $file ){
        handler(scope, { $file: $file });
      }
    });

  };
}]);

app.directive('onHoverover', ['$parse', function ($parse) {
  return function (scope, $ele, attr){

    var handler = $parse(attr.onHoverover);
    var scrolling = true;
    var scrollTimout;

    $ele.parents().on('scroll', function(evt){
      clearTimeout(scrollTimout);

      scrolling = true;

      scrollTimout = setTimeout(function(){
        scrolling = false;
      }, 100);
    });

    $ele[0].addEventListener('mouseenter', function(evt){
      handler(scope, { $event: evt });
    });

    $ele[0].addEventListener('touchstart', function(evt){
      if( evt.touches.length === 1 ){
        handler(scope, { $event: evt });

        evt.preventDefault();
      }
    });

  };
}]);

app.directive('onHoverout', ['$parse', function ($parse) {
  return function (scope, $ele, attr){

    var handler = $parse(attr.onHoverout);

    $ele[0].addEventListener('mouseleave', function(evt){
      handler(scope, { $event: evt });
    });

    $ele[0].addEventListener('touchend', function(evt){
      if( evt.touches.length === 0 ){
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
      //console.log(i, li);

      handler(scope, { $event: evt, $lineIndex: li, $charIndex: chi });
    };

    ele.addEventListener('change', next);
    ele.addEventListener('selectstart', next);
    ele.addEventListener('keydown', next);
    ele.addEventListener('keypress', next);
    ele.addEventListener('keyup', next);
    ele.addEventListener('click', next);
  };
}]);

app.directive('onShortcutFocus', ['$parse', function ($parse) {
  return function (scope, $ele, attr){
    var ele = $ele[0];

    Mousetrap.bind(attr.onShortcutFocus, function(e){
      setTimeout(function(){
        $ele.focus();
      }, 0);
    });

  };
}]);

app.directive('changeOnReady', [function() {
    return {
        link : function(scope, element, attrs) {
            var $e=$(element);

            PubSub.subscribe('ready', function(){
              scope.$apply(function(){
                $e.change();
              });
            });

        }
    };
}]);

app.directive('ngRangeslider', [function() {
    return {
        link : function(scope, element, attrs) {
            var $e=$(element);

            $e.rangeslider({
              polyfill: false
            });

            var updateOn = attrs.ngRangesliderUpdateOn;
            var updated = false;

            if( updateOn ){
              scope.$parent.$watch(updateOn, function(val){
                if( !updated && val ){
                  updated = true;

                  setTimeout(function(){
                    $e.rangeslider('update', true);
                  }, 0);
                }
              });
            }

        }
    };
}]);

$(function(){
  FastClick.attach( document.body ); // so touch devices get click quick

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
