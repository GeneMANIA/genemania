app.directive('qtip', function(){
  return {
    restrict: 'E',
    templateUrl: 'templates/qtip.html',
    transclude: true,
    replace: true,
    scope: {},
    link: function( $scope, $ele, attrs ){
      var $callingScope = $scope.$parent;
      var $target = $('#' + attrs.target);
      var showEvent = attrs.showEvent || 'click';
      var hideEvent = attrs.hideEvent || 'unfocus';
      var title = attrs.title;
      var classes = attrs.qtipClass;
      var showVal = attrs.qtipShow;

      if( attrs.showEvent === 'none' ){
        attrs.showEvent = '';
      }

      if( attrs.hideEvent === 'none' ){
        attrs.hideEvent = '';
      }

      console.log($scope);

      $callingScope.$watch(attrs.qtipShow, function(val){ console.log('watch', val)
        var tooltip = $target.qtip('api').elements.tooltip;
        var shown = tooltip && tooltip.is(':visible');
        var show = !shown && val;
        var hide = shown && !val;

        if( show ){
          $target.qtip('show');
        } else if( hide ){
          $target.qtip('hide');
        }
      });

      $ele.on('$destroy', function() {
        $target.qtip('destroy');
      });

      $target.qtip({
        content: {
          text: $ele,
          title: { button: false, text: title }
        },

        position: {
          container: $('body'),
          my: 'top center',
          at: 'bottom center',
          effect: false,
          viewport: true,
          adjust: {
            method: 'shift flipinvert flip',
            scroll: false
          }
        },

        show: {
          event: showEvent,
          delay: 0,
          solo: true,
          effect: function( offset ){
            $(this).css({
              opacity: 0,
              display: 'block'
            }).velocity({
              opacity: 1
            }, {
              duration: 150
            });
          }
        },

        hide: {
          event: hideEvent,
          delay: 0,
          fixed: true,
          leave: false,
          effect: function( offset ){
            var $this = $(this);

            $this.velocity({
              opacity: 0
            }, {
              duration: 150,
              complete: function(){
                $this.hide();
              }
            });
          }
        },

        style: {
          classes: 'qtip-bootstrap ' + classes,
          tip: {
            height: 10,
            width: 20
          }
        }
      });

    }
  };
});