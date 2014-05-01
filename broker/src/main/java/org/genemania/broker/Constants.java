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

package org.genemania.broker;

public class Constants {

	public class CONFIG_PROPERTIES {
		
		public static final String APP_VER = "appVer";
		public static final String BROKER_PROTOCOL = "brokerProtocol";
		public static final String BROKER_HOST = "brokerHost";
		public static final String BROKER_PORT = "brokerPort";
		
		public static final String MQ_REQUESTS_QUEUE_NAME = "mqRequestsQueueName";
		public static final String MQ_REPLIES_QUEUE_NAME = "mqRepliesQueueName";
		public static final String BROKER_ADMIN_PORT = "brokerAdminPort";
		public static final String CLIENT_TIMEOUT = "clientTimeout";
		public static final String MAX_INACTIVITY_DURATION = "maxInactivityDuration";
		
	}
	
	public class ERROR_CODES {
		
		public static final int NO_ERROR = 0;
		public static final int ENGINE_ERROR = 1;
		public static final int WORKER_ERROR = 2;
		public static final int JMS_ERROR = 3;
		
	}
}
