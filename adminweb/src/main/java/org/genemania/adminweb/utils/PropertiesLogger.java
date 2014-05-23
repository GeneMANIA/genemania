package org.genemania.adminweb.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Properties;

@Component
public class PropertiesLogger {
    private static Logger logger = LoggerFactory.getLogger(PropertiesLogger.class);

    @Autowired
    @Qualifier("allProperties")
    private Properties props;

    @PostConstruct
    public void logProperties() {
        for (Map.Entry<Object, Object> prop : props.entrySet()) {
            logger.debug("{}={}", prop.getKey(), prop.getValue());
        }
    }
}
