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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.genemania.exception.ApplicationException;

public class DataStoreConnector {

	private String filename;
	private Properties storage;
	private String keyValueSeparator = "|";
	private String itemSeparator = "||";
	
	public DataStoreConnector(String filename) {
		this.filename = filename;
		storage = new Properties();
	}
	
	public void put(String masterKey, Map<String, String> data) {
		StringBuffer buffer = new StringBuffer();
		Iterator<String> iterator = data.keySet().iterator();
		while(iterator.hasNext()) {
			String key = iterator.next();
			String val = data.get(key);
			buffer.append(key);
			buffer.append(keyValueSeparator);
			buffer.append(val);
			if(iterator.hasNext()) {
				buffer.append(itemSeparator);
			}
		}
		storage.put(masterKey, buffer.toString());
	}
	
	public void save() throws ApplicationException {
		FileOutputStream fos = null;   
		try {
			File file = new File(filename);
			if(!file.exists()) {
				file.createNewFile();
			}
			fos = new FileOutputStream(file);
//			storage.storeToXML(fos, String.valueOf(System.currentTimeMillis()));
			storage.store(fos, String.valueOf(System.currentTimeMillis()));
		} catch (Exception e) {
			throw new ApplicationException(e); 
		} finally {
			if(fos != null) {
				try { fos.close(); } catch (IOException e) {e.printStackTrace(); }
			}
		}
	}
	
}
