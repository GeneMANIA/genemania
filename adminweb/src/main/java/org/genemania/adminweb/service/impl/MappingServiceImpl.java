package org.genemania.adminweb.service.impl;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.genemania.adminweb.service.MappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/*
 * Object <--> String, via jackson. for stuffing in a db because we are
 * too lazy to create a bunch of misc fields for every bit of validation data
 */
@Service
public class MappingServiceImpl implements MappingService {
    final Logger logger = LoggerFactory.getLogger(MappingServiceImpl.class);

    @Override
    public String map(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        String string = null;

        try {
            string = mapper.writeValueAsString(object);
        }
        catch (JsonMappingException e) {
            logger.warn("failed to encode processing details");
        } catch (JsonGenerationException e) {
            logger.warn("failed to encode processing details");
        } catch (IOException e) {
            logger.warn("failed to encode processing details");
        }

        return string;
    }

    @Override
    public Object unmap(String string, Class clazz) {
        // TODO: hide jackson dependency behind a service
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Object object = null;
        try {
            object = mapper.readValue(string, clazz);
        } catch (JsonParseException e) {
            logger.warn("failed to convert processing details to json", e);
        } catch (JsonMappingException e) {
            logger.warn("failed to convert processing details to json", e);
        } catch (IOException e) {
            logger.warn("failed to convert processing details to json", e);
        }

        return object;
    }

}
