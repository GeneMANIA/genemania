'use strict';

app.factory('cyStylesheet',
[
function(){

  return window.cyStylesheet = function(){

    var stylesheet = [
      {
        selector: 'core',
        css: {
          'selection-box-color': '#AAD8FF',
          'selection-box-border-color': '#8BB0D0',
          'selection-box-opacity': 0.5
        }
      },

      // disable this for performance for now...
      // {
      //   selector: 'node, edge',
      //   css: {
      //     'transition-property': 'opacity',
      //     'transition-duration': '200ms',
      //     'transition-timing-function': 'ease-in-out-quad'
      //   }
      // },

      {
        selector: 'node',
        css: {
          'width': 'mapData(normScore, 0, 1, 20, 60)',
          'height': 'mapData(normScore, 0, 1, 20, 60)',
          'content': 'data(name)',
          'font-size': 16,
          'text-valign': 'center',
          'text-halign': 'center',
          'background-color': '#555',
          'text-outline-color': '#555',
          'text-outline-width': 2,
          'color': '#fff',
          'overlay-padding': 6,
          'z-index': 10
        }
      },

      {
        selector: 'node[?gene]',
        css: ( function(){
          var css = {};

          for( var p = 1; p <= 16; p++ ){
            css['pie-'+p+'-background-size'] = 'data( css.pie_'+p+'_background_size )';
            css['pie-'+p+'-background-color'] = 'data( css.pie_'+p+'_background_color )';
            css['pie-'+p+'-background-opacity'] = 'data( css.pie_'+p+'_background_opacity )';
          }

          return css;
        } )()
      },

      {
        selector: 'node[?attr]',
        css: {
          'shape': 'rectangle',
          'background-color': '#aaa',
          'text-outline-color': '#aaa',
          'width': 20,
          'height': 20,
          'font-size': 12,
          'z-index': 1,
          'text-wrap': 'none'
        }
      },

      {
        selector: 'node[?query]',
        css: {
          'background-clip': 'none',
          'background-fit': 'contain',
          'background-image': 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiBzdHlsZT0idmVjdG9yLWVmZmVjdDogbm9uLXNjYWxpbmctc3Ryb2tlOyIgc3Ryb2tlPSJudWxsIj4NCsKgPCEtLSBDcmVhdGVkIHdpdGggU1ZHLWVkaXQgLSBodHRwOi8vc3ZnLWVkaXQuZ29vZ2xlY29kZS5jb20vIC0tPg0KwqA8ZyBzdHJva2U9Im51bGwiPg0KwqAgPHRpdGxlIHN0cm9rZT0ibnVsbCI+TGF5ZXIgMTwvdGl0bGU+DQrCoCA8ZWxsaXBzZSBzdHJva2Utb3BhY2l0eT0iMCIgc3Ryb2tlPSIjMDAwMDAwIiB0cmFuc2Zvcm09InJvdGF0ZSg0NS4yNDA2MDgyMTUzMzIwMyA1NS42NjQxOTIxOTk3MDcwMSw1NS4xNzM5NzMwODM0OTYxKSAiIGZpbGwtb3BhY2l0eT0iMC4yIiBpZD0ic3ZnXzI3IiByeT0iNDIuMDkwMTY0IiByeD0iMS4zNjI3NTgiIGN5PSI1NS4xNzM5NzMiIGN4PSI1NS42NjQxOTEiIHN0cm9rZS1saW5lY2FwPSJudWxsIiBzdHJva2UtbGluZWpvaW49Im51bGwiIHN0cm9rZS1kYXNoYXJyYXk9Im51bGwiIHN0cm9rZS13aWR0aD0iMCIgZmlsbD0iI2ZmZmZmZiIvPg0KwqAgPGVsbGlwc2Ugc3Ryb2tlLW9wYWNpdHk9IjAiIHN0cm9rZT0iIzAwMDAwMCIgaWQ9InN2Z18yOSIgdHJhbnNmb3JtPSJyb3RhdGUoNDUuMjQwNjA4MjE1MzMyMDMgNTAuMTY4ODQyMzE1NjczODE0LDQ5LjgyMTgzMDc0OTUxMTcyNikgIiBmaWxsLW9wYWNpdHk9IjAuMiIgcnk9IjQyLjg3OTM3NyIgcng9IjEuMzYyNzU4IiBjeT0iNDkuODIxODMxIiBjeD0iNTAuMTY4ODQxIiBzdHJva2UtbGluZWNhcD0ibnVsbCIgc3Ryb2tlLWxpbmVqb2luPSJudWxsIiBzdHJva2UtZGFzaGFycmF5PSJudWxsIiBzdHJva2Utd2lkdGg9IjAiIGZpbGw9IiNmZmZmZmYiLz4NCsKgIDxlbGxpcHNlIHN0cm9rZS1vcGFjaXR5PSIwIiBzdHJva2U9IiMwMDAwMDAiIGZpbGwtb3BhY2l0eT0iMC4yIiByeT0iMC4yIiBpZD0ic3ZnXzciIGN5PSIyNS44IiBjeD0iNTMuOCIgc3Ryb2tlLWxpbmVjYXA9Im51bGwiIHN0cm9rZS1saW5lam9pbj0ibnVsbCIgc3Ryb2tlLWRhc2hhcnJheT0ibnVsbCIgc3Ryb2tlLXdpZHRoPSIwIiBmaWxsPSIjZmZmZmZmIi8+DQrCoCA8ZWxsaXBzZSBzdHJva2Utb3BhY2l0eT0iMCIgc3Ryb2tlPSIjMDAwMDAwIiBpZD0ic3ZnXzMwIiB0cmFuc2Zvcm09InJvdGF0ZSg0NS4yNDA2MDgyMTUzMzIwMyA0NC4yNDcyNjEwNDczNjMyOCw0My45NjQ0Mjc5NDc5OTgwNSkgIiBmaWxsLW9wYWNpdHk9IjAuMiIgcnk9IjQyLjE3ODM3OSIgcng9IjEuMzYyNzU4IiBjeT0iNDMuOTY0NDI2IiBjeD0iNDQuMjQ3MjYxIiBzdHJva2UtbGluZWNhcD0ibnVsbCIgc3Ryb2tlLWxpbmVqb2luPSJudWxsIiBzdHJva2UtZGFzaGFycmF5PSJudWxsIiBzdHJva2Utd2lkdGg9IjAiIGZpbGw9IiNmZmZmZmYiLz4NCsKgIDxlbGxpcHNlIHN0cm9rZS1vcGFjaXR5PSIwIiBzdHJva2U9IiMwMDAwMDAiIGlkPSJzdmdfMzQiIHRyYW5zZm9ybT0icm90YXRlKDQ1LjI0MDYwODIxNTMzMjAzIDY2LjI3NTgzMzEyOTg4Mjc4LDY2LjYwNDgyNzg4MDg1OTM5KSAiIGZpbGwtb3BhY2l0eT0iMC4yIiByeT0iMzYuMDUwNTU5IiByeD0iMS4zNjI3NTgiIGN5PSI2Ni42MDQ4MjYiIGN4PSI2Ni4yNzU4MzIiIHN0cm9rZS1saW5lY2FwPSJudWxsIiBzdHJva2UtbGluZWpvaW49Im51bGwiIHN0cm9rZS1kYXNoYXJyYXk9Im51bGwiIHN0cm9rZS13aWR0aD0iMCIgZmlsbD0iI2ZmZmZmZiIvPg0KwqAgPGVsbGlwc2Ugc3Ryb2tlLW9wYWNpdHk9IjAiIHN0cm9rZT0iIzAwMDAwMCIgaWQ9InN2Z18zMyIgdHJhbnNmb3JtPSJyb3RhdGUoNDUuMjQwNjA4MjE1MzMyMDMgNjAuNTgyODkzMzcxNTgyMDIsNjEuMzcwODE5MDkxNzk2ODgpICIgZmlsbC1vcGFjaXR5PSIwLjIiIHJ5PSIzOS41MzE2NjgiIHJ4PSIxLjM2Mjc1OCIgY3k9IjYxLjM3MDgxNyIgY3g9IjYwLjU4Mjg4OSIgc3Ryb2tlLWxpbmVjYXA9Im51bGwiIHN0cm9rZS1saW5lam9pbj0ibnVsbCIgc3Ryb2tlLWRhc2hhcnJheT0ibnVsbCIgc3Ryb2tlLXdpZHRoPSIwIiBmaWxsPSIjZmZmZmZmIi8+DQrCoCA8ZWxsaXBzZSBzdHJva2Utb3BhY2l0eT0iMCIgc3Ryb2tlPSIjMDAwMDAwIiBpZD0ic3ZnXzM1IiB0cmFuc2Zvcm09InJvdGF0ZSg0NS4yNDA2MDgyMTUzMzIwMyAzMi43MDI4MDA3NTA3MzI0MywzMi43OTI5NDIwNDcxMTkxNSkgIiBmaWxsLW9wYWNpdHk9IjAuMiIgcnk9IjM1LjEyNzAzIiByeD0iMS4zNjI3NTgiIGN5PSIzMi43OTI5NDMiIGN4PSIzMi43MDI4MDEiIHN0cm9rZS1saW5lY2FwPSJudWxsIiBzdHJva2UtbGluZWpvaW49Im51bGwiIHN0cm9rZS1kYXNoYXJyYXk9Im51bGwiIHN0cm9rZS13aWR0aD0iMCIgZmlsbD0iI2ZmZmZmZiIvPg0KwqAgPGVsbGlwc2Ugc3Ryb2tlLW9wYWNpdHk9IjAiIHN0cm9rZT0iIzAwMDAwMCIgaWQ9InN2Z18zNiIgdHJhbnNmb3JtPSJyb3RhdGUoNDUuMjQwNjA4MjE1MzMyMDMgMjYuODQ3MDk1NDg5NTAxOTUzLDI3LjQxMzA4NDAzMDE1MTM2NCkgIiBmaWxsLW9wYWNpdHk9IjAuMiIgcnk9IjI4LjIxODA2IiByeD0iMS4zNjI3NTgiIGN5PSIyNy40MTMwODUiIGN4PSIyNi44NDcwOTQiIHN0cm9rZS1saW5lY2FwPSJudWxsIiBzdHJva2UtbGluZWpvaW49Im51bGwiIHN0cm9rZS1kYXNoYXJyYXk9Im51bGwiIHN0cm9rZS13aWR0aD0iMCIgZmlsbD0iI2ZmZmZmZiIvPg0KwqAgPGVsbGlwc2Ugc3Ryb2tlLW9wYWNpdHk9IjAiIHN0cm9rZT0iIzAwMDAwMCIgaWQ9InN2Z18zMSIgdHJhbnNmb3JtPSJyb3RhdGUoNDUuMjQwNjA4MjE1MzMyMDMgMzguMDY4MjY0MDA3NTY4MzcsMzkuMDA1NDM5NzU4MzAwNzgpICIgZmlsbC1vcGFjaXR5PSIwLjIiIHJ5PSIzOC45NTU1NjMiIHJ4PSIxLjM2Mjc1OCIgY3k9IjM5LjAwNTQzOSIgY3g9IjM4LjA2ODI2NSIgc3Ryb2tlLWxpbmVjYXA9Im51bGwiIHN0cm9rZS1saW5lam9pbj0ibnVsbCIgc3Ryb2tlLWRhc2hhcnJheT0ibnVsbCIgc3Ryb2tlLXdpZHRoPSIwIiBmaWxsPSIjZmZmZmZmIi8+DQrCoCA8ZWxsaXBzZSBzdHJva2Utb3BhY2l0eT0iMCIgc3Ryb2tlPSIjMDAwMDAwIiBpZD0ic3ZnXzM4IiB0cmFuc2Zvcm09InJvdGF0ZSg0NS4yNDA2MDgyMTUzMzIwMyA3Mi4yNTg0OTE1MTYxMTMyOCw3Mi4xNTU3NTQwODkzNTU0NykgIiBmaWxsLW9wYWNpdHk9IjAuMiIgcnk9IjI5LjQ3MTEzNyIgcng9IjEuMzYyNzU4IiBjeT0iNzIuMTU1NzU1IiBjeD0iNzIuMjU4NDkxIiBzdHJva2UtbGluZWNhcD0ibnVsbCIgc3Ryb2tlLWxpbmVqb2luPSJudWxsIiBzdHJva2UtZGFzaGFycmF5PSJudWxsIiBzdHJva2Utd2lkdGg9IjAiIGZpbGw9IiNmZmZmZmYiLz4NCsKgIDxlbGxpcHNlIHN0cm9rZS1vcGFjaXR5PSIwIiBzdHJva2U9IiMwMDAwMDAiIGlkPSJzdmdfMzciIHRyYW5zZm9ybT0icm90YXRlKDQ1LjI0MDYwODIxNTMzMjAzIDIyLjAwMTU3NzM3NzMxOTMzMiwyMS45NTI4NDQ2MTk3NTA5NzMpICIgZmlsbC1vcGFjaXR5PSIwLjIiIHJ5PSIxNi43OTcwMzciIHJ4PSIxLjM2Mjc1OCIgY3k9IjIxLjk1Mjg0MyIgY3g9IjIyLjAwMTU3NiIgc3Ryb2tlLWxpbmVjYXA9Im51bGwiIHN0cm9rZS1saW5lam9pbj0ibnVsbCIgc3Ryb2tlLWRhc2hhcnJheT0ibnVsbCIgc3Ryb2tlLXdpZHRoPSIwIiBmaWxsPSIjZmZmZmZmIi8+DQrCoCA8ZWxsaXBzZSBzdHJva2Utb3BhY2l0eT0iMCIgc3Ryb2tlPSIjMDAwMDAwIiBpZD0ic3ZnXzM5IiB0cmFuc2Zvcm09InJvdGF0ZSg0NS4yNDA2MDgyMTUzMzIwMyA3Ny4xNDE3NzcwMzg1NzQyMiw3Ny43NDE0MDE2NzIzNjMyOCkgIiBmaWxsLW9wYWNpdHk9IjAuMiIgcnk9IjE3Ljk5NTAzOSIgcng9IjEuMzYyNzU4IiBjeT0iNzcuNzQxNDAzIiBjeD0iNzcuMTQxNzc3IiBzdHJva2UtbGluZWNhcD0ibnVsbCIgc3Ryb2tlLWxpbmVqb2luPSJudWxsIiBzdHJva2UtZGFzaGFycmF5PSJudWxsIiBzdHJva2Utd2lkdGg9IjAiIGZpbGw9IiNmZmZmZmYiLz4NCsKgPC9nPg0KPC9zdmc+'
        }
      },

      {
        selector: 'node:selected',
        css: {
          'border-width': 6,
          'border-color': '#AAD8FF',
          'border-opacity': 0.5,
          'background-color': '#77828C',
          'text-outline-color': '#77828C'
        }
      },

      {
        selector: 'edge',
        css: {
          'curve-style': 'haystack',
          'haystack-radius': 0.5,
          'opacity': 0.4,
          'line-color': '#bbb',
          'width': 'mapData(weight, 0, 1, 1, 8)',
          'overlay-padding': 3
        }
      },

      {
        selector: 'edge[?attr]',
        css: {
          'haystack-radius': 0
        }
      },

      {
        selector: 'node.unhighlighted',
        css: {
          'opacity': 0.2
        }
      },

      {
        selector: 'edge.unhighlighted',
        css: {
          'opacity': 0.05
        }
      },

      {
        selector: '.highlighted',
        css: {
          'z-index': 999999
        }
      },

      {
        selector: 'node.highlighted',
        css: {
          'border-width': 6,
          'border-color': '#AAD8FF',
          'border-opacity': 0.5,
          'background-color': '#394855',
          'text-outline-color': '#394855',
          'shadow-blur': 12,
          'shadow-color': '#000',
          'shadow-opacity': 0.8,
          'shadow-offset-x': 0,
          'shadow-offset-y': 4
        }
      },

      {
        selector: '.filtered',
        css: {
          'opacity': 0
        }
      },

      {
        selector: 'edge.collapsed',
        css: {
          'opacity': 0
        }
      },

      {
        selector: 'node.plain-label',
        css: {
          'text-valign': 'top',
          'text-halign': 'center',
          'color': '#000',
          'text-outline-width': 1.5,
          'text-outline-color': '#fff',
          'text-outline-opacity': 0.8
        }
      },

      {
        selector: 'node[?attr].plain-label',
        css: {
          'text-outline-width': 0.75
        }
      },

      {
        selector: 'node.with-descr',
        css: {
          'text-wrap': 'wrap',
          'text-max-width': 400,
          'content': 'data(nameDescr)',
          'text-halign': 'right',
          'color': '#000',
          'text-outline-width': 0
        }
      },

      {
        selector: 'node[?query].with-descr',
        css: {
          'text-halign': 'left'
        }
      },

      {
        selector: '.hidden',
        css: {
          'visibility': 'hidden'
        }
      }
    ];

    Array.prototype.push.apply( stylesheet, config.networks.colors.map(function( colorSpec ){
      return {
        selector: 'edge[group="'+ colorSpec.code +'"]',
        css: {
          'line-color': colorSpec.color
        }
      };
    }) );

    return stylesheet;
  };

}]);
