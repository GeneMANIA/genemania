package org.genemania.type;

import java.io.Serializable;

public enum SearchResultsErrorCode implements Serializable {
	UNKNOWN("unknown"), PARAM("invalid_parameter"), DATASTORE(
			"datastore_exception"), APP("application_exception"), USER_NETWORK(
			"no_user_network_exception");

	private String code = "";

	SearchResultsErrorCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public static SearchResultsErrorCode fromCode(String aCode) {
		SearchResultsErrorCode ret = SearchResultsErrorCode.UNKNOWN;
		SearchResultsErrorCode[] values = SearchResultsErrorCode.values();

		for (int i = 0; i < values.length; i++) {
			SearchResultsErrorCode next = (SearchResultsErrorCode) values[i];
			if (next.getCode().equalsIgnoreCase(aCode)) {
				ret = next;
				break;
			}
		}
		return ret;

	}
}
