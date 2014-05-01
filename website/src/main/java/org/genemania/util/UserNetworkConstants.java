package org.genemania.util;

import org.genemania.type.DataLayout;

public class UserNetworkConstants {
	public static final String CODE = "uploaded";
	public static final String SOURCE = "uploaded network";
	public static final String NETWORK_DESCRIPTION = "A user-uploaded network";
	public static final String GROUP_DESCRIPTION = "User-uploaded networks";
	public static final String GROUP_NAME = "Uploaded";

	public static String getProcessingDescription(DataLayout layout) {
		switch (layout) {
		case BINARY_NETWORK:
			return "A binary, user-uploaded network";
		case GEO_PROFILE:
			return "A GEO profile, user-uploaded network";
		case PROFILE:
			return "A profile, user-uploaded network";
		case SPARSE_PROFILE:
			return "A sparse profile, user-uploaded network";
		case UNKNOWN:
			return "A user-uploaded network of unknown format";
		case WEIGHTED_NETWORK:
			return "A weighted, user-uploaded network";
		default:
			return "A user-uploaded network";
		}
	}
}
