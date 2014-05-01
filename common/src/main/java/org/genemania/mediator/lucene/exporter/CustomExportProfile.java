package org.genemania.mediator.lucene.exporter;

import static org.genemania.mediator.lucene.exporter.Generic2LuceneExporter.NETWORK_NAME;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.genemania.configobj.ConfigObj;
import org.genemania.configobj.Section;

public class CustomExportProfile implements ExportProfile {

	Set<String> excludedNetworkNames;
	Pattern excludeNetworkPattern;
	
	public CustomExportProfile(String configFileName) throws IOException {
		this.excludedNetworkNames = new HashSet<String>();
		ConfigObj config = new ConfigObj(new FileReader(configFileName));
		Section section = config.getSection("Networks");
		excludedNetworkNames.addAll(section.getEntries("excludeNames"));
		String excludeNetworkPatternString = section.getEntry("excludePattern");
		if (excludeNetworkPatternString != null) {
			excludeNetworkPattern = Pattern.compile(excludeNetworkPatternString);
		}
	}
	
	@Override
	public boolean includesNetwork(String[] networkData) {
		String name = networkData[NETWORK_NAME];
		if (excludedNetworkNames.contains(name)) {
			return false;
		}
		if (excludeNetworkPattern != null && excludeNetworkPattern.matcher(name).matches()) {
			return false;
		}
		return true;
	}
}
