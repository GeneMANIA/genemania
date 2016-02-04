package org.genemania.service;

import org.genemania.exception.DataStoreException;

public interface VersionService {

	public class VersionInfo {
		private String webappVersion;
		private String dbVersion;

		public String getWebappVersion() {
			return webappVersion;
		}

		public void setWebappVersion(String webappVersion) {
			this.webappVersion = webappVersion;
		}

		public String getDbVersion() {
			return dbVersion;
		}

		public void setDbVersion(String dbVersion) {
			this.dbVersion = dbVersion;
		}

	}

	public VersionInfo getVersion() throws DataStoreException;

}
