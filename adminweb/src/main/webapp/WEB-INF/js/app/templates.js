
// there must be a simpler, and probably obvious way to
// set this up. but what?
define(['text!app/templates/network_details.html',
        'text!app/templates/identifier_details.html',
        'text!app/templates/identifier_folder_details.html',
        'text!app/templates/organism_details.html',
        'text!app/templates/group_details.html',
        'text!app/templates/functions_folder_details.html',
        'text!app/templates/functions_details.html',
        ],
        function(network, identifier, identifier_folder,
            organism, group, functions_folder, functions)  {

    templates = {};

    templates.network = network;
    templates.identifier = identifier;
    templates.identifier_folder = identifier_folder;
    templates.organism = organism;
    templates.group = group;
    templates.functions_folder = functions_folder;
    templates.functions = functions;

    return templates;
});

