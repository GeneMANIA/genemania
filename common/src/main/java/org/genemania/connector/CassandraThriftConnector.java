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

package org.genemania.connector;

import org.apache.log4j.Logger;
import org.genemania.exception.ApplicationException;

public class CassandraThriftConnector {

	// __[static]______________________________________________________________
	private static Logger LOG = Logger.getLogger(CassandraThriftConnector.class);
/*	
	private static final String DEFAULT_HOST = "localhost";
	private static final int DEFAULT_PORT = 9160;
	private static final String DEFAULT_KEYSPACE = "alias";
	private static final String DEFAULT_COLUMN_FAMILY = "synonyms";
	
	private String host;
	private int port;
	private String keyspace = DEFAULT_KEYSPACE;
	private TTransport transport;
	private Cassandra.Client client;
	
	public CassandraThriftConnector() {
		this(DEFAULT_HOST, DEFAULT_PORT);
	}
	
	public CassandraThriftConnector(String host, int port) {
		this.host = host;
		this.port = port;
	}
*/	
	public void openSession() throws ApplicationException {
/*	
		try {
			transport = new TSocket(host, port);
			TProtocol proto = new TBinaryProtocol(transport);
			client = new Cassandra.Client(proto);
			transport.open();
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
*/		
	}
	
	public void closeSession() throws ApplicationException {
/*		
		if((transport != null) && (transport.isOpen())) {
			transport.close();
		}
*/		
	}
	public void insert(String columnFamily, String key, String name, String value) throws ApplicationException {
/*		
		try {
			long timestamp = System.currentTimeMillis();
			client.insert(keyspace, key, new ColumnPath(columnFamily, null, name.getBytes("UTF-8")), value.getBytes("UTF-8"), timestamp, ConsistencyLevel.ONE);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
*/		
	}
/*	
	public void insert(String columnFamily, String key, Map<String, String> data) throws ApplicationException {
		try {
			Iterator<String> dataKeysIterator = data.keySet().iterator();
			long timestamp = System.currentTimeMillis();
//			LOG.debug("inserting " + keyspace + ":" + key + ":" + data);
			while(dataKeysIterator.hasNext()) {
				String dataKey = dataKeysIterator.next();
				String dataValue = data.get(dataKey);
				client.insert(keyspace, key, new ColumnPath(columnFamily, null, dataKey.getBytes("UTF-8")), dataValue.getBytes("UTF-8"), timestamp, ConsistencyLevel.ONE);
			}
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	public Map<String, String> get(String columnFamily, String key) throws ApplicationException {
		Map<String, String> ret = new Hashtable<String, String>();
		try {
	        SlicePredicate predicate = new SlicePredicate(null, new SliceRange(new byte[0], new byte[0], false, 10));
	        ColumnParent parent = new ColumnParent(columnFamily, null);
	        List<ColumnOrSuperColumn> results = client.get_slice(keyspace, key, parent, predicate, ConsistencyLevel.ONE);
	        for (ColumnOrSuperColumn result : results) {
	            Column column = result.column;
	            ret.put(new String(column.name, "UTF-8"), new String(column.value, "UTF-8"));
	        }
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return ret;
	}
*/	
	
}
