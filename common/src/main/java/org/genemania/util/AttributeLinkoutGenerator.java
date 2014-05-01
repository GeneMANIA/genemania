package org.genemania.util;

import java.util.LinkedHashMap;
import java.util.Map;

import org.genemania.domain.Attribute;
import org.genemania.domain.AttributeGroup;
import org.genemania.domain.Organism;

/*
 * not sure we need the fancy multiple linkouts
 * with fallbacks for attributes as we have for
 * genes. for now a very simple implementation
 * that just interpolates an external id into
 * a url from the data. 
 * 
 * but for future expansion, it works in the same style
 * as gene-linkout-generator, so returns a map of label
 * to url's. But why didn't i use some linkout-type object
 * with a label and url field instead of maps? 
 */
public class AttributeLinkoutGenerator {

    private static AttributeLinkoutGenerator instance;
    private static String PARAM = "{1}";
    
    // not thread safe, eh?
    public static AttributeLinkoutGenerator instance() {
        if (instance == null) {  
            instance = new AttributeLinkoutGenerator();            
        }
        
        return instance;
    }
    
    /*
     * ok, don't actually need the organism right now, but i feel i should ask for it
     * 
     * The convention for now is that the url in the group is a template with a {1} where
     * the external-id needs to go
     */
    public Map<String, String> getLinkouts(Organism organism, AttributeGroup group, Attribute attribute) {
        
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        
        if (group.getLinkoutUrl() != null) {
            
            String url = group.getLinkoutUrl().replace(PARAM, attribute.getExternalId());

            // use url for label if we don't have one in data
            String label = group.getLinkoutLabel();
            if (label == null || label.equals("")) {
                label = url;
            }

            result.put(label, url);
        }
        
        return result;
    }
}
