/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2008-2011 University of Toronto.
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

package org.genemania.plugin.apps;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.Organism;
import org.genemania.engine.apps.VectorCrossValidator;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.AttributeMediator;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.data.IMediatorProvider;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.Network;
import org.genemania.plugin.parsers.TabDelimitedQueryParser;
import org.kohsuke.args4j.Option;
import org.xml.sax.SAXException;

@SuppressWarnings("nls")
public class AbstractValidationApp extends AbstractPluginDataApp {
	@Option(name = "--organism", required = true, usage = "organism name")
	protected String fOrganismName;
	
	@Option(name = "--folds", usage = "number of folds to use for each query, defaults to 5")
	protected int fFolds = 5;
	
	@Option(name = "--query", required = true, usage = "name of file containing gene queries")
	protected String fQueryFile;

	@Option(name = "--outfile", required = true, usage = "name of output file to contain validation results")
	protected String fOutputFile;
	
	@Option(name = "--auto-negatives", usage = "force all non-positive genes to be negative")
	protected boolean fAutoNegatives = false;
	
    @Option(name = "--method", usage = "optional, network combining method, defaults to 'automatic'")
	protected String fCombiningMethod = "automatic";
    
    @Option(name = "--seed", usage = "optional, random seed to use when generating cross-validation folds, 0 (default) will select a seed based on system time")
    protected int fSeed = 0;
    
    @Option(name = "--use-go-cache", usage = "optional, use cached GO gene sets instead of genes in query file")
    protected boolean fUseGoCache = false;
    
    @Option(name = "--min", usage = "minimum number of positive genes per set")
    private Integer fMinimumGeneSetSize;
    
    @Option(name = "--max", usage = "maximum number of positive genes per set")
    private Integer fMaximumGeneSetSize;
    
    protected Organism fOrganism;

	private IQueryErrorHandler fErrorHandler;
	
	protected void initialize() throws ApplicationException, DataStoreException {
		try {
			checkPath(fDataPath);
			DataSetManager manager = createDataSetManager();
			fData = manager.open(new File(fDataPath));
			
			if (fThreads < 1) {
				fThreads = 1;
			}
			
	        fOrganism = parseOrganism(fData, fOrganismName);
	        if (fOrganism == null) {
	        	throw new ApplicationException(String.format("Unrecognized organism: %s", fOrganismName));
	        }

	        if (fVerbose) {
	            Logger.getLogger("org.genemania").setLevel(Level.INFO);
	        }
		} catch (SAXException e) {
			throw new ApplicationException(e);
		}
		
		final Logger logger = Logger.getLogger(getClass());
		fErrorHandler = new IQueryErrorHandler() {
			public void warn(String message) {
				logger.warn(message);
			}
			
			public void handleUnrecognizedNetwork(String network) {
				logger.warn(network);
			}
			
			public void handleUnrecognizedGene(String gene) {
				logger.warn(gene);
			}
			
			public void handleSynonym(String gene) {
			}
			
			public void handleNetwork(InteractionNetwork network) {
			}
		};
	}

	protected Collection<Group<?, ?>> parseNetworks(String networkData, String excludeData, Organism organism) {
	    TabDelimitedQueryParser parser = new TabDelimitedQueryParser();
	    
	    AttributeMediator attributeMediator = fData.getMediatorProvider().getAttributeMediator();
		Collection<Group<?, ?>> networks = parser.parseNetworks(networkData, organism, ",", fErrorHandler, attributeMediator);
	    Collection<Group<?, ?>> excludedNetworks = parser.parseNetworks(excludeData, organism, ",", fErrorHandler, attributeMediator);
	    
	    Set<Network<?>> excluded = new HashSet<Network<?>>();
	    for (Group<?, ?> group : excludedNetworks) {
	    	excluded.addAll(group.getNetworks());
	    }
	    
	    List<Group<?, ?>> result = new ArrayList<Group<?, ?>>();
	    for (Group<?, ?> group : networks) {
	    	Set<Network<?>> members = new HashSet<Network<?>>();
	    	for (Network<?> network : group.getNetworks()) {
	    		if (!excluded.contains(network)) {
	    			members.add(network);
	    		}
	    	}
	    	if (members.size() > 0) {
	    		result.add(group.filter(members));
	    	}
	    }
	    return result;
	}
	
	protected VectorCrossValidator createValidator(String networkData, String excludeData) throws ApplicationException {
	    VectorCrossValidator vcv = new VectorCrossValidator();
	    vcv.setCacheNamespace(DataSet.USER);
	    
	    IMediatorProvider provider = fData.getMediatorProvider();
	    vcv.setGeneMediator(provider.getGeneMediator());
	    vcv.setNetworkMediator(provider.getNetworkMediator());
	    vcv.setNodeMediator(provider.getNodeMediator());
	    vcv.setOrganismMediator(provider.getOrganismMediator());
	    vcv.setAttributeMediator(provider.getAttributeMediator());
	    
	    vcv.setCacheDir(fData.getFullPath(DataSet.CACHE_PATH));
	    vcv.setOrganismId(fOrganism.getId());
	    
	    if (networkData != null) {
	    	Collection<Group<?, ?>> networks = parseNetworks(networkData, excludeData, fOrganism);
			if (networks.size() == 0) {
				throw new ApplicationException("None of the subject networks you specified were recognized.");
			}
	    	vcv.setNetworkIds(collapseNetworks(networks));
	    	vcv.setAttrIds(collapseAttributeGroups(networks));
	    }
	    
	    vcv.setCombiningMethodName(fCombiningMethod);
	    vcv.setNumFolds(fFolds);
	    
	    vcv.setQueryFileName(fQueryFile);
	    vcv.setAllNegCrossVal(fAutoNegatives);
	    vcv.setThreads(fThreads);
	    vcv.setSeed(fSeed);
	    vcv.setUseCachedGoAnnotations(fUseGoCache);
	    vcv.setMinimumGeneSetSize(fMinimumGeneSetSize);
	    vcv.setMaxmimumGeneSetSize(fMaximumGeneSetSize);
	    return vcv;
	}
}
