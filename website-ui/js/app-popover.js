app.directive('popover', function(){
  return {
    restrict: 'E',
    templateUrl: 'templates/popover.html',
    transclude: true,
    replace: true,
    scope: {
      'class': '@'
    }
  };
});