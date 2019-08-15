package org.genemania.plugin.cytoscape3;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.cytoscape.io.util.StreamUtil;
import org.genemania.plugin.FileUtils;

public class CyFileUtils extends FileUtils {
	
	private final CytoscapeUtilsImpl cytoscapeUtils;
	private final StreamUtil streamUtil;

	public CyFileUtils(CytoscapeUtilsImpl cytoscapeUtils, StreamUtil streamUtil) {
		this.cytoscapeUtils = cytoscapeUtils;
		this.streamUtil = streamUtil;
	}
	
	@Override
	public URLConnection getUrlConnection(URL url) throws IOException {
		return streamUtil.getURLConnection(url);
	}
	
	@Override
	protected String getDataUrl() {
		String url = cytoscapeUtils.getPreference(CytoscapeUtilsImpl.DATA_URL_PROPERTY);
		
		if (url != null) {
			url = url.trim();
			
			if (!url.endsWith("/"))
				url += "/";
		}
		
		return url == null ? super.getDataUrl() : url;
	}
	
	@Override
	protected String getSchemaVersion() {
		String schema = cytoscapeUtils.getPreference(CytoscapeUtilsImpl.DATA_SCHEMA_VERSION_PROPERTY);
		
		if (schema != null)
			schema = schema.trim();
		
		// TODO Auto-generated method stub
		return schema == null || schema.isEmpty() ? super.getSchemaVersion() : schema;
	}
}
