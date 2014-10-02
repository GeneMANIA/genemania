app.factory('cy', 
[
function(  ){

  var cy = window.cy = cytoscape({
    container: document.getElementById('cy'),

    style: [
      {
        selector: 'node',
        css: {
          'width': 'mapData(score, 0, 1, 20, 60)',
          'height': 'mapData(score, 0, 1, 20, 60)',
          'content': 'data(symbol)',
          'font-size': 6,
          'text-valign': 'center',
          'text-halign': 'center',
          'background-color': '#888',
          'text-outline-color': '#888',
          'text-outline-width': 2,
          'color': '#fff',
          'overlay-padding': 6
        }
      },

      {
        selector: 'edge',
        css: {
          'curve-style': 'haystack',
          'opacity': 0.333,
          'width': 'mapData(weight, 0, 1, 1, 8)',
          'overlay-padding': 3
        }
      },

      {
        selector: 'edge[group="coexp"]',
        css: {
          'line-color': '#d0b7d5'
        }
      },

      {
        selector: 'edge[group="coloc"]',
        css: {
          'line-color': '#a0b3dc'
        }
      },

      {
        selector: 'edge[group="gi"]',
        css: {
          'line-color': '#90e190'
        }
      },

      {
        selector: 'edge[group="path"]',
        css: {
          'line-color': '#9bd8de'
        }
      },

      {
        selector: 'edge[group="pi"]',
        css: {
          'line-color': '#eaa2a2'
        }
      },

      {
        selector: 'edge[group="predict"]',
        css: {
          'line-color': '#f6c384'
        }
      },

      {
        selector: 'edge[group="spd"]',
        css: {
          'line-color': '#dad4a2'
        }
      },

      {
        selector: 'edge[group="spd_attr"]',
        css: {
          'line-color': '#D0D0D0'
        }
      },

      {
        selector: 'edge[group="reg"]',
        css: {
          'line-color': '#D0D0D0'
        }
      },

      {
        selector: 'edge[group="reg_attr"]',
        css: {
          'line-color': '#D0D0D0'
        }
      },

      {
        selector: 'edge[group="user"]',
        css: {
          'line-color': '#f0ec86'
        }
      }
    ],

    motionBlur: true
  });

  return cy;

}]);