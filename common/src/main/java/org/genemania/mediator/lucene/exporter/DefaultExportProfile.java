package org.genemania.mediator.lucene.exporter;

public class DefaultExportProfile implements ExportProfile {

	private static ExportProfile instance;
	private static Object mutex = new Object();

	public static ExportProfile instance() {
		synchronized (mutex) {
			if (instance == null) {
				instance = new DefaultExportProfile();
			}
		}
		return instance;
	}

	@Override
	public boolean includesNetwork(String[] networkData) {
		return true;
	}

}
