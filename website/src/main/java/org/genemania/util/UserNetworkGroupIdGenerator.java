package org.genemania.util;

public class UserNetworkGroupIdGenerator {

	public static String generateId(long organismId, String sessionId) {
		return "organism=" + organismId + "; " + "session=" + sessionId;
	}

}
