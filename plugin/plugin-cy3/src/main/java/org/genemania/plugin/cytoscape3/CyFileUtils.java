package org.genemania.plugin.cytoscape3;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.cytoscape.io.util.StreamUtil;
import org.genemania.plugin.FileUtils;

public class CyFileUtils extends FileUtils {
	private StreamUtil streamUtil;

	public CyFileUtils(StreamUtil streamUtil) {
		this.streamUtil = streamUtil;
	}
	
	@Override
	public URLConnection getUrlConnection(URL url) throws IOException {
		return streamUtil.getURLConnection(url);
	}
}
