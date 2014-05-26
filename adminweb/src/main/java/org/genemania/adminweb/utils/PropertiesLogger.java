package org.genemania.adminweb.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

@Component
public class PropertiesLogger {
    private static Logger logger = LoggerFactory.getLogger(PropertiesLogger.class);

    @Autowired(required=false) // not resolved for unit tests
    @Qualifier("allProperties")
    private Properties props;

    @PostConstruct
    public void logProperties() {

        if (props == null) {
            logger.debug("no properties found");
            return;
        }

        TreeMap<Object, Object> sorted = new TreeMap(props);
        for (Map.Entry<Object, Object> entry: sorted.entrySet()) {
            String key = entry.getKey().toString();
            String val = entry.getValue().toString();

            // scrub passwords
            if (key.toLowerCase().contains("pass")) {
                val = "(password)";
            }

            logger.debug("{}={}", key, val);
        }
    }
}
