app.directive('onTap', ['$parse', function ($parse) {
  return function (scope, ele, attr){

    var tapHandler = $parse(attr.onTap);

    ele[0].addEventListener('tap', function (evt){
      scope.$apply(function() {
        tapHandler(scope, { $event: evt });
      });
    });

  };
}]);

$(function(){
  FastClick.attach(document); // so touch devices get click quick

  // make tap event alias
  document.addEventListener('click', function(e){ 
    var el = e.target;

    if (window.CustomEvent) {
      var event = new CustomEvent('tap', e);
    } else {
      var event = document.createEvent('CustomEvent');
      event.initCustomEvent('tap', true, true, e);
    }

    el.dispatchEvent(event);
  });
});