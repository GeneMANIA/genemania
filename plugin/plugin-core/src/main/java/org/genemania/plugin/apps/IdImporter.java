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
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Node;
import org.genemania.domain.Organism;
import org.genemania.dto.AddOrganismEngineRequestDto;
import org.genemania.engine.IMania;
import org.genemania.engine.Mania2;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.cache.MemObjectCache;
import org.genemania.engine.cache.SynchronizedObjectCache;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.plugin.FileUtils;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.data.IModelWriter;
import org.genemania.plugin.data.Namespace;
import org.genemania.plugin.parsers.IdFileParser;
import org.genemania.util.NullProgressReporter;
import org.genemania.util.ProgressReporter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.xml.sax.SAXException;

public class IdImporter extends AbstractPluginApp {
	protected DataSet fData;

	@Option(name = "--data", usage = "optional, path to a GeneMANIA data set (e.g. gmdata-2010-06-24)")
	protected String fDataPath;

	private static final String DEFAULT_COLOR = "000000"; //$NON-NLS-1$

	private static final Pattern DATA_SET_PATTERN = Pattern.compile(".*?gmdata-(.*?)"); //$NON-NLS-1$

	@Option(name = "--filename", required = true, usage = "file containing object identifier information (e.g. gene symbols)")
	String fFilename;
	
	@Option(name = "--taxid", usage = "optional, taxonomy identifier for ids to import")
	Integer fTaxId;
	
	@Option(name = "--name", required = true, usage = "name of entity that identifiers belong to (e.g. organism)")
	String fName;
	
	@Option(name = "--alias", usage = "optional, alias of entity that identifiers belong to")
	String fAlias;
	
	@Option(name = "--description", usage = "optional, description of entity that identifiers belong to")
	String fDescription;

	@Option(name= "--namespace")
	protected String fNamespaceName = "user"; //$NON-NLS-1$

	private IMania fMania;

	private Namespace fNamespace;
	
	private void initialize() throws ApplicationException, DataStoreException {
		try {
			DataSetManager manager = createDataSetManager();

			if (fDataPath == null) {
				FileUtils fileUtils = new FileUtils();
				List<String> dataSets = fileUtils.getCompatibleDataSets(FileUtils.DEFAULT_BASE_URL, GeneMania.SCHEMA_VERSION);
				String latestDataSet = dataSets.get(0);
				String id = getDataSetId(latestDataSet);
				
				String baseUrl = fileUtils.findDataSetBaseUrl(FileUtils.DEFAULT_BASE_URL, id);
				URL url = new URL(String.format("%s.zip", baseUrl)); //$NON-NLS-1$
				ProgressReporter progress = NullProgressReporter.instance();
				File dataZipFile = fileUtils.download(url, new File("."), progress); //$NON-NLS-1$
				fileUtils.unzip(dataZipFile, dataZipFile.getParentFile(), progress);
				fDataPath = getDataPath(dataZipFile);
				dataZipFile.delete();
				System.err.printf("Warning: No data set was specified; creating a new one at: %s\n", fDataPath); //$NON-NLS-1$
			}

			File file = new File(fDataPath);
			if (!manager.isDataSet(file)) {
				throw new ApplicationException(String.format("%s is not a GeneMANIA data set", file)); //$NON-NLS-1$
			}
			fData = manager.open(file);

			DataCache cache = new DataCache(new SynchronizedObjectCache(new MemObjectCache(fData.getObjectCache(NullProgressReporter.instance(), false))));
			fMania = new Mania2(cache);
			
			if (fNamespaceName.equalsIgnoreCase("core")) { //$NON-NLS-1$
				fNamespace = Namespace.CORE;
			} else {
				fNamespace = Namespace.USER;
			}
		} catch (SAXException e) {
			throw new ApplicationException(e);
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}
	
	private String getDataPath(File zipFile) {
		String[] parts = zipFile.getPath().split(File.separator);
		String fileName = parts[parts.length - 1];
		int index = fileName.lastIndexOf("."); //$NON-NLS-1$
		fileName = fileName.substring(0, index);
		String basePath = zipFile.getParent();
		return String.format("%s%s%s", basePath, File.separator, fileName); //$NON-NLS-1$
	}

	public static void main(String[] args) throws Exception {
		IdImporter importer = new IdImporter();
        CmdLineParser parser = new CmdLineParser(importer);
        try {
        	parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println(String.format("\nUsage: %s options\n", IdImporter.class.getSimpleName())); //$NON-NLS-1$
            parser.printUsage(System.err);
            return;
        }
		importer.initialize();
		importer.importIds();
	}

	private void importIds() throws DataStoreException, ApplicationException, IOException {
		Organism organism = parseOrganism(fData, fName);
		if (organism != null) {
			throw new ApplicationException(String.format("Organism with name '%s' already exists in the data set", fName)); //$NON-NLS-1$
		}
		
		if (isEmpty(fAlias)) {
			fAlias = fName;
		}

		if (isEmpty(fDescription)) {
			fDescription = ""; //$NON-NLS-1$
		}

		organism = createOrganism();
		InteractionNetworkGroup group = createDefaultGroup();
		IdFileParser parser = new IdFileParser(fData, fNamespace);
		Set<Node> nodes = parser.parseNodes(new FileReader(fFilename), organism, NullProgressReporter.instance());
		
		IModelWriter writer = fData.createModelWriter();
		try {
			writer.addOrganism(organism);
			writer.addGroup(group, organism, DEFAULT_COLOR);
			for (Node node : nodes) {
				writer.addNode(node, organism);
			}
		} finally {
			writer.close();
		}
		
		AddOrganismEngineRequestDto request = new AddOrganismEngineRequestDto();
		request.setProgressReporter(NullProgressReporter.instance());
		request.setOrganismId(organism.getId());
		Collection<Long> nodeIds = parser.extractNodeIds(nodes);
		request.setNodeIds(nodeIds);
		fMania.addOrganism(request);
	}

	private InteractionNetworkGroup createDefaultGroup() throws ApplicationException {
		InteractionNetworkGroup group = new InteractionNetworkGroup();
		group.setId(fData.getNextAvailableId(InteractionNetworkGroup.class, fNamespace));
		group.setName("Other"); //$NON-NLS-1$
		group.setCode("other"); //$NON-NLS-1$
		group.setDescription("Other"); //$NON-NLS-1$
		return group;
	}

	private Organism createOrganism() throws ApplicationException {
		Organism organism = new Organism();
		organism.setName(fName);
		organism.setAlias(fAlias);
		organism.setDescription(fDescription);
		
		if (fTaxId != null) {
			organism.setTaxonomyId(fTaxId);
		} else {
			organism.setTaxonomyId(-1);
		}
		
		organism.setId(fData.getNextAvailableId(organism.getClass(), fNamespace));
		return organism;
	}

	protected boolean isEmpty(String value) {
		return value == null || value.trim().length() == 0;
	}
	
	private String getDataSetId(String url) {
		Matcher matcher = DATA_SET_PATTERN.matcher(url);
		if (!matcher.matches()) {
			return null;
		}
		return matcher.group(1); 
	}
}
