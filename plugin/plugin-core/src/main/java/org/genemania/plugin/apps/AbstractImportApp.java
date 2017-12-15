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
import java.io.IOException;
import java.util.regex.Pattern;

import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.engine.Mania2;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.cache.MemObjectCache;
import org.genemania.engine.cache.SynchronizedObjectCache;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.data.Namespace;
import org.genemania.util.NullProgressReporter;
import org.kohsuke.args4j.Option;
import org.xml.sax.SAXException;

public abstract class AbstractImportApp extends AbstractPluginDataApp {
	@Option(name = "--filename", required = true, usage = "file to import")
	protected String fFilename;

	@Option(name = "--name", required = true, usage = "name of new network")	
	protected String fNetworkName;
	
	@Option(name = "--group", usage = "group to which new network should be added.  If the group doesn't exist, it gets created; defaults to 'Other'")	
	protected String fNetworkGroupName = "other"; //$NON-NLS-1$
	
	@Option(name = "--group-description", usage = "description of new network group.  Only applicable if a new network group is being created")	
	protected String fNetworkGroupDescription = "Other"; //$NON-NLS-1$

	@Option(name = "--color", usage = "a 6 digit hex representation of the color of the network being created in RRGGBB format; defaults to '000000' (black)")	
	protected String fGroupColor = "000000"; //$NON-NLS-1$

	@Option(name = "--description", usage = "description of new network")
	protected String fDescription;
	
	@Option(name = "--organism", required = true, usage = "organism name")
	protected String fOrganismName;
	
	@Option(name= "--namespace")
	protected String fNamespaceName = "user"; //$NON-NLS-1$
	
	protected Mania2 fMania;
	protected File fFile;
	protected Organism fOrganism;
	protected InteractionNetworkGroup fNetworkGroup;
	protected Namespace fNamespace;
	
	private static final Pattern COLOR_PATTERN = Pattern.compile("[0-9a-fA-F]{6}"); //$NON-NLS-1$
	
	protected void initialize() throws ApplicationException, DataStoreException, IOException {
		try {
			DataSetManager manager = createDataSetManager();
			fData = manager.open(new File(fDataPath));
			fData.setHeadless(true);
			
			DataCache cache = new DataCache(new SynchronizedObjectCache(new MemObjectCache(fData.getObjectCache(NullProgressReporter.instance(), false))));
			fMania = new Mania2(cache);
	
			if (fNamespaceName.equalsIgnoreCase("core")) { //$NON-NLS-1$
				fNamespace = Namespace.CORE;
			} else {
				fNamespace = Namespace.USER;
			}
			
			fFile = new File(fFilename);
			File parent = fFile.getParentFile();
			if (parent != null && !parent.exists()) {
				throw new ApplicationException(String.format("Cannot write to file: %s", fFile.getCanonicalPath())); //$NON-NLS-1$
			}
			if (!(fFile.isFile() || !fFile.exists())) {
				throw new ApplicationException(String.format("Cannot write output because file already exists but isn't a regular file: %s", fFile.getCanonicalPath())); //$NON-NLS-1$
			}
			
	        fOrganism = parseOrganism(fData, fOrganismName);
	        if (fOrganism == null) {
	        	throw new ApplicationException(String.format("Unrecognized organism: %s", fOrganismName)); //$NON-NLS-1$
	        }

			if (fNetworkName.trim().length() == 0) {
				throw new ApplicationException("Network name cannot be blank"); //$NON-NLS-1$
			}
			
			fNetworkGroup = parseGroup(fOrganism, fNetworkGroupName);
			
			if (fNetworkGroup == null) {
				if (fNetworkGroupDescription.trim().length() == 0) {
					throw new ApplicationException("Network description cannot be blank"); //$NON-NLS-1$
				}

				if (!COLOR_PATTERN.matcher(fGroupColor).matches()) {
					throw new ApplicationException("Network group color should be in RRGGBB hex format"); //$NON-NLS-1$
				}

				fNetworkGroup = new InteractionNetworkGroup();
				fNetworkGroup.setName(fNetworkGroupDescription);
				fNetworkGroup.setDescription(fNetworkGroupDescription);
				fNetworkGroup.setCode(fNetworkGroupName);
			}
			
	        if (networkExists(fNetworkName, fOrganism, fNetworkGroup)) {
				throw new ApplicationException(String.format("Network named '%s' already exists in group '%s'", fNetworkName, fNetworkGroup.getName())); //$NON-NLS-1$
			}
		} catch (SAXException e) {
			throw new ApplicationException(e);
		}
	}

	private boolean networkExists(String networkName, Organism organism, InteractionNetworkGroup group) {
		for (InteractionNetworkGroup otherGroup : organism.getInteractionNetworkGroups()) {
			if (!otherGroup.getCode().equals(group.getCode())) {
				continue;
			}
			for (InteractionNetwork network : otherGroup.getInteractionNetworks()) {
				if (network.getName().equals(networkName)) {
					return true;
				}
			}
		}
		return false;
	}
}
