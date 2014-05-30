
require.config({
    baseUrl: 'js/lib',
    paths: {
        app: '../app',
        jquery: 'jquery-1.9.1.min',
        jqueryui: 'jquery-ui-1.10.0.custom.min',
        fancytree: 'fancytree/jquery.fancytree-all',
        fileupload: 'jquery-fileupload/js/jquery.fileupload',
        mustache: 'mustache',
        datatables: 'datatables/js/jquery.dataTables',
        bootstrap: 'bootstrap/js/bootstrap',
        bootbox: 'bootbox.min',
        jqueryform: 'jquery.form',
    },
    shim: {
        jqueryui: {
            exports: '$',
            deps: ['jquery'],
        },
        jqueryform: {
            deps: ['jquery']
        },
        fancytree: {
            deps: ['jqueryui'],
        },
        bootbox: {
            exports: 'bootbox',
            deps: ['jquery', 'bootstrap'],
        },
         bootstrap: {
            deps: ['jquery']
        },
        'app/tree': {
            deps: ['fancytree', 'jquery', 'app/constants', 'app/details'],
        },
        'app/menu': {
            deps: ['jquery', 'app/constants'],
        },
        'app/details': {
            deps: ['jquery', 'app/constants', 'app/templates'],
        },
    },
});

require(['jquery', 'bootstrap', 'app/menu', 'app/tree', 'app/constants'],
    function($, bootstrap, menu, tree, constants) {

        tree.setupTree(constants.INIT_ORGANISM_ID);
        tree.setupSearch();
        menu.setupMenu();

});