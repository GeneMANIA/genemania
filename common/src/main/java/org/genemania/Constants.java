/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2010 University of Toronto.
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

package org.genemania;

public class Constants {

	public static final String APP_CONFIG_FILENAME = "genemania_config.xml";
	public static final String USER_DEFINED_GROUP_NAME = "User-defined";
	public static final String REPORT_XSL_FILENAME = "report.xsl";
	public static final String LUCENE_DEFAULT_FIELD = "gene";
	public static final String FEEDBACK_SUBJECT_VAR = "{subject}";
	public static final String FEEDBACK_MESSAGE_VAR = "{message}";
	public static final String FEEDBACK_EMAIL_VAR = "{email}";
	public static final String FEEDBACK_NAME_VAR = "{name}";
	public static final String FEEDBACK_TIMESTAMP_VAR = "{timestamp}";
	
	public static final long oneKilobyte = 1024;	
	public static final long oneMegabyte = 1048576;	
	public static final String DEFAULT_GENE_SEPARATOR = ";";
	public static final String DEFAULT_FIELD_SEPARATOR_TXT = "\t";	
	public static final String DEFAULT_INTERACTION_SEPARATOR_TXT = "\n";	
	public static final String NETWORK_IDS_SEPARATOR = ";";

	public static final int ORGANISM_VALIDATION_CHECK_LINES = 4;	
	public static final String GEO_PROFILE_SIGNATURE = "^DATABASE = Geo";
	public static final String GEO_PROFILE_DATASET_BEGIN_INSTRUCTION = "!dataset_table_begin";
	public static final String GEO_COMMENT_PREFIX1 = "^";	
	public static final String GEO_COMMENT_PREFIX2 = "!";	
	public static final String GEO_COMMENT_PREFIX3 = "#";	

	public class ERROR_CODES {
		
		public static final int NO_ERROR = 0;
		public static final int SYSTEM_ERROR = 1; // check the SystemException data field for more info
		public static final int APPLICATION_ERROR = 2; // check the ApplicationException data field for more info
		public static final int UNKNOWN_FILE_FORMAT = 3;
		public static final int INVALID_DATA = 4;
		public static final int DATA_ERROR = 5; // check the ApplicationException data field for more info
		public static final int HIBERNATE_EXCEPTION = 6;
		public static final int NOT_MULTIPART_CONTENT = 7;
		public static final int INVALID_ORGANISM = 8;
		public static final int INTERNAL_ERROR = 9;
		public static final int LIMIT_EXCEEDED = 10;
		public static final int NO_DATA_FOR_ID = 11;
		public static final int INVALID_GENE_SYMBOLS = 12;
		public static final int GENE_SYNONYMS_ERROR = 13;
		public static final int SESSION_TIMEOUT_ERROR = 14;
		public static final int DUPLICATE_GENE_SYMBOLS = 15;
		public static final int INVALID_ENRICHMENT_MIN_CATEGORIES = 16;
		public static final int INVALID_ONTOLOGY = 17;
		public static final int INVALID_ENRICHMENT_Q_VALUE = 18;
		public static final int INVALID_LUCENE_INDEX = 19;
		public static final int EMPTY_GENE_LIST = 20;
		public static final int EMPTY_NETWORK_LIST = 21;
	}

	public class KEYS {
		
		public static final String CURRENT_ORGANISM = "current organism";
		public static final String REQUEST_SIGNATURE = "request signature";
		public static final String USER_NETWORKS = "user networks";
		public static final String REQUEST_TOKEN = "request token";
		public static final String USER_NETWORKS_IN_SESSION = "user networks in session";
		public static final String ORGANISM_USER_NETWORKS_MAP = "organism user networks map";
		public static final String ORGANISMS_CACHE = "organisms cache";
	}
	
	public class CONFIG_PROPERTIES {
		
		public static final String LUCENE_INDEX_DIR = "luceneIndexDir";
		public static final String NETWORK_CACHE_DIR = "networkCacheDir";
		public static final String FEEDBACK_FROM = "feedbackFrom";
		public static final String FEEDBACK_EMAIL_HOST = "feedbackEmailHost";
		public static final String SPARSIFICATION = "sparsification";
		public static final String ENRICHMENT_MIN_CATEGORIES = "enrichmentMinCategories";
		public static final String ENRICHMENT_Q_VAL_THRESHOLD = "enrichmentQValThreshold";
	}
	
}
