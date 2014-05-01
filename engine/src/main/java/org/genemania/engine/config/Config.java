/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2010 University of Toronto.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.genemania.engine.config;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.genemania.engine.Constants;
import org.genemania.exception.ApplicationException;

/**
 * parameters affecting engine function, eg internal alg
 * parameters & data storage formats.
 *
 * depends on apache commons-configuration, which is already a
 * dependency of common.
 *
 * 
 */
public class Config {
    private static Logger logger = Logger.getLogger(Config.class);
    private static Config config;

    public static final String DEFAULT_ENGINE_CONFIG_FILE = "org/genemania/engine/Engine.properties";

    // property names used in config data file. argh, why am i making these names so horribly loooonnnng
    static final String MATRIX_FACTORY_CLASS_NAME = "matrixFactoryClassName";
    static final String VERSION = "version";
    static final String IS_REG_ENABLED = "isRegularizationEnabled";
    static final String REG_CONST = "regularizationConstant";
    static final String IS_NETWORK_WEIGHT_NORMALIZATION_ENABLED = "isNetworkWeightNormalizationEnabled";
    static final String IS_COMBINED_NETWORK_NORMALIZATION_ENABLED = "isCombinedNetworkNormalizationEnabled";
    static final String ATTRIBUTE_ENRICHMENT_MAX_SIZE = "attributeEnrichmentMaxSize";
    
    
    private MatrixFactory matrixFactory;

    private Configuration configData;

    protected Config() {
    }
    
    public synchronized static Config instance() {
        if (config == null) {
            try {
                reload();
            }
            catch (ApplicationException e) {
                logger.error("configuration error", e);
            }
        }
        return config;
    }

    /*
     * drop current config and reload default
     */
    public synchronized static void reload() throws ApplicationException {
        reload(DEFAULT_ENGINE_CONFIG_FILE);
    }

    /*
     * drop current config and reload from givenfile
     */
    public static void reload(String configFile) throws ApplicationException {
        logger.info("loading engine config from " + configFile);

        try {
            Config newConfig = new Config();
            newConfig.configData = new PropertiesConfiguration(configFile);
            
            // replace previous instance if successful
            config = newConfig;
        }
        catch (ConfigurationException e) {
            throw new ApplicationException("failed to load configuration from " + configFile, e);
        }
    }
    
    private Object createObject(String className) {
        Object object = null;
        try {
            Class classDef = Class.forName(className);
            object = classDef.newInstance();
        }
        catch (Exception e) {
            logger.error("failed to create instance of: " + className, e);
        }
        return object;
    }

    /**
     * @return the matrixFactory
     */
    public MatrixFactory getMatrixFactory() {
        if (matrixFactory == null) {
            String className = configData.getString(MATRIX_FACTORY_CLASS_NAME);
            matrixFactory = (MatrixFactory) createObject(className);
        }
        return matrixFactory;
    }

    public String getVersion() {
        return configData.getString(VERSION, Constants.UNKNOWN_VERSION);
    }

    public boolean isRegularizationEnabled() {
        return configData.getBoolean(IS_REG_ENABLED, Constants.DEFAULT_IS_REGULARIZATION_ENABLED);
    }

    public double getRegularizationConstant() {
        return configData.getDouble(REG_CONST, Constants.DEFAULT_REGULARIZATION_CONSTANT);
    }

    public boolean isNetworkWeightNormalizationEnabled() {
        return configData.getBoolean(IS_NETWORK_WEIGHT_NORMALIZATION_ENABLED, Constants.DEFAULT_NORMALIZE_NETWORK_WEIGHTS_ENABLED);
    }

    public boolean isCombinedNetworkRenormalizationEnabled() {
        return configData.getBoolean(IS_COMBINED_NETWORK_NORMALIZATION_ENABLED, Constants.DEFAULT_COMBINED_NETWORK_NORMALIZATION_ENABLED);
    }
    
    public int getAttributeEnrichmentMaxSize() {
    	return configData.getInt(ATTRIBUTE_ENRICHMENT_MAX_SIZE, Constants.DEFAULT_ATTRIBUTE_ENRICHMENT_MAX_SIZE);
    }
}
