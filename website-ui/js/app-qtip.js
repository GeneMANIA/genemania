app.directive('qtip', function(){
  return {
    restrict: 'E',
    templateUrl: 'templates/qtip.html',
    transclude: true,
    replace: true,
    scope: {
    },
    link: function( $scope, $ele, attrs ){
      var id = attrs.qtipId;
      var $callingScope = $scope.$parent;
      var $target = $('#' + attrs.qtipTarget);
      var $posTarget = attrs.qtipPosTarget ? $('#' + attrs.qtipPosTarget) : undefined;
      var $vpTarget = attrs.qtipViewportTarget ? $('#' + attrs.qtipViewportTarget) : undefined;
      var $posCtr = attrs.qtipContainer ? $('#' + attrs.qtipContainer) : undefined;
      var showEvent = attrs.showEvent || 'click';
      var hideEvent = attrs.hideEvent || 'unfocus click';
      var adjustMethod = attrs.qtipAdjustMethod || 'shift flipinvert flip';
      var adjustResize = attrs.qtipAdjustResize === 'true' ? true : false;
      var title = attrs.title;
      var classes = attrs.qtipClass;
      var showVal = attrs.qtipShow;
      var posMy = attrs.qtipPosMy || 'top center';
      var posAt = attrs.qtipPosAt || 'bottom center';
      var visible = false;
      var qtipScopeId = attrs.qtipScopeId || Math.round( Math.random() * 1000000 );
      var digestOnShow = attrs.qtipDigestOnShow === 'true' || attrs.qtipDigestOnShow === true;

      if( attrs.showEvent === 'none' ){
        attrs.showEvent = '';
      }

      if( attrs.hideEvent === 'none' ){
        attrs.hideEvent = '';
      }

      if( attrs.qtipShow ){
        $callingScope.$watch(attrs.qtipShow, function(val){
          var api = $target.qtip('api');
          if( !api ){ return; } // or else err

          var tooltip = api.elements.tooltip;
          var shown = tooltip && tooltip.is(':visible');
          var show = !shown && val;
          var hide = shown && !val;

          if( show ){
            $target.qtip('show');
          } else if( hide ){
            $target.qtip('hide');
          }
        });
      }

      // $callingScope.qtip = $callingScope.qtip || {};

      // var qtipScope = $callingScope.qtip[ qtipScopeId ] = $callingScope.qtip[ qtipScopeId ] || {};
      // qtipScope.visible = false;

      $ele.on('$destroy', function() {
        $target.qtip('destroy');
      });

      $target.qtip({
        id: id ? id : undefined,

        content: {
          text: $ele,
          title: { button: false, text: title }
        },

        position: {
          container: $posCtr ? $posCtr : $('body'),
          my: posMy,
          at: posAt,
          effect: false,
          viewport: $vpTarget ? $vpTarget : true,
          adjust: {
            method: adjustMethod,
            scroll: false,
            resize: adjustResize
          },
          target: $posTarget
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
        },

        events: {
          show: function(){
            // visible = true;

            // qtipScope.visible = true;

            // if( digestOnShow && !$callingScope.$$phase ){
            //   $callingScope.$digest();
            // }
          },
          hide: function(){
            // visible = false;

            // qtipScope.visible = false;

            // if( !$callingScope.$$phase ){
            //   $callingScope.$digest();
            // }
          }
        }
      });

    }
  };
});